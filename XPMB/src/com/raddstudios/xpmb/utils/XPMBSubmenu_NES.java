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
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.ROMInfo.ROMInfoNode;

@SuppressWarnings("deprecation")
public class XPMBSubmenu_NES extends XPMB_Layout {

	class XPMBSubmenuItem_NES {

		private Drawable bmGameCover = null, bmGameBackground = null;
		private File fROMPath = null;
		private String strGameName = null, strGameCRC = null,
				strGameDescription = null, strGameRegions = null,
				strGameLanguages;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;

		public XPMBSubmenuItem_NES(File romPath, String gameCRC) {
			fROMPath = romPath;
			strGameName = fROMPath.getName();
			strGameCRC = gameCRC;
		}

		public File getROMPath() {
			return fROMPath;
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

	private ArrayList<XPMBSubmenuItem_NES> alItems = null;
	private XPMB_Activity mRoot = null;
	private Handler hMBus = null;
	private int intSelItem = 0;
	private File mROMRoot = null;
	private ROMInfo ridROMInfoDat = null;
	private TextView tv_no_game = null;

	public XPMBSubmenu_NES(XPMB_Activity root, Handler messageBus, File fROMRoot) {
		super(root, messageBus);
		mRoot = root;
		hMBus = messageBus;
		mROMRoot = fROMRoot;

		alItems = new ArrayList<XPMBSubmenuItem_NES>();
	}

	public void doInit() {
		mROMRoot.mkdirs();
		if (!mROMRoot.isDirectory()) {
			System.err
					.println("XPMBSubmenu_NES::doInit() : can't create or access "
							+ mROMRoot.getAbsolutePath());
			return;
		}
		File mROMResDir = new File(mROMRoot, "Resources");
		if (!mROMResDir.exists()) {
			mROMResDir.mkdirs();
			if (!mROMResDir.isDirectory()) {
				System.err
						.println("XPMBSubmenu_NES::doInit() : can't create or access "
								+ mROMResDir.getAbsolutePath());
				return;
			}
		}

		ridROMInfoDat = new ROMInfo(mRoot.getResources().getXml(
				R.xml.rominfo_nes), ROMInfo.TYPE_CRC);

		try {
			File[] storPtCont = mROMRoot.listFiles();
			for (File f : storPtCont) {
				if (f.getName().endsWith(".zip")) {
					ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
					Enumeration<? extends ZipEntry> ze = zf.entries();
					while (ze.hasMoreElements()) {
						ZipEntry zef = ze.nextElement();
						if (zef.getName().endsWith(".nes")
								|| zef.getName().endsWith(".NES")) {
							String gameCRC = Long.toHexString(zef.getCrc())
									.toUpperCase(
											mRoot.getResources()
													.getConfiguration().locale);
							XPMBSubmenuItem_NES cItem = new XPMBSubmenuItem_NES(
									f, gameCRC);
							loadAssociatedMetadata(cItem);
							alItems.add(cItem);
							break;
						}
					}
					zf.close();
				} else if (f.getName().endsWith(".nes")
						|| f.getName().endsWith(".NES")) {
					CRC32 cCRC = new CRC32();
					InputStream fi = new FileInputStream(f);
					int cByte = 0;
					while ((cByte = fi.read()) != -1) {
						cCRC.update(cByte);
					}
					fi.close();
					String gameCRC = Long
							.toHexString(cCRC.getValue())
							.toUpperCase(
									mRoot.getResources().getConfiguration().locale);

					XPMBSubmenuItem_NES cItem = new XPMBSubmenuItem_NES(f,
							gameCRC);
					loadAssociatedMetadata(cItem);
					alItems.add(cItem);
				}

			}
		} catch (Exception e) {
			// TODO Handle errors when loading found ROMs
			e.printStackTrace();
		}
	}

	private void loadAssociatedMetadata(XPMBSubmenuItem_NES item) {

		try {
			ROMInfoNode rinCData = ridROMInfoDat.getNode(item.getGameCRC());

			if (rinCData != null) {
				String romName = rinCData.getROMData().getROMName();

				if (romName.indexOf('(') != -1) {
					String romRegions = romName.substring(
							romName.indexOf('(') + 1, romName.indexOf(')'));
					item.setGameRegions(romRegions);
				}
				if (romName.indexOf('(', romName.indexOf(')')) != -1) {
					String romLanguages = romName.substring(romName.indexOf(
							'(', romName.indexOf(')')));
					romLanguages = romLanguages.substring(
							romLanguages.indexOf('(') + 1,
							romLanguages.indexOf(')'));
					item.setGameLanguages(romLanguages);
				}
				if (rinCData.getNumReleases() == 0) {
					if (romName.indexOf('(') != -1) {

						item.setGameName(romName.substring(0,
								romName.indexOf('(') - 1));
					} else {
						item.setGameName(romName.substring(0,
								romName.indexOf(".nes")));
					}
				} else {
					item.setGameName(rinCData.getReleaseData(0)
							.getReleaseName());
				}
			}

			File resStor = new File(mROMRoot, "Resources");
			if (resStor.exists()) {
				File fExtRes = new File(resStor, item.getGameName() + "-CV.jpg");
				if (fExtRes.exists()) {
					item.setGameCover(new BitmapDrawable(mRoot.getResources(),
							BitmapFactory.decodeStream(new FileInputStream(
									fExtRes))));
				} else {
					item.setGameCover(mRoot.getResources().getDrawable(
							mRoot.getResources().getIdentifier(
									"drawable/ui_cover_not_found_nes", null,
									mRoot.getPackageName())));
				}
				fExtRes = new File(resStor, item.getGameName() + "-BG.jpg");
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
						if (el.startsWith(item.getGameName())) {
							item.setGameDescription(el.substring(el
									.indexOf(item.getGameName()) + 5));
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

	public void parseInitLayout(ViewGroup base) {
		int cId = 0xC0DE;

		if (alItems.size() == 0) {
			tv_no_game = new TextView(base.getContext());
			LayoutParams lp_ng = new LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(100), pxFromDip(48), pxFromDip(128));
			tv_no_game.setLayoutParams(lp_ng);
			tv_no_game.setText(mRoot.getText(R.string.strNoGames));
			tv_no_game.setTextColor(Color.WHITE);
			tv_no_game.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_game.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_game.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			base.addView(tv_no_game);
			return;
		}

		for (XPMBSubmenuItem_NES xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView cItem = new ImageView(base.getContext());
			TextView cLabel = new TextView(base.getContext());

			// Setup Icon
			cItem.setImageDrawable(xsi.getGameCover());
			cItem.setPivotX(0.0f);
			cItem.setPivotY(0.0f);
			if (idx == 0) {
				cItem.setScaleX(2.56f);
				cItem.setScaleY(2.56f);
				LayoutParams cItemParams = new LayoutParams(
						(int) pxFromDip(50), (int) pxFromDip(50),
						pxFromDip(48), pxFromDip(104));
				cItem.setLayoutParams(cItemParams);
			} else {
				LayoutParams cItemParams = new LayoutParams(
						(int) pxFromDip(50), (int) pxFromDip(50),
						pxFromDip(48), pxFromDip(248 + (50 * (idx - 1))));
				cItem.setLayoutParams(cItemParams);
			}
			cItem.setId(cId);
			++cId;
			// Setup Label
			cLabel.setText(xsi.getGameName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

			if (idx == 0) {
				LayoutParams cLabelParams = new LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(128),
						pxFromDip(184), pxFromDip(104));
				cLabel.setLayoutParams(cLabelParams);
			} else {
				LayoutParams cLabelParams = new LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(128),
						pxFromDip(184),
						pxFromDip((248 + (50 * (idx - 1)) - 39)));
				cLabel.setLayoutParams(cLabelParams);
				cLabel.setAlpha(0.0f);
			}
			cLabel.setId(cId);
			++cId;

			xsi.setParentView(cItem);
			xsi.setParentLabel(cLabel);
			base.addView(cItem);
			base.addView(cLabel);
		}
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
			mRoot.requestUnloadSubmenu();
			break;
		}
	}

	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBSubmenuItem_NES xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView iv_c_i = xsi.getParentView();
			TextView tv_c_l = xsi.getParentLabel();

			if (idx == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 1.0f, 0.0f));
			} else if (idx == (intSelItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(144))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 0.0f, 1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_mu = new AnimatorSet();
		ag_xmb_sm_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_mu.setDuration(150);
		mRoot.lockKeys(true);
		reloadGameBG();
		ag_xmb_sm_mu.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBSubmenuItem_NES xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView iv_c_i = xsi.getParentView();
			TextView tv_c_l = xsi.getParentLabel();

			if (idx == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(144))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 1.0f, 0.0f));
			} else if (idx == (intSelItem - 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 0.0f, 1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_md = new AnimatorSet();
		ag_xmb_sm_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_md.setDuration(150);
		mRoot.lockKeys(true);
		reloadGameBG();
		ag_xmb_sm_md.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	private void reloadGameBG() {
		ImageView iv_bg = mRoot.getCustomBGView();
		if (iv_bg.getDrawable() != null) {
			ObjectAnimator rbg_a_pre = ObjectAnimator.ofFloat(iv_bg, "Alpha",
					1.0f, 0.0f);
			rbg_a_pre.setDuration(200);
			rbg_a_pre.start();
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				ImageView iv_bg = mRoot.getCustomBGView();
				iv_bg.setAlpha(0.0f);
				iv_bg.setImageDrawable(alItems.get(intSelItem)
						.getGameBackground());
				if (iv_bg.getDrawable() == null) {
					return;
				}
				ObjectAnimator rbg_a_pos = ObjectAnimator.ofFloat(iv_bg,
						"Alpha", 0.0f, 1.0f);
				rbg_a_pos.setDuration(200);
				rbg_a_pos.start();
			}

		}, 200);
	}

	public void execSelectedItem() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setComponent(ComponentName
				.unflattenFromString("com.androidemu.nes/.EmulatorActivity"));
		intent.setData(Uri.fromFile(alItems.get(intSelItem).getROMPath()));
		intent.setFlags(0x10000000);
		mRoot.startActivity(intent);
	}

	@Override
	public void doCleanup(ViewGroup base) {
		if (alItems.size() == 0 && tv_no_game != null) {
			base.removeView(tv_no_game);
			return;
		}
		for (XPMBSubmenuItem_NES xig : alItems) {
			base.removeView(xig.getParentView());
			base.removeView(xig.getParentLabel());
		}
		ImageView iv_bg = mRoot.getCustomBGView();
		if (iv_bg.getDrawable() != null) {
			ObjectAnimator rbg_a_pre = ObjectAnimator.ofFloat(iv_bg, "Alpha",
					1.0f, 0.0f);
			rbg_a_pre.setDuration(200);
			rbg_a_pre.start();
			hMessageBus.postDelayed(new Runnable() {

				@Override
				public void run() {
					ImageView iv_bg = mRoot.getCustomBGView();
					iv_bg.setImageDrawable(null);
					iv_bg.setAlpha(1.0f);
				}

			}, 200);
		}
	}
}
