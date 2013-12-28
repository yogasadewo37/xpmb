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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import com.raddstudios.xpmb.menus.modules.games.romdata.tgdbapi.TGDB_GameData;

public class XPMB_RCDatNode {
	String strGameName = null, strGameCloneOf = null, strGameDescription = null;
	XPMB_RCDatNode_ROM rinROMData = null;
	XPMB_RCDatNode_Release[] rinReleaseList = null;
	TGDB_GameData tgdExtData = null;

	private final String BD_STRGAMENAME = "strGameName", BD_STRGAMECLONEOF = "strGameCloneOf",
			BD_STRGAMEDESCRIPTION = "strGameDescription", BD_RINROMDATA = "rinROMData",
			BD_RINRELEASELIST = "rinReleaseList";

	public XPMB_RCDatNode(String name, String cloneOf, String description,
			ArrayList<XPMB_RCDatNode_Release> releases, XPMB_RCDatNode_ROM romData) {
		strGameName = name;
		strGameCloneOf = cloneOf;
		strGameDescription = description;
		rinROMData = romData;
		rinReleaseList = new XPMB_RCDatNode_Release[releases.size()];
		releases.toArray(rinReleaseList);
		
	}

	public XPMB_RCDatNode(Bundle source) {
		strGameName = source.getString(BD_STRGAMENAME);
		strGameCloneOf = source.getString(BD_STRGAMECLONEOF);
		strGameDescription = source.getString(BD_STRGAMEDESCRIPTION);
		rinROMData = new XPMB_RCDatNode_ROM(source.getBundle(BD_RINROMDATA));
		rinReleaseList = getReleasesFromBundle(BD_RINRELEASELIST, source);
	}

	public XPMB_RCDatNode(InputStream src, InputStream extData){
		XmlPullParser xpp = Xml.newPullParser();
		ArrayList<XPMB_RCDatNode_Release> releases = new ArrayList<XPMB_RCDatNode_Release>();
		try {
			xpp.setInput(src, "UTF-8");

			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					break;
				case XmlResourceParser.START_TAG:
					cName = xpp.getName();
					if (cName.equalsIgnoreCase("game")) {
						strGameName = xpp.getAttributeValue(null, "name");
						strGameCloneOf = xpp.getAttributeValue(null, "cloneof");
					}
					if (cName.equalsIgnoreCase("release")) {
						releases.add(new XPMB_RCDatNode_Release(xpp.getAttributeValue(null, "name"),
								xpp.getAttributeValue(null, "region")));
					}
					if (cName.equalsIgnoreCase("rom")) {
						rinROMData = new XPMB_RCDatNode_ROM(xpp.getAttributeValue(null, "name"),
								Integer.parseInt(xpp.getAttributeValue(null, "size")),
								xpp.getAttributeValue(null, "crc"), xpp.getAttributeValue(null,
										"md5"), xpp.getAttributeValue(null, "sha1"),
								xpp.getAttributeValue(null, "status"));
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = xpp.getName();
					if (cName.equalsIgnoreCase("game")) {
						rinReleaseList = new XPMB_RCDatNode_Release[releases.size()];
						releases.toArray(rinReleaseList);
					}
					break;
				}
				eventType = xpp.next();
			}

			src.close();
		} catch (XmlPullParserException e) {
			Log.e(getClass().getSimpleName(), "+XPMB_RCDatNode(): Couldn't parse XML file.");
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "+XPMB_RCDatNode(): Error parsing XML file.");
		}
		
		if (extData != null){
			tgdExtData = new TGDB_GameData(extData);
		}
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

	public XPMB_RCDatNode_Release getReleaseData(int index) {
		return rinReleaseList[index];
	}

	public XPMB_RCDatNode_ROM getROMData() {
		return rinROMData;
	}
	
	public TGDB_GameData getExtendedData(){
		return tgdExtData;
	}

	private void storeReleasesInBundle(XPMB_RCDatNode_Release[] nodes, String baseKey, Bundle dest) {
		dest.putInt(baseKey + ".count", nodes.length);
		for (int n = 0; n < nodes.length; n++) {
			dest.putBundle(baseKey + "." + n, nodes[n].storeInBundle());
		}
	}

	private XPMB_RCDatNode_Release[] getReleasesFromBundle(String baseKey, Bundle source) {
		XPMB_RCDatNode_Release[] o = new XPMB_RCDatNode_Release[source.getInt(baseKey + ".count")];

		for (int n = 0; n < o.length; n++) {
			o[n] = new XPMB_RCDatNode_Release(source.getBundle(baseKey + "." + n));
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

	public void writeToXML(File dest) {
		XmlSerializer xs = Xml.newSerializer();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(dest);
			xs.setOutput(fos, "UTF-8");

			xs.startDocument("UTF-8", true);

			xs.startTag(null, "game");
			xs.attribute(null, "name", strGameName);
			if (strGameCloneOf != null) {
				xs.attribute(null, "cloneof", strGameCloneOf);
			}
			xs.startTag(null, "description");
			xs.text(strGameDescription);
			xs.endTag(null, "description");
			for (XPMB_RCDatNode_Release rin : rinReleaseList) {
				xs.startTag(null, "release");
				xs.attribute(null, "name", rin.getReleaseName());
				xs.attribute(null, "region", rin.getReleaseRegion());
				xs.endTag(null, "release");
			}
			xs.startTag(null, "rom");
			xs.attribute(null, "name", rinROMData.getROMName());
			xs.attribute(null, "size", String.valueOf(rinROMData.getROMSize()));
			if (rinROMData.getROMCRC() != null) {
				xs.attribute(null, "crc", rinROMData.getROMCRC());
			}
			if (rinROMData.getROMMD5() != null) {
				xs.attribute(null, "md5", rinROMData.getROMMD5());
			}
			if (rinROMData.getROMSHA1() != null) {
				xs.attribute(null, "sha1", rinROMData.getROMSHA1());
			}
			if (rinROMData.getROMStatus() != null) {
				xs.attribute(null, "status", rinROMData.getROMStatus());
			}
			xs.endTag(null, "rom");
			xs.endTag(null, "game");

			xs.endDocument();

			fos.close();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "writeToXML(): Couldn't export XML.");
		}

	}

}