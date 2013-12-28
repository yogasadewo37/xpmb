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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;

import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.UI.UILayer;

public class Modules_Base extends UILayer {

	public interface ListAnimator {
		public void initializeItems();

		public void resetContainer(XPMBMenuCategory container);

		public void setNextItem(int nextItem);

		public void start();
	}

	private UILayer mLayer = null;
	private XPMBMenuCategory mContainer = null;
	private FinishedListener mListener = null;
	private int intMaxItemsOnScreen = 0;
	private ListAnimator mAnimator = null;

	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect(), rILoc = new Rect();

	public Modules_Base(XPMBActivity rootActivity) {
		super(rootActivity);
	}

	public void initialize(UILayer parentLayer, FinishedListener listener) {
		mLayer = parentLayer;
		mContainer = new XPMBMenuCategory("@root");
		mListener = listener;
	}

	public String getModuleID() {
		return "com.xpmb.system.dummy";
	}

	protected void saveSettings() {
	}

	protected void reloadSettings() {
	}

	protected void setContainerCategory(XPMBMenuCategory container) {
		mContainer = container;
		if (mAnimator != null) {
			mAnimator.resetContainer(container);
		}
	}

	protected XPMBMenuCategory getContainerCategory() {
		return mContainer;
	}

	protected FinishedListener getFinishedListener() {
		return mListener;
	}

	public void drawTo(Canvas canvas) {
		// TODO: Take in account the actual orientation of the device
		int px_y = mContainer.getSubitemsPos().y, iAlpha = 255;

		// Process subitems
		for (int y = 0; y < mContainer.getNumSubitems(); y++) {
			XPMBMenuItemDef xmi_y = mContainer.getSubitem(y);

			px_y += xmi_y.getMargins().top;
			rILoc = xmi_y.getComputedLocation();
			rILoc.offsetTo(mContainer.getSubitemsPos().x, px_y);
			rILoc = getScaledRect(rILoc, xmi_y.getIconScale().x, xmi_y.getIconScale().y,
					Gravity.TOP | Gravity.LEFT);

			if (px_y > getDrawingConstraints().top - pxfd(64)
					&& px_y < getDrawingConstraints().bottom + pxfd(64)) {
				// Draw Icon/Counter
				switch (xmi_y.getIconType()) {
				case XPMBMenuItemDef.ICON_TYPE_COUNTER:
					iAlpha = (int) ((255 * xmi_y.getIconAlpha()) * mContainer.getSubitemsAlpha());

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
					canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom, iAlpha,
							Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
					drawText(num, rTextBounds, pParams, canvas);
					pParams.reset();
					canvas.restore();
					break;
				case XPMBMenuItemDef.ICON_TYPE_BITMAP:
					iAlpha = (int) ((255 * xmi_y.getIconAlpha()) * mContainer.getSubitemsAlpha());

					canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom, iAlpha,
							Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
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
				iAlpha = (int) (((255 * xmi_y.getLabelAlpha()) * mContainer.getSubitemsAlpha()) * mLayer
						.getOpacity());
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
					int s_alpha = (int) ((255 * xmi_y.getSeparatorAlpha()) * mLayer.getOpacity());
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

			px_y += (xmi_y.getSize().y * xmi_y.getIconScale().y) + xmi_y.getMargins().bottom;
		}
	}

	public void setSubitemOffsetX(int offset) {
		mContainer.setSubitemsPosX(offset);
	}

	public void loadIn() {
	}

	protected void setListAnimator(ListAnimator animator) {
		mAnimator = animator;
	}

	protected ListAnimator getListAnimator() {
		return mAnimator;
	}

	public void setSubmenuType(int animator) {
		mContainer.setListAnimator(animator);
	}

	public int getSubmenuType() {
		return mContainer.getListAnimator();
	};

	public void moveUp() {
		if (mContainer.getSelectedSubitem() == 0 || mContainer.getNumSubitems() == 0) {
			return;
		}

		mAnimator.setNextItem(mContainer.getSelectedSubitem() - 1);
		mAnimator.start();

		mContainer.setSelectedSubitem(mContainer.getSelectedSubitem() - 1);
	}

	public void moveDown() {
		if (mContainer.getSelectedSubitem() == mContainer.getNumSubitems() - 1
				|| mContainer.getNumSubitems() == 0) {
			return;
		}

		mAnimator.setNextItem(mContainer.getSelectedSubitem() + 1);
		mAnimator.start();

		mContainer.setSelectedSubitem(mContainer.getSelectedSubitem() + 1);
	}

	public void centerOnItem(int index) {
		if (index < 0 || index >= mContainer.getNumSubitems()) {
			return;
		}

		mAnimator.setNextItem(index);
		mAnimator.start();

		mContainer.setSelectedSubitem(index);
	}

	public boolean isInitialized() {
		return false;
	}

	public void processItem(XPMBMenuItemDef item) {
	}

	@Override
	public void sendClickEvent(Point clickedPoint) {
		for (int i = getContainerCategory().getSelectedSubitem() - (intMaxItemsOnScreen / 2); i <= getContainerCategory()
				.getSelectedSubitem() + (intMaxItemsOnScreen / 2); i++) {
			if ((i >= 0) && (i < getContainerCategory().getNumSubitems())) {
				XPMBMenuItemDef xmi = getContainerCategory().getSubitem(i);
				if (xmi.getComputedLocation().contains(clickedPoint.x, clickedPoint.y)) {
					centerOnItem(i);
					processItem(getContainerCategory().getSubitem(i));
				}
			}
		}
	}

	@Override
	public void setDrawingConstraints(RectF constraints) {
		super.setDrawingConstraints(constraints);
		intMaxItemsOnScreen = getMaxItemsOnScreen(UILayer.TYPE_VERTICAL, pxfd(64), 0, 0);
		Log.v(getClass().getSimpleName(),
				"setDrawingConstraints():Calculated max items on screen (vertical) are "
						+ intMaxItemsOnScreen + ".");
	}
}
