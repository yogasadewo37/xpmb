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
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.ROMInfo.ROMInfoNode;
import com.raddstudios.xpmb.utils.XPMB_Activity.IntentFinishedListener;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class XPMBSubmenu_NES extends XPMB_Layout {

	class XPMBSubmenuItem_NES {

		private Drawable bmGameCover = null, bmGameBackground = null;
		private File fROMPath = null;
		private String strGameName = null, strGameCRC = null, strGameDescription = null,
				strGameRegions = null, strGameLanguages;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private XPMB_TableRow trParentContainer = null;

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

	private ArrayList<XPMBSubmenuItem_NES> alItems = null;
	private int intSelItem = 0;
	private File mROMRoot = null;
	private ROMInfo ridROMInfoDat = null;
	private XPMB_TextView tv_no_game = null;
	private XPMB_TableLayout tlRoot = null;

	public XPMBSubmenu_NES(XPMB_Activity root, Handler messageBus, ViewGroup rootView, File fROMRoot) {
		super(root, messageBus, rootView);
		mROMRoot = fROMRoot;

		alItems = new ArrayList<XPMBSubmenuItem_NES>();
	}

	public void doInit() {
		mROMRoot.mkdirs();
		if (!mROMRoot.isDirectory()) {
			System.err.println("XPMBSubmenu_NES::doInit() : can't create or access "
					+ mROMRoot.getAbsolutePath());
			return;
		}
		File mROMResDir = new File(mROMRoot, "Resources");
		if (!mROMResDir.exists()) {
			mROMResDir.mkdirs();
			if (!mROMResDir.isDirectory()) {
				System.err.println("XPMBSubmenu_NES::doInit() : can't create or access "
						+ mROMResDir.getAbsolutePath());
				return;
			}
		}

		ridROMInfoDat = new ROMInfo(getRootActivity().getResources().getXml(R.xml.rominfo_nes),
				ROMInfo.TYPE_CRC);

		try {
			File[] storPtCont = mROMRoot.listFiles();
			for (File f : storPtCont) {
				if (f.getName().endsWith(".zip")) {
					ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
					Enumeration<? extends ZipEntry> ze = zf.entries();
					while (ze.hasMoreElements()) {
						ZipEntry zef = ze.nextElement();
						if (zef.getName().endsWith(".nes") || zef.getName().endsWith(".NES")) {
							String gameCRC = Long.toHexString(zef.getCrc()).toUpperCase(
									getRootActivity().getResources().getConfiguration().locale);
							XPMBSubmenuItem_NES cItem = new XPMBSubmenuItem_NES(f, gameCRC);
							loadAssociatedMetadata(cItem);
							alItems.add(cItem);
							break;
						}
					}
					zf.close();
				} else if (f.getName().endsWith(".nes") || f.getName().endsWith(".NES")) {
					CRC32 cCRC = new CRC32();
					InputStream fi = new FileInputStream(f);
					int cByte = 0;
					while ((cByte = fi.read()) != -1) {
						cCRC.update(cByte);
					}
					fi.close();
					String gameCRC = Long.toHexString(cCRC.getValue()).toUpperCase(
							getRootActivity().getResources().getConfiguration().locale);

					XPMBSubmenuItem_NES cItem = new XPMBSubmenuItem_NES(f, gameCRC);
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
						item.setGameName(romName.substring(0, romName.indexOf(".nes")));
					}
				} else {
					item.setGameName(rinCData.getReleaseData(0).getReleaseName());
				}
			}

			File resStor = new File(mROMRoot, "Resources");
			if (resStor.exists()) {
				File fExtRes = new File(resStor, item.getGameName() + "-CV.jpg");
				if (fExtRes.exists()) {
					item.setGameCover(new BitmapDrawable(getRootActivity().getResources(),
							BitmapFactory.decodeStream(new FileInputStream(fExtRes))));
				} else {
					item.setGameCover(getRootActivity().getResources().getDrawable(
							getRootActivity().getResources().getIdentifier(
									"drawable/ui_cover_not_found_nes", null,
									getRootActivity().getPackageName())));
				}
				fExtRes = new File(resStor, item.getGameName() + "-BG.jpg");
				if (fExtRes.exists()) {
					item.setGameBackground(new BitmapDrawable(getRootActivity().getResources(),
							BitmapFactory.decodeStream(new FileInputStream(fExtRes))));
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
			tv_no_game.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_game.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			getRootView().addView(tv_no_game);
			return;
		}

		tlRoot = new XPMB_TableLayout(getRootView().getContext());
		AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(pxFromDip(396),
				pxFromDip(160 + (60 * alItems.size())), pxFromDip(48), pxFromDip(88));
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_NES xsi : alItems) {

			int idx = alItems.indexOf(xsi);

			XPMB_TableRow cItem = new XPMB_TableRow(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cItem.setId(getNextID());

			// Setup Container
			TableLayout.LayoutParams cItemP = new TableLayout.LayoutParams(pxFromDip(464),
					TableLayout.LayoutParams.WRAP_CONTENT);
			if (idx == 0) {
				cItemP.topMargin = pxFromDip(16);
				cItemP.bottomMargin = pxFromDip(16);
			}
			cItem.setLayoutParams(cItemP);

			// Setup Icon
			TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(128),
					(int) pxFromDip(128));
			cIconParams.column = 0;
			cIcon.setLayoutParams(cIconParams);

			cIcon.setImageDrawable(xsi.getGameCover());

			// Setup Label
			TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(128));
			cLabelParams.column = 1;
			cLabelParams.leftMargin = pxFromDip(16);
			cLabel.setLayoutParams(cLabelParams);

			if (idx != 0) {
				cLabel.setAlpha(0.0f);
			}
			cLabel.setText(xsi.getGameName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

			// Add everything to their parent containers and holders
			cItem.addView(cIcon);
			cItem.addView(cLabel);
			xsi.setParentView(cIcon);
			xsi.setParentLabel(cLabel);
			xsi.setParentContainer(cItem);
			tlRoot.addView(cItem);
		}
		// Prevent Image scale changes to distort layout during animations
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

		final float pY = tlRoot.getY();
		final int intAnimItem = intSelItem;

		ValueAnimator va_mu = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_mu.setInterpolator(new DecelerateInterpolator());
		va_mu.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float posY = pY - (pxFromDip(50) * completion);
				float scaleO = 2.56f - (1.56f * completion);
				float scaleI = 1.0f + (1.56f * completion);
				float alphaO = 1.0f - completion;
				float alphaI = completion;
				int marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				int marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setY(posY);
				alItems.get(intAnimItem).getParentView().setScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setScaleY(scaleO);
				alItems.get(intAnimItem).getParentLabel().setAlpha(alphaO);
				alItems.get(intAnimItem).getParentContainer().setTopMargin(marginO);
				alItems.get(intAnimItem).getParentContainer().setBottomMargin(marginO);
				alItems.get(intAnimItem + 1).getParentView().setScaleX(scaleI);
				alItems.get(intAnimItem + 1).getParentView().setScaleY(scaleI);
				alItems.get(intAnimItem + 1).getParentLabel().setAlpha(alphaI);
				alItems.get(intAnimItem + 1).getParentContainer().setTopMargin(marginI);
				alItems.get(intAnimItem + 1).getParentContainer().setBottomMargin(marginI);
			}
		});

		va_mu.setDuration(150);
		getRootActivity().lockKeys(true);
		reloadGameBG();
		va_mu.start();

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

		final float pY = tlRoot.getY();
		final int intAnimItem = intSelItem;

		ValueAnimator va_mu = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_mu.setInterpolator(new DecelerateInterpolator());
		va_mu.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float posY = pY + (pxFromDip(50) * completion);
				float scaleO = 2.56f - (1.56f * completion);
				float scaleI = 1.0f + (1.56f * completion);
				float alphaO = 1.0f - completion;
				float alphaI = completion;
				int marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				int marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setY(posY);
				alItems.get(intAnimItem).getParentView().setScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setScaleY(scaleO);
				alItems.get(intAnimItem).getParentLabel().setAlpha(alphaO);
				alItems.get(intAnimItem).getParentContainer().setTopMargin(marginO);
				alItems.get(intAnimItem).getParentContainer().setBottomMargin(marginO);
				alItems.get(intAnimItem - 1).getParentView().setScaleX(scaleI);
				alItems.get(intAnimItem - 1).getParentView().setScaleY(scaleI);
				alItems.get(intAnimItem - 1).getParentLabel().setAlpha(alphaI);
				alItems.get(intAnimItem - 1).getParentContainer().setTopMargin(marginI);
				alItems.get(intAnimItem - 1).getParentContainer().setBottomMargin(marginI);
			}
		});

		va_mu.setDuration(150);
		getRootActivity().lockKeys(true);
		reloadGameBG();
		va_mu.start();

		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	private void reloadGameBG_Pre() {
		ValueAnimator va_bgr_pr = ValueAnimator.ofFloat(1.0f, 0.0f);
		va_bgr_pr.setInterpolator(new DecelerateInterpolator());
		va_bgr_pr.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float alphaI = 1.0f - completion;
				getRootActivity().getCustomBGView().setAlpha(alphaI);

				if (completion == 1.0f) {
					getRootActivity().getCustomBGView().setVisibility(View.INVISIBLE);
				}
			}
		});
		va_bgr_pr.setDuration(200);
		va_bgr_pr.start();
	}

	private void reloadGameBG_Pos() {
		getRootActivity().getCustomBGView().setVisibility(View.VISIBLE);
		ValueAnimator va_bgr_po = ValueAnimator.ofFloat(1.0f, 0.0f);
		va_bgr_po.setInterpolator(new DecelerateInterpolator());
		va_bgr_po.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float alphaI = completion;
				getRootActivity().getCustomBGView().setAlpha(alphaI);
			}
		});
		va_bgr_po.setDuration(200);
		va_bgr_po.start();
	}

	private void reloadGameBG() {
		if (getRootActivity().getCustomBGView().getDrawable() != null) {
			reloadGameBG_Pre();
		}
		getMessageBus().postDelayed(new Runnable() {
			@Override
			public void run() {
				getRootActivity().getCustomBGView().setImageDrawable(
						alItems.get(intSelItem).getGameBackground());
				reloadGameBG_Pos();
			}

		}, 201);
	}

	public void execSelectedItem() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setComponent(ComponentName
				.unflattenFromString("com.androidemu.nes/.EmulatorActivity"));
		intent.setData(Uri.fromFile(alItems.get(intSelItem).getROMPath()));
		intent.setFlags(0x10000000);
		if (getRootActivity().isActivityAvailable(intent)) {
			getRootActivity().showLoadingAnim(true);
			getRootActivity().postIntentStartWait(new IntentFinishedListener() {
				@Override
				public void onFinished(Intent intent) {
					getRootActivity().showLoadingAnim(false);
				}
			}, intent);
		} else {
			Toast tst = Toast.makeText(
					getRootActivity().getWindow().getContext(),
					getRootActivity().getString(R.string.strAppNotInstalled).replace("%s",
							intent.getComponent().getPackageName()), Toast.LENGTH_SHORT);
			tst.show();
		}
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
		if (getRootActivity().getCustomBGView().getDrawable() != null) {
			reloadGameBG_Pos();
		}
	}
}
