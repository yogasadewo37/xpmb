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

import java.util.ArrayList;

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
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_GBA;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_NES;
import com.raddstudios.xpmb.menus.modules.media.Module_Media_Music;
import com.raddstudios.xpmb.menus.modules.system.Module_System_Apps;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.XPMB_Layout;

public class XPMBMenuModule extends XPMB_Layout implements FinishedListener {

	private XPMBMenuCategory alItems = null;
	private int intSelItem = 0, intMaxItemsOnScreenH = 0, intMaxItemsOnScreenV = 0;
	private float fOpacity = 1.0f, fSelIconAlpha = 0.0f;
	private boolean mInit = false, firstBackPress = false, bLockedKeyPad = false,
			bShowSelIcon = false;
	private ExecItemThread rExecItem = null;
	private Modules_Base curModule = null;
	private ArrayList<Modules_Base> alModules = null;

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
				intAnimItem = intSelItem;
				intNextItem = intAnimItem - 1;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_MENU_MOVE_RIGHT:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				intNextItem = intAnimItem + 1;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				intAnimItem = ((XPMBMenuCategory) alItems.getSubitem(intSelItem))
						.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				pInitPosY = ((XPMBMenuCategory) alItems.getSubitem(intSelItem)).getSubitemsPos().y;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = ((XPMBMenuCategory) alItems.getSubitem(intSelItem))
						.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				pInitPosY = ((XPMBMenuCategory) alItems.getSubitem(intSelItem)).getSubitemsPos().y;
				break;
			case ANIM_HIDE_MENU_HALF:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_SHOW_MENU_HALF:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_HIGHLIGHT_MENU_PRE:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				pInitPosX = alItems.getSubitemsPos().x;
				break;
			case ANIM_HIGHLIGHT_MENU_POS:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
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

				XPMBMenuCategory axmc = (XPMBMenuCategory) alItems.getSubitem(intSelItem);

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
				((XPMBMenuCategory) alItems.getSubitem(intSelItem)).setSubitemsPosY(pInitPosY
						+ ((intNextItem - intAnimItem) * pxfd(-85)));
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

	public XPMBMenuModule(XPMB_Activity root) {
		super(root);

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
		rExecItem = new ExecItemThread(this);
		rTextBounds = new Rect();
		tS = new Rect();
		cP = new Rect();
		alModules = new ArrayList<Modules_Base>();
	}

	@Override
	public void doInit() {
		Log.v(getClass().getSimpleName(), "doInit():Initializing XPMB Main Menu (XMB type) Module.");
		doInit(getRootActivity().getResources().getXml(R.xml.xmb_layout));
		getRootActivity().getDrawingLayerManager().addLayer(this);
		bmSelIcon = flipBitmap((Bitmap) getRootActivity().getThemeManager().getAsset(
				"theme.icon|ui_backicon"));
		rSelIconRect = new Rect(0, 0, bmSelIcon.getWidth(), bmSelIcon.getHeight());
		Log.v(getClass().getSimpleName(),
				"doInit():Finished XPMB Main Menu (XMB type) Module initialization.");
	}

	public void doInit(XmlResourceParser xrpRes) {
		Log.v(getClass().getSimpleName(), "doInit():Start module initialization.");
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
						cItem = new XPMBMenuCategory(xrpRes.getAttributeValue(null, "label"));
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
					if (cName.equals("subitem")) {

						// Item Data
						XPMBMenuItem cSubitem = new XPMBMenuItem(xrpRes.getAttributeValue(null,
								"label"));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubitem.setIconBitmapID(cAtt);
						} else {
							cSubitem.setIconBitmapID("theme.icon|icon_onlinemanual");
						}
						cSubitem.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
						cSubitem.setWidth(pxfd(85));
						cSubitem.setHeight(pxfd(85));
						String cAct = xrpRes.getAttributeValue(null, "action"), cComp = xrpRes
								.getAttributeValue(null, "exec");
						if (cAct == null) {
							cAct = Intent.ACTION_MAIN;
						}
						Intent cIntent = new Intent(cAct);
						if (cComp != null) {
							cIntent.setComponent(ComponentName.unflattenFromString(cComp));
						}
						cSubitem.setData(cIntent);
						if (x != 0 && y != 0) {
							cSubitem.setLabelAlpha(0.0f);
						}
						if (x == 0 && y != 0) {
							cSubitem.setLabelAlpha(0.7f);
						}
						if (y == 0) {
							cSubitem.setMarginTop(pxfd(85));
						}

						cItem.addSubitem(cSubitem);
						++y;
					}
					if (cName.equals("subcategory")) {

						// Subcategory Data
						XPMBMenuCategory cSubcategory = new XPMBMenuCategory(
								xrpRes.getAttributeValue(null, "label"));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubcategory.setIconBitmapID(cAtt);
						} else {
							cSubcategory.setIconBitmapID("theme.icon|icon_gamedata");
						}
						cSubcategory.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
						cSubcategory.setWidth(pxfd(85));
						cSubcategory.setHeight(pxfd(85));
						cSubcategory.setSubmoduleID(xrpRes.getAttributeValue(null,
								"module"));
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

				pParams.setAlpha(iAlpha);
				pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
				canvas.drawBitmap(bmIcon_h, null, rILoc, pParams);
				pParams.reset();

				// Label
				String strLabel_c = xmi_x.getLabel();
				iAlpha = (int) ((255 * xmi_x.getLabelAlpha()) * fOpacity);
				pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
				pParams.setColor(Color.WHITE);
				pParams.setTextSize(pxfd(13));
				pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
				pParams.getTextBounds(strLabel_c, 0, strLabel_c.length(), rTextBounds);
				rTextBounds = getBoundsFromTextRect(rTextBounds);
				rTextBounds.offsetTo(rILoc.centerX() - rTextBounds.centerX(),
						(int) (rILoc.bottom + (pParams.ascent() / 2)));
				rTextBounds.offset(0, (int) pParams.ascent());
				canvas.saveLayerAlpha(rTextBounds.left - pxfd(6), rTextBounds.top - pxfd(6),
						rTextBounds.right + pxfd(6), rTextBounds.bottom + pxfd(6), iAlpha,
						Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
				rTextBounds.offset(0, (int) (pParams.ascent() * -1));
				canvas.drawText(xmi_x.getLabel(), rTextBounds.left, rTextBounds.top, pParams);
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

				if (x == intSelItem && y == xmi_x.getSelectedSubitem()) {
					centerRect(rILoc, rSelIconRect, Gravity.CENTER_VERTICAL | Gravity.LEFT);
					rSelIconRect.offset(rILoc.width(), 0);
				}

				if (px_y > getDrawingConstraints().top - pxfd(64)
						&& px_y < getDrawingConstraints().bottom + pxfd(64)) {
					// Draw Icon/Counter
					switch (xmi_y.getIconType()) {
					case XPMBMenuItemDef.ICON_TYPE_COUNTER:
						iAlpha = (int) (((255 * xmi_y.getIconAlpha()) * xmi_x.getSubitemsAlpha()) * fOpacity);

						canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom,
								iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
						pParams.setColor(Color.WHITE);
						pParams.setStyle(Style.STROKE);
						String num = String.valueOf(y + 1);
						canvas.drawRoundRect(new RectF(rILoc.left + pxfd(10), rILoc.top + pxfd(10),
								rILoc.right - pxfd(10), rILoc.bottom - pxfd(10)), pxfd(5), pxfd(5),
								pParams);
						pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
						pParams.setTextSize(pxfd(18));
						pParams.getTextBounds(num, 0, num.length(), tS);
						Gravity.apply(Gravity.CENTER, tS.width(), tS.height(), rILoc, cP);
						canvas.drawText(num, cP.left, cP.top + pxfd(18) - pParams.descent(),
								pParams);
						pParams.reset();
						canvas.restore();
						break;
					case XPMBMenuItemDef.ICON_TYPE_BITMAP:
						iAlpha = (int) (((255 * xmi_y.getIconAlpha()) * xmi_x.getSubitemsAlpha()) * fOpacity);

						canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom,
								iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						Bitmap bmIcon = getRootActivity().getThemeManager().getAsset(
								xmi_y.getIconBitmapID());
						if (bmIcon == null) {
							bmIcon = getRootActivity().getThemeManager().getAsset(
									"theme.icon|icon_mbox_received");
						}
						pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
						canvas.drawBitmap(bmIcon, null, rILoc, pParams);
						pParams.reset();
						canvas.restore();
						break;
					}

					// Draw Label
					String strLabel_A = xmi_y.getLabel();
					String strLabel_B = xmi_y.getLabelB();
					iAlpha = (int) (((255 * xmi_y.getLabelAlpha()) * xmi_x.getSubitemsAlpha()) * getOpacity());
					canvas.saveLayerAlpha(rILoc.right, rILoc.top, getDrawingConstraints().right,
							rILoc.bottom, iAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
					pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
					pParams.setTextSize(pxfd(18));
					pParams.setColor(Color.WHITE);
					pParams.setTextAlign(Align.LEFT);
					pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
					pParams.getTextBounds(strLabel_A, 0, strLabel_A.length(), rTextBounds);
					rTextBounds = getBoundsFromTextRect(rTextBounds);

					if (xmi_y.isTwoLines()) {
						// Text A
						rTextBounds.offsetTo(rILoc.right + pxfd(10), (int) (rILoc.centerY()
								- pxfd(2) - pParams.descent()));
						canvas.drawText(strLabel_A, rTextBounds.left, rTextBounds.top, pParams);
						canvas.restore();
						// Line Separator
						int s_alpha = (int) ((255 * xmi_y.getSeparatorAlpha()) * getOpacity());
						canvas.saveLayerAlpha(rILoc.right + pxfd(10), rILoc.top,
								getDrawingConstraints().right, rILoc.bottom, s_alpha,
								Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.GRAY);
						canvas.drawLine(rILoc.right + pxfd(10), rILoc.centerY() - pxfd(2),
								getDrawingConstraints().right - pxfd(2), rILoc.centerY() - pxfd(2),
								pParams);
						canvas.restore();
						// Text B
						canvas.saveLayerAlpha(rILoc.right + pxfd(10), rILoc.top,
								getDrawingConstraints().right, rILoc.bottom, iAlpha,
								Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
						rTextBounds.offsetTo(rILoc.right + pxfd(10), (int) (rILoc.centerY()
								+ pxfd(2) - pParams.ascent()));
						pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
						canvas.drawText(strLabel_B, rTextBounds.left, rTextBounds.top, pParams);
						canvas.restore();
					} else {
						rTextBounds.offsetTo(rILoc.right + pxfd(10),
								(int) (rILoc.centerY() - (pParams.ascent() / 2)));
						canvas.drawText(strLabel_A, rTextBounds.left, rTextBounds.top, pParams);
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
		if (intSelItem == 0 || alItems.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_LEFT);
		aUIAnimator.start();

		--intSelItem;
	}

	public void moveRight() {
		if (intSelItem == (alItems.getNumSubitems() - 1) || alItems.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_RIGHT);
		aUIAnimator.start();

		++intSelItem;
	}

	public void moveUp() {
		XPMBMenuCategory xmc = ((XPMBMenuCategory) alItems.getSubitem(intSelItem));
		if (xmc.getSelectedSubitem() == 0 || xmc.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		xmc.setSelectedSubitem(xmc.getSelectedSubitem() - 1);
	}

	public void moveDown() {
		XPMBMenuCategory xmc = ((XPMBMenuCategory) alItems.getSubitem(intSelItem));
		if (xmc.getSelectedSubitem() == xmc.getNumSubitems() - 1 || xmc.getNumSubitems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		xmc.setSelectedSubitem(xmc.getSelectedSubitem() + 1);
	}

	public void setSelectedCategory(int category) {
		intSelItem = category;
	}

	public int getSelectedCategory() {
		return intSelItem;
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
		if (bLockedKeyPad) {
			return;
		}
		switch (keyCode) {
		case XPMB_Activity.KEYCODE_LEFT:
			firstBackPress = false;
			moveLeft();
			break;
		case XPMB_Activity.KEYCODE_RIGHT:
			firstBackPress = false;
			moveRight();
			break;
		case XPMB_Activity.KEYCODE_UP:
			firstBackPress = false;
			moveUp();
			break;
		case XPMB_Activity.KEYCODE_DOWN:
			firstBackPress = false;
			moveDown();
			break;
		case XPMB_Activity.KEYCODE_CROSS:
			firstBackPress = false;
			execSelectedItem();
			break;
		case XPMB_Activity.KEYCODE_CIRCLE:
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

	@Override
	public void onFinished(Object data) {
		bLockedKeyPad = true;
		getRootActivity().getDrawingLayerManager().removeLayer((UILayer) curModule);
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
		curModule.deInitialize();
		curModule = null;
		getMessageBus().postDelayed(new Runnable() {
			@Override
			public void run() {
				bLockedKeyPad = false;
			}
		}, 250);
	}

	private class ExecItemThread implements Runnable {
		XPMBMenuModule mOwner = null;
		XPMBMenuCategory xmc = null;

		public ExecItemThread(XPMBMenuModule owner) {
			mOwner = owner;
		}

		public void setSubcategory(XPMBMenuCategory subcategory) {
			xmc = subcategory;
		}

		@Override
		public void run() {
			if (curModule != null) {
				curModule.initialize(mOwner, xmc, mOwner);
				curModule.setSubmenuType(xmc.getListAnimator());
				curModule.loadIn();
				getRootActivity().getDrawingLayerManager().addLayer((UILayer) curModule);
				getRootActivity().getDrawingLayerManager().setFocusOnLayer(curModule);
			}
			bLockedKeyPad = false;
			((XPMBUIModule) getRootActivity().getDrawingLayerManager().getLayer(0))
					.setLoadingAnimationVisible(false);
		}
	}

	// TODO: Split this method (V/H).
	private void execCustItem(int index) {
		XPMBMenuItemDef xmid = ((XPMBMenuCategory) alItems.getSubitem(intSelItem))
				.getSubitem(index);
		if (xmid instanceof XPMBMenuCategory) {
			final XPMBMenuCategory xmc = (XPMBMenuCategory) xmid;
			
			curModule = getRootActivity().getModule(xmc.getSubmoduleID());

			if (curModule != null) {
				bLockedKeyPad = true;
				((XPMBUIModule) getRootActivity().getDrawingLayerManager().getLayer(0))
						.setLoadingAnimationVisible(true);
				switch (xmc.getListAnimator()) {
				case XPMBMenuCategory.LIST_ANIM_FULL:
					startAnim(XPMBMenuModule.ANIM_HIDE_MENU_FULL);
					break;
				case XPMBMenuCategory.LIST_ANIM_HALF:
					startAnim(XPMBMenuModule.ANIM_HIDE_MENU_HALF);
					break;
				case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
					startAnim(XPMBMenuModule.ANIM_HIGHLIGHT_MENU_PRE);
					break;
				}
				getMessageBus().postDelayed(new Runnable() {

					@Override
					public void run() {
						rExecItem.setSubcategory(xmc);
						new Thread(rExecItem).start();
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
						((XPMBUIModule) getRootActivity().getDrawingLayerManager().getLayer(0)).setLoadingAnimationVisible(false);
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
		execCustItem(((XPMBMenuCategory) alItems.getSubitem(intSelItem)).getSelectedSubitem());
	}

	@Override
	public void sendClickEvent(Point clickedPoint) {
		for (int h = intSelItem - 1; h <= intSelItem + (intMaxItemsOnScreenH - 2); h++) {
			if ((h >= 0) && (h < alItems.getNumSubitems())) {
				XPMBMenuItemDef xmi = alItems.getSubitem(h);
				if (xmi.getComputedLocation().contains(clickedPoint.x, clickedPoint.y)) {
					// centerOnItemH(i);
					// processItem(getContainerCategory().getSubitem(i));
					return;
				}
			}
		}
		XPMBMenuCategory xmc = (XPMBMenuCategory) alItems.getSubitem(intSelItem);
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
