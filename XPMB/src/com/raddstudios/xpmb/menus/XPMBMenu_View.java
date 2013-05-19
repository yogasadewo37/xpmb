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
import java.util.Hashtable;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.XPMB_UI.UILayer;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Layout;

public class XPMBMenu_View extends XPMB_Layout implements UILayer {

	private XmlResourceParser xrpRes = null;
	private ArrayList<XPMBMenuCategory> alItems = null;
	private Hashtable<String, Bitmap> hGraphAssets = null;
	private int intSelItem = 0;
	private float fOpacity = 1.0f;
	private Modules_Base mbChildModule = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	public static final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_MENUITEM = 2, ANIM_CENTER_ON_SUBMENUITEM = 3, ANIM_HIDE_MENU_HALF = 4,
			ANIM_HIDE_MENU_FULL = 5, ANIM_MENU_MOVE_LEFT = 6, ANIM_MENU_MOVE_RIGHT = 7,
			ANIM_SHOW_MENU_HALF = 8, ANIM_SHOW_MENU_FULL = 9, ANIM_HIGHLIGHT_MENU_PRE = 10,
			ANIM_HIGHLIGHT_MENU_POS = 11;

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
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
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = alItems.get(intSelItem).getSelectedSubitem();
				intNextItem = intAnimItem + 1;
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

			int dispA = 0, dispB = 0;
			float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f, scaleA = 0.0f, scaleB = 0.0f;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_RIGHT:
			case ANIM_MENU_MOVE_LEFT:
				if (intAnimType == ANIM_MENU_MOVE_RIGHT) {
					dispA = (int) (-128 * completion);
				} else {
					dispA = (int) (128 * completion);
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
						for (int y = 0; y < alItems.get(x).getNumSubItems(); y++) {
							alItems.get(x).getSubitem(y)
									.setPositionX(alItems.get(x).getPosition().x);
						}
					}
				}
				if (completion == 1.0f) {
					alItems.get(intAnimItem).setSubitemsVisibility(false);
				}
				break;
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:

				if (intAnimType == ANIM_MENU_MOVE_UP) {
					dispA = (int) (96 * completion);
					dispB = (int) (224 * completion);
					alphaA = 1.0f - completion;
					alphaB = completion;
				} else {
					dispA = (int) (-96 * completion);
					dispB = (int) (-224 * completion);
					alphaA = completion;
					alphaB = 1.0f - completion;
				}

				for (int y = 0; y < alItems.get(intSelItem).getNumSubItems(); y++) {
					XPMBMenuItemDef xmid = alItems.get(intSelItem).getSubitem(y);

					if (intAnimType == ANIM_MENU_MOVE_UP) {
						if (y == intNextItem) {
							xmid.setPositionY(apInitValues[intSelItem][y + 1].y + dispB);
							xmid.setLabelAlpha(alphaB);
						} else {
							if (y == intAnimItem) {
								xmid.setLabelAlpha(alphaA);
							}
							xmid.setPositionY(apInitValues[intSelItem][y + 1].y + dispA);
						}
					} else {
						if (y == intAnimItem) {
							xmid.setPositionY(apInitValues[intSelItem][y + 1].y + dispB);
							xmid.setLabelAlpha(alphaB);
						} else {
							if (y == intNextItem) {
								xmid.setLabelAlpha(alphaA);
							}
							xmid.setPositionY(apInitValues[intSelItem][y + 1].y + dispA);
						}
					}
				}
				break;
			case ANIM_HIGHLIGHT_MENU_PRE:
			case ANIM_HIGHLIGHT_MENU_POS:
				if (intAnimType == ANIM_HIGHLIGHT_MENU_PRE) {
					dispA = (int) (-112 * completion);
					alphaA = 1.0f - completion;
					alphaB = 1.0f - (0.5f * completion);
					alphaC = 0.7f - (0.7f * completion);
				} else {
					dispA = (int) (112 * completion);
					alphaA = completion;
					alphaB = 0.5f + (0.5f * completion);
					alphaC = 0.7f * completion;
				}

				for (int x = 0; x < alItems.size(); x++) {
					alItems.get(x).setPositionX(apInitValues[x][0].x + dispA);
					if (x == intAnimItem) {
						for (int y = 0; y < alItems.get(x).getNumSubItems(); y++) {
							if (y == alItems.get(x).getSelectedSubitem()) {
								alItems.get(x).getSubitem(y)
										.setPositionX(apInitValues[x][y + 1].x + dispA);
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
					if (x == intAnimItem) {
						alItems.get(x).setLabelAlpha(alphaA);
						for (int y = 0; y < alItems.get(x).getNumSubItems(); y++) {
							alItems.get(x).getSubitem(y)
									.setPositionX(apInitValues[x][y + 1].x + dispA);
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
			// requestRedraw();
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
			apInitValues = null;
			// iaParams = null;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			apInitValues = null;
			// iaParams = null;
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}
	};

	@SuppressWarnings("unchecked")
	public XPMBMenu_View(Context context, XmlResourceParser source, XPMB_Activity root) {
		super(root);
		xrpRes = source;
		hGraphAssets = (Hashtable<String, Bitmap>) root.getStorage().getCollection(
				XPMB_Main.GRAPH_ASSETS_COL_KEY);

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
	}

	public void doInit() {
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
							cItem.setIcon(cAtt);
						} else {
							cItem.setIcon("theme.icon|icon_gamedata");
						}

						// Positioning Data
						cItem.setPosition(new Point(112 + (128 * x), 64));
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
							cSubitem.setIcon(cAtt);
						} else {
							cItem.setIcon("theme.icon|icon_onlinemanual");
						}
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

						// Positioning Data
						cSubitem.setPosition(new Point(112 + (128 * x), 192 + (96 * y)));
						cSubitem.setIconScale(new PointF(0.75f, 0.75f));
						if (x != 0 && y != 0) {
							cSubitem.setLabelAlpha(0.0f);
						}
						if (x == 0 && y != 0) {
							cSubitem.setLabelAlpha(0.7f);
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
							cSubcategory.setIcon(cAtt);
						} else {
							cItem.setIcon("theme.icon|icon_gamedata");
						}
						cSubcategory.setItemFilter(xrpRes.getAttributeValue(null, "filter"));
						cSubcategory.setListAnimator(xrpRes.getAttributeValue(null, "anim"));

						// Positioning Data
						cSubcategory.setPosition(new Point(112 + (128 * x), 192 + (96 * y)));
						cSubcategory.setIconScale(new PointF(0.75f, 0.75f));
						if (x != 0 && y != 0) {
							cSubcategory.setLabelAlpha(0.0f);
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

		Log.v(getClass().getSimpleName(), "doInit():Finished module initialization.");
	}

	private boolean drawing = false;
	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect();
	private int px_c_i = 0, py_c_i = 0, px_c_l = 0, py_c_l = 0, px_i_i = 0, py_i_i = 0, px_i_l = 0,
			py_i_l = 0, textH = 0, textW = 0;

	private Rect getAlignedAndScaledRect(int left, int top, int width, int height, float scaleX,
			float scaleY, int gravity) {
		int sizeX = (int) (width * scaleX);
		int sizeY = (int) (height * scaleY);
		Rect in = new Rect(left, top, left + width, top + height);
		Rect out = new Rect(0, 0, 0, 0);

		Gravity.apply(gravity, sizeX, sizeY, in, out);

		return out;
	}

	@Override
	public void drawTo(Canvas canvas) {
		// TODO: Take in account the actual orientation of the device

		drawing = true;
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

		for (int x = 0; x < alItems.size(); x++) {
			XPMBMenuItemDef xmi_x = alItems.get(x);

			// Setup icon
			String strIcon_c = xmi_x.getIcon();
			float alpha_c_i = (255 * xmi_x.getIconAlpha()), scale_x_c = xmi_x.getIconScale().x, scale_y_c = xmi_x
					.getIconScale().y;
			int size_x_c = hGraphAssets.get(strIcon_c).getWidth(), size_y_c = hGraphAssets.get(
					strIcon_c).getHeight();
			pParams.setAlpha((int) (alpha_c_i * fOpacity));
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			// Draw Icon
			px_c_i = xmi_x.getPosition().x;
			py_c_i = xmi_x.getPosition().y;
			canvas.drawBitmap(
					hGraphAssets.get(strIcon_c),
					null,
					getAlignedAndScaledRect(px_c_i, py_c_i, size_x_c, size_y_c, scale_x_c,
							scale_y_c, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM), pParams);
			pParams.reset();

			// Setup Label
			String strLabel_c = xmi_x.getLabel();
			float alpha_c_l = (255 * xmi_x.getLabelAlpha());
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			pParams.setColor(Color.WHITE);
			pParams.setAlpha((int) (alpha_c_l * fOpacity));
			pParams.setTextSize(20);
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
				pParams.setShadowLayer(4, 0, 0, Color.WHITE);
			}
			pParams.getTextBounds(strLabel_c, 0, strLabel_c.length(), rTextBounds);
			textW = rTextBounds.right - rTextBounds.left;
			textH = (int) (Math.abs(pParams.getFontMetrics().ascent) + Math.abs(pParams
					.getFontMetrics().bottom));
			// Draw Label
			px_c_l = px_c_i + (64 - (textW / 2));
			py_c_l = py_c_i + 128;
			canvas.drawText(strLabel_c, px_c_l, py_c_l, pParams);
			pParams.reset();

			if (!xmi_x.getSubitemsVisibility()) {
				continue;
			}
			// Process subitems
			for (int y = 0; y < ((XPMBMenuCategory) xmi_x).getNumSubItems(); y++) {
				XPMBMenuItemDef xmi_y = ((XPMBMenuCategory) xmi_x).getSubitem(y);

				// Setup Icon
				String strIcon_i = xmi_y.getIcon();
				float alpha_i_i = (255 * xmi_y.getIconAlpha()) * xmi_x.getSubitemsAlpha(), scale_x_i = xmi_y
						.getIconScale().x, scale_y_i = xmi_y.getIconScale().y;
				int size_x_i = hGraphAssets.get(strIcon_i).getWidth(), size_y_i = hGraphAssets.get(
						strIcon_i).getHeight();
				pParams.setAlpha((int) (alpha_i_i * fOpacity));
				pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
				// Draw Icon
				px_i_i = xmi_y.getPosition().x;
				py_i_i = xmi_y.getPosition().y;
				canvas.drawBitmap(
						hGraphAssets.get(strIcon_i),
						null,
						getAlignedAndScaledRect(px_i_i, py_i_i, size_x_i, size_y_i, scale_x_i,
								scale_y_i, Gravity.CENTER), pParams);
				pParams.reset();

				// Setup Label
				String strLabel_i = xmi_y.getLabel();
				float alpha_i_l = (255 * xmi_y.getLabelAlpha()) * xmi_x.getSubitemsAlpha();
				pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
				pParams.setTextSize(24);
				pParams.setColor(Color.WHITE);
				pParams.setAlpha((int) (alpha_i_l * fOpacity));
				pParams.setTextAlign(Align.LEFT);
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
					pParams.setShadowLayer(4, 0, 0, Color.WHITE);
				}
				pParams.getTextBounds(strLabel_i, 0, strLabel_i.length(), rTextBounds);
				textW = rTextBounds.right - rTextBounds.left;
				textH = (int) (Math.abs(pParams.getFontMetrics().ascent) + Math.abs(pParams
						.getFontMetrics().bottom));
				// Draw Label
				px_i_l = px_i_i + 128;
				py_i_l = (int) (py_i_i + (64 + (textH / 2) - pParams.getFontMetrics().descent));
				canvas.drawText(strLabel_i, px_i_l, py_i_l, pParams);
				pParams.reset();
			}
		}
		if (mbChildModule != null) {
			mbChildModule.drawTo(canvas);
		}
		drawing = false;
	}

	public void setChildModule(Modules_Base child) {
		mbChildModule = child;
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
