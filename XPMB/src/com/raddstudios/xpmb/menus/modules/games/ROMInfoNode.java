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

package com.raddstudios.xpmb.menus.modules.games;

import java.util.ArrayList;

import android.os.Bundle;

public class ROMInfoNode {
	String strGameName = null, strGameCloneOf = null, strGameDescription = null;
	ROMInfoNode_ROM rinROMData = null;
	ROMInfoNode_Release[] rinReleaseList = null;

	private final String BD_STRGAMENAME = "strGameName", BD_STRGAMECLONEOF = "strGameCloneOf",
			BD_STRGAMEDESCRIPTION = "strGameDescription", BD_RINROMDATA = "rinROMData",
			BD_RINRELEASELIST = "rinReleaseList";

	public ROMInfoNode(String name, String cloneOf, String description,
			ArrayList<ROMInfoNode_Release> releases, ROMInfoNode_ROM romData) {
		strGameName = name;
		strGameCloneOf = cloneOf;
		strGameDescription = description;
		rinROMData = romData;
		rinReleaseList = (ROMInfoNode_Release[]) releases.toArray();
	}

	public ROMInfoNode(Bundle source) {
		strGameName = source.getString(BD_STRGAMENAME);
		strGameCloneOf = source.getString(BD_STRGAMECLONEOF);
		strGameDescription = source.getString(BD_STRGAMEDESCRIPTION);
		rinROMData = new ROMInfoNode_ROM(source.getBundle(BD_RINROMDATA));
		rinReleaseList = getReleasesFromBundle(BD_RINRELEASELIST, source);
	}

	public String getGameName() {
		return strGameName;
	}

	public String getGameDescription() {
		return strGameDescription;
	}

	public int getNumReleases() {
		return rinReleaseList.length;
	}

	public ROMInfoNode_Release getReleaseData(int index) {
		return rinReleaseList[index];
	}

	public ROMInfoNode_ROM getROMData() {
		return rinROMData;
	}

	private void storeReleasesInBundle(ROMInfoNode_Release[] nodes, String baseKey, Bundle dest) {
		dest.putInt(baseKey + ".count", nodes.length);
		for (int n = 0; n < nodes.length; n++) {
			dest.putBundle(baseKey + "." + n, nodes[n].storeInBundle());
		}
	}

	private ROMInfoNode_Release[] getReleasesFromBundle(String baseKey, Bundle source) {
		ROMInfoNode_Release[] o = new ROMInfoNode_Release[source.getInt(baseKey + ".count")];

		for (int n = 0; n < o.length; n++) {
			o[n] = new ROMInfoNode_Release(source.getBundle(baseKey + "." + n));
		}

		return o;
	}

	public Bundle storeInBundle() {
		Bundle o = new Bundle();

		o.putString(BD_STRGAMENAME, strGameName);
		o.putString(BD_STRGAMECLONEOF, strGameCloneOf);
		o.putString(BD_STRGAMEDESCRIPTION, strGameDescription);
		o.putBundle(BD_RINROMDATA, rinROMData.storeInBundle());
		storeReleasesInBundle(rinReleaseList, BD_RINRELEASELIST, o);

		return o;
	}

}