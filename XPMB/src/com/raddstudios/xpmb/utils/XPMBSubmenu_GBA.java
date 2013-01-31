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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Submenu_GBA;
import com.raddstudios.xpmb.utils.ROMInfo.ROMInfoNode;

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
	private XPMB_Submenu_GBA mRoot = null;
	private Handler hMBus = null;
	private int intSelItem = 0;
	private File mROMRoot = null;

	public XPMBSubmenu_GBA(File fROMRoot, Handler messageBus,
			XPMB_Submenu_GBA root) {
		mRoot = root;
		hMBus = messageBus;
		mROMRoot = fROMRoot;

		alItems = new ArrayList<XPMBSubmenuItem_GBA>();
	}

	public void doInit() {
		mROMRoot.mkdirs();
		if (!mROMRoot.isDirectory()) {
			System.err.println("GBA_ROM_LOAD: can't create or access "
					+ mROMRoot.getAbsolutePath());
			return;
		}

		try {
			File[] storPtCont = mROMRoot.listFiles();
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
									mRoot.getResources().getConfiguration().locale);

					XPMBSubmenuItem_GBA cItem = new XPMBSubmenuItem_GBA(f,
							gameCode, gameCRC);
					alItems.add(cItem);
				}

			}
		} catch (Exception e) {
			// TODO Handle errors when loading found ROMs
			e.printStackTrace();
		}
	}

	private void loadAssociatedMetadata(XPMBSubmenuItem_GBA item, ViewGroup base) {

		try {
			ROMInfoNode rinCData = mRoot.ridROMInfoDat.getNode(item
					.getGameCRC());
			if (rinCData != null) {
				item.setGameName(rinCData.getGameName());
			}
			File resStor = new File(new File(
					Environment.getExternalStorageDirectory(), "GBA"),
					"Resources");
			if (resStor.exists()) {
				File fExtRes = new File(resStor, item.getGameCode() + "-CV.jpg");
				if (fExtRes.exists()) {
					item.setGameCover(new BitmapDrawable(mRoot.getResources(),
							BitmapFactory.decodeStream(new FileInputStream(
									fExtRes))));
				} else {
					item.setGameCover(base.getResources().getDrawable(
							base.getResources().getIdentifier(
									"drawable/ui_cover_not_found", null,
									base.getContext().getPackageName())));
				}
				fExtRes = new File(resStor, item.getGameCode() + "-BG.jpg");
				if (fExtRes.exists()) {
					item.setGameBackground(new BitmapDrawable(mRoot
							.getResources(), BitmapFactory
							.decodeStream(new FileInputStream(fExtRes))));
				}
				fExtRes = new File(resStor, "META_DESC");
				if (fExtRes.exists()) {
					BufferedReader ebr = new BufferedReader(
							new InputStreamReader(new FileInputStream(fExtRes)));
					String el = null;
					while ((el = ebr.readLine()) != null) {
						if (el.startsWith(item.getGameCode())) {
							item.setGameDescription(el.substring(el
									.indexOf(item.getGameCode()) + 5));
						}
					}
					ebr.close();
				}
			}
		} catch (Exception e) {
			// TODO Handle errors when loading associated ROM metadata
			e.printStackTrace();
		}
	}

	private float pxFromDip(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				mRoot.getResources().getDisplayMetrics());
	}

	public void parseInitLayout(ViewGroup base) {
		if (alItems.size() == 0) {
			LayoutParams cItemParams = new LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(100));
			TextView cItem = new TextView(base.getContext());
			cItem.setText(mRoot.getText(R.string.strNoGames));
			cItem.setTextColor(Color.WHITE);
			cItem.setShadowLayer(16, 0, 0, Color.WHITE);
			cItem.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cItem.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cItem.setX(pxFromDip(48));
			cItem.setY(pxFromDip(110));
			base.addView(cItem, cItemParams);
			return;
		}

		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {
			loadAssociatedMetadata(alItems.get(mY), base);
			// Setup Icon
			LayoutParams cItemParams = new LayoutParams((int) pxFromDip(50),
					(int) pxFromDip(50));
			ImageView cItem = new ImageView(base.getContext());
			cItem.setImageDrawable(alItems.get(mY).getGameCover());
			cItem.setX(pxFromDip(48));
			cItem.setPivotX(0.0f);
			cItem.setPivotY(0.0f);
			if (mY == 0) {
				cItem.setY(pxFromDip(110));
				cItem.setScaleX(2.0f);
				cItem.setScaleY(2.0f);
			} else {
				cItem.setY(pxFromDip(226 + (50 * (mY - 1))));
			}
			cItem.setImageDrawable(alItems.get(mY).getGameCover());
			// Setup Label
			LayoutParams cLabelParams = new LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(100));
			TextView cLabel = new TextView(base.getContext());
			cLabel.setText(alItems.get(mY).getGameName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cLabel.setX(pxFromDip(164));
			if (mY == 0) {
				cLabel.setY(pxFromDip(110));
			} else {
				cLabel.setY(pxFromDip(226 + (50 * (mY - 1))));
				cLabel.setAlpha(0.0f);
			}
			alItems.get(mY).setParentView(cItem);
			alItems.get(mY).setParentLabel(cLabel);
			base.addView(cItem, cItemParams);
			base.addView(cLabel, cLabelParams);
		}
	}

	public void moveToNextItem() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {

			ImageView ivCurItem = alItems.get(mY).getParentView();
			TextView tvCurLabel = alItems.get(mY).getParentLabel();
			float cY = ivCurItem.getY(), cY_l = tvCurLabel.getY();

			if (mY == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY - pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l - pxFromDip(91))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 1.0f,
						0.0f));
			} else if (mY == (intSelItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY - pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l - pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 0.0f,
						1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY - pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l - pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_mu = new AnimatorSet();
		ag_xmb_sm_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_mu.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_sm_mu.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	public void moveToPrevItem() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {

			ImageView ivCurItem = alItems.get(mY).getParentView();
			TextView tvCurLabel = alItems.get(mY).getParentLabel();
			float cY = ivCurItem.getY(), cY_l = tvCurLabel.getY();

			if (mY == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY + pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l + pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 1.0f,
						0.0f));
			} else if (mY == (intSelItem - 1)) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY + pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l + pxFromDip(91))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 0.0f,
						1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY + pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l + pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_md = new AnimatorSet();
		ag_xmb_sm_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_md.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_sm_md.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	public void runSelectedItem() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setComponent(ComponentName
				.unflattenFromString("com.androidemu.gba/.EmulatorActivity"));
		intent.setData(Uri.fromFile(alItems.get(intSelItem).getROMPath()));
		intent.setFlags(0x10000000);
		mRoot.startActivity(intent);
	}
}
