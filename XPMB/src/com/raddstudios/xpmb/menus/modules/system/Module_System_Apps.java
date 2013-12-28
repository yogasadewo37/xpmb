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

package com.raddstudios.xpmb.menus.modules.system;

import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.menus.XPMBSideMenuItem;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemApp;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V1;

public class Module_System_Apps extends Modules_Base implements FinishedListener {

	private boolean bInit = false, bLoaded = false;
	private ProcessItemThread rProcessItem = null;

	private final String SETTINGS_BUNDLE_KEY = "module.system.apps",
			SETTING_LAST_ITEM = "lastitem";

	@Override
	public String getModuleID() {
		return "com.xpmb.system.apps";
	}

	private class SMInfo extends XPMBSideMenuItem {
		@Override
		public int getIndex() {
			return 7;
		}

		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuInfo);
		}

		@Override
		public void executeAction() {
		}
	}

	private class SMCopyItem extends XPMBSideMenuItem {
		@Override
		public int getIndex() {
			return 14;
		}

		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuCopyElement);
		}

		@Override
		public void executeAction() {
			XPMBMenuItemDef xmi = getContainerCategory().getSubitem(
					getContainerCategory().getSelectedSubitem());
			Bundle item = xmi.storeInBundle();
			getRootActivity().getSettingBundle(XPMBActivity.SETTINGS_BUNDLE_GLOBAL).putBundle(
					XPMBActivity.SETTINGS_GLOBAL_COPIED_MENUITEM, item);
			getRootActivity().getSettingBundle(XPMBActivity.SETTINGS_BUNDLE_GLOBAL).putString(
					XPMBActivity.SETTINGS_GLOBAL_COPIED_MENUITEM_TYPE, xmi.getTypeDescriptor());
		}
	}

	private class ProcessItemThread implements Runnable {

		private XPMBMenuItemApp f_item = null;
		private FinishedListener mOwner = null;

		public ProcessItemThread(FinishedListener owner) {
			mOwner = owner;
		}

		public void setItem(XPMBMenuItemApp item) {
			f_item = item;
		}

		@Override
		public void run() {
			Log.v(getClass().getSimpleName(), "processItem():Starting app's main activity... /"
					+ f_item.getIntent().getPackage());
			getRootActivity().setLoading(true);
			getFinishedListener().onFinished(Module_System_Apps.this);
			getRootActivity().postIntentStartWait(mOwner, f_item.getIntent());
		}
	}

	public Module_System_Apps(XPMBActivity root) {
		super(root);
		rProcessItem = new ProcessItemThread(this);
	}

	@Override
	public void initialize(UILayer parentLayer, FinishedListener listener) {
		super.initialize(parentLayer, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		reloadSettings();
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	@Override
	protected void reloadSettings() {
		Bundle settings = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		getContainerCategory().setSelectedSubitem(settings.getInt(SETTING_LAST_ITEM, -1));
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>="
						+ String.valueOf(getContainerCategory().getSelectedSubitem()));
	}

	@Override
	public void dispose() {
		Bundle saveData = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		saveData.putInt(SETTING_LAST_ITEM, getContainerCategory().getSelectedSubitem());

		getContainerCategory().clearSubitems();
	}

	@Override
	public void loadIn() {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}
		if (bLoaded) {
			Log.i(getClass().getSimpleName(), "loadIn():Module already loaded. Skipping process.");
			return;
		}

		super.loadIn();
		super.setListAnimator(new SubmenuAnimator_V1(getContainerCategory(), this));
		long t = System.currentTimeMillis();
		PackageManager pm = getRootActivity().getPackageManager();
		Intent filter = new Intent(Intent.ACTION_MAIN);
		filter.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> ri = pm.queryIntentActivities(filter, PackageManager.GET_META_DATA);

		int y = 0;
		for (ResolveInfo rinf : ri) {
			long ct = System.currentTimeMillis();
			if (rinf.activityInfo.packageName.equals(getRootActivity().getPackageName())) {
				continue;
			}

			String strAppLabel = rinf.loadLabel(pm).toString();
			Intent strAppIntent = pm.getLaunchIntentForPackage(rinf.activityInfo.packageName);

			Log.i(getClass().getSimpleName(), "loadIn():Found app with name '" + strAppLabel
					+ "' ID #" + y);

			XPMBMenuItemApp xmi = new XPMBMenuItemApp(strAppLabel);
			String strIcon = "module.system.apps.icon|" + strAppLabel;
			long dt = System.currentTimeMillis();
			Bitmap bmAsset = ((BitmapDrawable) rinf.loadIcon(pm)).getBitmap();
			Bitmap bmModAsset = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
			Canvas bmModAssetCanvas = new Canvas(bmModAsset);
			Rect bA = new Rect(0, 0, 96, 96), bB = new Rect(0, 0, bmAsset.getWidth(),
					bmAsset.getHeight());
			gravitateRect(bA, bB, Gravity.CENTER);
			bmModAssetCanvas.drawBitmap(bmAsset, null, bB, new Paint());
			getRootActivity().getThemeManager().addCustomAsset(strIcon, bmModAsset);
			Log.i(getClass().getSimpleName(), "loadIn():Icon asset loading for app '" + strAppLabel
					+ "' done. Took " + dt + "ms.");

			xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
			xmi.setIconBitmapID(strIcon);
			xmi.setIntent(strAppIntent);
			xmi.setWidth(pxfd(64));
			xmi.setHeight(pxfd(64));

			getContainerCategory().addSubitem(xmi);
			y++;
			Log.d(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
					+ ". Process took " + (System.currentTimeMillis() - ct) + "ms.");
		}
		getListAnimator().initializeItems();
		bLoaded = true;
		Log.i(getClass().getSimpleName(), "loadIn():App list load finished. Process took: "
				+ (System.currentTimeMillis() - t) + "ms.");
	}

	@Override
	public void onFinished(Object data) {
		Log.v(getClass().getSimpleName(),
				"onFinished():App activity finished. Returning to XPMB...");
		getRootActivity().setLoading(false);
	}

	@Override
	public void processItem(XPMBMenuItemDef item) {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. You shouldn't even be calling this method.");
			return;
		}

		rProcessItem.setItem((XPMBMenuItemApp) item);
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
			break;
		case XPMBActivity.KEYCODE_CROSS:
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(
					getContainerCategory().getSelectedSubitem()));
			break;
		case XPMBActivity.KEYCODE_TRIANGLE:
			getRootActivity().setupSideMenu(
					new XPMBSideMenuItem[] { new SMInfo(), new SMCopyItem() }, 7);
			getRootActivity().showSideMenu(this);
			break;
		}
	}
}
