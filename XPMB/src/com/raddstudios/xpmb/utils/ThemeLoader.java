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

package com.raddstudios.xpmb.utils;

import java.io.File;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ThemeLoader {

	Hashtable<String, Bitmap> hAssets = null;

	public ThemeLoader(Hashtable<String, Bitmap> dest) {
		hAssets = dest;
	}

	public void reloadTheme(ZipFile container) {
		long startT = System.currentTimeMillis();
		Hashtable<String, String> hCachedIcons = new Hashtable<String, String>();
		String zipName = new File(container.getName()).getName();
		zipName = zipName.substring(0, zipName.indexOf('.'));

		Log.d(getClass().getSimpleName(),
				"reloadTheme():Attempting to load theme at '" + container.getName() + "'");

		try {
			XmlPullParser xrpRes = XmlPullParserFactory.newInstance().newPullParser();
			ZipEntry thmSrc = container.getEntry(zipName + ".xml");

			if (thmSrc == null) {
				Log.e(getClass().getSimpleName(),
						"reloadTheme():Theme XML not found, must be named exactly as the container zip file. ["
								+ zipName
								+ ".xml]\rNow expect a NullPointerException at next drawing operation.");
				return;
			}

			xrpRes.setInput(container.getInputStream(thmSrc), null);

			int eventType = xrpRes.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					hAssets.clear();
					break;
				case XmlResourceParser.END_TAG:
					cName = xrpRes.getName();
					if (cName.equalsIgnoreCase("info")) {
						// TODO: Load theme information from the xml file.
					}
					if (cName.equalsIgnoreCase("icon")) {
						String strIconID = xrpRes.getAttributeValue(null, "id"), strIconName = xrpRes
								.getAttributeValue(null, "src");

						Log.d(getClass().getSimpleName(), "reloadTheme():Found icon ID '"
								+ strIconID + "' and points to file '" + strIconName + "'");
						Long i = System.currentTimeMillis();
						ZipEntry srcIcon = container.getEntry(strIconName);
						if (srcIcon == null) {
							Log.e(getClass().getSimpleName(),
									"reloadTheme():Source icon file not found. Check XML and zip contents."
											+ "\rNow expect a NullPointerException at next drawing operation");
							Log.i(getClass().getSimpleName(),
									"reloadTheme():Icon '" + strIconID + "' loading took "
											+ String.valueOf(System.currentTimeMillis() - i)
											+ "ms.");
							continue;
						}
						if (!hCachedIcons.containsKey(strIconName)) {
							hAssets.put("theme.icon|" + strIconID,
									BitmapFactory.decodeStream(container.getInputStream(srcIcon)));
							hCachedIcons.put(strIconName, strIconID);
						} else {
							Log.w(getClass().getSimpleName(), "reloadTheme():Icon '" + strIconName
									+ "' already cached under ID '" + hCachedIcons.get(strIconName)
									+ ". duplicating item.");
							hAssets.put("theme.icon|" + strIconID,
									hAssets.get("theme.icon|" + hCachedIcons.get(strIconName)));
						}
						Log.i(getClass().getSimpleName(),
								"reloadTheme():Icon '" + strIconID + "' loading took "
										+ String.valueOf(System.currentTimeMillis() - i) + "ms.");
					}
					break;
				}
				eventType = xrpRes.next();
			}
			container.close();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"reloadTheme():Couldn't load theme due to an unhandled exception.");
			e.printStackTrace();
		}

		Log.i(getClass().getSimpleName(),
				"reloadTheme():Theme load finished. Took "
						+ String.valueOf(System.currentTimeMillis() - startT) + "ms.");
	}

}
