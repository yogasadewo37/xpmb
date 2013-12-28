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

package com.raddstudios.xpmb.menus.modules.games.romdata;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.raddstudios.xpmb.menus.modules.games.romdata.rcdat.XPMB_RCDatNode;
import com.raddstudios.xpmb.menus.modules.games.romdata.rcdat.XPMB_RCDatParser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class XPMB_ROMData {

	private final String BD_FSRC = "fSrc", BD_LCRC = "lCRC", BD_MDATA = "mData";

	private File fSrc = null;
	private boolean bCompressed = false;
	private long lCRC = 0L;
	private XPMB_RCDatNode mData = null;

	public XPMB_ROMData(Bundle source) {
		fSrc = new File(source.getString(BD_FSRC));
		lCRC = source.getLong(BD_LCRC);
		mData = new XPMB_RCDatNode(source.getBundle(BD_MDATA));
	}

	public XPMB_ROMData(File source, XPMB_RCDatParser datSource) {
		fSrc = source;
		bCompressed = false;
		lCRC = getCRCFor(source);
		loadData(datSource);
	}

	public XPMB_ROMData(File parent, ZipEntry source, XPMB_RCDatParser datSource) {
		fSrc = parent;
		bCompressed = true;
		lCRC = source.getCrc();
		loadData(datSource);
	}

	public File getSourceFile() {
		return fSrc;
	}

	public boolean isCompressed() {
		return bCompressed;
	}

	public long getCRC() {
		return lCRC;
	}

	public XPMB_RCDatNode getInfo() {
		return mData;
	}

	public Bundle storeInBundle() {
		Bundle o = new Bundle();
		o.putString(BD_FSRC, fSrc.getAbsolutePath());
		o.putLong(BD_LCRC, lCRC);
		o.putBundle(BD_MDATA, mData.storeInBundle());
		return o;
	}

	private long getCRCFor(File source) {
		try {
			BufferedInputStream fi = new BufferedInputStream(new FileInputStream(source));
			CRC32 cCRC = new CRC32();
			int cByte = 0;
			long i = System.currentTimeMillis();
			byte[] buf = new byte[1024 * 64];
			while ((cByte = fi.read(buf)) > 0) {
				cCRC.update(buf, 0, cByte);
			}
			Log.i(getClass().getSimpleName(), "loadIn():CRC Calculation for '" + source.getName()
					+ "' took " + (System.currentTimeMillis() - i) + "ms.");
			fi.close();
			return cCRC.getValue();
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "getCRCFor(): Couldn't read '" + source.getName()
					+ "' to get CRC calculation.");
		}
		return 0L;
	}

	private void loadData(XPMB_RCDatParser datSource) {

		boolean resourceFound = false;
		File srcResourcePath = new File(fSrc.getParentFile(), "Resources");
		if (srcResourcePath.exists()) {
			File srcResourceFile = new File(srcResourcePath, Long.toHexString(lCRC).toUpperCase()
					+ ".zip");
			if (srcResourceFile.exists()) {
				try {
					ZipFile zf = new ZipFile(srcResourceFile, ZipFile.OPEN_READ);

					ZipEntry ze = zf.getEntry("base_data.xml");
					if (ze != null) {
						ZipEntry zet = zf.getEntry("tgdb_data.xml");
						if (zet != null) {
							mData = new XPMB_RCDatNode(zf.getInputStream(ze),
									zf.getInputStream(zet));
						} else {
							mData = new XPMB_RCDatNode(zf.getInputStream(ze), null);
						}
						resourceFound = true;
					}
					zf.close();
				} catch (FileNotFoundException e) {
					Log.e(getClass().getSimpleName(),
							"+XPMB_ROMData(): Couldn't access resource file '"
									+ srcResourceFile.getName() + "'.");
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(),
							"+XPMB_ROMData(): Error reading resource file '"
									+ srcResourceFile.getName() + "'.");
				}
			}
		}
		if (!resourceFound) {
			mData = datSource.getNode(Long.toHexString(lCRC).toUpperCase(),
					XPMB_RCDatParser.TYPE_CRC);
		}
	}

}
