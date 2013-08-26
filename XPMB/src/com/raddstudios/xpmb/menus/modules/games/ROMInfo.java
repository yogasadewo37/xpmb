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
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;

public class ROMInfo {

	public static final int TYPE_CRC = 0, TYPE_MD5 = 1, TYPE_SHA1 = 2;

	private Hashtable<String, ROMInfoNode> htItems = null;
	private int intCheckType = 0;
	private ROMInfoHeader mHeader = null;

	public ROMInfo(XmlResourceParser src, int checkType) {
		intCheckType = checkType;

		try {
			int eventType = src.getEventType();

			String cHeader = "";

			// Used in header
			String hn = null, hd = null, hv = null, hdt = null, ha = null, hu = null;
			// Used in game
			String gn = null, gc = null, gd = null;
			ArrayList<ROMInfoNode_Release> gr = null;
			ROMInfoNode_ROM grr = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					htItems = new Hashtable<String, ROMInfoNode>();
					break;
				case XmlResourceParser.START_TAG:
					cName = src.getName();
					if (cName.equals("header")) {
						cHeader = cName;
						break;
					}
					if (cName.equals("game")) {
						cHeader = cName;
						gr = new ArrayList<ROMInfoNode_Release>();
						gn = src.getAttributeValue(null, "name");
						gc = src.getAttributeValue(null, "cloneof");
						break;
					}
					if (cHeader.equals("header")) {
						if (cName.equals("name")) {
							hn = src.getText();
						}
						if (cName.equals("author")) {
							hd = src.getText();
						}
						if (cName.equals("version")) {
							hv = src.getText();
						}
						if (cName.equals("date")) {
							hdt = src.getText();
						}
						if (cName.equals("author")) {
							ha = src.getText();
						}
						if (cName.equals("url")) {
							hu = src.getText();
						}
					} else if (cHeader.equals("game")) {
						if (cName.equals("description")) {
							gd = src.getText();
						}
						if (cName.equals("release")) {
							gr.add(new ROMInfoNode_Release(src.getAttributeValue(null, "name"), src
									.getAttributeValue(null, "region")));
						}
						if (cName.equals("rom")) {
							grr = new ROMInfoNode_ROM(src.getAttributeValue(null, "name"),
									src.getAttributeIntValue(null, "size", 0),
									src.getAttributeValue(null, "crc"), src.getAttributeValue(null,
											"md5"), src.getAttributeValue(null, "sha1"),
									src.getAttributeValue(null, "status"));
						}
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = src.getName();
					if (cName.equals("game")) {
						switch (intCheckType) {
						case TYPE_MD5:
							htItems.put(grr.getROMMD5(), new ROMInfoNode(gn, gc, gd, gr, grr));
							break;
						case TYPE_SHA1:
							htItems.put(grr.getROMSHA1(), new ROMInfoNode(gn, gc, gd, gr, grr));
							break;
						case TYPE_CRC:
						default:
							htItems.put(grr.getROMCRC(), new ROMInfoNode(gn, gc, gd, gr, grr));
							break;
						}
						gn = null;
						gc = null;
						gd = null;
						gr = null;
						grr = null;
						cHeader = "";
					}
					if (cName.equals("header")) {
						mHeader = new ROMInfoHeader(hn, hd, hv, hdt, ha, hu);
						hn = null;
						hd = null;
						hv = null;
						hdt = null;
						ha = null;
						hu = null;
						cHeader = "";
					}
					break;
				}
				eventType = src.next();
			}
			src.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getCheckType() {
		return intCheckType;
	}

	public ROMInfoHeader getHeader() {
		return mHeader;
	}

	public int getNumNodes() {
		return htItems.size();
	}

	public ROMInfoNode getNode(String key) {
		return htItems.get(key);
	}

}
