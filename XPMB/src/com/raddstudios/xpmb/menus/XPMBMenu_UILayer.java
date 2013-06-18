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
import android.util.Log;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.UILayer;
import com.raddstudios.xpmb.utils.XPMB_Layout;

public class XPMBMenu_UILayer extends UILayer {

	private ArrayList<XPMBMenuCategory> alItems = null;
	private int intSelItem = 0;
	private float fOpacity = 1.0f;
	private boolean mInit = false;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect(), tS = new Rect(), cP = new Rect();

	public static final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_MENUITEM = 2, ANIM_CENTER_ON_SUBMENUITEM = 3, ANIM_HIDE_MENU_HALF = 4,
			ANIM_HIDE_MENU_FULL = 5, ANIM_MENU_MOVE_LEFT = 6, ANIM_MENU_MOVE_RIGHT = 7,
			ANIM_SHOW_MENU_HALF = 8, ANIM_SHOW_MENU_FULL = 9, ANIM_HIGHLIGHT_MENU_PRE = 10,
			ANIM_HIGHLIGHT_MENU_POS = 11;

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		private int intAnimType = -1, pInitPosY = 0, intAnimItem = -1, intNextItem = -1;
		private Point[][] apInitValues = null;
		// private int[] iaParams = null;
		private ValueAnimator mOwner = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		// public void setParams(int[] params) {
		// iaParams = params;
		// }

		private void setInitialValues() {
			apInitValues = new Point[alItems.size()][];
			for (int i = 0; i < alItems.size(); i++) {
				apInitValues[i] = new Point[alItems.get(i).getNumSubItems() + 1];
				apInitValues[i][0] = alItems.get(i).getPosition();
				for (int j = 0; j < alItems.get(i).getNumSubItems(); j++) {
					apInitValues[i][j + 1] = alItems.get(i).getSubitem(j).getPosition();
				}
			}
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;
			setInitialValues();

			switch (type) {
			case ANIM_MENU_MOVE_LEFT:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				intNextItem = intAnimItem - 1;
				alItems.get(intNextItem).setSubitemsVisibility(true);
				break;
			case ANIM_MENU_MOVE_RIGHT:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				intNextItem = intAnimItem + 1;
				alItems.get(intNextItem).setSubitemsVisibility(true);
				break;
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				intAnimItem = alItems.get(intSelItem).getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				pInitPosY = alItems.get(intSelItem).getSubitemsPos().y;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = alItems.get(intSelItem).getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				pInitPosY = alItems.get(intSelItem).getSubitemsPos().y;
				break;
			case ANIM_HIDE_MENU_HALF:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				break;
			case ANIM_SHOW_MENU_HALF:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				break;
			case ANIM_HIGHLIGHT_MENU_PRE:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				break;
			case ANIM_HIGHLIGHT_MENU_POS:
				mOwner.setDuration(250);
				intAnimItem = intSelItem;
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			int dispA = 0, marginA = 0, marginB = 0;
			float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f, scaleA = 0.0f, scaleB = 0.0f;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_RIGHT:
			case ANIM_MENU_MOVE_LEFT:
				if (intAnimType == ANIM_MENU_MOVE_RIGHT) {
					dispA = (int) (pxfd(-85) * completion);
				} else {
					dispA = (int) (pxfd(85) * completion);
				}
				scaleA = 1.0f - (0.27f * completion);
				scaleB = 0.7f + (0.27f * completion);
				alphaA = 1.0f - (0.5f * completion);
				alphaB = 0.5f + (0.5f * completion);
				alphaC = 1.0f - completion;
				alphaD = completion;

				for (int x = 0; x < alItems.size(); x++) {
					alItems.get(x).setPositionX(apInitValues[x][0].x + dispA);
					if (x == intAnimItem || x == intNextItem) {
						if (x == intAnimItem) {
							alItems.get(x).setIconScale(new PointF(scaleA, scaleA));
							alItems.get(x).setIconAlpha(alphaA);
							alItems.get(x).setLabelAlpha(alphaC);
							alItems.get(x).setSubitemsAlpha(alphaC);
						}
						if (x == intNextItem) {
							alItems.get(x).setIconScale(new PointF(scaleB, scaleB));
							alItems.get(x).setIconAlpha(alphaB);
							alItems.get(x).setLabelAlpha(alphaD);
							alItems.get(x).setSubitemsAlpha(alphaD);
						}
						alItems.get(x).setSubitemsPosX(alItems.get(x).getPosition().x);
					}
				}
				if (completion == 1.0f) {
					alItems.get(intAnimItem).setSubitemsVisibility(false);
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

				alItems.get(intSelItem).setSubitemsPosY(pInitPosY + dispA);
				for (int y = 0; y < alItems.get(intSelItem).getNumSubItems(); y++) {
					XPMBMenuItemDef xmid = alItems.get(intSelItem).getSubitem(y);

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
				} else {
					dispA = (int) (pxfd(74) * completion);
					alphaA = completion;
					alphaB = 0.5f + (0.5f * completion);
					alphaC = 0.7f * completion;
				}

				for (int x = 0; x < alItems.size(); x++) {
					alItems.get(x).setPositionX(apInitValues[x][0].x + dispA);
					alItems.get(x).setSubitemsPosX(alItems.get(x).getPosition().x);
					if (x == intAnimItem) {
						for (int y = 0; y < alItems.get(x).getNumSubItems(); y++) {
							if (y == alItems.get(x).getSelectedSubitem()) {
								alItems.get(x).getSubitem(y).setLabelAlpha(alphaA);
							} else {
								alItems.get(x).getSubitem(y).setIconAlpha(alphaB);
							}
						}
					} else {
						alItems.get(x).setIconAlpha(alphaC);
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

				} else {
					dispA = (int) (176 * completion);
					alphaA = completion;
					alphaB = 0.5f + (0.5f * completion);
					alphaC = 0.7f * completion;
				}

				for (int x = 0; x < alItems.size(); x++) {
					alItems.get(x).setPositionX(apInitValues[x][0].x + dispA);
					alItems.get(x).setSubitemsPosX(alItems.get(x).getPosition().x);
					if (x == intAnimItem) {
						alItems.get(x).setLabelAlpha(alphaA);
						for (int y = 0; y < alItems.get(x).getNumSubItems(); y++) {
							if (y == alItems.get(x).getSelectedSubitem()) {
								alItems.get(x).getSubitem(y).setLabelAlpha(alphaA);
							} else {
								alItems.get(x).getSubitem(y).setIconAlpha(alphaB);
							}
						}
					} else {
						alItems.get(x).setIconAlpha(alphaC);
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
			apInitValues = null;
			// iaParams = null;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			apInitValues = null;
			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
				alItems.get(intSelItem).setSubitemsPosY(
						pInitPosY + ((intNextItem - intAnimItem) * pxfd(-85)));
			}
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}
	};

	public XPMBMenu_UILayer(XPMB_Layout root) {
		super(root.getRootActivity());

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
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
					alItems = new ArrayList<XPMBMenuCategory>();
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
						cItem.setPosition(new Point(pxfd(74) + (pxfd(85) * x), pxfd(42)));
						cItem.setSubitemsPosX(cItem.getPosition().x);
						cItem.setSubitemsPosY(pxfd(42));
						if (x == 0) {
							cItem.setSubitemsVisibility(true);
						} else {
							cItem.setIconScale(new PointF(0.7f, 0.7f));
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
						cSubcategory.setSubmoduleIDFromString(xrpRes.getAttributeValue(null,
								"filter"));
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
						alItems.add(cItem);
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
		int iAlpha = 255;

		for (int x = 0; x < alItems.size(); x++) {
			XPMBMenuCategory xmi_x = alItems.get(x);

			rILoc = getScaledRect(xmi_x.getComputedLocation(), xmi_x.getIconScale().x,
					xmi_x.getIconScale().y, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

			// Icon
			Bitmap bmIcon_h = (Bitmap) getRootActivity().getStorage().getObject(
					XPMB_Main.GRAPH_ASSETS_COL_KEY, xmi_x.getIconBitmapID(),
					"theme.icon|icon_mbox_received");
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
			rTextBounds.offsetTo(rILoc.centerX() - rTextBounds.centerX(), rILoc.bottom);
			rTextBounds.offset(0, (int) pParams.ascent());
			canvas.saveLayerAlpha(rTextBounds.left - pxfd(6), rTextBounds.top - pxfd(6),
					rTextBounds.right + pxfd(6), rTextBounds.bottom + pxfd(6), iAlpha,
					Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
			rTextBounds.offset(0, (int) (pParams.ascent() * -1));
			canvas.drawText(xmi_x.getLabel(), rTextBounds.left, rTextBounds.top, pParams);
			canvas.restore();
			pParams.reset();

			if (!xmi_x.getSubitemsVisibility()) {
				continue;
			}

			rILoc = new Rect();
			// Process subitems
			int px_y = xmi_x.getSubitemsPos().y;
			for (int y = 0; y < xmi_x.getNumSubItems(); y++) {
				XPMBMenuItemDef xmi_y = xmi_x.getSubitem(y);

				px_y += xmi_y.getMargins().top;
				rILoc = xmi_y.getComputedLocation();
				rILoc.offsetTo(xmi_x.getSubitemsPos().x, px_y);
				rILoc = getScaledRect(rILoc, xmi_y.getIconScale().x, xmi_y.getIconScale().y,
						Gravity.CENTER_VERTICAL | Gravity.LEFT);

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
						Bitmap bmIcon = (Bitmap) getRootActivity().getStorage().getObject(
								XPMB_Main.GRAPH_ASSETS_COL_KEY, xmi_y.getIconBitmapID(),
								"theme.icon|icon_mbox_received");
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
								(int) (rILoc.centerY() + pParams.descent()));
						canvas.drawText(strLabel_A, rTextBounds.left, rTextBounds.top, pParams);
						canvas.restore();
					}
					pParams.reset();
				}

				px_y += rILoc.height() + xmi_y.getMargins().bottom;
			}
		}
	}

	public void moveLeft() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_LEFT);
		aUIAnimator.start();

		--intSelItem;
	}

	public void moveRight() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_RIGHT);
		aUIAnimator.start();

		++intSelItem;
	}

	public void moveUp() {
		if (alItems.get(intSelItem).getSelectedSubitem() == 0
				|| alItems.get(intSelItem).getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		alItems.get(intSelItem)
				.setSelectedSubItem(alItems.get(intSelItem).getSelectedSubitem() - 1);
	}

	public void moveDown() {
		if (alItems.get(intSelItem).getSelectedSubitem() == alItems.get(intSelItem)
				.getNumSubItems() - 1 || alItems.get(intSelItem).getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		alItems.get(intSelItem)
				.setSelectedSubItem(alItems.get(intSelItem).getSelectedSubitem() + 1);
	}

	public ArrayList<XPMBMenuCategory> getItems() {
		return alItems;
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
}
