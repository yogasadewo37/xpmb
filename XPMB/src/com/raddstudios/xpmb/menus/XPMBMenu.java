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

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.modules.Module_Emu_GBA;
import com.raddstudios.xpmb.menus.modules.Module_Emu_NES;
import com.raddstudios.xpmb.menus.modules.Module_Media_Music;
import com.raddstudios.xpmb.menus.modules.Module_System_Apps;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.UILayer;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Layout;

public class XPMBMenu extends XPMB_Layout {

	private XPMBMenu_UILayer xmvRoot = null;
	private Modules_Base curFilter = null;
	private boolean firstBackPress = false, isFocusedOnSubcategory = false, bLockedKeyPad = false;

	public XPMBMenu(XPMB_Activity root) {
		super(root);
	}

	@Override
	public void doInit() {
		Log.v(getClass().getSimpleName(), "doInit():Initializing XPMB menu.");
		xmvRoot = new XPMBMenu_UILayer(this);
		xmvRoot.doInit(getRootActivity().getResources().getXml(R.xml.xmb_layout));
		getRootActivity().getDrawingLayerManager().addLayer(xmvRoot);
		Log.v(getClass().getSimpleName(), "doInit():Finished initialization.");
	}

	@Override
	public void parseInitLayout() {
	}

	@Override
	public void sendKeyDown(int keyCode) {
		if (bLockedKeyPad) {
			return;
		}
		if (isFocusedOnSubcategory && curFilter != null) {
			curFilter.processkeyDown(keyCode);
			return;
		}
		switch (keyCode) {
		case XPMB_Main.KEYCODE_LEFT:
			firstBackPress = false;
			xmvRoot.moveLeft();
			break;
		case XPMB_Main.KEYCODE_RIGHT:
			firstBackPress = false;
			xmvRoot.moveRight();
			break;
		case XPMB_Main.KEYCODE_UP:
			firstBackPress = false;
			xmvRoot.moveUp();
			break;
		case XPMB_Main.KEYCODE_DOWN:
			firstBackPress = false;
			xmvRoot.moveDown();
			break;
		case XPMB_Main.KEYCODE_CROSS:
			firstBackPress = false;
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			if (!firstBackPress) {
				firstBackPress = true;
				Toast tst = Toast.makeText(getRootActivity().getWindow().getContext(),
						getRootActivity().getString(R.string.strBackKeyHint), Toast.LENGTH_SHORT);
				tst.show();
			} else {
				getRootActivity().requestActivityEnd();
			}
			break;
		}
	}

	public FinishedListener flListener = new FinishedListener() {
		@Override
		public void onFinished(Object data) {
			bLockedKeyPad = true;
			getRootActivity().getDrawingLayerManager().removeLayer((UILayer) curFilter);
			switch (curFilter.getListAnimator()) {
			case XPMBMenuCategory.LIST_ANIM_FULL:
				xmvRoot.startAnim(XPMBMenu_UILayer.ANIM_SHOW_MENU_FULL);
				break;
			case XPMBMenuCategory.LIST_ANIM_HALF:
				xmvRoot.startAnim(XPMBMenu_UILayer.ANIM_SHOW_MENU_HALF);
				break;
			case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
				xmvRoot.startAnim(XPMBMenu_UILayer.ANIM_HIGHLIGHT_MENU_POS);
				break;
			}
			isFocusedOnSubcategory = false;
			curFilter.deInitialize();
			curFilter = null;
			getMessageBus().postDelayed(new Runnable() {
				@Override
				public void run() {
					bLockedKeyPad = false;
				}
			}, 250);
		}
	};

	private XPMBMenuCategory xmc = null;
	private Runnable rExecCustItem = new Runnable() {
		@Override
		public void run() {
			if (curFilter != null) {
				curFilter.initialize(xmvRoot, xmc, flListener);
				curFilter.setListAnimator(xmc.getListAnimator());
				curFilter.loadIn();
				getRootActivity().getDrawingLayerManager().addLayer((UILayer) curFilter);
			}
			isFocusedOnSubcategory = true;
			bLockedKeyPad = false;
			((XPMB_BaseUILayer) getRootActivity().getDrawingLayerManager().getLayer(0))
					.setLoadingAnimationVisible(false);
		}
	};

	private void execCustItem(int index) {
		XPMBMenuItemDef xmid = xmvRoot.getItems().get(xmvRoot.getSelectedCategory())
				.getSubitem(index);
		if (xmid instanceof XPMBMenuCategory) {
			xmc = (XPMBMenuCategory) xmid;

			if (xmc.getSubmoduleID() == XPMBMenuCategory.MODULE_MEDIA_MUSIC) {
				curFilter = new Module_Media_Music(getRootActivity());
			}
			if (xmc.getSubmoduleID() == XPMBMenuCategory.MODULE_EMU_GBA) {
				curFilter = new Module_Emu_GBA(getRootActivity());
			}
			if (xmc.getSubmoduleID() == XPMBMenuCategory.MODULE_EMU_NES) {
				curFilter = new Module_Emu_NES(getRootActivity());
			}
			if (xmc.getSubmoduleID() == XPMBMenuCategory.MODULE_SYSTEM_APPS) {
				curFilter = new Module_System_Apps(getRootActivity());
			}

			if (curFilter != null) {
				bLockedKeyPad = true;
				((XPMB_BaseUILayer) getRootActivity().getDrawingLayerManager().getLayer(0))
						.setLoadingAnimationVisible(true);
				switch (xmc.getListAnimator()) {
				case XPMBMenuCategory.LIST_ANIM_FULL:
					xmvRoot.startAnim(XPMBMenu_UILayer.ANIM_HIDE_MENU_FULL);
					break;
				case XPMBMenuCategory.LIST_ANIM_HALF:
					xmvRoot.startAnim(XPMBMenu_UILayer.ANIM_HIDE_MENU_HALF);
					break;
				case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
					xmvRoot.startAnim(XPMBMenu_UILayer.ANIM_HIGHLIGHT_MENU_PRE);
					break;
				}
				getMessageBus().postDelayed(new Runnable() {

					@Override
					public void run() {
						new Thread(rExecCustItem).start();
					}
				}, 251);
			} else {
				Log.e(getClass().getSimpleName(),
						"execCustItem():Module not found for ID '" + xmc.getSubmoduleID() + "'");
			}
		} else if (xmid instanceof XPMBMenuItem) {
			XPMBMenuItem xmi = (XPMBMenuItem) xmid;
			Intent cExInt = (Intent) xmi.getData();
			if (getRootActivity().isActivityAvailable(cExInt)) {
				getRootActivity().showLoadingAnim(true);
				getRootActivity().postIntentStartWait(new FinishedListener() {
					@Override
					public void onFinished(Object data) {
						((XPMB_BaseUILayer) getRootActivity().getDrawingLayerManager().getLayer(0)).setLoadingAnimationVisible(false);
					}
				}, cExInt);
			} else {
				Toast tst = Toast.makeText(
						getRootActivity().getWindow().getContext(),
						getRootActivity().getString(R.string.strAppNotInstalled).replace("%s",
								cExInt.getComponent().getPackageName()), Toast.LENGTH_SHORT);
				tst.show();
			}
		}
	}

	private void execSelectedItem() {
		if (isFocusedOnSubcategory) {
			XPMBMenuCategory xms = (XPMBMenuCategory) xmvRoot
					.getItems()
					.get(xmvRoot.getSelectedCategory())
					.getSubitem(
							xmvRoot.getItems().get(xmvRoot.getSelectedCategory())
									.getSelectedSubitem());
			curFilter.processItem((XPMBMenuItem) xms.getSubitem(xms.getSelectedSubitem()));
		} else {
			execCustItem(xmvRoot.getItems().get(xmvRoot.getSelectedCategory()).getSelectedSubitem());
		}
	}

	@Override
	public void doCleanup() {
		// TODO: cleanup code here
	}
}
