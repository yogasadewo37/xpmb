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

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.utils.XPMB_Activity;

public class XPMBMenuItemApp extends XPMBMenuItem {

	private XPMB_Activity mRoot = null;

	public XPMBMenuItemApp(String label, XPMB_Activity root) {
		super(label);
		mRoot = root;
	}

	public void setVersion(String version) {
		if (version != null) {
			super.enableTwoLine(false);
		} else {
			super.enableTwoLine(true);
			super.setLabelB(mRoot.getString(R.string.strVersion) + ":" + version);
		}
	}

	public String getVersion() {
		String v = super.getLabelB();
		if (v != null) {
			return v.substring(v.indexOf(':') + 1);
		}
		return super.getLabelB();
	}
}
