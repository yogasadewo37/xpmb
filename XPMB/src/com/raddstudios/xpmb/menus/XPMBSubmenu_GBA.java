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

package com.raddstudios.xpmb.menus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.utils.ROMInfo;
import com.raddstudios.xpmb.menus.utils.ROMInfo.ROMInfoNode;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Layout;
import com.raddstudios.xpmb.utils.XPMB_Activity.IntentFinishedListener;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

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

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_ITEM = 2;

	private class UIAnimatorWorker implements AnimatorUpdateListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
		private int pY = 0, destY = 0;
		private ValueAnimator mOwner = null;
		private float[] mArgs = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setArguments(float[] arguments) {
			mArgs = arguments;
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;
			intAnimItem = intSelItem;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				intNextItem = intAnimItem - 1;
				destY = pxFromDip(88) - (pxFromDip(50) * intNextItem);
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				intNextItem = intAnimItem + 1;
				destY = pxFromDip(88) - (pxFromDip(50) * intNextItem);
				break;
			case ANIM_CENTER_ON_ITEM:
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				intNextItem = (int) mArgs[0];
				destY = pxFromDip(88) - (pxFromDip(50) * intNextItem);
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			float dispY = 0, alphaO = 0, alphaI = 0, scaleO = 0, scaleI = 0;
			int marginO = 0, marginI = 0;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_CENTER_ON_ITEM:
				dispY = destY - pY;
				scaleO = 2.56f - (1.56f * completion);
				scaleI = 1.0f + (1.56f * completion);
				alphaO = 1.0f - completion;
				alphaI = completion;
				marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setTopMargin((int) (pY + (dispY * completion)));
				alItems.get(intAnimItem).getParentView().setViewScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setViewScaleY(scaleO);
				alItems.get(intAnimItem).getParentLabel().setAlphaLevel(alphaO);
				alItems.get(intAnimItem).getParentContainer().setTopMargin(marginO);
				alItems.get(intAnimItem).getParentContainer().setBottomMargin(marginO);
				alItems.get(intNextItem).getParentView().setViewScaleX(scaleI);
				alItems.get(intNextItem).getParentView().setViewScaleY(scaleI);
				alItems.get(intNextItem).getParentLabel().setAlphaLevel(alphaI);
				alItems.get(intNextItem).getParentContainer().setTopMargin(marginI);
				alItems.get(intNextItem).getParentContainer().setBottomMargin(marginI);
				break;
			case ANIM_NONE:
			default:
				break;
			}
		}
	};

	private final int SCROLL_DIR_UP = 0, SCROLL_DIR_DOWN = 1;

	private class RapidScroller extends TimerTask {

		private int mDirection = 0;
		private boolean bEnabled = false;

		public void setScrollDirection(int direction) {
			mDirection = direction;
		}

		public void setEnabled(boolean enabled) {
			bEnabled = enabled;
		}

		public boolean isEnabled() {
			return bEnabled;
		}

		@Override
		public void run() {
			if (bEnabled) {
				switch (mDirection) {
				case SCROLL_DIR_UP:
					getMessageBus().post(new Runnable() {
						@Override
						public void run() {
							moveUp();
						}
					});
					break;
				case SCROLL_DIR_DOWN:
					getMessageBus().post(new Runnable() {
						@Override
						public void run() {
							moveDown();
						}
					});
					break;
				}
			}
		}
	};

	private ArrayList<XPMBSubmenuItem_GBA> alItems = null;
	private int intSelItem = 0;
	private boolean isFocused = true;
	private File mROMRoot = null;
	private ROMInfo ridROMInfoDat = null;
	private XPMB_TextView tv_no_game = null;
	private XPMB_TableLayout tlRoot = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;
	private RapidScroller ttFastScroll = null;

	public XPMBSubmenu_GBA(XPMB_Activity root, Handler messageBus, ViewGroup rootView, File fROMRoot) {
		super(root, messageBus, rootView);
		mROMRoot = fROMRoot;

		alItems = new ArrayList<XPMBSubmenuItem_GBA>();

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);

		ttFastScroll = new RapidScroller();
		new Timer().scheduleAtFixedRate(ttFastScroll, 0, 50);
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
					item.setGameCover(new BitmapDrawable(getRootActivity().getResources(),
							BitmapFactory.decodeStream(new FileInputStream(fExtRes))));
				} else {
					item.setGameCover(getRootActivity().getResources().getDrawable(
							getRootActivity().getResources().getIdentifier(
									"drawable/ui_cover_not_found_gba", null,
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

	private OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (isFocused){
			getRootActivity().setTouchedChildView(v);
			}
			return false;
		}
	};

	@Override
	public void sendClickEventToView(View v) {
		if (v.getTag() != null) {
			int tag = (Integer) v.getTag();

			if (tag != intSelItem) {
				doCenterOnItemPre();
				centerOnItem(tag);
			}
			execCustItem(tag);
		}
	}

	private void doCenterOnItemPre() {
		alItems.get(intSelItem).getParentContainer().setTopMargin(0);
		alItems.get(intSelItem).getParentContainer().setBottomMargin(0);
		alItems.get(intSelItem).getParentView().setViewScaleX(1.0f);
		alItems.get(intSelItem).getParentView().setViewScaleY(1.0f);
		alItems.get(intSelItem).getParentLabel().setAlphaLevel(0.0f);
	}

	private void centerOnNearestItem() {
		float cPosY = tlRoot.getTopMargin();
		int destItem = ((int) (pxFromDip(122) - cPosY) / pxFromDip(60)) + 1;
		if (destItem < 0) {
			destItem = 0;
		} else if (destItem > (alItems.size() - 1)) {
			destItem = (alItems.size() - 1);
		}
		centerOnItem(destItem);

	}

	private void centerOnItem(int index) {
		aUIAnimatorW.setArguments(new float[] { index });
		aUIAnimatorW.setAnimationType(ANIM_CENTER_ON_ITEM);
		aUIAnimator.start();
		intSelItem = index;
	}

	public void parseInitLayout() {

		if (alItems.size() == 0) {
			tv_no_game = new XPMB_TextView(getRootView().getContext());
			RelativeLayout.LayoutParams lp_ng = new RelativeLayout.LayoutParams(pxFromDip(320),
					pxFromDip(100));
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp_ng.leftMargin = pxFromDip(48);
			lp_ng.topMargin = pxFromDip(128);
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
		RelativeLayout.LayoutParams rootP = new RelativeLayout.LayoutParams(pxFromDip(464),
				pxFromDip(160 + (60 * alItems.size())));
		rootP.leftMargin = pxFromDip(48);
		rootP.topMargin = pxFromDip(88);
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_GBA xsi : alItems) {

			int idy = alItems.indexOf(xsi);

			XPMB_TableRow cItem = new XPMB_TableRow(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cItem.setId(getNextID());

			// Setup Container
			TableLayout.LayoutParams cItemP = new TableLayout.LayoutParams(pxFromDip(386),
					TableLayout.LayoutParams.WRAP_CONTENT);
			if (idy == 0) {
				cItemP.topMargin = pxFromDip(16);
				cItemP.bottomMargin = pxFromDip(16);
			}
			cItem.setLayoutParams(cItemP);

			// Setup Icon
			TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(50),
					(int) pxFromDip(50));
			cIconParams.column = 0;
			cIcon.setLayoutParams(cIconParams);
			cIcon.resetScaleBase();
			if (idy == 0) {
				cIcon.setViewScaleX(2.56f);
				cIcon.setViewScaleY(2.56f);
			}

			cIcon.setImageDrawable(xsi.getGameCover());
			cIcon.setTag(idy);
			cIcon.setOnTouchListener(mTouchListener);

			// Setup Label
			TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(50));
			cLabelParams.column = 1;
			cLabelParams.leftMargin = pxFromDip(16);
			cLabelParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			cLabel.setLayoutParams(cLabelParams);

			if (idy != 0) {
				cLabel.setAlphaLevel(0.0f);
			}
			cLabel.setText(xsi.getGameName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cLabel.setTag(idy);
			cLabel.setOnTouchListener(mTouchListener);

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
		reloadGameBG(0);
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_DOWN:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveDown();
			}
			break;
		case XPMB_Main.KEYCODE_UP:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveUp();
			}
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

	@Override
	public void sendKeyHold(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_UP:
			ttFastScroll.setScrollDirection(SCROLL_DIR_UP);
			ttFastScroll.setEnabled(true);
			break;
		case XPMB_Main.KEYCODE_DOWN:
			ttFastScroll.setScrollDirection(SCROLL_DIR_DOWN);
			ttFastScroll.setEnabled(true);
			break;
		}
	}

	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		++intSelItem;
	}

	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		--intSelItem;
	}

	private void reloadGameBG_Pre() {
		ValueAnimator va_bgr_pr = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_bgr_pr.setInterpolator(new DecelerateInterpolator());
		va_bgr_pr.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float alphaI = 1.0f - completion;
				getRootActivity().getCustomBGView().setAlphaLevel(alphaI);

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
		ValueAnimator va_bgr_po = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_bgr_po.setInterpolator(new DecelerateInterpolator());
		va_bgr_po.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float alphaI = completion;
				getRootActivity().getCustomBGView().setAlphaLevel(alphaI);
			}
		});
		va_bgr_po.setDuration(200);
		va_bgr_po.start();
	}

	private void reloadGameBG(final int index) {
		if (getRootActivity().getCustomBGView().getDrawable() != null) {
			reloadGameBG_Pre();
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				getRootActivity().getCustomBGView().setImageDrawable(
						alItems.get(index).getGameBackground());
				reloadGameBG_Pos();
			}

		}, 201);
	}

	public void execCustItem(int index) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setComponent(ComponentName
				.unflattenFromString("com.androidemu.gba/.EmulatorActivity"));
		intent.setData(Uri.fromFile(alItems.get(index).getROMPath()));
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

	public void execSelectedItem() {
		execCustItem(intSelItem);
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
			reloadGameBG_Pre();
		}
	}
}
