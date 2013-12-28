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

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;

public class XPMB_RCDatParser {

	public static final int TYPE_CRC = 0, TYPE_FULL_NAME = 1;

	private ArrayList<String> alItemsByName = null, alItemsByCRC = null;
	private ArrayList<XPMB_RCDatNode> alItems = null;

	private XPMB_RCDatHeader mHeader = null;

	public XPMB_RCDatParser(XmlResourceParser src) {

		try {
			int eventType = src.getEventType();

			String cHeader = "";

			// Used in header
			String hn = null, hd = null, hv = null, hdt = null, ha = null, hu = null;
			// Used in game data
			String gn = null, gc = null, gd = null;
			ArrayList<XPMB_RCDatNode_Release> gr = null;
			XPMB_RCDatNode_ROM grr = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					alItemsByName = new ArrayList<String>();
					alItemsByCRC = new ArrayList<String>();
					alItems = new ArrayList<XPMB_RCDatNode>();
					break;
				case XmlResourceParser.START_TAG:
					cName = src.getName();
					if (cName.equals("header")) {
						cHeader = cName;
						break;
					}
					if (cName.equals("game")) {
						cHeader = cName;
						gr = new ArrayList<XPMB_RCDatNode_Release>();
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
							gr.add(new XPMB_RCDatNode_Release(src.getAttributeValue(null, "name"),
									src.getAttributeValue(null, "region")));
						}
						if (cName.equals("rom")) {
							grr = new XPMB_RCDatNode_ROM(src.getAttributeValue(null, "name"),
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
						alItemsByName.add(gn);
						alItemsByCRC.add(grr.getROMCRC());
						alItems.add(new XPMB_RCDatNode(gn, gc, gd, gr, grr));

						gn = null;
						gc = null;
						gd = null;
						gr = null;
						grr = null;
						cHeader = "";
					}
					if (cName.equals("header")) {
						mHeader = new XPMB_RCDatHeader(hn, hd, hv, hdt, ha, hu);
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

	public XPMB_RCDatHeader getHeader() {
		return mHeader;
	}

	public int getNumNodes() {
		return alItems.size();
	}

	public XPMB_RCDatNode getNode(String key, int type) {
		switch (type) {
		case TYPE_CRC:
			return alItems.get(alItemsByCRC.indexOf(key));
		case TYPE_FULL_NAME:
			return alItems.get(alItemsByName.indexOf(key));
		}
		return null;
	}

}
