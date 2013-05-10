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
		String zipName = new File(container.getName()).getName();
		zipName = zipName.substring(0, zipName.indexOf('.'));

		try {
			XmlPullParser xrpRes = XmlPullParserFactory.newInstance().newPullParser();
			xrpRes.setInput(container.getInputStream(container.getEntry(zipName + ".xml")), null);

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
						hAssets.put("theme.icon|" + xrpRes.getAttributeValue(null, "id"), BitmapFactory
								.decodeStream(container.getInputStream(container.getEntry(xrpRes
										.getAttributeValue(null, "src")))));
					}
					break;
				}
				eventType = xrpRes.next();
			}
			container.close();
		} catch (Exception e) {
			Log.e("ThemeLoader:reloadTheme()", "Couldn't load theme");
			e.printStackTrace();
		}
	}

}
