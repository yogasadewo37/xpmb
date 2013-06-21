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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V1;

public class Module_System_Apps extends Modules_Base implements FinishedListener {

	private boolean bInit = false;
	private ArrayList<String> alIconKeys = null;
	private ProcessItemThread rProcessItem = null;

	private final String SETTING_LAST_ITEM = "appsmenu.lastitem";

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
			Log.v(getClass().getSimpleName(), "processItem():Starting app's main activity...");
			((XPMBUIModule) getRootActivity().getDrawingLayerManager().getLayer(0))
					.setLoadingAnimationVisible(true);
			getRootActivity().postIntentStartWait(mOwner, (Intent) f_item.getData());
		}
	}

	public Module_System_Apps(XPMB_Activity root) {
		super(root);
		alIconKeys = new ArrayList<String>();
		rProcessItem = new ProcessItemThread(this);
	}

	@Override
	public void initialize(UILayer parentLayer, XPMBMenuCategory container,
			FinishedListener listener) {
		super.initialize(parentLayer, container, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		reloadSettings();
		super.setListAnimator(new SubmenuAnimator_V1(container, this));
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	private void reloadSettings() {
		getContainerCategory().setSelectedSubitem((Integer) getStorage().getObject(XPMB_Main.SETTINGS_COL_KEY,
				SETTING_LAST_ITEM, -1));
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>=" + String.valueOf(getContainerCategory().getSelectedSubitem()));
	}

	@Override
	public void deInitialize() {
		getStorage().putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, getContainerCategory().getSelectedSubitem());

		getContainerCategory().clearSubitems();
		Log.v(getClass().getSimpleName(), "Removing " + String.valueOf(alIconKeys.size())
				+ " cached icon assets");
		for (String ck : alIconKeys) {
			getStorage().removeObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, ck);
		}
		alIconKeys.clear();
	}

	@Override
	public void loadIn() {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}

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

			XPMBMenuItem xmi = new XPMBMenuItem(strAppLabel);
			String strIcon = "module.system.apps.icon|" + strAppLabel;
			if (getStorage().getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, strIcon) == null) {
				long dt = System.currentTimeMillis();
				getStorage().putObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, strIcon,
						((BitmapDrawable) rinf.loadIcon(pm)).getBitmap());
				Log.i(getClass().getSimpleName(), "loadIn():Icon asset loading for app '"
						+ strAppLabel + "' done. Took " + dt + "ms.");
			}
			if (!alIconKeys.contains(strIcon)) {
				alIconKeys.add(strIcon);
			}
			xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
			xmi.setIconBitmapID(strIcon);
			xmi.setData(strAppIntent);
			xmi.setWidth(pxfd(64));
			xmi.setHeight(pxfd(64));

			Log.v(getClass().getSimpleName(),
					"loadin():Item #" + y + " is at [" + xmi.getPosition().x + ","
							+ xmi.getPosition().y + "].");

			getContainerCategory().addSubitem(xmi);
			y++;
			Log.d(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
					+ ". Process took " + (System.currentTimeMillis() - ct) + "ms.");
		}
		getListAnimator().initializeItems();
		Log.i(getClass().getSimpleName(), "loadIn():App list load finished. Process took: "
				+ (System.currentTimeMillis() - t) + "ms.");
	}

	@Override
	public void onFinished(Object data) {
		Log.v(getClass().getSimpleName(),
				"onFinished():App activity finished. Returning to XPMB...");
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
