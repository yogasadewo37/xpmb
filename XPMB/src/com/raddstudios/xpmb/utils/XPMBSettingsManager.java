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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

public class XPMBSettingsManager {

	Bundle mSettingsStorage = null;

	private static final String DATA_TYPE_NULL = "NULL", DATA_TYPE_NONE = "UnknownType",
			DATA_TYPE_INT = "Integer", DATA_TYPE_LONG = "Long", DATA_TYPE_FLOAT = "Float",
			DATA_TYPE_BOOLEAN = "Boolean", DATA_TYPE_BYTE = "Byte", DATA_TYPE_STRING = "String",
			DATA_TYPE_DOUBLE = "Double", DATA_TYPE_BUNDLE = "Bundle", DATA_KEY_IDENT = "Key";

	public XPMBSettingsManager() {
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

	public void writeToFile(File dest) {
		try {
			FileOutputStream fos = new FileOutputStream(dest);
			DataOutputStream dos = new DataOutputStream(fos);
			writeBundleTo(mSettingsStorage, dos);
			dos.close();
			fos.close();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"writeToFile():Error trying to access '" + dest.getAbsolutePath()
							+ "' while saving settings to file.");
		}
	}

	public void readFromFile(File src) {
		try {
			FileInputStream fis = new FileInputStream(src);
			DataInputStream dis = new DataInputStream(fis);
			mSettingsStorage = readBundleFrom(dis);
			dis.close();
			fis.close();
		} catch (FileNotFoundException fnf) {
			Log.w(getClass().getSimpleName(),
					"readFromFile():No settings file found. Starting with a blank one.");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"readFromFile():Error trying to access '" + src.getAbsolutePath()
							+ "' while reading settings from file.");
		}
	}

	public static void writeBundleTo(Bundle src, XmlSerializer xs) throws IOException {
		Set<String> ks = src.keySet();

		for (String s : ks) {
			Object o = src.get(s);
			if (o == null) {
				xs.startTag(null, DATA_TYPE_NULL);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.endTag(null, DATA_TYPE_NULL);
			} else if (o instanceof Byte) {
				xs.startTag(null, DATA_TYPE_BYTE);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(Byte.toString((Byte) o));
				xs.endTag(null, DATA_TYPE_BYTE);
			} else if (o instanceof Integer) {
				xs.startTag(null, DATA_TYPE_INT);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(Integer.toString((Integer) o));
				xs.endTag(null, DATA_TYPE_INT);
			} else if (o instanceof Long) {
				xs.startTag(null, DATA_TYPE_LONG);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(Long.toString((Long) o));
				xs.endTag(null, DATA_TYPE_LONG);
			} else if (o instanceof Double) {
				xs.startTag(null, DATA_TYPE_DOUBLE);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(Double.toString((Double) o));
				xs.endTag(null, DATA_TYPE_DOUBLE);
			} else if (o instanceof Float) {
				xs.startTag(null, DATA_TYPE_FLOAT);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(Float.toString((Float) o));
				xs.endTag(null, DATA_TYPE_FLOAT);
			} else if (o instanceof String) {
				xs.startTag(null, DATA_TYPE_STRING);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text((String) o);
				xs.endTag(null, DATA_TYPE_STRING);
			} else if (o instanceof Boolean) {
				xs.startTag(null, DATA_TYPE_BOOLEAN);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(Boolean.toString((Boolean) o));
				xs.endTag(null, DATA_TYPE_BOOLEAN);
			} else if (o instanceof Bundle) {
				xs.startTag(null, DATA_TYPE_BUNDLE);
				xs.attribute(null, DATA_KEY_IDENT, s);
				writeBundleTo((Bundle) o, xs);
				xs.endTag(null, DATA_TYPE_BUNDLE);
			} else {
				xs.startTag(null, DATA_TYPE_NONE);
				xs.attribute(null, DATA_KEY_IDENT, s);
				xs.text(o.toString());
				xs.endTag(null, DATA_TYPE_NONE);
			}
		}
	}

	public static void writeBundleTo(Bundle src, DataOutputStream dest) throws IOException {
		XmlSerializer xs = Xml.newSerializer();
		xs.setOutput(dest, "utf-8");
		xs.startDocument(null, null);
		
		xs.startTag(null, DATA_TYPE_BUNDLE);
		xs.attribute(null, DATA_KEY_IDENT, "@root");
		writeBundleTo(src, xs);
		xs.endTag(null, DATA_TYPE_BUNDLE);

		xs.endDocument();
		dest.close();
	}

	public static Bundle readBundleFrom(XmlPullParser src) throws IOException,
			XmlPullParserException {
		Bundle o = new Bundle();
		int eventType = src.next();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String cName = null, cType = null;

			switch (eventType) {
			case XmlResourceParser.START_TAG:
				cType = src.getName();
				cName = src.getAttributeValue(null, DATA_KEY_IDENT);

				if (cType.equals(DATA_TYPE_BYTE)) {
					o.putByte(cName, Byte.valueOf(src.nextText()));
				} else if (cType.equals(DATA_TYPE_INT)) {
					o.putInt(cName, Integer.valueOf(src.nextText()));
				} else if (cType.equals(DATA_TYPE_LONG)) {
					o.putLong(cName, Long.valueOf(src.nextText()));
				} else if (cType.equals(DATA_TYPE_FLOAT)) {
					o.putFloat(cName, Float.valueOf(src.nextText()));
				} else if (cType.equals(DATA_TYPE_DOUBLE)) {
					o.putDouble(cName, Double.valueOf(src.nextText()));
				} else if (cType.equals(DATA_TYPE_BOOLEAN)) {
					o.putBoolean(cName, Boolean.valueOf(src.nextText()));
				} else if (cType.equals(DATA_TYPE_STRING)) {
					o.putString(cName, src.nextText());
				} else if (cType.equals(DATA_TYPE_BUNDLE)) {
					o.putBundle(cName, readBundleFrom(src));
				} else if (cType.equals(DATA_TYPE_NULL)) {
					o.putString(cName, null);
				} else if (cType.equals(DATA_TYPE_NONE)) {
					Log.w("XPMBSettingsManager", "readBundleFrom(): Found value '" + cName
							+ "' with unknown type '" + src.nextText() + "'.");
				}
				break;
			case XmlResourceParser.END_TAG:
				cType = src.getName();
				if (cType.equals(DATA_TYPE_BUNDLE)) {
					return o;
				}
				break;
			}
			eventType = src.next();
		}
		return o;
	}

	public static Bundle readBundleFrom(DataInputStream src) throws IOException,
			XmlPullParserException {
		XmlPullParser xpp = Xml.newPullParser();
		xpp.setInput(src, "utf-8");

		Bundle o = readBundleFrom(xpp).getBundle("@root");

		src.close();
		return o;
	}
}
