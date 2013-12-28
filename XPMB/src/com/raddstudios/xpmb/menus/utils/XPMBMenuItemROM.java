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

import com.raddstudios.xpmb.menus.modules.games.romdata.XPMB_ROMData;
import com.raddstudios.xpmb.menus.modules.games.romdata.rcdat.XPMB_RCDatNode;
import com.raddstudios.xpmb.menus.modules.games.romdata.tgdbapi.TGDB_GameData;

public class XPMBMenuItemROM extends XPMBMenuItem {

	public static final String TYPE_DESC = "menuitem.media.rom";

	private XPMB_ROMData mData = null;

	private String strROMBGKey = null;

	private final String BD_STRROMBGKEY = "strROMBGKey", BD_MDATA = "mData";

	public XPMBMenuItemROM(XPMB_ROMData source) {
		super(source.getInfo().getGameName());
		mData = source;

		initialize();
	}

	public XPMBMenuItemROM(Bundle source) {
		super(source);

		strROMBGKey = source.getString(BD_STRROMBGKEY);
		mData = new XPMB_ROMData(source.getBundle(BD_MDATA));

		initialize();
	}

	private void initialize() {
		if (mData != null) {
			TGDB_GameData extData = mData.getInfo().getExtendedData();
			if (extData != null) {
				super.setLabel(mData.getInfo().getExtendedData().getTitle());
				if (extData.getPublisher() != null) {
					super.enableTwoLine(true);
					super.setLabelB(extData.getPublisher());
				}
				if (extData.getDeveloper() != null) {
					super.setLabelB(super.getLabelB() + "/" + extData.getDeveloper());
				}
			} else {
				super.setLabel(mData.getInfo().getReleaseData(0).getReleaseName());
			}
		}
	}

	@Override
	public Bundle storeInBundle() {
		Bundle s = super.storeInBundle();
		s.putString(BD_STRROMBGKEY, strROMBGKey);
		s.putBundle(BD_MDATA, mData.storeInBundle());
		return s;
	}

	@Override
	public String getTypeDescriptor() {
		return XPMBMenuItemROM.TYPE_DESC;
	}

	public XPMB_RCDatNode getROMInfo() {
		return mData.getInfo();
	}

	public File getROMPath() {
		return mData.getSourceFile().getParentFile();
	}

	public long getROMCRC() {
		return mData.getCRC();
	}

	public String getROMBGKey() {
		return strROMBGKey;
	}

	public void setROMBGKey(String key) {
		strROMBGKey = key;
	}

}
