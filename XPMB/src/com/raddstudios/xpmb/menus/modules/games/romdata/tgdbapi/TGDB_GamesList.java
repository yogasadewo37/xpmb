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

package com.raddstudios.xpmb.menus.modules.games.romdata.tgdbapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class TGDB_GamesList {

	public class TGDB_GamesListEntry {
		private int intGameID = 0;
		private String strGameName = null, strGameDate = null, strGamePlatform = null;

		public TGDB_GamesListEntry(int gameID, String gameName, String gameDate,
				String gamePlatform) {
			intGameID = gameID;
			strGameName = gameName;
			strGameDate = gameDate;
			strGamePlatform = gamePlatform;
		}

		public int getId() {
			return intGameID;
		}

		public String getGameName() {
			return strGameName;
		}

		public String getGameDate() {
			return strGameDate;
		}

		public String getGamePlatform() {
			return strGamePlatform;
		}

		public TGDB_GameData getData() {
			URL uRemData = null;
			try {
				uRemData = new URL("http://thegamesdb.net/api/GetGame.php?id="
						+ String.valueOf(intGameID));
				return new TGDB_GameData(uRemData.openStream());
			} catch (MalformedURLException e) {
				Log.e(getClass().getSimpleName(), "+TGDB_GameData(): Wrong URL was created.");
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), "+TGDB_GameData(): Error accesing '"
						+ uRemData.toString() + "'.");
			}
			return null;
		}
	}

	ArrayList<TGDB_GamesListEntry> alEntries = null;

	public TGDB_GamesList(URL url) {
		try {
			XmlPullParser xrpRes = XmlPullParserFactory.newInstance().newPullParser();
			xrpRes.setInput(url.openStream(), null);

			int eventType = xrpRes.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String cName = null;

				int intGameID = 0;
				String strGameName = null, strGameDate = null, strGamePlatform = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					if (alEntries != null) {
						alEntries.clear();
					} else {
						alEntries = new ArrayList<TGDB_GamesListEntry>();
					}
					break;
				case XmlResourceParser.START_TAG:
					cName = xrpRes.getName();
					if (cName.equalsIgnoreCase("Game")) {
						intGameID = 0;
						strGameName = null;
						strGameDate = null;
						strGamePlatform = null;
					}
					if (cName.equalsIgnoreCase("id")) {
						intGameID = Integer.parseInt(xrpRes.getText());
					}
					if (cName.equalsIgnoreCase("GameTitle")) {
						strGameName = xrpRes.getText();
					}
					if (cName.equalsIgnoreCase("ReleaseDate")) {
						strGameDate = xrpRes.getText();
					}
					if (cName.equalsIgnoreCase("Platform")) {
						strGamePlatform = xrpRes.getText();
					}
					if (cName.equalsIgnoreCase("Error")) {
						Log.e(getClass().getSimpleName(),
								"+TGDB_GamesList(): Got an empty XML file. Server returned error: '"
										+ xrpRes.getText() + "'");
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = xrpRes.getName();
					if (cName.equalsIgnoreCase("Game")) {
						alEntries.add(new TGDB_GamesListEntry(intGameID, strGameName,
								strGameDate, strGamePlatform));
					}
					break;
				}
				eventType = xrpRes.next();
			}

		} catch (XmlPullParserException e) {
			Log.e(getClass().getSimpleName(),
					"parseXML(): Couldn't create new XML Parser instance.");
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "parseXML(): Couldn't open remote xml resource.");
		}
	}
}
