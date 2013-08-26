//-----------------------------------------------------------------------------
//    
//    This file is part of XPMB.
//
//    XPMB is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    XPMB is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with XPMB.  If not, see <http://www.gnu.org/licenses/>.
//
//-----------------------------------------------------------------------------

package com.raddstudios.xpmb.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.raddstudios.xpmb.XPMBActivity;

public class XPMBSettingsManager {

	Bundle mSettingsStorage = null;
	XPMBActivity mRoot = null;

	private final byte DATA_TYPE_NONE = -1, DATA_TYPE_INT = 0, DATA_TYPE_LONG = 1,
			DATA_TYPE_FLOAT = 2, DATA_TYPE_BOOLEAN = 3, DATA_TYPE_BYTE = 4, DATA_TYPE_STRING = 5,
			DATA_TYPE_DOUBLE = 6, DATA_TYPE_BUNDLE = 7;

	public XPMBSettingsManager(XPMBActivity owner) {
		mRoot = owner;
		mSettingsStorage = new Bundle();
	}

	public Bundle getSettingBundle(String id) {
		if (!mSettingsStorage.containsKey(id)) {
			mSettingsStorage.putBundle(id, new Bundle());
		}
		return mSettingsStorage.getBundle(id);
	}

	public void setRootBundle(Bundle root) {
		if (mSettingsStorage != null) {
			mSettingsStorage.clear();
		}
		mSettingsStorage = root;
	}

	public Bundle getRootBundle() {
		return mSettingsStorage;
	}

	public void writeToFile(String name) {
		try {
			FileOutputStream fos = mRoot.openFileOutput(name, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(fos);
			writeBundleTo(mSettingsStorage, dos);
			dos.close();
			fos.close();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "writeToFile():Error trying to access '" + name
					+ "' while saving settings to file.");
		}
	}

	public void readFromFile(String name) {
		try {
			FileInputStream fis = mRoot.openFileInput(name);
			DataInputStream dis = new DataInputStream(fis);
			mSettingsStorage = readBundleFrom(dis);
			dis.close();
			fis.close();
		} catch (FileNotFoundException fnf) {
			Log.w(getClass().getSimpleName(),
					"readFromFile():No settings file found. Starting with a blank one.");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "readFromFile():Error trying to access '" + name
					+ "' while reading settings from file.");
		}
	}

	private void writeBundleTo(Bundle src, DataOutputStream dest) throws IOException {
		Set<String> ks = src.keySet();

		dest.writeInt(ks.size());
		for (String s : ks) {
			writeString(s, dest);
			Object o = src.get(s);

			if (o instanceof Byte) {
				dest.writeByte(DATA_TYPE_BYTE);
				dest.writeByte((Byte) o);
			} else if (o instanceof Integer) {
				dest.writeByte(DATA_TYPE_INT);
				dest.writeInt((Integer) o);
			} else if (o instanceof Long) {
				dest.writeByte(DATA_TYPE_LONG);
				dest.writeLong((Long) o);
			} else if (o instanceof Double) {
				dest.writeByte(DATA_TYPE_DOUBLE);
				dest.writeDouble((Double) o);
			} else if (o instanceof Float) {
				dest.writeByte(DATA_TYPE_FLOAT);
				dest.writeFloat((Float) o);
			} else if (o instanceof String) {
				dest.writeByte(DATA_TYPE_STRING);
				writeString((String) o, dest);
			} else if (o instanceof Boolean) {
				dest.writeByte(DATA_TYPE_BOOLEAN);
				dest.writeBoolean((Boolean) o);
			} else if (o instanceof Bundle) {
				dest.writeByte(DATA_TYPE_BUNDLE);
				writeBundleTo((Bundle) o, dest);
			} else {
				dest.writeByte(DATA_TYPE_NONE);
			}
		}
	}

	private Bundle readBundleFrom(DataInputStream src) throws IOException {
		Bundle o = new Bundle();
		int sz = src.readInt();
		int i = 0;
		while (i < sz) {
			String key = readString(src);
			byte type = src.readByte();
			switch (type) {
			case DATA_TYPE_BYTE:
				o.putByte(key, src.readByte());
				break;
			case DATA_TYPE_INT:
				o.putInt(key, src.readInt());
				break;
			case DATA_TYPE_LONG:
				o.putLong(key, src.readLong());
				break;
			case DATA_TYPE_FLOAT:
				o.putFloat(key, src.readFloat());
				break;
			case DATA_TYPE_DOUBLE:
				o.putDouble(key, src.readDouble());
				break;
			case DATA_TYPE_BOOLEAN:
				o.putBoolean(key, src.readBoolean());
				break;
			case DATA_TYPE_STRING:
				o.putString(key, readString(src));
				break;
			case DATA_TYPE_BUNDLE:
				o.putBundle(key, readBundleFrom(src));
				break;
			case DATA_TYPE_NONE:
				break;
			}
			i++;
		}
		return o;
	}

	private void writeString(String v, DataOutputStream dest) throws IOException {
		dest.write(v.length());
		dest.writeChars(v);
	}

	private String readString(DataInputStream src) throws IOException {
		StringBuilder sb = new StringBuilder();
		int sz = src.read();
		int c = 0;
		while (c < sz) {
			sb.append(src.readChar());
			c++;
		}
		return sb.toString();
	}
}
