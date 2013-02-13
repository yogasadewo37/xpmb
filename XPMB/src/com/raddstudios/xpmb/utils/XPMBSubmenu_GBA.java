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

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.TableRow;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.ROMInfo.ROMInfoNode;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class XPMBSubmenu_GBA extends XPMB_Layout {

	class XPMBSubmenuItem_GBA {

		private Drawable bmGameCover = null, bmGameBackground = null;
		private File fROMPath = null;
		private String strGameName = null, strGameCode = null, strGameCRC = null,
				strGameDescription = null, strGameRegions = null, strGameLanguages;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private XPMB_TableRow trParentContainer = null;

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

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		public XPMB_ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(XPMB_TextView label) {
			tvParentLabel = label;
		}

		public XPMB_TextView getParentLabel() {
			return tvParentLabel;
		}

		public void setParentContainer(XPMB_TableRow parent) {
			trParentContainer = parent;
		}

		public XPMB_TableRow getParentContainer() {
			return trParentContainer;
		}
	}

	private ArrayList<XPMBSubmenuItem_GBA> alItems = null;
	private int intSelItem = 0;
	private File mROMRoot = null;
	private ROMInfo ridROMInfoDat = null;
	private XPMB_TextView tv_no_game = null;
	private XPMB_TableLayout tlRoot = null;

	public XPMBSubmenu_GBA(XPMB_Activity root, Handler messageBus,ViewGroup rootView, File fROMRoot) {
		super(root, messageBus,rootView);
		mROMRoot = fROMRoot;

		alItems = new ArrayList<XPMBSubmenuItem_GBA>();
	}

	public void doInit() {
		mROMRoot.mkdirs();
		if (!mROMRoot.isDirectory()) {
			System.err.println("XPMBSubmenu_GBA::doInit() : can't create or access "
					+ mROMRoot.getAbsolutePath());
			return;
		}
		File mROMResDir = new File(mROMRoot, "Resources");
		if (!mROMResDir.exists()) {
			mROMResDir.mkdirs();
			if (!mROMResDir.isDirectory()) {
				System.err.println("XPMBSubmenu_GBA::doInit() : can't create or access "
						+ mROMResDir.getAbsolutePath());
				return;
			}
		}
		ridROMInfoDat = new ROMInfo(getRootActivity().getResources().getXml(R.xml.rominfo_gba),
				ROMInfo.TYPE_CRC);

		try {
			File[] storPtCont = mROMRoot.listFiles();
			for (File f : storPtCont) {
				if (f.getName().endsWith(".zip")) {
					ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
					Enumeration<? extends ZipEntry> ze = zf.entries();
					while (ze.hasMoreElements()) {
						ZipEntry zef = ze.nextElement();
						if (zef.getName().endsWith(".gba") || zef.getName().endsWith(".GBA")) {
							InputStream fi = zf.getInputStream(zef);
							fi.skip(0xAC);
							String gameCode = "";
							gameCode += (char) fi.read();
							gameCode += (char) fi.read();
							gameCode += (char) fi.read();
							gameCode += (char) fi.read();
							fi.close();
							String gameCRC = Long.toHexString(zef.getCrc()).toUpperCase(
									getRootActivity().getResources().getConfiguration().locale);
							XPMBSubmenuItem_GBA cItem = new XPMBSubmenuItem_GBA(f, gameCode,
									gameCRC);
							loadAssociatedMetadata(cItem);
							alItems.add(cItem);
							break;
						}
					}
					zf.close();
				} else if (f.getName().endsWith(".gba") || f.getName().endsWith(".GBA")) {
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
					String gameCRC = Long.toHexString(cCRC.getValue()).toUpperCase(
							getRootActivity().getResources().getConfiguration().locale);

					XPMBSubmenuItem_GBA cItem = new XPMBSubmenuItem_GBA(f, gameCode, gameCRC);
					loadAssociatedMetadata(cItem);
					alItems.add(cItem);
				}

			}
		} catch (Exception e) {
			// TODO Handle errors when loading found ROMs
			e.printStackTrace();
		}
	}

	private void loadAssociatedMetadata(XPMBSubmenuItem_GBA item) {

		try {
			ROMInfoNode rinCData = ridROMInfoDat.getNode(item.getGameCRC());

			if (rinCData != null) {
				String romName = rinCData.getROMData().getROMName();

				if (romName.indexOf('(') != -1) {
					String romRegions = romName.substring(romName.indexOf('(') + 1,
							romName.indexOf(')'));
					item.setGameRegions(romRegions);
				}
				if (romName.indexOf('(', romName.indexOf(')')) != -1) {
					String romLanguages = romName.substring(romName.indexOf('(',
							romName.indexOf(')')));
					romLanguages = romLanguages.substring(romLanguages.indexOf('(') + 1,
							romLanguages.indexOf(')'));
					item.setGameLanguages(romLanguages);
				}
				if (rinCData.getNumReleases() == 0) {
					if (romName.indexOf('(') != -1) {

						item.setGameName(romName.substring(0, romName.indexOf('(') - 1));
					} else {
						item.setGameName(romName.substring(0, romName.indexOf(".gba")));
					}
				} else {
					item.setGameName(rinCData.getReleaseData(0).getReleaseName());
				}
			}

			File resStor = new File(mROMRoot, "Resources");
			if (resStor.exists()) {
				File fExtRes = new File(resStor, item.getGameName() + "-CV.jpg");
				if (fExtRes.exists()) {
					item.setGameCover(new BitmapDrawable(getRootActivity().getResources(), BitmapFactory
							.decodeStream(new FileInputStream(fExtRes))));
				} else {
					item.setGameCover(getRootActivity().getResources().getDrawable(
							getRootActivity().getResources().getIdentifier("drawable/ui_cover_not_found_gba",
									null, getRootActivity().getPackageName())));
				}
				fExtRes = new File(resStor, item.getGameName() + "-BG.jpg");
				if (fExtRes.exists()) {
					item.setGameBackground(new BitmapDrawable(getRootActivity().getResources(), BitmapFactory
							.decodeStream(new FileInputStream(fExtRes))));
				}
				fExtRes = new File(resStor, "META_DESC");
				if (fExtRes.exists()) {
					BufferedReader ebr = new BufferedReader(new InputStreamReader(
							new FileInputStream(fExtRes)));
					String el = null;
					while ((el = ebr.readLine()) != null) {
						if (el.startsWith(item.getGameName())) {
							item.setGameDescription(el.substring(el.indexOf(item.getGameName()) + 5));
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

	public void parseInitLayout() {

		if (alItems.size() == 0) {
			tv_no_game = new XPMB_TextView(getRootView().getContext());
			LayoutParams lp_ng = new LayoutParams((int) pxFromDip(320), (int) pxFromDip(100),
					pxFromDip(48), pxFromDip(128));
			tv_no_game.setLayoutParams(lp_ng);
			tv_no_game.setText(getRootActivity().getText(R.string.strNoGames));
			tv_no_game.setTextColor(Color.WHITE);
			tv_no_game.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_game.setTextAppearance(getRootView().getContext(), android.R.style.TextAppearance_Medium);
			tv_no_game.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			getRootView().addView(tv_no_game);
			return;
		}

		tlRoot = new XPMB_TableLayout(getRootView().getContext());
		AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(pxFromDip(396),
				pxFromDip(160 + (60 * alItems.size())), pxFromDip(48), pxFromDip(94));
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_GBA xsi : alItems) {

			int idx = alItems.indexOf(xsi);

			XPMB_TableRow cItem = new XPMB_TableRow(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cItem.setId(getNextID());

			// Setup Icon
			if (idx == 0) {
				TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(128),
						(int) pxFromDip(128));
				cIconParams.column = 0;
				cIconParams.topMargin = pxFromDip(16);
				cIconParams.bottomMargin = pxFromDip(16);
				cIcon.setLayoutParams(cIconParams);
			} else {
				TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(50),
						(int) pxFromDip(50));
				cIconParams.column = 0;
				cIcon.setLayoutParams(cIconParams);
			}
			cIcon.setImageDrawable(xsi.getGameCover());
			// Setup Label
			if (idx == 0) {
				TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(128));
				cLabelParams.column = 1;
				cLabelParams.leftMargin = pxFromDip(144);
				cLabelParams.topMargin = pxFromDip(16);
				cLabelParams.bottomMargin = pxFromDip(16);
				cLabel.setLayoutParams(cLabelParams);
			} else {
				TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(50));
				cLabelParams.column = 1;
				cLabelParams.leftMargin = pxFromDip(144);
				cLabel.setLayoutParams(cLabelParams);
				cLabel.setAlpha(0.0f);
			}
			cLabel.setText(xsi.getGameName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(getRootView().getContext(), android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cItem.addView(cIcon);
			cItem.addView(cLabel);

			xsi.setParentView(cIcon);
			xsi.setParentLabel(cLabel);
			xsi.setParentContainer(cItem);

			tlRoot.addView(cItem);
		}
		// Prevent Image cover size change to distort layout during animations
		XPMB_TableRow tlFiller = new XPMB_TableRow(getRootView().getContext());
		XPMB_ImageView ivFiller = new XPMB_ImageView(getRootView().getContext());
		XPMB_TextView tvFiller = new XPMB_TextView(getRootView().getContext());
		TableRow.LayoutParams iv_f_lp = new TableRow.LayoutParams(pxFromDip(128), pxFromDip(128));
		TableRow.LayoutParams tv_f_lp = new TableRow.LayoutParams(pxFromDip(320), pxFromDip(128));
		iv_f_lp.column = 0;
		tv_f_lp.column = 1;
		ivFiller.setLayoutParams(iv_f_lp);
		tvFiller.setLayoutParams(tv_f_lp);
		tlFiller.addView(ivFiller);
		tlFiller.addView(tvFiller);
		tlRoot.addView(tlFiller);
		getRootView().addView(tlRoot);
		reloadGameBG();
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_START:
		case XPMB_Main.KEYCODE_CROSS:
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_LEFT:
		case XPMB_Main.KEYCODE_CIRCLE:
			getRootActivity().requestUnloadSubmenu();
			break;
		}
	}

	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		final int intAnimItem = intSelItem;
		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "Y", tlRoot.getY() - pxFromDip(50)));
		ValueAnimator va_ci_sc = ValueAnimator.ofInt(pxFromDip(128), pxFromDip(50));
		va_ci_sc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.width = (Integer) animation.getAnimatedValue();
				cIconP.height = (Integer) animation.getAnimatedValue();
				cLabelP.height = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_ni_sc = ValueAnimator.ofInt(pxFromDip(50), pxFromDip(128));
		va_ni_sc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem + 1);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.width = (Integer) animation.getAnimatedValue();
				cIconP.height = (Integer) animation.getAnimatedValue();
				cLabelP.height = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_ci_tbm = ValueAnimator.ofInt(pxFromDip(16), 0);
		va_ci_tbm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_ni_tbm = ValueAnimator.ofInt(0, pxFromDip(16));
		va_ni_tbm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem + 1);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});

		alAnims.add(va_ci_sc);
		alAnims.add(va_ci_tbm);
		alAnims.add(va_ni_tbm);
		alAnims.add(va_ni_sc);
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem).getParentLabel(), "Alpha", 0.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem + 1).getParentLabel(), "Alpha",
				1.0f));

		AnimatorSet ag_xmb_sm_mu = new AnimatorSet();
		ag_xmb_sm_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_mu.setDuration(150);
		getRootActivity().lockKeys(true);
		reloadGameBG();
		ag_xmb_sm_mu.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		final int intAnimItem = intSelItem;
		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "Y", tlRoot.getY() + pxFromDip(50)));
		ValueAnimator va_ci_sc = ValueAnimator.ofInt(pxFromDip(128), pxFromDip(50));
		va_ci_sc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.width = (Integer) animation.getAnimatedValue();
				cIconP.height = (Integer) animation.getAnimatedValue();
				cLabelP.height = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_pi_sc = ValueAnimator.ofInt(pxFromDip(50), pxFromDip(128));
		va_pi_sc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem - 1);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.width = (Integer) animation.getAnimatedValue();
				cIconP.height = (Integer) animation.getAnimatedValue();
				cLabelP.height = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_ci_tbm = ValueAnimator.ofInt(pxFromDip(16), 0);
		va_ci_tbm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_pi_tbm = ValueAnimator.ofInt(0, pxFromDip(16));
		va_pi_tbm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_GBA cSub = alItems.get(intAnimItem - 1);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});

		alAnims.add(va_ci_sc);
		alAnims.add(va_ci_tbm);
		alAnims.add(va_pi_tbm);
		alAnims.add(va_pi_sc);
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem).getParentLabel(), "Alpha", 0.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem - 1).getParentLabel(), "Alpha",
				1.0f));

		AnimatorSet ag_xmb_sm_md = new AnimatorSet();
		ag_xmb_sm_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_md.setDuration(150);
		getRootActivity().lockKeys(true);
		reloadGameBG();
		ag_xmb_sm_md.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	private void reloadGameBG() {
		XPMB_ImageView iv_bg = getRootActivity().getCustomBGView();
		if (iv_bg.getDrawable() != null) {
			ObjectAnimator rbg_a_pre = ObjectAnimator.ofFloat(iv_bg, "Alpha", 1.0f, 0.0f);
			rbg_a_pre.setDuration(200);
			rbg_a_pre.start();
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				XPMB_ImageView iv_bg = getRootActivity().getCustomBGView();
				iv_bg.setAlpha(0.0f);
				iv_bg.setImageDrawable(alItems.get(intSelItem).getGameBackground());
				if (iv_bg.getDrawable() == null) {
					return;
				}
				ObjectAnimator rbg_a_pos = ObjectAnimator.ofFloat(iv_bg, "Alpha", 0.0f, 1.0f);
				rbg_a_pos.setDuration(200);
				rbg_a_pos.start();
			}

		}, 200);
	}

	public void execSelectedItem() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setComponent(ComponentName
				.unflattenFromString("com.androidemu.gba/.EmulatorActivity"));
		intent.setData(Uri.fromFile(alItems.get(intSelItem).getROMPath()));
		intent.setFlags(0x10000000);
		getRootActivity().startActivity(intent);
	}

	@Override
	public void doCleanup() {
		if (alItems.size() == 0 && tv_no_game != null) {
			getRootView().removeView(tv_no_game);
			return;
		}
		if (tlRoot != null) {
			tlRoot.removeAllViews();
			getRootView().removeView(tlRoot);
		}
		XPMB_ImageView iv_bg = getRootActivity().getCustomBGView();
		if (iv_bg.getDrawable() != null) {
			ObjectAnimator rbg_a_pre = ObjectAnimator.ofFloat(iv_bg, "Alpha", 1.0f, 0.0f);
			rbg_a_pre.setDuration(200);
			rbg_a_pre.start();
			getMessageBus().postDelayed(new Runnable() {

				@Override
				public void run() {
					XPMB_ImageView iv_bg = getRootActivity().getCustomBGView();
					iv_bg.setImageDrawable(null);
					iv_bg.setAlpha(1.0f);
				}

			}, 200);
		}
	}
}
