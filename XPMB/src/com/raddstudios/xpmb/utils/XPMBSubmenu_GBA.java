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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.raddstudios.xpmb.GBA_launcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class XPMBSubmenu_GBA {

	class XPMBSubmenuItem_GBA {

		private Drawable bmGameCover = null, bmGameBackground = null;
		private File fROMPath = null;
		private String strGameName = null, strGameCode = null,
				strGameCRC = null, strGameDescription = null,
				strGameRegions = null, strGameLanguages;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;

		public XPMBSubmenuItem_GBA(File romPath, String gameCode, String gameCRC) {
			fROMPath = romPath;
			strGameCode = gameCode;
			strGameName = fROMPath.getName();
			strGameCRC = gameCRC;
		}

		public File getROMPath() {
			return fROMPath;
		}

		public String getGameCode() {
			return strGameCode;
		}

		public void setGameRegions(String gameRegions) {
			strGameRegions = gameRegions;
		}

		public String getGameRegions() {
			return strGameRegions;
		}

		public void setGameLanguages(String gameLanguages) {
			strGameLanguages = gameLanguages;
		}

		public String getGameLanguages() {
			return strGameLanguages;
		}

		public void setGameBackground(Drawable gameBackground) {
			bmGameBackground = gameBackground;
		}

		public Drawable getGameBackground() {
			return bmGameBackground;
		}

		public void setGameCover(Drawable cover) {
			bmGameCover = cover;
		}

		public Drawable getGameCover() {
			return bmGameCover;
		}

		public void setGameName(String gameName) {
			strGameName = gameName;
		}

		public String getGameName() {
			return strGameName;
		}

		public String getGameCRC() {
			return strGameCRC;
		}

		public void setGameDescription(String gameDescription) {
			strGameDescription = gameDescription;
		}

		public String getGameDescription() {
			return strGameDescription;
		}

		public void setParentView(ImageView parent) {
			ivParentView = parent;
		}

		public ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(TextView label) {
			tvParentLabel = label;
		}

		public TextView getParentLabel() {
			return tvParentLabel;
		}
	}

	private ArrayList<XPMBSubmenuItem_GBA> alItems = null;
	private GBA_launcher mRoot = null;

	public XPMBSubmenu_GBA(GBA_launcher root) {
		mRoot = root;

		File storPt = new File(Environment.getExternalStorageDirectory(), "GBA");
		storPt.mkdirs();
		if (!storPt.isDirectory()) {
			System.err.println("GBA_ROM_LOAD: can't create or access "
					+ storPt.getAbsolutePath());
			return;
		}

		alItems = new ArrayList<XPMBSubmenuItem_GBA>();
		try {
			File[] storPtCont = storPt.listFiles();
			for (File f : storPtCont) {
				if (f.getName().endsWith(".zip")) {
					ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
					Enumeration<? extends ZipEntry> ze = zf.entries();
					while (ze.hasMoreElements()) {
						ZipEntry zef = ze.nextElement();
						if (zef.getName().endsWith(".gba")
								|| zef.getName().endsWith(".GBA")) {
							InputStream fi = zf.getInputStream(zef);
							fi.skip(0xAC);
							String gameCode = "";
							gameCode += (char) fi.read();
							gameCode += (char) fi.read();
							gameCode += (char) fi.read();
							gameCode += (char) fi.read();
							fi.close();
							String gameCRC = Long.toHexString(zef.getCrc())
									.toUpperCase(
											mRoot.getResources()
													.getConfiguration().locale);
							XPMBSubmenuItem_GBA cItem = new XPMBSubmenuItem_GBA(
									f, gameCode, gameCRC);
							// TODO Finish implementing game metadata loading
							alItems.add(cItem);
							break;
						}
					}
					zf.close();
				} else if (f.getName().endsWith(".gba")) {
					InputStream fi = new FileInputStream(f);
					fi.skip(0xAC);
					String gameCode = "";
					gameCode += (char) fi.read();
					gameCode += (char) fi.read();
					gameCode += (char) fi.read();
					gameCode += (char) fi.read();
					fi.close();
					CRC32 cCRC = new CRC32();
					fi = new FileInputStream(f);
					int cByte = 0;
					while ((cByte = fi.read()) != -1) {
						cCRC.update(cByte);
					}
					fi.close();
					String gameCRC = Long
							.toHexString(cCRC.getValue())
							.toUpperCase(
									root.getResources().getConfiguration().locale);
					alItems.add(new XPMBSubmenuItem_GBA(f, gameCode, gameCRC));
				}
			}
		} catch (Exception e) {
			// TODO Handle errors when loading found ROMs
			e.printStackTrace();
		}
	}

	private float pxFromDip(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				mRoot.getResources().getDisplayMetrics());
	}

	public void parseInitLayout(ViewGroup base) {
		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {
			// Setup Icon
			LayoutParams cItemParams = new LayoutParams((int) pxFromDip(70),
					(int) pxFromDip(70));
			ImageView cItem = new ImageView(base.getContext());
			cItem.setX(pxFromDip(48));
			if (mY == 0) {
				cItem.setY(pxFromDip(84));
			} else {
				cItem.setY(pxFromDip(240 + (16 * (mY - 1)) + (75 * (mY - 1))));
			}
			cItem.setPivotX(pxFromDip(35));
			cItem.setPivotY(pxFromDip(55));
			String cCover = "drawable/" + alItems.get(mY)base.getIcon();
			Drawable cDrawable = base.getResources().getDrawable(
					vgParent.getResources().getIdentifier(cIcon, null,
							vgParent.getContext().getPackageName()));
			cItem.setImageDrawable(cDrawable);
			if (mX > 0) {
				cItem.setScaleX(0.7f);
				cItem.setScaleY(0.7f);
			}
		}
	}

}
