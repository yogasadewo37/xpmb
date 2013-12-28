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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import android.util.Log;

public abstract class XPMB_TGDBApi {

	public static final int DEFAULT_SEARCH_HIT_THRESHOLD = 50;
	public static final String BASE_API_URL = "http://thegamesdb.net/api/";

	public static final String TGDB_PLATFORM_PCE = "TurboGrafx 16",
			TGDB_PLATFORM_SEGA_MEGADRIVE = "Sega Genesis",
			TGDB_PLATFORM_SEGA_MASTER_SYS = "Sega Master System",
			TGDB_PLATFORM_NINTENDO_GBA = "Nintendo Game Boy Advance",
			TGDB_PLATFORM_NINTENDO_GBC = "Nintendo Game Boy Color",
			TGDB_PLATFORM_NINTENDO_GB = "Nintendo Game Boy",
			TGDB_PLATFORM_NINTENDO_NES = "Nintendo Entertainment System (NES)",
			TGDB_PLATFORM_NINTENDO_SNES = "Super Nintendo (SNES)";

	public void downloadXMLForID(int id, File dest) {
		try {
			URL uSrc = new URL(BASE_API_URL + "GetGame.php?id=" + id);

			InputStream is = uSrc.openStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			FileOutputStream fos = new FileOutputStream(dest);

			int current = 0;
			while ((current = bis.read()) != -1) {
				fos.write(current);
			}

			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(getClass().getSimpleName(),
					"downloadXMLForID(): Couldn't write to '" + dest.getAbsolutePath() + "'.");
		} catch (MalformedURLException e) {
			Log.e(getClass().getSimpleName(), "downloadXMLForID(): Wrong URL was created.");
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "downloadXMLForID(): Couldn't connect to server.");
		}
	}

	public TGDB_GamesList getEntryMatchesFor(String gameTitle) {
		try {
			return new TGDB_GamesList(new URL(BASE_API_URL + "GetGamesList.php?name="
					+ URLEncoder.encode(gameTitle)));
		} catch (MalformedURLException e) {
			Log.e(getClass().getSimpleName(), "getEntryMatchesFor(): Wrong URL was created.");
		}
		return null;
	}

	public TGDB_GamesList getEntryMatchesFor(String gameTitle, String gamePlatform) {
		try {
			return new TGDB_GamesList(
					new URL(BASE_API_URL + "GetGamesList.php?name=" + URLEncoder.encode(gameTitle)
							+ "&platform=" + URLEncoder.encode(gamePlatform)));
		} catch (MalformedURLException e) {
			Log.e(getClass().getSimpleName(), "getEntryMatchesFor(): Wrong URL was created.");
		}
		return null;
	}

	public boolean stringMatch(String romName, String dbName, int threshold) {
		// TODO: Test this
		romName = Normalizer.normalize(romName, Form.NFD).replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");
		dbName = Normalizer.normalize(dbName, Form.NFD).replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");

		romName = romName.replaceAll("[^a-zA-Z0-9\\s]+", "");
		dbName = dbName.replaceAll("[^a-zA-Z0-9\\s]+", "");

		String[] rom = romName.split("\\s+");
		String[] db = dbName.split("\\s+");
		int match = 0;
		for (int i = 0; i < rom.length; i++) {
			for (int j = 0; j < db.length; j++) {

				if (rom[i].equalsIgnoreCase(db[j])) match++;
				// System.out.println(rom[i] + " == " + db[j] + " MATCH " +
				// match);
			}
		}

		int rate = (match * 100) / Math.min(rom.length, db.length);
		Log.v(getClass().getSimpleName(), "stringMatch(): ROM NAME " + romName + " DB NAME "
				+ dbName + " MATCHING " + rate);
		return rate > threshold;
	}
}
