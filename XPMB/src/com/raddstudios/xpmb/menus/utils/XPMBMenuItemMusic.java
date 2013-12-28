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

import android.os.Bundle;

public class XPMBMenuItemMusic extends XPMBMenuItem {

	public static final String TYPE_DESC = "menuitem.media.music";

	private long albumArtID = 0l;
	private String strTrackName = null, strAlbumName = null, strAuthorName = null,
			strTrackPath = null;

	private final String BD_STRTRACKNAME = "strTrackName", BD_STRALBUMNAME = "strAlbumName",
			BD_STRAUTHORNAME = "strAuthorName", BD_STRTRACKPATH = "strTrackPath",
			BD_ALBUMARTID = "albumArtID";

	public XPMBMenuItemMusic(String trackPath) {
		super(trackPath);
		strTrackPath = trackPath;
	}

	public XPMBMenuItemMusic(Bundle source) {
		super(source);

		albumArtID = source.getLong(BD_ALBUMARTID);
		strTrackName = source.getString(BD_STRTRACKNAME);
		strAuthorName = source.getString(BD_STRAUTHORNAME);
		strAlbumName = source.getString(BD_STRALBUMNAME);
		strTrackPath = source.getString(BD_STRTRACKPATH);
	}

	@Override
	public Bundle storeInBundle() {
		Bundle s = super.storeInBundle();

		s.putLong(BD_ALBUMARTID, albumArtID);
		s.putString(BD_STRTRACKNAME, strTrackName);
		s.putString(BD_STRAUTHORNAME, strAuthorName);
		s.putString(BD_STRALBUMNAME, strAlbumName);
		s.putString(BD_STRTRACKPATH, strTrackPath);

		return s;
	}

	@Override
	public String getTypeDescriptor() {
		return XPMBMenuItemMusic.TYPE_DESC;
	}

	public void setAlbumArtID(long mediaID) {
		albumArtID = mediaID;
	}

	public long getAlbumArtID() {
		return albumArtID;
	}

	public void setTrackName(String name) {
		strTrackName = name;
	}

	public String getTrackName() {
		return strTrackName;
	}

	public void setAuthorName(String name) {
		strAuthorName = name;
	}

	public String getAuthorName() {
		return strAuthorName;
	}

	public void setAlbumName(String name) {
		strAlbumName = name;
	}

	public String getAlbumName() {
		return strAlbumName;
	}

	public void setTrackPath(String path) {
		strTrackPath = path;
	}

	public String getTrackPath() {
		return strTrackPath;
	}
}
