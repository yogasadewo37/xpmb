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

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipFile;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.filters.Filter_Base;
import com.raddstudios.xpmb.menus.utils.filters.Filter_Media_Music;
import com.raddstudios.xpmb.utils.ThemeLoader;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_MainMenu;
import com.raddstudios.xpmb.utils.backports.XPMBMenu_View;

public class XPMBMenu extends XPMB_MainMenu {

	private XPMBMenu_View xmvRoot = null;
	private Filter_Base curFilter = null;
	private ThemeLoader thm = null;
	private XmlResourceParser src = null; 
	private boolean firstBackPress = false, isFocusedOnSubcategory = false, bLockedKeyPad = false;

	public XPMBMenu(XmlResourceParser source, Handler messageBus, ViewGroup rootView,
			XPMB_Activity root) {
		super(root, messageBus, rootView);
		src = source;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
		Looper.prepare();
		thm = new ThemeLoader((Hashtable<String, Bitmap>) getRootActivity().getStorage()
				.getCollection(XPMB_Main.GRAPH_ASSETS_COL_KEY));
		xmvRoot = new XPMBMenu_View(getRootView().getContext(), src, getRootActivity());
		xmvRoot.doInit();
	}

	@Override
	public void parseInitLayout() {
		try {
			thm.reloadTheme(new ZipFile(new File(Environment.getExternalStorageDirectory()
					.getPath() + "/XPMB/themes/simple.zip"), ZipFile.OPEN_READ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		xmvRoot.setLayoutParams(rp);
		xmvRoot.setVisibility(View.VISIBLE);
		getRootView().addView(xmvRoot);
		xmvRoot.requestRedraw();
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
		public void onFinished(Intent intent) {
			bLockedKeyPad = true;
			getRootView().removeView((View) curFilter);
			xmvRoot.setFocus(true);
			switch (curFilter.getListAnimator()) {
			case XPMBMenuCategory.LIST_ANIM_FULL:
				xmvRoot.startAnim(XPMBMenu_View.ANIM_SHOW_MENU_FULL);
				break;
			case XPMBMenuCategory.LIST_ANIM_HALF:
				xmvRoot.startAnim(XPMBMenu_View.ANIM_SHOW_MENU_HALF);
				break;
			case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
				xmvRoot.startAnim(XPMBMenu_View.ANIM_HIGHLIGHT_MENU_POS);
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

	private void execCustItem(int index) {
		if (xmvRoot.getItems().get(xmvRoot.getSelectedCategory()).getSubitem(index) instanceof XPMBMenuItem) {
			XPMBMenuItem xmi = (XPMBMenuItem) xmvRoot.getItems().get(xmvRoot.getSelectedCategory())
					.getSubitem(index);
			Intent cExInt = (Intent) xmi.getData();
			if (getRootActivity().isActivityAvailable(cExInt)) {
				getRootActivity().showLoadingAnim(true);
				getRootActivity().postIntentStartWait(new FinishedListener() {
					@Override
					public void onFinished(Intent intent) {
						getRootActivity().showLoadingAnim(false);
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
		if (xmvRoot.getItems().get(xmvRoot.getSelectedCategory()).getSubitem(index) instanceof XPMBMenuCategory) {
			final XPMBMenuCategory xms = (XPMBMenuCategory) xmvRoot.getItems()
					.get(xmvRoot.getSelectedCategory()).getSubitem(index);
			bLockedKeyPad = true;
			getRootActivity().showLoadingAnim(true);
			switch (xms.getListAnimator()) {
			case XPMBMenuCategory.LIST_ANIM_FULL:
				xmvRoot.startAnim(XPMBMenu_View.ANIM_HIDE_MENU_FULL);
				break;
			case XPMBMenuCategory.LIST_ANIM_HALF:
				xmvRoot.startAnim(XPMBMenu_View.ANIM_HIDE_MENU_HALF);
				break;
			case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
				xmvRoot.startAnim(XPMBMenu_View.ANIM_HIGHLIGHT_MENU_PRE);
				break;
			}
			getMessageBus().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (xms.getItemFilter() == XPMBMenuCategory.FILTER_MEDIA_MUSIC) {
						curFilter = new Filter_Media_Music(getRootActivity().getBaseContext());
					}
					if (curFilter != null) {
						curFilter.initialize(xmvRoot, xms, getRootActivity(), flListener);
						curFilter.setListAnimator(xms.getListAnimator());
						curFilter.loadIn();
						RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
								LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
						((View) curFilter).setLayoutParams(rp);
					}
					getRootView().addView((View) curFilter);
					getRootActivity().showLoadingAnim(false);
					xmvRoot.setFocus(false);
					isFocusedOnSubcategory = true;
					bLockedKeyPad = false;
				}
			}, 250);
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
		getRootView().removeView(xmvRoot);
	}
}
