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

public class XPMB_RCDatNode_Release {

	private final String BD_STRRELEASENAME = "strReleaseName",
			BD_STRRELEASEREGION = "strReleaseRegion";

	private String strReleaseName = null, strReleaseRegion = null;

	public XPMB_RCDatNode_Release(String name, String region) {
		strReleaseName = name;
		strReleaseRegion = region;
	}

	public XPMB_RCDatNode_Release(Bundle source) {
		strReleaseName = source.getString(BD_STRRELEASENAME);
		strReleaseRegion = source.getString(BD_STRRELEASEREGION);
	}

	public String getReleaseName() {
		return strReleaseName;
	}

	public String getReleaseRegion() {
		return strReleaseRegion;
	}

	public Bundle storeInBundle() {
		Bundle o = new Bundle();

		o.putString(BD_STRRELEASENAME, strReleaseName);
		o.putString(BD_STRRELEASEREGION, strReleaseRegion);

		return o;
	}
}