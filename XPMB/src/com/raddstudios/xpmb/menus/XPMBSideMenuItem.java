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

package com.raddstudios.xpmb.menus;

public class XPMBSideMenuItem {

	public String getLabel() {
		return null;
	}

	public String getIconBitmapID() {
		return null;
	}
	
	public boolean isEnabled() {
		return false;
	}
	
	public void setEnabled(boolean enabled) {
	}

	public boolean hasChildren() {
		return false;
	}

	public XPMBSideMenuItem getChildren(int index) {
		return null;
	}

	public void executeAction() {
	}

}
