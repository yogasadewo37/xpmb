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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBMenu_View;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Activity.ObjectCollections;
import com.raddstudios.xpmb.utils.XPMB_Layout;

public class Module_System_Apps extends XPMB_Layout implements Modules_Base {

	private ObjectCollections mStor = null;
	private XPMBMenuCategory dest = null;
	private XPMBMenu_View container = null;
	private boolean bInit = false;
	private FinishedListener flListener = null;
	private int intAnimator = 0, intLastItem = -1, intMaxItemsOnScreen = 1;
	private ArrayList<String> alIconKeys = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	private final String SETTING_LAST_ITEM = "appsmenu.lastitem";

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

	public Module_System_Apps(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView);

		alIconKeys = new ArrayList<String>();
		mStor = getRootActivity().getStorage();
		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
	}

	public void initialize(XPMBMenu_View owner, XPMBMenuCategory root, FinishedListener finishedL) {
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		flListener = finishedL;
		container = owner;
		dest = root;
		reloadSettings();
		intMaxItemsOnScreen = (owner.getHeight() / 96) + 1;
		Log.v(getClass().getSimpleName(),
				"initialize():Max vertical items on screen: " + String.valueOf(intMaxItemsOnScreen));
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	private void reloadSettings() {
		intLastItem = (Integer) mStor.getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, -1);
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>=" + String.valueOf(intLastItem));
	}

	@Override
	public void deInitialize() {
		mStor.putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, intLastItem);

		dest.clearSubitems();
		Log.v(getClass().getSimpleName(), "Removing " + String.valueOf(alIconKeys.size())
				+ " cached icon assets");
		for (String ck : alIconKeys) {
			mStor.removeObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, ck);
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
			if (mStor.getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, strIcon) == null) {
				long dt = System.currentTimeMillis();
				mStor.putObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, strIcon,
						((BitmapDrawable) rinf.loadIcon(pm)).getBitmap());
				Log.i(getClass().getSimpleName(), "loadIn():Icon asset loading for app '"
						+ strAppLabel + "' done. Took " + dt + "ms.");
			}
			if (!alIconKeys.contains(strIcon)) {
				alIconKeys.add(strIcon);
			}
			xmi.setIcon(strIcon);
			xmi.setData(strAppIntent);

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
			Log.v(getClass().getSimpleName(), "loadin():Item #" + y + " is at [" + xmi.getPosition().x + ","
					+ xmi.getPosition().y + "].");
			xmi.setWidth(96);
			xmi.setHeight(96);

			dest.addSubitem(xmi);
			y++;
			Log.d(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
					+ ". Process took " + (System.currentTimeMillis() - ct) + "ms.");
		}
		Log.i(getClass().getSimpleName(), "loadIn():App list load finished. Process took: "
				+ (System.currentTimeMillis() - t) + "ms.");
	}

	FinishedListener flAppEnd = new FinishedListener() {
		@Override
		public void onFinished(Intent intent) {
			Log.v(getClass().getSimpleName(),
					"onFinished():App activity finished. Returning to XPMB...");
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
				Log.v(getClass().getSimpleName(),
						"processItem():Starting app's main activity...");
				getRootActivity().showLoadingAnim(true);
				getRootActivity().postIntentStartWait(flAppEnd, (Intent) f_item.getData());
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

	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect();
	private int px_i_l = 0, py_i_l = 0, textH = 0;

	private Rect getAlignedAndScaledRect(int left, int top, int width, int height, float scaleX,
			float scaleY, int gravity) {
		int sizeX = (int) (width * scaleX);
		int sizeY = (int) (height * scaleY);
		Rect in = new Rect(left, top, left + width, top + height);
		Rect out = new Rect(0, 0, 0, 0);

		Gravity.apply(gravity, sizeX, sizeY, in, out);

		return out;
	}

	public void drawTo(Canvas canvas) {
		// TODO: Take in account the actual orientation and DPI of the device

		//canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

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
	public void setListAnimator(int animator) {
		intAnimator = animator;
	}

	@Override
	public int getListAnimator() {
		return intAnimator;
	}
}
