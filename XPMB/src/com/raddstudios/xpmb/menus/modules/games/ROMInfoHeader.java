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

public class ROMInfoHeader {
	private String strHeaderName = null, strHeaderDescription = null, strHeaderVersion = null,
			strHeaderDate = null, strHeaderAuthor = null, strHeaderUrl = null;

	public ROMInfoHeader(String name, String description, String version, String date,
			String author, String url) {
		strHeaderName = name;
		strHeaderDescription = description;
		strHeaderVersion = version;
		strHeaderDate = date;
		strHeaderAuthor = author;
		strHeaderUrl = url;
	}

	public String getHeaderName() {
		return strHeaderName;
	}

	public String getHeaderDescription() {
		return strHeaderDescription;
	}

	public String getHeaderVersion() {
		return strHeaderVersion;
	}

	public String getHeaderDate() {
		return strHeaderDate;
	}

	public String getHeaderAuthor() {
		return strHeaderAuthor;
	}

	public String getHeaderUrl() {
		return strHeaderUrl;
	}
}