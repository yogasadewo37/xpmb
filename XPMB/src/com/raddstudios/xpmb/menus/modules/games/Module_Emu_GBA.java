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
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.modules.games.ROMInfo.ROMInfoNode;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V2;

public class Module_Emu_GBA extends Modules_Base implements FinishedListener {

	private boolean bInit = false;
	private String strEmuAct = null;
	private ArrayList<String> alCoverKeys = null;
	private ProcessItemThread rProcessItem = null;

	private final String SETTING_LAST_ITEM = "emu.gba.lastitem",
			SETTING_EMU_ACT = "emu.gba.emuact";

	private class ProcessItemThread implements Runnable {

		private XPMBMenuItemDef f_item = null;
		private FinishedListener mOwner = null;

		public ProcessItemThread(FinishedListener owner) {
			mOwner = owner;
		}

		public void setItem(XPMBMenuItemDef item) {
			f_item = item;
		}

		@Override
		public void run() {
			Log.d(getClass().getSimpleName(), "processItem():Trying to boot '" + f_item.getLabel()
					+ "' ROM file. Using activity '" + strEmuAct + "' as emulator.");
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.setComponent(ComponentName.unflattenFromString(strEmuAct));
			intent.setData(Uri.fromFile((File) f_item.getData()));
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

	public Module_Emu_GBA(XPMB_Activity root) {
		super(root);

		alCoverKeys = new ArrayList<String>();
		rProcessItem = new ProcessItemThread(this);
	}

	private static final String ASSET_GRAPH_GBA_CV_NOTFOUND = "theme.icon|ui_cover_not_found_gba";

	@Override
	public void initialize(UILayer parentLayer, XPMBMenuCategory container,
			FinishedListener listener) {
		super.initialize(parentLayer, container, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		reloadSettings();
		super.setListAnimator(new SubmenuAnimator_V2(container, this));
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	private void reloadSettings() {
		getContainerCategory()
				.setSelectedSubitem(
						(Integer) getStorage().getObject(XPMB_Main.SETTINGS_COL_KEY,
								SETTING_LAST_ITEM, -1));
		strEmuAct = (String) getStorage().getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_EMU_ACT,
				"com.androidemu.gba/.EmulatorActivity");
		Log.d(getClass().getSimpleName(), "reloadSettings():<Selected Item>="
				+ getContainerCategory().getSelectedSubitem());
		Log.d(getClass().getSimpleName(), "reloadSettings():<GBA Emulator Activity>=" + strEmuAct);
	}

	@Override
	public void deInitialize() {
		getStorage().putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM,
				getContainerCategory().getSelectedSubitem());

		getContainerCategory().clearSubitems();
		Log.v(getClass().getSimpleName(), "Removing " + alCoverKeys.size() + " cached cover assets");
		for (String ck : alCoverKeys) {
			getStorage().removeObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, ck);
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

							xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
							xmi.setIconBitmapID(ASSET_GRAPH_GBA_CV_NOTFOUND);
							xmi.setData(f);
							xmi.setWidth(pxfd(85));
							xmi.setHeight(pxfd(64));

							Log.v(getClass().getSimpleName(), "loadin():Item #" + y + " is at ["
									+ xmi.getPosition().x + "," + xmi.getPosition().y + "].");

							getContainerCategory().addSubitem(xmi);
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

					xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
					xmi.setIconBitmapID(ASSET_GRAPH_GBA_CV_NOTFOUND);
					xmi.setData(f);
					xmi.setWidth(pxfd(85));
					xmi.setHeight(pxfd(64));

					Log.v(getClass().getSimpleName(),
							"loadin():Item #" + y + " is at [" + xmi.getPosition().x + ","
									+ xmi.getPosition().y + "].");

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
		rProcessItem.setItem(item);
		new Thread(rProcessItem).run();
	}

	@Override
	public boolean isInitialized() {
		return bInit;
	}

	@Override
	public void sendKeyDown(int keyCode) {

		switch (keyCode) {
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_LEFT:
		case XPMB_Main.KEYCODE_CIRCLE:
			getFinishedListener().onFinished(null);
			break;
		case XPMB_Main.KEYCODE_CROSS:
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(
					getContainerCategory().getSelectedSubitem()));
			break;
		}
	}
}
