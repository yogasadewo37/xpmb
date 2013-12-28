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

package com.raddstudios.xpmb.menus.modules.games;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.modules.games.romdata.XPMB_ROMData;
import com.raddstudios.xpmb.menus.modules.games.romdata.rcdat.XPMB_RCDatParser;
import com.raddstudios.xpmb.menus.modules.games.romdata.tgdbapi.TGDB_GameData;
import com.raddstudios.xpmb.menus.modules.games.romdata.tgdbapi.TGDB_GameData.TGDB_GameData_ImageEntry;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemROM;
import com.raddstudios.xpmb.utils.XPMBSettingsManager;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V2;

public class Module_Emu_GBA extends Modules_Base implements FinishedListener {

	private boolean bInit = false, bLoaded = false;
	private String strEmuAct = null;
	private ArrayList<String> alCoverKeys = null;
	private ProcessItemThread rProcessItem = null;

	private final String SETTINGS_BUNDLE_KEY = "module.emu.gba", SETTING_LAST_ITEM = "lastitem",
			SETTING_EMU_ACT = "emuact",
			ASSET_GRAPH_GBA_CV_NOTFOUND = "theme.icon|ui_cover_not_found_gba";

	@Override
	public String getModuleID() {
		return "com.xpmb.emu.gba";
	}

	private class BackgroundFadeAnimator extends ValueAnimator implements AnimatorUpdateListener,
			AnimatorListener {

		boolean fi = false;

		public BackgroundFadeAnimator(boolean fadein) {
			super();
			super.setFloatValues(0.0f, 1.0f);
			super.setInterpolator(new AccelerateDecelerateInterpolator());
			super.addUpdateListener(this);
			fi = fadein;
			super.setDuration(1000);
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			if (fi) {
				if (getRootActivity().getDrawingLayerManager().isBackgroundBitmapSet()) {
					getRootActivity().getDrawingLayerManager().setBackgroundOpacity(completion);
				}
			} else {
				getRootActivity().getDrawingLayerManager().setBackgroundOpacity(1.0f - completion);
			}
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
			if (fi) {
				getRootActivity().getDrawingLayerManager().setBackgroundBitmap(
						getRootActivity().getThemeManager()
								.getAsset(
										((XPMBMenuItemROM) getContainerCategory().getSubitem(
												getContainerCategory().getSelectedSubitem()))
												.getROMBGKey()));
			}
		}
	}

	private Runnable LoadImagesThread = new Runnable() {

		@Override
		public void run() {
			Iterator<XPMBMenuItemDef> it = getContainerCategory().getSubitems();

			while (it.hasNext()) {
				getRootActivity().setLoading(true);
				XPMBMenuItemROM xmir = (XPMBMenuItemROM) it.next();

				if (xmir.getROMInfo() != null) {
					TGDB_GameData gd = xmir.getROMInfo().getExtendedData();
					String boxFront = null, artwork = null;
					boolean boxSet = false, artSet = false;
					if (gd != null) {
						Iterator<TGDB_GameData_ImageEntry> gd_ie = gd.getImages();
						while (gd_ie.hasNext()) {
							TGDB_GameData_ImageEntry ie = gd_ie.next();
							if (ie.getType() == TGDB_GameData_ImageEntry.IMG_ENTRY_TYPE_BOXART
									&& ie.getBoxSide() == TGDB_GameData_ImageEntry.IMG_ENTRY_BOX_SIDE_FRONT
									&& !boxSet) {
								boxFront = ie.getOriginalImageURL().getFile();
								boxSet = true;
							}
							if (ie.getType() == TGDB_GameData_ImageEntry.IMG_ENTRY_TYPE_FANART
									&& !artSet) {
								artwork = ie.getOriginalImageURL().getFile();
								artSet = true;
							}
						}
					}

					if (boxFront == null && artwork == null) {
						continue;
					}

					try {
						File resFile = new File(new File(xmir.getROMPath(), "Resources"), Long
								.toHexString(xmir.getROMCRC()).toUpperCase(Locale.ROOT) + ".zip");
						ZipFile zf = new ZipFile(resFile);
						ZipEntry bf = null, aw = null;
						if (boxFront != null) {
							bf = zf.getEntry(boxFront.substring(1));
						}
						if (artwork != null) {
							aw = zf.getEntry(artwork.substring(1));
						}
						if (bf != null) {
							getRootActivity().getThemeManager().addCustomAsset(
									Long.toHexString(xmir.getROMCRC()).toUpperCase(Locale.ROOT)
											+ boxFront,
									BitmapFactory.decodeStream(zf.getInputStream(bf)));
							alCoverKeys.add(Long.toHexString(xmir.getROMCRC()).toUpperCase(
									Locale.ROOT)
									+ boxFront);
							xmir.setIconBitmapID(Long.toHexString(xmir.getROMCRC()).toUpperCase(
									Locale.ROOT)
									+ boxFront);
							xmir.setWidth(pxfd(64));
						}
						if (aw != null) {
							getRootActivity().getThemeManager().addCustomAsset(
									Long.toHexString(xmir.getROMCRC()).toUpperCase(Locale.ROOT)
											+ artwork,
									BitmapFactory.decodeStream(zf.getInputStream(aw)));
							alCoverKeys.add(Long.toHexString(xmir.getROMCRC()).toUpperCase(
									Locale.ROOT)
									+ artwork);
							xmir.setROMBGKey(Long.toHexString(xmir.getROMCRC()).toUpperCase(
									Locale.ROOT)
									+ artwork);
						}
						zf.close();
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(),
								"run()[in-loop]:Error while loading assets for ROM '"
										+ xmir.getLabel() + "'");
						e.printStackTrace();
					}
				}
			}
			getMessageBus().post(new Runnable() {
				@Override
				public void run() {
					startBGFadeAnim();
				}
			});
			getRootActivity().setLoading(false);
		}

	};

	private class ProcessItemThread implements Runnable {

		private XPMBMenuItemROM f_item = null;
		private FinishedListener mOwner = null;

		public ProcessItemThread(FinishedListener owner) {
			mOwner = owner;
		}

		public void setItem(XPMBMenuItemROM item) {
			f_item = item;
		}

		@Override
		public void run() {
			Log.d(getClass().getSimpleName(), "processItem():Trying to boot '" + f_item.getLabel()
					+ "' ROM file. Using activity '" + strEmuAct + "' as emulator.");
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.setComponent(ComponentName.unflattenFromString(strEmuAct));
			intent.setData(Uri.fromFile(f_item.getROMPath()));
			intent.setFlags(0x10000000);
			if (getRootActivity().isActivityAvailable(intent)) {
				Log.v(getClass().getSimpleName(),
						"processItem():Emulator activity found. Starting emulation...");
				((XPMBUIModule) getRootActivity().getDrawingLayerManager().getLayer(0))
						.setLoadingAnimationVisible(true);
				getRootActivity().postIntentStartWait(mOwner, intent);
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
	}

	public Module_Emu_GBA(XPMBActivity root) {
		super(root);

		alCoverKeys = new ArrayList<String>();
		rProcessItem = new ProcessItemThread(this);
	}

	private void startBGFadeAnim() {
		if (getContainerCategory().getNumSubitems() == 0) {
			return;
		}
		if (getRootActivity().getDrawingLayerManager().isBackgroundBitmapSet()) {
			BackgroundFadeAnimator va_o = new BackgroundFadeAnimator(false), va_i = new BackgroundFadeAnimator(
					true);
			va_i.setStartDelay(1001);
			va_o.start();
			va_i.start();
		} else {
			new BackgroundFadeAnimator(true).start();
		}
	}

	@Override
	public void initialize(UILayer parentLayer, FinishedListener listener) {
		super.initialize(parentLayer, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		super.setListAnimator(new SubmenuAnimator_V2(super.getContainerCategory(), this));
		reloadSettings();
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	@Override
	protected void reloadSettings() {
		Bundle settings = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		getContainerCategory().setSelectedSubitem(settings.getInt(SETTING_LAST_ITEM, -1));
		strEmuAct = settings.getString(SETTING_EMU_ACT);
		if (strEmuAct == null) {
			strEmuAct = "com.androidemu.gba/.EmulatorActivity";
		}
		Log.d(getClass().getSimpleName(), "reloadSettings():<Selected Item>="
				+ getContainerCategory().getSelectedSubitem());
		Log.d(getClass().getSimpleName(), "reloadSettings():<GBA Emulator Activity>=" + strEmuAct);
	}

	@Override
	public void dispose() {
		Bundle saveData = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		saveData.putInt(SETTING_LAST_ITEM, getContainerCategory().getSelectedSubitem());
		saveData.putString(SETTING_EMU_ACT, strEmuAct);
		for (String k : alCoverKeys) {
			getRootActivity().getThemeManager().removeCustomAsset(k);
		}

		if (bLoaded) {
			Log.v(getClass().getSimpleName(), "dispose():Started saving module state.");
			File fMenuState = new File(getRootActivity().getCacheDir(), getModuleID() + ".state");
			try {
				FileOutputStream fos = new FileOutputStream(fMenuState);
				DataOutputStream dos = new DataOutputStream(fos);

				XPMBSettingsManager.writeBundleTo(getContainerCategory().storeInBundle(), dos);

				dos.close();

				Log.v(getClass().getSimpleName(), "dispose():Finished saving module state.");
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "dispose():Couldn't save module state");
			}
		}
		getContainerCategory().clearSubitems();
	}

	@Override
	public void loadIn() {
		super.loadIn();
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}

		if (bInit && !bLoaded) {
			File fMenuState = new File(getRootActivity().getCacheDir(), getModuleID() + ".state");
			if (fMenuState.exists()) {
				try {
					FileInputStream fis = new FileInputStream(fMenuState);
					DataInputStream dis = new DataInputStream(fis);

					long t = System.currentTimeMillis();
					Log.i(getClass().getSimpleName(),
							"doInit():Started reading module state cache.");
					super.setContainerCategory(new XPMBMenuCategory(XPMBSettingsManager
							.readBundleFrom(dis)));
					Log.i(getClass().getSimpleName(),
							"doInit():Finished reading module state cache. Took "
									+ (System.currentTimeMillis() - t) + "ms.");
					dis.close();

					for (int id = 0; id < getContainerCategory().getNumSubitems(); id++) {
						XPMBMenuItemROM xmir = (XPMBMenuItemROM) getContainerCategory().getSubitem(
								id);
						xmir.setIconBitmapID(ASSET_GRAPH_GBA_CV_NOTFOUND);
					}
					new Thread(LoadImagesThread).start();

					bLoaded = true;
					Log.v(getClass().getSimpleName(), "doInit():Finished module initialization.");
					return;
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "doInit():Couldn't read module state");
				}
			}
		}

		if (bLoaded) {
			Log.i(getClass().getSimpleName(), "loadIn():Module already loaded. Skipping process.");
			getMessageBus().post(new Runnable() {
				@Override
				public void run() {
					startBGFadeAnim();
				}
			});
			return;
		}

		long t = System.currentTimeMillis();
		File mROMRoot = new File(Environment.getExternalStorageDirectory().getPath() + "/GBA");
		XPMB_RCDatParser ridROMInfoDat = null;
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
		ridROMInfoDat = new XPMB_RCDatParser(getRootActivity().getResources().getXml(
				R.xml.rominfo_gba));

		try {
			File[] storPtCont = mROMRoot.listFiles();
			for (File f : storPtCont) {
				if (f.getName().endsWith(".zip")) {
					ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
					ZipEntry ze = zis.getNextEntry();
					while (ze != null) {
						if (ze.getName().toLowerCase(Locale.ROOT).endsWith(".gba")) {
							Log.v(getClass().getSimpleName(), "loadIn():Found compressed ROM '"
									+ ze.getName() + "' inside '" + f.getAbsolutePath() + "' ID #"
									+ y);

							XPMBMenuItemROM xmi = new XPMBMenuItemROM(new XPMB_ROMData(f, ze,
									ridROMInfoDat));
							xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
							xmi.setIconBitmapID(ASSET_GRAPH_GBA_CV_NOTFOUND);
							xmi.setWidth(pxfd(85));
							xmi.setHeight(pxfd(64));

							getContainerCategory().addSubitem(xmi);
							y++;
							break;
						}
						ze = zis.getNextEntry();
					}
					zis.close();
				} else if (f.getName().toLowerCase(Locale.ROOT).endsWith(".gba")) {
					long ct = System.currentTimeMillis();
					Log.v(getClass().getSimpleName(),
							"loadIn():Found uncompressed ROM '" + f.getAbsolutePath() + "' ID #"
									+ y);

					XPMBMenuItemROM xmi = new XPMBMenuItemROM(new XPMB_ROMData(f, ridROMInfoDat));
					xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
					xmi.setIconBitmapID(ASSET_GRAPH_GBA_CV_NOTFOUND);
					xmi.setWidth(pxfd(85));
					xmi.setHeight(pxfd(64));

					getContainerCategory().addSubitem(xmi);
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
		getListAnimator().initializeItems();
		new Thread(LoadImagesThread).start();
		bLoaded = true;
		Log.i(getClass().getSimpleName(), "loadIn():ROM list load finished. Process took: "
				+ (System.currentTimeMillis() - t) + "ms.");
	}

	@Override
	public void onFinished(Object data) {
		Log.v(getClass().getSimpleName(),
				"onFinished():Emulator activity finished. Returning to XPMB...");
		((XPMBUIModule) getRootActivity().getDrawingLayerManager().getLayer(0))
				.setLoadingAnimationVisible(false);
	}

	@Override
	public void processItem(XPMBMenuItemDef item) {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. You shouldn't even be calling this method.");
			return;
		}
		rProcessItem.setItem((XPMBMenuItemROM) item);
		new Thread(rProcessItem).run();
	}

	@Override
	public boolean isInitialized() {
		return bInit;
	}

	@Override
	public void sendKeyDown(int keyCode) {

		switch (keyCode) {
		case XPMBActivity.KEYCODE_UP:
			moveUp();
			break;
		case XPMBActivity.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMBActivity.KEYCODE_LEFT:
		case XPMBActivity.KEYCODE_CIRCLE:
			getFinishedListener().onFinished(this);
			getRootActivity().getDrawingLayerManager().setBackgroundOpacity(0.0f);
			getRootActivity().getDrawingLayerManager().setBackgroundBitmap(null);
			break;
		case XPMBActivity.KEYCODE_CROSS:
			processItem(getContainerCategory().getSubitem(
					getContainerCategory().getSelectedSubitem()));
			break;
		}
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMBActivity.KEYCODE_DOWN:
		case XPMBActivity.KEYCODE_UP:
			startBGFadeAnim();
			break;
		}
	}
}
