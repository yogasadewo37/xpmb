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

package com.raddstudios.xpmb.menus.utils;

import java.io.File;

import android.os.Bundle;

import com.raddstudios.xpmb.menus.modules.games.ROMInfo;
import com.raddstudios.xpmb.menus.modules.games.ROMInfoNode;

public class XPMBMenuItemROM extends XPMBMenuItem {

	public static final String TYPE_DESC = "menuitem.media.rom";
	
	private ROMInfoNode mData = null;
	private File fROMPath = null;
	private String strROMCRC = null;

	private final String BD_STRROMPATH = "fROMPath", BD_STRROMCRC = "strROMCRC", BD_MDATA = "mData";

	public XPMBMenuItemROM(ROMInfo datasource, String fCRC, File rompath) {
		super(rompath.getName());
		super.setTypeDescriptor(TYPE_DESC);
		
		fROMPath = rompath;
		
		mData = datasource.getNode(fCRC);
		if (mData != null){
			super.setLabel(mData.getReleaseData(0).getReleaseName());
			super.enableTwoLine(true);
			super.setLabelB(mData.getReleaseData(0).getReleaseRegion());
		}		
	}

	public XPMBMenuItemROM(Bundle source) {
		super(source);

		fROMPath = new File(source.getString(BD_STRROMPATH));
		strROMCRC = source.getString(BD_STRROMCRC);
		mData = new ROMInfoNode(source.getBundle(BD_MDATA));
		
		if (mData != null){
			super.setLabel(mData.getReleaseData(0).getReleaseName());
			super.enableTwoLine(true);
			super.setLabelB(mData.getReleaseData(0).getReleaseRegion());
		}	
	}


	@Override
	public Bundle storeInBundle() {
		Bundle s = super.storeInBundle();
		s.putString(BD_STRROMPATH, fROMPath.getAbsolutePath());
		s.putString(BD_STRROMCRC, strROMCRC);
		s.putBundle(BD_MDATA, mData.storeInBundle());
		return s;
	}

	public ROMInfoNode getROMInfo(){
		return mData;
	}
	
	public File getROMPath(){
		return fROMPath;
	}
	
	public String getROMCRC(){
		return strROMCRC;
	}
	
	
}
