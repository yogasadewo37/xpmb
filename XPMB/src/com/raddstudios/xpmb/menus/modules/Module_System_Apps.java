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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBMenu_UILayer;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;

public class Module_System_Apps extends Modules_Base {

	private boolean bInit = false;
	private int intLastItem = -1, intMaxItemsOnScreen = 1;
	private ArrayList<String> alIconKeys = null;

	private final String SETTING_LAST_ITEM = "appsmenu.lastitem";

	public Module_System_Apps(XPMB_Activity root) {
		super(root);
		alIconKeys = new ArrayList<String>();
	}

	@Override
	public void initialize(XPMBMenu_UILayer parentLayer, XPMBMenuCategory container, FinishedListener listener) {
		super.initialize(parentLayer, container, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		reloadSettings();
		intMaxItemsOnScreen = (getRootActivity().getDrawingLayerManager().getHeight() / pxfd(64)) + 1;
		Log.v(getClass().getSimpleName(),
				"initialize():Max vertical items on screen: " + String.valueOf(intMaxItemsOnScreen));
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	private void reloadSettings() {
		intLastItem = (Integer) getStorage().getObject(XPMB_Main.SETTINGS_COL_KEY,
				SETTING_LAST_ITEM, -1);
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>=" + String.valueOf(intLastItem));
	}

	@Override
	public void deInitialize() {
		getStorage().putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, intLastItem);

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

		// TODO: prepare assets and animation for scaled (1.17x) effect

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

			if (intLastItem != -1) {
				getContainerCategory().setSubitemsPosY(pxfd(122) - (pxfd(64) * intLastItem));
				if (y != intLastItem) {
					xmi.setSeparatorAlpha(0.0f);
					xmi.setLabelAlpha(0.5f);
				} else {
					xmi.setMarginTop(pxfd(16));
					xmi.setMarginBottom(pxfd(16));
				}
			} else {
				if (y == 0) {
					xmi.setMarginTop(pxfd(16));
					xmi.setMarginBottom(pxfd(16));
				} else {
					xmi.setSeparatorAlpha(0.0f);
					xmi.setLabelAlpha(0.5f);
				}
			}
			Log.v(getClass().getSimpleName(),
					"loadin():Item #" + y + " is at [" + xmi.getPosition().x + ","
							+ xmi.getPosition().y + "].");

			getContainerCategory().addSubitem(xmi);
			y++;
			Log.d(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
					+ ". Process took " + (System.currentTimeMillis() - ct) + "ms.");
		}
		Log.i(getClass().getSimpleName(), "loadIn():App list load finished. Process took: "
				+ (System.currentTimeMillis() - t) + "ms.");
	}

	FinishedListener flAppEnd = new FinishedListener() {
		@Override
		public void onFinished(Object data) {
			Log.v(getClass().getSimpleName(),
					"onFinished():App activity finished. Returning to XPMB...");
			// TODO: Loading animation stop
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
				Log.v(getClass().getSimpleName(), "processItem():Starting app's main activity...");
				// TODO: Loading animation start
				getRootActivity().postIntentStartWait(flAppEnd, (Intent) f_item.getData());
			}
		}).run();
	}

	@Override
	public boolean isInitialized() {
		return bInit;
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
			getFinishedListener().onFinished(null);
			break;
		case XPMB_Main.KEYCODE_CROSS:
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(
					getContainerCategory().getSelectedSubitem()));
			break;
		}
	}

	public void moveUp() {
		if (getContainerCategory().getSelectedSubitem() == 0
				|| getContainerCategory().getNumSubItems() == 0) {
			return;
		}

		getAnimatorWorker().setAnimationType(Modules_Base.UIAnimatorWorker.ANIM_MENU_MOVE_UP);
		getAnimator().start();

		getContainerCategory().setSelectedSubItem(getContainerCategory().getSelectedSubitem() - 1);
		intLastItem = getContainerCategory().getSelectedSubitem();
	}

	public void moveDown() {
		if (getContainerCategory().getSelectedSubitem() == getContainerCategory().getNumSubItems() - 1
				|| getContainerCategory().getNumSubItems() == 0) {
			return;
		}

		getAnimatorWorker().setAnimationType(Modules_Base.UIAnimatorWorker.ANIM_MENU_MOVE_DOWN);
		getAnimator().start();

		getContainerCategory().setSelectedSubItem(getContainerCategory().getSelectedSubitem() + 1);
		intLastItem = getContainerCategory().getSelectedSubitem();
	}

	public void centerOnItem(int index) {
		if (index < 0 || index >= getContainerCategory().getNumSubItems()) {
			return;
		}

		getAnimatorWorker().setParams(new int[] { index });
		getAnimatorWorker()
				.setAnimationType(Modules_Base.UIAnimatorWorker.ANIM_MENU_CENTER_ON_ITEM);
		getAnimator().start();

		getContainerCategory().setSelectedSubItem(index);
		intLastItem = index;
	}
}
