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

package com.raddstudios.xpmb.menus.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBMenu_View;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.ROMInfo;
import com.raddstudios.xpmb.utils.ROMInfo.ROMInfoNode;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Activity.ObjectCollections;
import com.raddstudios.xpmb.utils.XPMB_Layout;

public class Module_Emu_GBA extends XPMB_Layout implements Modules_Base, SurfaceHolder.Callback {

	private ObjectCollections mStor = null;
	private XPMBMenuCategory dest = null;
	private XPMBMenu_View container = null;
	private boolean bInit = false;
	private DrawThread mDrwTh = null;
	private FinishedListener flListener = null;
	private int intAnimator = 0, intLastItem = -1, intMaxItemsOnScreen = 1;
	private String strEmuAct = null;
	private ArrayList<String> alCoverKeys = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	private final String SETTING_LAST_ITEM = "emu.gba.lastitem",
			SETTING_EMU_ACT = "emu.gba.emuact";

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_MENU_CENTER_ON_ITEM = 2;

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		private int intAnimType = -1, intAnimItem = -1, intNextItem = -1;
		private int[] iaParams = null;
		private Point[] apInitValues = null;
		private ValueAnimator mOwner = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setParams(int[] params) {
			iaParams = params;
		}

		private void setInitialValues() {
			apInitValues = new Point[dest.getNumSubItems()];
			for (int i = 0; i < dest.getNumSubItems(); i++) {
				apInitValues[i] = dest.getSubitem(i).getPosition();
			}
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				break;
			case ANIM_MENU_CENTER_ON_ITEM:
				mOwner.setDuration(250);
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = iaParams[0];
				break;
			}
			setInitialValues();
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			int dispA = 0, marginA = 0;
			float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_MENU_CENTER_ON_ITEM:
				dispA = (int) (((intNextItem - intAnimItem) * -96) * completion);
				if (dispA > 0) {
					marginA = (int) (24 * completion);
				} else {
					marginA = (int) (-24 * completion);
				}
				alphaA = 1.0f - completion;
				alphaB = completion;
				alphaC = 1.0f - (0.5f * completion);
				alphaD = 0.5f + (0.5f * completion);

				for (int y = 0; y < dest.getNumSubItems(); y++) {
					XPMBMenuItemDef xmid = dest.getSubitem(y);

					if (y == intAnimItem || y == intNextItem) {
						xmid.setPositionY(apInitValues[y].y + dispA + marginA);
						if (y == intAnimItem) {
							xmid.setSeparatorAlpha(alphaA);
							xmid.setLabelAlpha(alphaC);
						} else {
							xmid.setSeparatorAlpha(alphaB);
							xmid.setLabelAlpha(alphaD);
						}
					} else {
						xmid.setPositionY(apInitValues[y].y + dispA);
					}
				}
				break;
			case ANIM_NONE:
			default:
				break;
			}
			// requestRedraw();
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
			apInitValues = null;
			iaParams = null;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			apInitValues = null;
			iaParams = null;
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}
	};

	private class DrawThread extends Thread {
		boolean mRun;
		Module_Emu_GBA mMenuView;

		public DrawThread(Context ctx, Module_Emu_GBA sView) {
			mRun = false;
			mMenuView = sView;
		}

		void setRunning(boolean bRun) {
			mRun = bRun;
		}

		@Override
		public void run() {
			super.run();

			while (mRun) {
				mMenuView.requestRedraw();
			}
		}
	}

	public Module_Emu_GBA(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView);
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		this.setZOrderOnTop(true);

		alCoverKeys = new ArrayList<String>();
		mStor = getRootActivity().getStorage();
		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
	}

	private static final String ASSET_GRAPH_GBA_CV_NOTFOUND = "theme.asset|gba_emu_cv_nf";

	public void initialize(XPMBMenu_View owner, XPMBMenuCategory root, FinishedListener finishedL) {
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		flListener = finishedL;
		container = owner;
		dest = root;
		reloadSettings();

		if (mStor.getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, ASSET_GRAPH_GBA_CV_NOTFOUND) == null) {
			Log.v(getClass().getSimpleName(), "initialize():Caching \"cover not found\" asset");
			mStor.putObject(
					XPMB_Main.GRAPH_ASSETS_COL_KEY,
					ASSET_GRAPH_GBA_CV_NOTFOUND,
					((BitmapDrawable) getRootView().getResources().getDrawable(
							R.drawable.ui_cover_not_found_gba)).getBitmap());
		}

		intMaxItemsOnScreen = (owner.getHeight() / 96) + 1;
		Log.v(getClass().getSimpleName(), "initialize():Max vertical items on screen: "
				+ intMaxItemsOnScreen);
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	private void reloadSettings() {
		intLastItem = (Integer) mStor.getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, -1);
		strEmuAct = (String) mStor.getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_EMU_ACT,
				"com.androidemu.gba/.EmulatorActivity");
		Log.d(getClass().getSimpleName(), "reloadSettings():<Selected Item>=" + intLastItem);
		Log.d(getClass().getSimpleName(), "reloadSettings():<GBA Emulator Activity>=" + strEmuAct);
	}

	@Override
	public void deInitialize() {
		mStor.putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, intLastItem);

		dest.clearSubitems();
		Log.v(getClass().getSimpleName(), "Removing " + alCoverKeys.size() + " cached cover assets");
		for (String ck : alCoverKeys) {
			mStor.removeObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, ck);
		}
		alCoverKeys.clear();
	}

	@Override
	public void loadIn() {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}
		long t = System.currentTimeMillis();
		File mROMRoot = new File(Environment.getExternalStorageDirectory().getPath() + "/GBA");
		ROMInfo ridROMInfoDat = null;
		int y = 0;

		mROMRoot.mkdirs();
		if (!mROMRoot.isDirectory()) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Couldn't create or access '" + mROMRoot.getAbsolutePath() + "'");
			return;
		}
		File mROMResDir = new File(mROMRoot, "Resources");
		if (!mROMResDir.exists()) {
			mROMResDir.mkdirs();
			if (!mROMResDir.isDirectory()) {
				Log.e(getClass().getSimpleName(), "loadIn():Couldn't create or access '"
						+ mROMResDir.getAbsolutePath() + "'");
				return;
			}
		}
		ridROMInfoDat = new ROMInfo(getRootActivity().getResources().getXml(R.xml.rominfo_gba),
				ROMInfo.TYPE_CRC);

		// TODO: prepare assets and animation for scaled (1.17x) effect

		try {
			File[] storPtCont = mROMRoot.listFiles();
			for (File f : storPtCont) {
				if (f.getName().endsWith(".zip")) {
					ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
					Enumeration<? extends ZipEntry> ze = zf.entries();
					while (ze.hasMoreElements()) {
						ZipEntry zef = ze.nextElement();
						if (zef.getName().endsWith(".gba") || zef.getName().endsWith(".GBA")) {
							long ct = System.currentTimeMillis();
							Log.v(getClass().getSimpleName(), "loadIn():Found compressed ROM '"
									+ zef.getName() + "' inside '" + f.getAbsolutePath() + "' ID #"
									+ y);
							// InputStream fi = null;
							// InputStream fi = zf.getInputStream(zef);
							// fi.skip(0xAC);
							// String gameCode = "";

							// gameCode += (char) fi.read();
							// gameCode += (char) fi.read();
							// gameCode += (char) fi.read();
							// gameCode += (char) fi.read();
							// fi.close();
							String gameCRC = Long.toHexString(zef.getCrc()).toUpperCase(
									getRootActivity().getResources().getConfiguration().locale);
							Log.d(getClass().getSimpleName(),
									"loadIn():CRC for ROM '" + zef.getName() + "' is 0x" + gameCRC);

							ROMInfoNode rinCData = ridROMInfoDat.getNode(gameCRC);
							XPMBMenuItem xmi = null;
							if (rinCData != null) {
								xmi = new XPMBMenuItem(rinCData.getGameName());
								Log.v(getClass().getSimpleName(),
										"loadIn():Data for ROM with CRC 0x" + gameCRC
												+ " found. ROM name: '" + rinCData.getGameName()
												+ "'");
							} else {
								xmi = new XPMBMenuItem(f.getName());
								Log.e(getClass().getSimpleName(),
										"loadIn():Data for ROM with CRC 0x" + gameCRC
												+ " not found. Using source filename instead.");
							}
							xmi.setLabelB(getRootActivity().getString(R.string.strEmuGBARom));
							xmi.enableTwoLine(true);

							// xmi.setIcon("theme.icon|icon_pspms");
							xmi.setIcon(ASSET_GRAPH_GBA_CV_NOTFOUND);
							xmi.setData(f);

							xmi.setPositionX(80);
							if (intLastItem != -1) {
								if (y < intLastItem) {
									xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y) - 24);
									xmi.setSeparatorAlpha(0.0f);
									xmi.setLabelAlpha(0.5f);
								} else if (y > intLastItem) {
									xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y) + 24);
									xmi.setSeparatorAlpha(0.0f);
									xmi.setLabelAlpha(0.5f);
								} else {
									xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y));
								}
							} else {
								xmi.setPositionY(208 + (96 * y));
								if (y > 0) {
									xmi.setPositionY(xmi.getPosition().y + 24);
									xmi.setSeparatorAlpha(0.0f);
									xmi.setLabelAlpha(0.5f);
								}
							}
							Log.v(getClass().getSimpleName(), "loadin():Item #" + y + " is at ["
									+ xmi.getPosition().x + "," + xmi.getPosition().y + "].");

							xmi.setWidth(128);
							xmi.setHeight(96);

							dest.addSubitem(xmi);
							y++;
							Log.i(getClass().getSimpleName(),
									"loadIn():Item loading completed for item #" + y
											+ ". Process took " + (System.currentTimeMillis() - ct)
											+ "ms.");
						}
					}
					zf.close();
				} else if (f.getName().endsWith(".gba") || f.getName().endsWith(".GBA")) {
					long ct = System.currentTimeMillis();
					Log.v(getClass().getSimpleName(),
							"loadIn():Found uncompressed ROM '" + f.getAbsolutePath() + "' ID #"
									+ y);
					InputStream fi = null;
					// InputStream fi = new FileInputStream(f);
					// fi.skip(0xAC); // TODO: Find a better way to associate
					// ROMS with DB titles
					// String gameCode = "";
					// gameCode += (char) fi.read();
					// gameCode += (char) fi.read();
					// gameCode += (char) fi.read();
					// gameCode += (char) fi.read();
					// fi.close();

					fi = new BufferedInputStream(new FileInputStream(f));
					CRC32 cCRC = new CRC32();
					int cByte = 0;
					long i = System.currentTimeMillis();
					byte[] buf = new byte[1024 * 64];
					while ((cByte = fi.read(buf)) > 0) {
						cCRC.update(buf, 0, cByte);
					}
					Log.i(getClass().getSimpleName(),
							"loadIn():CRC Calculation for '" + f.getName() + "' took "
									+ (System.currentTimeMillis() - i) + "ms.");
					fi.close();

					String gameCRC = Long.toHexString(cCRC.getValue()).toUpperCase(
							getRootActivity().getResources().getConfiguration().locale);
					Log.d(getClass().getSimpleName(), "loadIn():CRC for ROM '" + f.getName()
							+ "' is 0x" + gameCRC);

					ROMInfoNode rinCData = ridROMInfoDat.getNode(gameCRC);
					XPMBMenuItem xmi = null;
					if (rinCData != null) {
						xmi = new XPMBMenuItem(rinCData.getGameName());
						Log.d(getClass().getSimpleName(), "loadIn():Data for ROM with CRC 0x"
								+ gameCRC + " found. ROM name: '" + rinCData.getGameName() + "'");
					} else {
						xmi = new XPMBMenuItem(f.getName());
						Log.d(getClass().getSimpleName(), "loadIn():Data for ROM with CRC 0x"
								+ gameCRC + " not found. Using source filename instead.");
					}
					xmi.setLabelB(getRootActivity().getString(R.string.strEmuGBARom));
					xmi.enableTwoLine(true);

					// xmi.setIcon("theme.icon|icon_pspms");
					xmi.setIcon(ASSET_GRAPH_GBA_CV_NOTFOUND);
					xmi.setData(f);

					xmi.setPositionX(80);
					if (intLastItem != -1) {
						if (y < intLastItem) {
							xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y) - 24);
							xmi.setSeparatorAlpha(0.0f);
							xmi.setLabelAlpha(0.5f);
						} else if (y > intLastItem) {
							xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y) + 24);
							xmi.setSeparatorAlpha(0.0f);
							xmi.setLabelAlpha(0.5f);
						} else {
							xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y));
						}
					} else {
						xmi.setPositionY(208 + (96 * y));
						if (y > 0) {
							xmi.setPositionY(xmi.getPosition().y + 24);
							xmi.setSeparatorAlpha(0.0f);
							xmi.setLabelAlpha(0.5f);
						}
					}
					Log.v(getClass().getSimpleName(),
							"loadin():Item #" + y + " is at [" + xmi.getPosition().x + ","
									+ xmi.getPosition().y + "].");

					xmi.setWidth(128);
					xmi.setHeight(96);

					dest.addSubitem(xmi);
					y++;
					Log.i(getClass().getSimpleName(), "loadIn():Item loading completed for item #"
							+ y + ". Process took " + (System.currentTimeMillis() - ct) + "ms.");
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Error when loading info/reading ROM data due to an unhandled exception.");
			// TODO Handle errors when loading found ROMs
			e.printStackTrace();
		}
		Log.i(getClass().getSimpleName(), "loadIn():ROM list load finished. Process took: "
				+ (System.currentTimeMillis() - t) + "ms.");
	}

	FinishedListener flEmuEnd = new FinishedListener() {
		@Override
		public void onFinished(Intent intent) {
			Log.v(getClass().getSimpleName(),
					"onFinished():Emulator activity finished. Returning to XPMB...");
			getRootActivity().showLoadingAnim(false);
		}
	};

	@Override
	public void processItem(XPMBMenuItem item) {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. You shouldn't even be calling this method.");
			return;
		}
		final XPMBMenuItem f_item = item;
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d(getClass().getSimpleName(),
						"processItem():Trying to boot '" + f_item.getLabel()
								+ "' ROM file. Using activity '" + strEmuAct + "' as emulator.");
				Intent intent = new Intent("android.intent.action.VIEW");
				intent.setComponent(ComponentName.unflattenFromString(strEmuAct));
				intent.setData(Uri.fromFile((File) f_item.getData()));
				intent.setFlags(0x10000000);
				if (getRootActivity().isActivityAvailable(intent)) {
					Log.v(getClass().getSimpleName(),
							"processItem():Emulator activity found. Starting emulation...");
					getRootActivity().showLoadingAnim(true);
					getRootActivity().postIntentStartWait(flEmuEnd, intent);
				} else {
					Log.e(getClass().getSimpleName(),
							"processItem():Emulator activity not found. Giving up emulation.");
					Toast tst = Toast.makeText(
							getRootActivity().getWindow().getContext(),
							getRootActivity().getString(R.string.strAppNotInstalled).replace("%s",
									intent.getComponent().getPackageName()), Toast.LENGTH_SHORT);
					tst.show();
				}
			}
		}).run();
	}

	@Override
	public boolean isInitialized() {
		return bInit;
	}

	@Override
	public void processkeyUp(int keyCode) {
	}

	@Override
	public void processkeyDown(int keyCode) {

		switch (keyCode) {
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_LEFT:
		case XPMB_Main.KEYCODE_CIRCLE:
			flListener.onFinished(null);
			break;
		case XPMB_Main.KEYCODE_CROSS:
			processItem((XPMBMenuItem) dest.getSubitem(dest.getSelectedSubitem()));
			break;
		}
	}

	private boolean drawing = false;
	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect();
	private int px_i_l = 0, py_i_l = 0, textH = 0;

	@Override
	public void dispatchDraw(Canvas canvas) {
		processDraw(canvas);
	}

	public void requestRedraw() {
		if (drawing) {
			return;
		}
		Canvas mcanvas = getHolder().lockCanvas();

		if (mcanvas != null) {
			processDraw(mcanvas);
			getHolder().unlockCanvasAndPost(mcanvas);
		}
	}

	private Rect getAlignedAndScaledRect(int left, int top, int width, int height, float scaleX,
			float scaleY, int gravity) {
		int sizeX = (int) (width * scaleX);
		int sizeY = (int) (height * scaleY);
		Rect in = new Rect(left, top, left + width, top + height);
		Rect out = new Rect(0, 0, 0, 0);

		Gravity.apply(gravity, sizeX, sizeY, in, out);

		return out;
	}

	private void processDraw(Canvas canvas) {
		// TODO: Take in account the actual orientation and DPI of the device

		drawing = true;
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

		// Process subitems
		for (int y = (intLastItem - intMaxItemsOnScreen / 2); y < (intLastItem + intMaxItemsOnScreen); y++) {
			if (y < 0) {
				continue;
			} else if (y > dest.getNumSubItems() - 1) {
				break;
			}
			XPMBMenuItemDef xmi_y = dest.getSubitem(y);

			// Setup Icon
			String strIcon_i = xmi_y.getIcon();
			float alpha_i_i = (255 * xmi_y.getIconAlpha()) * dest.getSubitemsAlpha(), scale_x_i = xmi_y
					.getIconScale().x, scale_y_i = xmi_y.getIconScale().y;
			Rect rLoc = xmi_y.getComputedLocation();
			pParams.setAlpha((int) (alpha_i_i * container.getOpacity()));
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			// Draw Icon
			canvas.drawBitmap(
					(Bitmap) mStor.getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, strIcon_i, null),
					null,
					getAlignedAndScaledRect(rLoc.left, rLoc.top, rLoc.width(), rLoc.height(),
							scale_x_i, scale_y_i, Gravity.CENTER), pParams);
			pParams.reset();

			// Setup Label
			String strLabel_i = xmi_y.getLabel();
			String strLabel_i_b = xmi_y.getLabelB();
			float alpha_i_l = (255 * xmi_y.getLabelAlpha()) * dest.getSubitemsAlpha();
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			pParams.setTextSize(28);
			pParams.setColor(Color.WHITE);
			pParams.setAlpha((int) (alpha_i_l * container.getOpacity()));
			pParams.setTextAlign(Align.LEFT);
			pParams.setShadowLayer(4, 0, 0, Color.WHITE);
			pParams.getTextBounds(strLabel_i, 0, strLabel_i.length(), rTextBounds);
			// textW = rTextBounds.right - rTextBounds.left;
			textH = (int) (Math.abs(pParams.getFontMetrics().ascent) + Math.abs(pParams
					.getFontMetrics().bottom));
			// Draw Label
			px_i_l = rLoc.right + 16;

			if (!xmi_y.isTwoLines()) {
				py_i_l = (int) (rLoc.top + (xmi_y.getSize().y / 2) + (textH / 2) - pParams
						.getFontMetrics().descent);
				canvas.drawText(strLabel_i, px_i_l, py_i_l, pParams);
			} else {
				// Text A
				py_i_l = (int) (rLoc.top + (textH / 4) + textH - pParams.getFontMetrics().descent);
				canvas.drawText(strLabel_i, px_i_l, py_i_l, pParams);
				pParams.setAlpha((int) ((255 * xmi_y.getSeparatorAlpha()) * container.getOpacity()));
				// Line Separator
				py_i_l += (pParams.getFontMetrics().descent + 4);
				canvas.drawLine(px_i_l, py_i_l, canvas.getWidth() - 4, py_i_l, pParams);
				// Text B
				pParams.setAlpha((int) (alpha_i_l * container.getOpacity()));
				py_i_l = (int) (rLoc.bottom - (textH / 4) - (textH / 2) + pParams.getFontMetrics().descent);
				canvas.drawText(strLabel_i_b, px_i_l, py_i_l, pParams);
			}
			pParams.reset();
		}
		drawing = false;
	}

	public void moveUp() {
		if (dest.getSelectedSubitem() == 0 || dest.getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		dest.setSelectedSubItem(dest.getSelectedSubitem() - 1);
		intLastItem = dest.getSelectedSubitem();
	}

	public void moveDown() {
		if (dest.getSelectedSubitem() == dest.getNumSubItems() - 1 || dest.getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		dest.setSelectedSubItem(dest.getSelectedSubitem() + 1);
		intLastItem = dest.getSelectedSubitem();
	}

	public void centerOnItem(int index) {
		if (index < 0 || index >= dest.getNumSubItems()) {
			return;
		}

		aUIAnimatorW.setParams(new int[] { index });
		aUIAnimatorW.setAnimationType(ANIM_MENU_CENTER_ON_ITEM);
		aUIAnimator.start();

		dest.setSelectedSubItem(index);
		intLastItem = index;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mDrwTh = new DrawThread(getContext(), this);
		mDrwTh.setRunning(true);
		mDrwTh.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		mDrwTh.setRunning(false);
		boolean retry = true;

		while (retry) {
			try {
				mDrwTh.join();
				retry = false;
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void setListAnimator(int animator) {
		intAnimator = animator;
	}

	@Override
	public int getListAnimator() {
		return intAnimator;
	}
}
