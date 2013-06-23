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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
//import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class XPMB_TheGamesDB {
	private static final int THRESHOLD = 50;
	private String platform;
	private String gameName;
	private Activity callerActivity;
	private File romRoot;

	public XPMB_TheGamesDB(File romRoot, Activity callerActivity, String gameName) {
		this(romRoot, callerActivity, gameName, null);
	}

	public XPMB_TheGamesDB(File romRoot, Activity callerActivity, String gameName, String platform) {
		this.romRoot = romRoot;
		this.gameName = gameName;
		this.platform = platform;
		this.callerActivity = callerActivity;
	}

	private String getXmlFromUrl(String url) {
		String xml = null;

		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return XML
		return xml;
	}

	private Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

	private String getValue(Element item, String str) {
		try {
			NodeList n = item.getElementsByTagName(str);
			return this.getElementValue(n.item(0));
		} catch (NullPointerException e) {
			return "";
		}
	}

	private final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	private final String getCoverLink() {
		String coverURL = null;
		String URL = "http://thegamesdb.net/api/GetGamesList.php?name="
				+ URLEncoder.encode(this.gameName.split("\\[")[0].split("\\(")[0]);
		if (null != this.platform)
			URL = URL + "&platform=" + URLEncoder.encode(getPlatform());
		System.out.println("URL: " + URL);
		String xml = this.getXmlFromUrl(URL); // getting XML
		Document doc = this.getDomElement(xml); // getting DOM element

		NodeList nl = doc.getElementsByTagName("Game");
		Element bestMatch = (Element) nl.item(0);

		String id = getValue(bestMatch, "id");
		System.out.println(gameName + " has id " + id);
		if (null != id) {
			String URL2 = "http://thegamesdb.net/api/GetGame.php?id=" + id;
			String xml2 = this.getXmlFromUrl(URL2);
			Document doc2 = this.getDomElement(xml2);

			coverURL = getElementValue(doc2.getElementsByTagName("baseImgUrl").item(0));
			System.out.println("BaseURL: " + coverURL);
			Element game = (Element) doc2.getElementsByTagName("Game").item(0);

			String dbTitle = getElementValue(doc2.getElementsByTagName("GameTitle").item(0));
			if (this.stringMatch(this.gameName.split("\\[")[0].split("\\(")[0],
					dbTitle.split("\\[")[0].split("\\(")[0]))
				try {
					NodeList nl2 = game.getElementsByTagName("boxart");
					for (int i = 0; i < nl2.getLength(); i++) {
						Element current = (Element) nl2.item(i);
						if (current.getAttribute("side").equals("front")) {
							coverURL += current.getAttribute("thumb");
						}
					}
				} catch (NullPointerException e) {
				}

		}
		Log.i(getClass().getSimpleName(), "getCoverLink():Cover URL for game '"+gameName+"' is '"+coverURL+"'");
		return coverURL;
	}

	private String getPlatform() {
		// TODO Auto-generated method stub
		if (platform.equals("PCE"))
			return "TurboGrafx 16";
		else if (platform.equals("MD"))
			return "Sega Genesis";
		else if (platform.equals("SMS"))
			return "Sega Master System";
		else if (platform.equals("GBA"))
			return "Nintendo Game Boy Advance";
		else if (platform.equals("GBC"))
			return "Nintendo Game Boy Color";
		else if (platform.equals("GB"))
			return "Nintendo Game Boy";
		else if (platform.equals("NES"))
			return "Nintendo Entertainment System (NES)";
		else if (platform.equals("SNES"))
			return "Super Nintendo (SNES)";
		else
			return null;
	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) callerActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private boolean stringMatch(String romName, String dbName) {
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

				if (rom[i].equalsIgnoreCase(db[j]))
					match++;
				System.out.println(rom[i] + " == " + db[j] + " MATCH " + match);
			}
		}

		int rate = (match * 100) / Math.min(rom.length, db.length);
		System.out.println("ROM NAME " + romName + " DB NAME " + dbName + " MATCHING " + rate);
		return rate > XPMB_TheGamesDB.THRESHOLD;

	}

	public boolean DownloadFromUrl() {
		if (this.isOnline()) {
			String DownloadUrl = getCoverLink();

			try {
				if (!romRoot.exists()) {
					romRoot.mkdirs();
				}

				URL url = new URL(DownloadUrl); // you can write here any link
				File file = new File(romRoot, this.gameName + "-CV.jpg");
				System.out.println(file.getAbsolutePath().toString());
				long startTime = System.currentTimeMillis();
				Log.d(getClass().getSimpleName(), "download begining");
				Log.d(getClass().getSimpleName(), "download url:" + url);
				Log.d(getClass().getSimpleName(), "downloaded file name:" + gameName + "-CV.jpg");

				/* Open a connection to that URL. */
				URLConnection ucon = url.openConnection();

				/*
				 * Define InputStreams to read from the URLConnection.
				 */
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);

				/*
				 * Read bytes to the Buffer until there is nothing more to
				 * read(-1).
				 */
				ByteArrayBuffer baf = new ByteArrayBuffer(5000);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				/* Convert the Bytes read to a String. */
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray());
				fos.flush();
				fos.close();
				Log.d("DownloadManager", "download ready in"
						+ ((System.currentTimeMillis() - startTime) / 1000) + " sec");
				return true;
			} catch (IOException e) {
				Log.d("DownloadManager", "Error: " + e);
				return false;

			}

		}
		return false;
	}
}
