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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.util.Log;

public class TGDB_GameData {

	public class TGDB_GameData_ImageEntry {
		public static final int IMG_ENTRY_TYPE_FANART = 0, IMG_ENTRY_TYPE_BOXART = 1,
				IMG_ENTRY_TYPE_BANNER = 2;
		public static final int IMG_ENTRY_BOX_SIDE_NONE = 0, IMG_ENTRY_BOX_SIDE_FRONT = 1,
				IMG_ENTRY_BOX_SIDE_BACK = 2, IMG_ENTRY_BOX_SIDE_LEFT = 3,
				IMG_ENTRY_BOX_SIDE_RIGHT = 4, IMG_ENTRY_BOX_SIDE_UPPER = 5,
				IMG_ENTRY_BOX_SIDE_LOWER = 6;

		private int intType = -1, intBoxSide = 0;
		private URL uOriginalImage = null, uThumbImage = null;
		private Rect rOriginalSize = null;

		public TGDB_GameData_ImageEntry(int type, URL original, URL thumb, Rect originalSize,
				int boxSide) {
			intType = type;
			uOriginalImage = original;
			uThumbImage = thumb;
			rOriginalSize = originalSize;
			intBoxSide = boxSide;
		}

		public int getType() {
			return intType;
		}

		public URL getOriginalImageURL() {
			return uOriginalImage;
		}

		public URL getThumbImageURL() {
			return uThumbImage;
		}

		public Rect getOriginalSizeRect() {
			return rOriginalSize;
		}

		public int getBoxSide() {
			return intBoxSide;
		}
	}

	private int intID = 0, intPlatformID = 0;
	private float fRating = 0.0f;
	private String strTitle = null, strReleaseDate = null, strPlatform = null, strOverview = null,
			strESRB = null, strNumPlayers = null, strCoOp = null, strPublisher = null,
			strDeveloper = null, strBaseImageURL = null;
	private ArrayList<String> alGenres = null;
	private ArrayList<TGDB_GameData_ImageEntry> alImages = null;

	public TGDB_GameData(InputStream src) {
		try {
			XmlPullParser xrpRes = XmlPullParserFactory.newInstance().newPullParser();
			xrpRes.setInput(src, null);

			int eventType = xrpRes.getEventType();

			int intImageBoxSide = 0;
			URL uOriginalImage = null, uThumbImage = null;
			Rect rOriginalImageSize = new Rect(0, 0, 0, 0);

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					if (alGenres != null) {
						alGenres.clear();
					} else {
						alGenres = new ArrayList<String>();
					}
					if (alImages != null) {
						alImages.clear();
					} else {
						alImages = new ArrayList<TGDB_GameData_ImageEntry>();
					}
					break;
				case XmlResourceParser.START_TAG:
					cName = xrpRes.getName();
					if (cName.equalsIgnoreCase("baseImgUrl")) {
						strBaseImageURL = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("Game")) {
						intID = 0;
						strTitle = null;
						strReleaseDate = null;
						strPlatform = null;
					}
					if (cName.equalsIgnoreCase("id")) {
						intID = Integer.parseInt(xrpRes.nextText());
					}
					if (cName.equalsIgnoreCase("GameTitle")) {
						strTitle = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("PlatformId")) {
						intPlatformID = Integer.parseInt(xrpRes.nextText());
					}
					if (cName.equalsIgnoreCase("Platform")) {
						strPlatform = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("ReleaseDate")) {
						strReleaseDate = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("Overview")) {
						strOverview = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("ESRB")) {
						strESRB = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("genre")) {
						alGenres.add(xrpRes.nextText());
					}
					if (cName.equalsIgnoreCase("Players")) {
						strNumPlayers = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("Co-op")) {
						strCoOp = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("Publisher")) {
						strPublisher = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("Developer")) {
						strDeveloper = xrpRes.nextText();
					}
					if (cName.equalsIgnoreCase("Rating")) {
						fRating = Float.parseFloat(xrpRes.nextText());
					}
					if (cName.equalsIgnoreCase("original")) {
						rOriginalImageSize.right = Integer.parseInt(xrpRes.getAttributeValue(null,
								"width"));
						rOriginalImageSize.bottom = Integer.parseInt(xrpRes.getAttributeValue(null,
								"height"));
						uOriginalImage = new URL(strBaseImageURL + xrpRes.nextText());
					}
					if (cName.equalsIgnoreCase("thumb")) {
						uThumbImage = new URL(strBaseImageURL + xrpRes.nextText());
					}
					if (cName.equalsIgnoreCase("boxart")) {
						String side = xrpRes.getAttributeValue(null, "side"), width = xrpRes
								.getAttributeValue(null, "width"), height = xrpRes
								.getAttributeValue(null, "height"), thumbImage = xrpRes
								.getAttributeValue(null, "thumb");
						if (width != null && height != null) {
							rOriginalImageSize.right = Integer.parseInt(width);
							rOriginalImageSize.bottom = Integer.parseInt(height);
						}
						if (side != null) {
							if (side.equalsIgnoreCase("back")) {
								intImageBoxSide = TGDB_GameData_ImageEntry.IMG_ENTRY_BOX_SIDE_BACK;
							} else if (side.equalsIgnoreCase("front")) {
								intImageBoxSide = TGDB_GameData_ImageEntry.IMG_ENTRY_BOX_SIDE_FRONT;
							}
						}

						if (thumbImage != null) {
							uThumbImage = new URL(strBaseImageURL + thumbImage);
						}
						uOriginalImage = new URL(strBaseImageURL + xrpRes.nextText());
						alImages.add(new TGDB_GameData_ImageEntry(
								TGDB_GameData_ImageEntry.IMG_ENTRY_TYPE_BOXART, uOriginalImage,
								uThumbImage, rOriginalImageSize, intImageBoxSide));
					}
					if (cName.equalsIgnoreCase("banner")) {
						String width = xrpRes.getAttributeValue(null, "width"), height = xrpRes
								.getAttributeValue(null, "height");
						if (width != null && height != null) {
							rOriginalImageSize.right = Integer.parseInt(width);
							rOriginalImageSize.bottom = Integer.parseInt(height);
						}
						uOriginalImage = new URL(strBaseImageURL + xrpRes.nextText());
						alImages.add(new TGDB_GameData_ImageEntry(
								TGDB_GameData_ImageEntry.IMG_ENTRY_TYPE_BANNER, uOriginalImage,
								null, rOriginalImageSize,
								TGDB_GameData_ImageEntry.IMG_ENTRY_BOX_SIDE_NONE));
					}
					break;
				case XmlPullParser.END_TAG:
					cName = xrpRes.getName();
					if (cName.equalsIgnoreCase("fanart")) {
						alImages.add(new TGDB_GameData_ImageEntry(
								TGDB_GameData_ImageEntry.IMG_ENTRY_TYPE_FANART, uOriginalImage,
								uThumbImage, rOriginalImageSize,
								TGDB_GameData_ImageEntry.IMG_ENTRY_BOX_SIDE_NONE));
						uOriginalImage = null;
						uThumbImage = null;
					}
					break;
				}
				eventType = xrpRes.next();
			}
			src.close();
		} catch (XmlPullParserException e) {
			Log.e(getClass().getSimpleName(),
					"parseXML(): Couldn't create new XML Parser instance.");
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "parseXML(): Couldn't open remote xml resource.");
		}
	}

	public int getID() {
		return intID;
	}

	public String getTitle() {
		return strTitle;
	}

	public int getPlatformID() {
		return intPlatformID;
	}

	public String getPlatform() {
		return strPlatform;
	}

	public String getReleaseDate() {
		return strReleaseDate;
	}

	public String getOverview() {
		return strOverview;
	}

	public String getESRB() {
		return strESRB;
	}

	public Iterator<String> getGenres() {
		return alGenres.iterator();
	}

	public String getNumPlayers() {
		return strNumPlayers;
	}

	public String hasCoOp() {
		return strCoOp;
	}

	public String getPublisher() {
		return strPublisher;
	}

	public String getDeveloper() {
		return strDeveloper;
	}

	public float getRating() {
		return fRating;
	}

	public Iterator<TGDB_GameData_ImageEntry> getImages() {
		return alImages.iterator();
	}
}
