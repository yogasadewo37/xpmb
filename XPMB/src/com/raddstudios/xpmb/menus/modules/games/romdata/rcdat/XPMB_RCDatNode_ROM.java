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

package com.raddstudios.xpmb.menus.modules.games.romdata.rcdat;

import android.os.Bundle;

public class XPMB_RCDatNode_ROM {

	private final String BD_STRROMNAME = "strROMName", BD_STRROMCRC = "strRomCRC",
			BD_STRROMMD5 = "strROMMD5", BD_STRROMSHA1 = "strROMSHA1",
			BD_STRROMSTATUS = "strROMStatus", BD_INTROMSIZE = "intRomSize";

	private String strROMName = null, strROMCRC = null, strROMMD5 = null, strROMSHA1 = null,
			strROMStatus = null;
	private int intROMSize = 0;

	public XPMB_RCDatNode_ROM(String name, int size, String crc, String md5, String sha1,
			String status) {
		strROMName = name;
		intROMSize = size;
		strROMCRC = crc;
		strROMMD5 = md5;
		strROMSHA1 = sha1;
		strROMStatus = status;
	}

	public XPMB_RCDatNode_ROM(Bundle source) {
		strROMName = source.getString(BD_STRROMNAME);
		intROMSize = source.getInt(BD_INTROMSIZE);
		strROMCRC = source.getString(BD_STRROMCRC);
		strROMMD5 = source.getString(BD_STRROMMD5);
		strROMSHA1 = source.getString(BD_STRROMSHA1);
		strROMStatus = source.getString(BD_STRROMSTATUS);
	}

	public String getROMName() {
		return strROMName;
	}

	public int getROMSize() {
		return intROMSize;
	}

	public String getROMCRC() {
		return strROMCRC;
	}

	public String getROMMD5() {
		return strROMMD5;
	}

	public String getROMSHA1() {
		return strROMSHA1;
	}

	public String getROMStatus() {
		return strROMStatus;
	}

	public Bundle storeInBundle() {
		Bundle o = new Bundle();

		o.putString(BD_STRROMNAME, strROMName);
		o.putInt(BD_INTROMSIZE, intROMSize);
		o.putString(BD_STRROMCRC, strROMCRC);
		o.putString(BD_STRROMMD5, strROMMD5);
		o.putString(BD_STRROMSHA1, strROMSHA1);
		o.putString(BD_STRROMSTATUS, strROMStatus);

		return o;
	}

}