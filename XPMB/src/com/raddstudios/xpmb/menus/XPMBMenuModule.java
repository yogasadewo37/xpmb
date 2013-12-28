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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemApp;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemMusic;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemROM;
import com.raddstudios.xpmb.utils.XPMBSettingsManager;
import com.raddstudios.xpmb.utils.UI.UILayer;

public class XPMBMenuModule extends UILayer implements FinishedListener {

	private XPMBMenuCategory alItems = null;
	private int intMaxItemsOnScreenH = 0, intMaxItemsOnScreenV = 0;
	private float fOpacity = 1.0f, fSelIconAlpha = 0.0f;
	private boolean mInit = false, firstBackPress = false, bShowSelIcon = false;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	private Paint pParams = new Paint();
	private Rect rTextBounds = null, tS = null, cP = null, rSelIconRect = null;
	private Bitmap bmSelIcon = null;

	public static final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_MENUITEM = 2, ANIM_CENTER_ON_SUBMENUITEM = 3, ANIM_HIDE_MENU_HALF = 4,
			ANIM_HIDE_MENU_FULL = 5, ANIM_MENU_MOVE_LEFT = 6, ANIM_MENU_MOVE_RIGHT = 7,
			ANIM_SHOW_MENU_HALF = 8, ANIM_SHOW_MENU_FULL = 9, ANIM_HIGHLIGHT_MENU_PRE = 10,
			ANIM_HIGHLIGHT_MENU_POS = 11;

	private class SMMoveItemUp extends XPMBSideMenuItem {
		@Override
		public int getIndex() {
			return 5;
		}

		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuMoveItemUp);
		}

		@Override
		public void executeAction() {
			XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(alItems
					.getSelectedSubitem());
			if (xmc.getNumSubitems() > 0) {
				if (xmc.getSelectedSubitem() > 0) {
					XPMBMenuItemDef t = xmc.getSubitem(xmc.getSelectedSubitem() - 1);
					xmc.setSubitem(xmc.getSelectedSubitem() - 1,
							xmc.getSubitem(xmc.getSelectedSubitem()));
					xmc.setSubitem(xmc.getSelectedSubitem(), t);
					xmc.getSubitem(xmc.getSelectedSubitem()).setLabelAlpha(1.0f);
					xmc.getSubitem(xmc.getSelectedSubitem() - 1).setLabelAlpha(0.0f);
				}
			}
		}
	}

	private class SMMoveItemDown extends XPMBSideMenuItem {
		@Override
		public int getIndex() {
			return 6;
		}

		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuMoveItemDown);
		}

		@Override
		public void executeAction() {
			XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(alItems
					.getSelectedSubitem());
			if (xmc.getNumSubitems() > 0) {
				if (xmc.getSelectedSubitem() < xmc.getNumSubitems()) {
					XPMBMenuItemDef t = xmc.getSubitem(xmc.getSelectedSubitem() + 1);
					xmc.setSubitem(xmc.getSelectedSubitem() + 1,
							xmc.getSubitem(xmc.getSelectedSubitem()));
					xmc.setSubitem(xmc.getSelectedSubitem(), t);
					xmc.getSubitem(xmc.getSelectedSubitem()).setLabelAlpha(1.0f);
					xmc.getSubitem(xmc.getSelectedSubitem() + 1).setLabelAlpha(0.0f);
				}
			}
		}
	}

	private class SMPasteItem extends XPMBSideMenuItem {
		@Override
		public int getIndex() {
			return 7;
		}

		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuPasteElement);
		}

		@Override
		public void executeAction() {
			Bundle bGlobal = getRootActivity()
					.getSettingBundle(XPMBActivity.SETTINGS_BUNDLE_GLOBAL);
			Bundle item = bGlobal.getBundle(XPMBActivity.SETTINGS_GLOBAL_COPIED_MENUITEM);
			if (item != null) {
				XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(alItems
						.getSelectedSubitem());
				XPMBMenuItemDef xmi = null;
				String desc = bGlobal.getString(XPMBActivity.SETTINGS_GLOBAL_COPIED_MENUITEM_TYPE);
				if (desc.equals(XPMBMenuItem.TYPE_DESC)) {
					xmi = new XPMBMenuItem(item);
				}
				if (desc.equals(XPMBMenuItemApp.TYPE_DESC)) {
					xmi = new XPMBMenuItemApp(item);
				}
				if (desc.equals(XPMBMenuItemMusic.TYPE_DESC)) {
					xmi = new XPMBMenuItemMusic(item);
				}
				if (desc.equals(XPMBMenuItemROM.TYPE_DESC)) {
					xmi = new XPMBMenuItemROM(item);
				}
				if (xmi != null) {
					xmi.setHeight(pxfd(85));
					xmi.setWidth(pxfd(85));
					if (xmc.getNumSubitems() != 0) {
						xmi.setMarginTop(0);
						xmc.addSubitem(xmc.getSelectedSubitem() + 1, xmi);
					} else {
						xmi.setMarginTop(pxfd(85));
						xmc.addSubitem(xmi);
					}
				} else {
					Log.e(getClass().getSimpleName(),
							"executeAction():Item type descriptor not recognized: '" + desc
									+ "'. Ignoring item.");
				}
				bGlobal.remove(XPMBActivity.SETTINGS_GLOBAL_COPIED_MENUITEM);
				bGlobal.remove(XPMBActivity.SETTINGS_GLOBAL_COPIED_MENUITEM_TYPE);
			}
		}
	}

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		// Common Vars
		private int intAnimType = -1, pInitPosY = 0, pInitPosX = 0, intAnimItem = -1,
				intNextItem = -1;
		// private int[] iaParams = null;
		private ValueAnimator mOwner = null;

		// Animator Vars
		int dispA = 0, marginA = 0, marginB = 0;
		float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f, scaleA = 0.0f,
				scaleB = 0.0f;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;

			switch (type) {
			case ANIM_MENU_MOVE_LEFT:
				mOwner.setDuration(250);
				intAnimItem = alItems.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_MENU_MOVE_RIGHT:
				mOwner.setDuration(250);
				intAnimItem = alItems.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				intAnimItem = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
						.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				pInitPosY = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
						.getSubitemsPos().y;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
						.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				pInitPosY = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
						.getSubitemsPos().y;
				break;
			case ANIM_HIDE_MENU_HALF:
				mOwner.setDuration(250);
				intAnimItem = alItems.getSelectedSubitem();
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_SHOW_MENU_HALF:
				mOwner.setDuration(250);
				intAnimItem = alItems.getSelectedSubitem();
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_HIGHLIGHT_MENU_PRE:
				mOwner.setDuration(250);
				intAnimItem = alItems.getSelectedSubitem();
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_HIGHLIGHT_MENU_POS:
				mOwner.setDuration(250);
				intAnimItem = alItems.getSelectedSubitem();
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			switch (intAnimType) {
			case ANIM_MENU_MOVE_RIGHT:
			case ANIM_MENU_MOVE_LEFT:
				if (intAnimType == ANIM_MENU_MOVE_RIGHT) {
					dispA = (int) (pxfd(-85) * completion);
				} else {
					dispA = (int) (pxfd(85) * completion);
				}
				scaleA = 1.0f - (0.25f * completion);
				scaleB = 0.75f + (0.25f * completion);
				alphaA = 1.0f - (0.5f * completion);
				alphaB = 0.5f + (0.5f * completion);
				alphaC = 1.0f - completion;
				alphaD = completion;
				alItems.setSubitemsPosX(pInitPosX + dispA);

				for (int x = 0; x < alItems.getNumSubitems(); x++) {
					XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(x);
					if (x == intAnimItem || x == intNextItem) {
						if (x == intAnimItem) {
							xmc.setIconScaleX(scaleA);
							xmc.setIconScaleY(scaleA);
							xmc.setIconAlpha(alphaA);
							xmc.setLabelAlpha(alphaC);
							xmc.setSubitemsAlpha(alphaC);
						}
						if (x == intNextItem) {
							xmc.setIconScaleX(scaleB);
							xmc.setIconScaleY(scaleB);
							xmc.setIconAlpha(alphaB);
							xmc.setLabelAlpha(alphaD);
							xmc.setSubitemsAlpha(alphaD);
						}
					}
				}
				break;
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
				if (intAnimType == ANIM_MENU_MOVE_UP) {
					alphaA = 1.0f - completion;
					alphaB = completion;
					marginA = (int) (pxfd(85) * completion);
					marginB = pxfd(85) - marginA;
				} else {
					alphaA = completion;
					alphaB = 1.0f - completion;
					marginB = (int) (pxfd(85) * completion);
					marginA = pxfd(85) - marginB;
				}
				dispA = (int) (((intNextItem - intAnimItem) * pxfd(-85)) * completion);

				XPMBMenuCategory axmc = (XPMBMenuCategory) alItems.getSubitem(alItems
						.getSelectedSubitem());

				axmc.setSubitemsPosY(pInitPosY + dispA);
				for (int y = 0; y < axmc.getNumSubitems(); y++) {
					XPMBMenuItemDef xmid = axmc.getSubitem(y);

					if (intAnimType == ANIM_MENU_MOVE_UP) {
						if (y == intNextItem) {
							xmid.setLabelAlpha(alphaB);
							xmid.setMarginTop(marginA);
						} else if (y == intAnimItem) {
							xmid.setLabelAlpha(alphaA);
							xmid.setMarginTop(marginB);
						}
					} else {
						if (y == intAnimItem) {
							xmid.setLabelAlpha(alphaB);
							xmid.setMarginTop(marginA);
						} else if (y == intNextItem) {
							xmid.setLabelAlpha(alphaA);
							xmid.setMarginTop(marginB);
						}
					}
				}
				break;
			case ANIM_HIGHLIGHT_MENU_PRE:
			case ANIM_HIGHLIGHT_MENU_POS:
				if (intAnimType == ANIM_HIGHLIGHT_MENU_PRE) {
					dispA = (int) (pxfd(-74) * completion);
					alphaA = 1.0f - completion;
					alphaB = 1.0f - (0.5f * completion);
					alphaC = 0.7f - (0.7f * completion);
					fSelIconAlpha = completion;
				} else {
					dispA = (int) (pxfd(74) * completion);
					alphaA = completion;
					alphaB = 0.5f + (0.5f * completion);
					alphaC = 0.7f * completion;
					fSelIconAlpha = 1.0f - completion;
				}

				alItems.setSubitemsPosX(pInitPosX + dispA);

				for (int x = 0; x < alItems.getNumSubitems(); x++) {
					XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(x);
					if (x == intAnimItem) {
						for (int y = 0; y < xmc.getNumSubitems(); y++) {
							if (y == xmc.getSelectedSubitem()) {
								xmc.getSubitem(y).setLabelAlpha(alphaA);
							} else {
								xmc.getSubitem(y).setIconAlpha(alphaB);
							}
						}
					} else {
						xmc.setIconAlpha(alphaC);
					}
				}
				break;
			case ANIM_HIDE_MENU_FULL:
			case ANIM_SHOW_MENU_FULL:
				if (intAnimType == ANIM_HIDE_MENU_FULL) {
					alphaA = 1.0f - completion;
				} else {
					alphaA = completion;
				}

				setOpacity(alphaA);
				break;
			case ANIM_HIDE_MENU_HALF:
			case ANIM_SHOW_MENU_HALF:
				if (intAnimType == ANIM_HIDE_MENU_HALF) {
					dispA = (int) (-176 * completion);
					alphaA = 1.0f - completion;
					alphaB = 1.0f - (0.5f * completion);
					alphaC = 0.7f - (0.7f * completion);
					fSelIconAlpha = completion;

				} else {
					dispA = (int) (176 * completion);
					alphaA = completion;
					alphaB = 0.5f + (0.5f * completion);
					alphaC = 0.7f * completion;
					fSelIconAlpha = 1.0f - completion;
				}
				alItems.setSubitemsPosX(pInitPosX + dispA);

				for (int x = 0; x < alItems.getNumSubitems(); x++) {
					XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(x);
					if (x == intAnimItem) {
						xmc.setLabelAlpha(alphaA);
						for (int y = 0; y < xmc.getNumSubitems(); y++) {
							if (y == xmc.getSelectedSubitem()) {
								xmc.getSubitem(y).setLabelAlpha(alphaA);
							} else {
								xmc.getSubitem(y).setIconAlpha(alphaB);
							}
						}
					} else {
						xmc.setIconAlpha(alphaC);
					}
				}

				break;
			case ANIM_NONE:
			default:
				break;
			}
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
				((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
						.setSubitemsPosY(pInitPosY + ((intNextItem - intAnimItem) * pxfd(-85)));
				break;
			case ANIM_MENU_MOVE_RIGHT:
			case ANIM_MENU_MOVE_LEFT:
				((XPMBMenuCategory) alItems.getSubitem(intAnimItem)).setSubitemsVisibility(false);
				break;
			case ANIM_SHOW_MENU_HALF:
			case ANIM_HIGHLIGHT_MENU_POS:
				bShowSelIcon = false;
				break;
			}
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
			switch (intAnimType) {
			case ANIM_MENU_MOVE_RIGHT:
			case ANIM_MENU_MOVE_LEFT:
				((XPMBMenuCategory) alItems.getSubitem(intNextItem)).setSubitemsVisibility(true);
				break;
			case ANIM_HIDE_MENU_HALF:
			case ANIM_HIGHLIGHT_MENU_PRE:
				bShowSelIcon = true;
				break;
			}
		}
	};

	public XPMBMenuModule(XPMBActivity root) {
		super(root);

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
		rTextBounds = new Rect();
		tS = new Rect();
		cP = new Rect();
	}

	@Override
	public void initialize() {
		Log.v(getClass().getSimpleName(), "doInit():Initializing XPMB Main Menu (XMB type) Module.");
		doInit(getRootActivity().getResources().getXml(R.xml.xmb_layout));
		bmSelIcon = flipBitmap((Bitmap) getRootActivity().getThemeManager().getAsset(
				"theme.icon|ui_backicon"));
		rSelIconRect = new Rect(0, 0, bmSelIcon.getWidth(), bmSelIcon.getHeight());
		Log.v(getClass().getSimpleName(),
				"doInit():Finished XPMB Main Menu (XMB type) Module initialization.");
	}

	@Override
	public void dispose() {
		Log.v(getClass().getSimpleName(), "dispose():Started saving menu state.");
		File fMenuState = new File(getRootActivity().getCacheDir(), "menustate");
		try {
			FileOutputStream fos = new FileOutputStream(fMenuState);
			DataOutputStream dos = new DataOutputStream(fos);

			XPMBSettingsManager.writeBundleTo(alItems.storeInBundle(), dos);

			dos.close();

			Log.v(getClass().getSimpleName(), "dispose():Finished saving menu state.");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "dispose():Couldn't save menu state");
			e.printStackTrace();
		}
	}

	public void doInit(XmlResourceParser xrpRes) {
		Log.v(getClass().getSimpleName(), "doInit():Start module initialization.");

		File fMenuState = new File(getRootActivity().getCacheDir(), "menustate");
		if (fMenuState.exists()) {
			try {
				FileInputStream fis = new FileInputStream(fMenuState);
				DataInputStream dis = new DataInputStream(fis);

				long t = System.currentTimeMillis();
				Log.i(getClass().getSimpleName(), "doInit():Started reading menu state cache.");
				alItems = new XPMBMenuCategory(XPMBSettingsManager.readBundleFrom(dis));
				Log.i(getClass().getSimpleName(),
						"doInit():Finished reading menu state cache. Took "
								+ (System.currentTimeMillis() - t) + "ms.");

				dis.close();

				mInit = true;
				Log.v(getClass().getSimpleName(), "doInit():Finished module initialization.");
				return;
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "doInit():Couldn't read last menu state");
				e.printStackTrace();
			}
		}

		try {

			int eventType = xrpRes.getEventType(), x = 0, y = 0;
			boolean done = false;
			XPMBMenuCategory cItem = null;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					alItems = new XPMBMenuCategory("@RootXPMBNode");
					alItems.setSubitemsPosX(pxfd(74));
					alItems.setSubitemsPosY(pxfd(42));
					break;
				case XmlResourceParser.START_TAG:
					cName = xrpRes.getName();
					if (cName.equals("category")) {

						// Category Data
						String label = xrpRes.getAttributeValue(null, "label");
						if (label.startsWith("$")) {
							cItem = new XPMBMenuCategory(getRootActivity().getString(
									getRootActivity().getResources().getIdentifier(
											label.substring(1), "string",
											getRootActivity().getPackageName())));
						} else {
							cItem = new XPMBMenuCategory(label);
						}
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cItem.setIconBitmapID(cAtt);
						} else {
							cItem.setIconBitmapID("theme.icon|icon_gamedata");
						}
						cItem.setWidth(pxfd(85));
						cItem.setHeight(pxfd(85));

						// Positioning Data
						cItem.setSubitemsPosX(alItems.getSubitemsPos().x);
						cItem.setSubitemsPosY(pxfd(42));
						if (x == 0) {
							cItem.setSubitemsVisibility(true);
						} else {
							cItem.setIconScale(new PointF(0.75f, 0.75f));
							cItem.setIconAlpha(0.5f);
							cItem.setLabelAlpha(0.0f);
						}
					}
					if (cName.equals("subitem.link")) {

						// Item Data
						XPMBMenuItemApp cSublink = null;

						String label = xrpRes.getAttributeValue(null, "label");
						if (label.startsWith("$")) {
							cSublink = new XPMBMenuItemApp(getRootActivity().getString(
									getRootActivity().getResources().getIdentifier(
											label.substring(1), "string",
											getRootActivity().getPackageName())));
						} else {
							cSublink = new XPMBMenuItemApp(label);
						}
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSublink.setIconBitmapID(cAtt);
						} else {
							cSublink.setIconBitmapID("theme.icon|icon_onlinemanual");
						}
						cSublink.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
						cSublink.setWidth(pxfd(85));
						cSublink.setHeight(pxfd(85));
						String cAct = xrpRes.getAttributeValue(null, "action"), cComp = xrpRes
								.getAttributeValue(null, "exec");
						if (cAct == null) {
							cAct = Intent.ACTION_MAIN;
						}
						Intent cIntent = new Intent(cAct);
						if (cComp != null) {
							cIntent.setComponent(ComponentName.unflattenFromString(cComp));
						}
						cSublink.setIntent(cIntent);
						if (x != 0 && y != 0) {
							cSublink.setLabelAlpha(0.0f);
						}
						if (x == 0 && y != 0) {
							cSublink.setLabelAlpha(0.7f);
						}
						if (y == 0) {
							cSublink.setMarginTop(pxfd(85));
						}

						cItem.addSubitem(cSublink);
						++y;
					}
					if (cName.equals("subitem.dummy")) {
						XPMBMenuItem cSubitem = null;

						String label = xrpRes.getAttributeValue(null, "label");
						if (label.startsWith("$")) {
							cSubitem = new XPMBMenuItemApp(getRootActivity().getString(
									getRootActivity().getResources().getIdentifier(
											label.substring(1), "string",
											getRootActivity().getPackageName())));
						} else {
							cSubitem = new XPMBMenuItem(label);
						}
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubitem.setIconBitmapID(cAtt);
						} else {
							cSubitem.setIconBitmapID("theme.icon|icon_onlinemanual");
						}
						cSubitem.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
						cSubitem.setWidth(pxfd(85));
						cSubitem.setHeight(pxfd(85));

						if (x != 0 && y != 0) {
							cSubitem.setLabelAlpha(0.0f);
						}
						if (x == 0 && y != 0) {
							cSubitem.setLabelAlpha(0.7f);
						}
						if (y == 0) {
							cSubitem.setMarginTop(pxfd(85));
						}
					}
					if (cName.equals("subcategory")) {

						// Subcategory Data
						XPMBMenuCategory cSubcategory = null;
						String label = xrpRes.getAttributeValue(null, "label");
						if (label.startsWith("$")) {
							cSubcategory = new XPMBMenuCategory(getRootActivity().getString(
									getRootActivity().getResources().getIdentifier(
											label.substring(1), "string",
											getRootActivity().getPackageName())));
						} else {
							cSubcategory = new XPMBMenuCategory(label);
						}
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubcategory.setIconBitmapID(cAtt);
						} else {
							cSubcategory.setIconBitmapID("theme.icon|icon_gamedata");
						}
						cSubcategory.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
						cSubcategory.setWidth(pxfd(85));
						cSubcategory.setHeight(pxfd(85));
						cSubcategory.setSubmoduleID(xrpRes.getAttributeValue(null, "module"));
						cSubcategory.setListAnimator(xrpRes.getAttributeValue(null, "anim"));
						if (x != 0 && y != 0) {
							cSubcategory.setLabelAlpha(0.0f);
						}
						if (y == 0) {
							cSubcategory.setMarginTop(pxfd(85));
						}

						cItem.addSubitem(cSubcategory);
						++y;
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = xrpRes.getName();
					if (cName.equals("category")) {
						alItems.addSubitem(cItem);
						++x;
						y = 0;
					}
					break;
				}
				eventType = xrpRes.next();
			}
			xrpRes.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		mInit = true;
		Log.v(getClass().getSimpleName(), "doInit():Finished module initialization.");
	}

	@Override
	public void drawTo(Canvas canvas) {
		if (!mInit) {
			return;
		}
		// TODO: Take in account the actual orientation of the device
		Rect rILoc = new Rect();
		int iAlpha = 255, px_x = 0, px_y = 0;

		px_x = alItems.getSubitemsPos().x;
		for (int x = 0; x < alItems.getNumSubitems(); x++) {
			XPMBMenuCategory xmi_x = (XPMBMenuCategory) alItems.getSubitem(x);

			px_x += xmi_x.getMargins().left;

			rILoc = xmi_x.getComputedLocation();
			rILoc.offsetTo(px_x, alItems.getSubitemsPos().y);
			rILoc = getScaledRect(rILoc, xmi_x.getIconScale().x, xmi_x.getIconScale().y,
					Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

			if (px_x > pxfd(-85) && px_x < getDrawingConstraints().right + pxfd(85)) {
				// Icon
				Bitmap bmIcon_h = getRootActivity().getThemeManager().getAsset(
						xmi_x.getIconBitmapID());
				if (bmIcon_h == null) {
					bmIcon_h = getRootActivity().getThemeManager().getAsset(
							"theme.icon|icon_mbox_received");
				}
				iAlpha = (int) (255 * xmi_x.getIconAlpha() * fOpacity);

				canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom, iAlpha,
						Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
				pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
				canvas.drawBitmap(bmIcon_h, null, rILoc, pParams);
				canvas.restore();
				pParams.reset();

				// Label
				String strLabel_c = xmi_x.getLabel();
				iAlpha = (int) ((255 * xmi_x.getLabelAlpha()) * fOpacity);

				pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
				pParams.setColor(Color.WHITE);
				pParams.setTextSize(pxfd(13));
				pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
				pParams.getTextBounds(strLabel_c, 0, strLabel_c.length(), rTextBounds);
				getRectFromTextBounds(rTextBounds, pParams);
				rTextBounds.offsetTo(rILoc.centerX() - (rTextBounds.width() / 2),
						(int) (rILoc.bottom + pParams.ascent()));
				canvas.saveLayerAlpha(rTextBounds.left, rTextBounds.top, rTextBounds.right,
						rTextBounds.bottom, iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
				drawText(xmi_x.getLabel(), rTextBounds, pParams, canvas);
				canvas.restore();
				pParams.reset();
			}

			if (!xmi_x.getSubitemsVisibility()) {
				px_x += xmi_x.getSize().x + xmi_x.getMargins().right;
				continue;
			}

			// Process subitems
			px_y = xmi_x.getSubitemsPos().y;
			for (int y = 0; y < xmi_x.getNumSubitems(); y++) {
				XPMBMenuItemDef xmi_y = xmi_x.getSubitem(y);

				px_y += xmi_y.getMargins().top;
				rILoc = xmi_y.getComputedLocation();
				rILoc.offsetTo(px_x, px_y);
				rILoc = getScaledRect(rILoc, xmi_y.getIconScale().x, xmi_y.getIconScale().y,
						Gravity.CENTER_VERTICAL | Gravity.LEFT);

				if (x == alItems.getSelectedSubitem() && y == xmi_x.getSelectedSubitem()) {
					gravitateRect(rILoc, rSelIconRect, Gravity.CENTER_VERTICAL | Gravity.LEFT);
					rSelIconRect.offset(rILoc.width(), 0);
				}

				if (px_y > getDrawingConstraints().top - pxfd(64)
						&& px_y < getDrawingConstraints().bottom + pxfd(64)) {
					// Draw Icon/Counter
					switch (xmi_y.getIconType()) {
					case XPMBMenuItemDef.ICON_TYPE_COUNTER:
						iAlpha = (int) ((255 * xmi_y.getIconAlpha()) * xmi_x.getSubitemsAlpha());

						pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
						pParams.setColor(Color.WHITE);
						pParams.setStyle(Style.STROKE);
						String num = String.valueOf(y + 1);
						canvas.drawRoundRect(new RectF(rILoc.left + pxfd(10), rILoc.top + pxfd(10),
								rILoc.right - pxfd(10), rILoc.bottom - pxfd(10)), pxfd(5), pxfd(5),
								pParams);
						pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
						pParams.setTextSize(pxfd(18));
						pParams.getTextBounds(num, 0, num.length(), rTextBounds);
						getRectFromTextBounds(rTextBounds, pParams);
						gravitateRect(rILoc, rTextBounds, Gravity.CENTER);
						canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom,
								iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						drawText(num, rTextBounds, pParams, canvas);
						pParams.reset();
						canvas.restore();
						break;
					case XPMBMenuItemDef.ICON_TYPE_BITMAP:
						iAlpha = (int) ((255 * xmi_y.getIconAlpha()) * xmi_x.getSubitemsAlpha());

						canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom,
								iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						Bitmap bmIcon = getRootActivity().getThemeManager().getAsset(
								xmi_y.getIconBitmapID(), "theme.icon|icon_mbox_received");
						pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
						canvas.drawBitmap(bmIcon, null, rILoc, pParams);
						pParams.reset();
						canvas.restore();
						break;
					}

					// Draw Label
					String strLabel_A = xmi_y.getLabel();
					String strLabel_B = xmi_y.getLabelB();
					iAlpha = (int) (((255 * xmi_y.getLabelAlpha()) * xmi_x.getSubitemsAlpha()) * fOpacity);
					pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
					pParams.setTextSize(pxfd(18));
					pParams.setColor(Color.WHITE);
					pParams.setTextAlign(Align.LEFT);
					pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);

					if (xmi_y.isTwoLines()) {
						// Text A
						pParams.getTextBounds(strLabel_A, 0, strLabel_A.length(), rTextBounds);
						getRectFromTextBounds(rTextBounds, pParams);
						rTextBounds.offsetTo(rILoc.right + pxfd(10),
								rILoc.centerY() - (rTextBounds.height() + pxfd(4)));
						canvas.saveLayerAlpha(rTextBounds.left, rTextBounds.top, rTextBounds.right,
								rTextBounds.bottom, iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						drawText(strLabel_A, rTextBounds, pParams, canvas);
						canvas.restore();
						// Line Separator
						int s_alpha = (int) ((255 * xmi_y.getSeparatorAlpha()) * fOpacity);
						pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.GRAY);
						canvas.saveLayerAlpha(rILoc.right + pxfd(10), rILoc.centerY() - pxfd(2),
								getDrawingConstraints().right - pxfd(2), rILoc.centerY() + pxfd(2),
								s_alpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						canvas.drawLine(rILoc.right + pxfd(10), rILoc.centerY(),
								getDrawingConstraints().right - pxfd(2), rILoc.centerY(), pParams);
						canvas.restore();
						// Text B
						pParams.getTextBounds(strLabel_B, 0, strLabel_B.length(), rTextBounds);
						getRectFromTextBounds(rTextBounds, pParams);
						rTextBounds.offsetTo(rILoc.right + pxfd(10), rILoc.centerY() + pxfd(4));
						canvas.saveLayerAlpha(rTextBounds.left, rTextBounds.top, rTextBounds.right,
								rTextBounds.bottom, iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
						drawText(strLabel_B, rTextBounds, pParams, canvas);
						canvas.restore();
					} else {
						pParams.getTextBounds(strLabel_A, 0, strLabel_A.length(), rTextBounds);
						getRectFromTextBounds(rTextBounds, pParams);
						rTextBounds.offsetTo(rILoc.right + pxfd(10),
								(int) (rILoc.centerY() - rTextBounds.height() / 2));
						canvas.saveLayerAlpha(rTextBounds.left, rTextBounds.top, rTextBounds.right,
								rTextBounds.bottom, iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						drawText(strLabel_A, rTextBounds, pParams, canvas);
						canvas.restore();
					}
					pParams.reset();
				}

				px_y += rILoc.height() + xmi_y.getMargins().bottom;
			}

			px_x += xmi_x.getSize().x + xmi_x.getMargins().right;
		}
		if (bShowSelIcon) {
			canvas.saveLayerAlpha(new RectF(rSelIconRect), (int) (255 * fSelIconAlpha),
					Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
			canvas.drawBitmap(bmSelIcon, null, rSelIconRect, pParams);
			canvas.restore();
		}
	}

	public void moveLeft() {
		if (alItems.getSelectedSubitem() == 0 || alItems.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_LEFT);
		aUIAnimator.start();

		alItems.setSelectedSubitem(alItems.getSelectedSubitem() - 1);
	}

	public void moveRight() {
		if (alItems.getSelectedSubitem() == (alItems.getNumSubitems() - 1)
				|| alItems.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_RIGHT);
		aUIAnimator.start();

		alItems.setSelectedSubitem(alItems.getSelectedSubitem() + 1);
	}

	public void moveUp() {
		XPMBMenuCategory xmc = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()));
		if (xmc.getSelectedSubitem() == 0 || xmc.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		xmc.setSelectedSubitem(xmc.getSelectedSubitem() - 1);
	}

	public void moveDown() {
		XPMBMenuCategory xmc = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()));
		if (xmc.getSelectedSubitem() == xmc.getNumSubitems() - 1 || xmc.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		xmc.setSelectedSubitem(xmc.getSelectedSubitem() + 1);
	}

	public void setOpacity(float opacity) {
		fOpacity = opacity;
	}

	public float getOpacity() {
		return fOpacity;
	}

	public void startAnim(int animType) {
		aUIAnimatorW.setAnimationType(animType);
		aUIAnimator.start();
	}

	@Override
	public void sendKeyDown(int keyCode) {
		switch (keyCode) {
		case XPMBActivity.KEYCODE_LEFT:
			firstBackPress = false;
			moveLeft();
			break;
		case XPMBActivity.KEYCODE_RIGHT:
			firstBackPress = false;
			moveRight();
			break;
		case XPMBActivity.KEYCODE_UP:
			firstBackPress = false;
			moveUp();
			break;
		case XPMBActivity.KEYCODE_DOWN:
			firstBackPress = false;
			moveDown();
			break;
		case XPMBActivity.KEYCODE_CROSS:
			firstBackPress = false;
			execSelectedItem();
			break;
		case XPMBActivity.KEYCODE_CIRCLE:
			if (!firstBackPress) {
				firstBackPress = true;
				Toast tst = Toast.makeText(getRootActivity().getWindow().getContext(),
						getRootActivity().getString(R.string.strBackKeyHint), Toast.LENGTH_SHORT);
				tst.show();
			} else {
				getRootActivity().requestActivityEnd();
			}
			break;
		case XPMBActivity.KEYCODE_TRIANGLE:
			getRootActivity().setupSideMenu(
					new XPMBSideMenuItem[] { new SMPasteItem(), new SMMoveItemUp(),
							new SMMoveItemDown() }, 7);
			getRootActivity().showSideMenu(this);
			break;
		}
	}

	@Override
	public void onFinished(Object data) {
		Modules_Base curModule = (Modules_Base) data;

		getRootActivity().lockKeys(true);
		getRootActivity().hideModule(curModule.getModuleID());
		switch (curModule.getSubmenuType()) {
		case XPMBMenuCategory.LIST_ANIM_FULL:
			startAnim(XPMBMenuModule.ANIM_SHOW_MENU_FULL);
			break;
		case XPMBMenuCategory.LIST_ANIM_HALF:
			startAnim(XPMBMenuModule.ANIM_SHOW_MENU_HALF);
			break;
		case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
			startAnim(XPMBMenuModule.ANIM_HIGHLIGHT_MENU_POS);
			break;
		}
		getRootActivity().getDrawingLayerManager().setFocusOnLayer(this);
		getMessageBus().postDelayed(new Runnable() {
			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}
		}, 250);
	}

	// TODO: Split this method (V/H).
	private void execCustItem(int index) {
		XPMBMenuItemDef xmid = ((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
				.getSubitem(index);
		if (xmid instanceof XPMBMenuCategory) {
			final XPMBMenuCategory xmc = (XPMBMenuCategory) xmid;
			final Modules_Base curModule = getRootActivity().getModule(xmc.getSubmoduleID());

			if (curModule != null) {
				getRootActivity().lockKeys(true);
				getRootActivity().setLoading(true);
				curModule.setSubmenuType(xmc.getListAnimator());
				switch (xmc.getListAnimator()) {
				case XPMBMenuCategory.LIST_ANIM_FULL:
					startAnim(XPMBMenuModule.ANIM_HIDE_MENU_FULL);
					break;
				case XPMBMenuCategory.LIST_ANIM_HALF:
					curModule.setSubitemOffsetX(pxfd(62));
					startAnim(XPMBMenuModule.ANIM_HIDE_MENU_HALF);
					break;
				case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
					curModule.setSubitemOffsetX(pxfd(122));
					startAnim(XPMBMenuModule.ANIM_HIGHLIGHT_MENU_PRE);
					break;
				}
				getMessageBus().postDelayed(new Runnable() {

					@Override
					public void run() {
						new Thread(new Runnable() {

							@Override
							public void run() {
								curModule.loadIn();
								getRootActivity().showModule(xmc.getSubmoduleID());
								getRootActivity().lockKeys(false);
								getRootActivity().setLoading(false);
							}
						}).start();
					}
				}, 251);
			} else {
				Log.e(getClass().getSimpleName(),
						"execCustItem():Module not found for ID '" + xmc.getSubmoduleID() + "'");
			}
		} else if (xmid instanceof XPMBMenuItemApp) {
			XPMBMenuItemApp xmi = (XPMBMenuItemApp) xmid;
			Intent cExInt = xmi.getIntent();
			if (getRootActivity().isActivityAvailable(cExInt)) {
				getRootActivity().lockKeys(true);
				getRootActivity().setLoading(true);
				getRootActivity().postIntentStartWait(new FinishedListener() {
					@Override
					public void onFinished(Object data) {
						getRootActivity().lockKeys(false);
						getRootActivity().setLoading(false);
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
		execCustItem(((XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem()))
				.getSelectedSubitem());
	}

	@Override
	public void sendClickEvent(Point clickedPoint) {
		for (int h = alItems.getSelectedSubitem() - 1; h <= alItems.getSelectedSubitem()
				+ (intMaxItemsOnScreenH - 2); h++) {
			if ((h >= 0) && (h < alItems.getNumSubitems())) {
				XPMBMenuItemDef xmi = alItems.getSubitem(h);
				if (xmi.getComputedLocation().contains(clickedPoint.x, clickedPoint.y)) {
					// centerOnItemH(i);
					// processItem(getContainerCategory().getSubitem(i));
					return;
				}
			}
		}
		XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(alItems.getSelectedSubitem());
		for (int v = (xmc.getSelectedSubitem() - (intMaxItemsOnScreenV / 2)); v <= (xmc
				.getSelectedSubitem() + (intMaxItemsOnScreenV / 2)); v++) {
			if ((v >= 0) && v < xmc.getNumSubitems()) {
				XPMBMenuItemDef xmi = xmc.getSubitem(v);
				if (xmi.getComputedLocation().contains(clickedPoint.x, clickedPoint.y)) {
					// centerOnItemV(i)
					execCustItem(v);
				}
			}
		}
	}

	@Override
	public void setDrawingConstraints(RectF constraints) {
		super.setDrawingConstraints(constraints);
		intMaxItemsOnScreenV = getMaxItemsOnScreen(UILayer.TYPE_VERTICAL, pxfd(85), 0, 0);
		intMaxItemsOnScreenV--;
		intMaxItemsOnScreenH = getMaxItemsOnScreen(UILayer.TYPE_HORIZONTAL, pxfd(85), 0, 0);
		Log.v(getClass().getSimpleName(),
				"setDrawingConstraints():Calculated max items on screen (vertical) are "
						+ intMaxItemsOnScreenV + ".");
		Log.v(getClass().getSimpleName(),
				"setDrawingConstraints():Calculated max items on screen (horizontal) are "
						+ intMaxItemsOnScreenH + ".");
	}
}
