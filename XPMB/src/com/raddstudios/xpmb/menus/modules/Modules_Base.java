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
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBMenu_UILayer;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.UILayer;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Activity.ObjectCollections;

public class Modules_Base extends UILayer {

	public class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		public static final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
				ANIM_MENU_CENTER_ON_ITEM = 2, ANIM_STARTPLAYING = 3, ANIM_STOPPLAYING = 4;

		private int pInitPosY = 0, intAnimType = -1, intAnimItem = -1, intNextItem = -1;
		private int[] iaParams = null;
		private float fScaling = 0.0f;
		private boolean bUseScaling = false;
		private Point[] apInitValues = null;
		private ValueAnimator mOwner = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setParams(int[] params) {
			iaParams = params;
		}

		public void setIconScaling(boolean useScaling, float scaling) {
			bUseScaling = useScaling;
			if (useScaling) {
				fScaling = scaling;
			} else {
				fScaling = 1.0f;
			}
		}

		private void setInitialValues() {
			apInitValues = new Point[mContainer.getNumSubItems()];
			for (int i = 0; i < mContainer.getNumSubItems(); i++) {
				apInitValues[i] = mContainer.getSubitem(i).getPosition();
			}
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;
			pInitPosY = mContainer.getSubitemsPos().y;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				intAnimItem = mContainer.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = mContainer.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				break;
			case ANIM_MENU_CENTER_ON_ITEM:
				mOwner.setDuration(250);
				intAnimItem = mContainer.getSelectedSubitem();
				intNextItem = iaParams[0];
				break;
			}
			setInitialValues();
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			int dispA = 0, marginA = 0, marginB = 0;
			float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f, scaleA = 1.0f, scaleB = 1.0f;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_MENU_CENTER_ON_ITEM:
				dispA = (int) (((intNextItem - intAnimItem) * pxfd(-64)) * completion);

				marginA = (int) (pxfd(16) * completion);
				marginB = (pxfd(16) - marginA);
				alphaA = 1.0f - completion;
				alphaB = completion;
				alphaC = 1.0f - (0.5f * completion);
				alphaD = 0.5f + (0.5f * completion);
				scaleA = 1.0f + ((fScaling - 1.0f) * completion);
				scaleB = fScaling - ((fScaling - 1.0f) * completion);
				mContainer.setSubitemsPosY(pInitPosY + dispA);

				for (int y = 0; y < mContainer.getNumSubItems(); y++) {
					XPMBMenuItemDef xmid = mContainer.getSubitem(y);

					if (y == intAnimItem) {
						xmid.setSeparatorAlpha(alphaA);
						xmid.setLabelAlpha(alphaC);
						xmid.setMarginTop(marginB);
						xmid.setMarginBottom(marginB);
						if (bUseScaling) {
							xmid.setIconScaleX(scaleB);
							xmid.setIconScaleY(scaleB);
						}
					} else if (y == intNextItem) {
						xmid.setSeparatorAlpha(alphaB);
						xmid.setLabelAlpha(alphaD);
						xmid.setMarginTop(marginA);
						xmid.setMarginBottom(marginA);
						if (bUseScaling) {
							xmid.setIconScaleX(scaleA);
							xmid.setIconScaleY(scaleA);
						}
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
			iaParams = null;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			apInitValues = null;
			iaParams = null;
			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_MENU_CENTER_ON_ITEM:
				mContainer.setSubitemsPosY(pInitPosY + ((intNextItem - intAnimItem) * -pxfd(64)));
			}
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}
	};

	private ObjectCollections mStor = null;
	private XPMBMenu_UILayer mLayer = null;
	private XPMBMenuCategory mContainer = null;
	private FinishedListener mListener = null;
	private int intAnimator = 0;

	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect(), tS = new Rect(), cP = new Rect(), rILoc = new Rect();

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	public Modules_Base(XPMB_Activity rootActivity) {
		super(rootActivity);
		mStor = getRootActivity().getStorage();
		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
	}

	public void initialize(XPMBMenu_UILayer parentLayer, XPMBMenuCategory container,
			FinishedListener listener) {
		mLayer = parentLayer;
		mContainer = container;
		mListener = listener;
		switch (container.getListAnimator()) {
		case XPMBMenuCategory.LIST_ANIM_HALF:
			mContainer.setSubitemsPosX(pxfd(53));
			mContainer.setSubitemsPosY(pxfd(122));
			break;
		case XPMBMenuCategory.LIST_ANIM_HIGHLIGHT:
			mContainer.setSubitemsPosX(pxfd(122));
			mContainer.setSubitemsPosY(pxfd(122));
			break;
		}
	}

	public void deInitialize() {
	};

	protected XPMBMenuCategory getContainerCategory() {
		return mContainer;
	}

	protected ObjectCollections getStorage() {
		return mStor;
	}

	protected FinishedListener getFinishedListener() {
		return mListener;
	}

	public void drawTo(Canvas canvas) {
		// TODO: Take in account the actual orientation of the device
		int px_y = mContainer.getSubitemsPos().y, iAlpha = 255;

		// Process subitems
		for (int y = 0; y < mContainer.getNumSubItems(); y++) {
			XPMBMenuItemDef xmi_y = mContainer.getSubitem(y);

			px_y += xmi_y.getMargins().top;
			rILoc = xmi_y.getComputedLocation();
			rILoc.offsetTo(mContainer.getSubitemsPos().x, px_y);
			rILoc = getScaledRect(rILoc, xmi_y.getIconScale().x, xmi_y.getIconScale().y,
					Gravity.CENTER_VERTICAL | Gravity.LEFT);

			if (px_y > getDrawingConstraints().top - pxfd(64)
					&& px_y < getDrawingConstraints().bottom + pxfd(64)) {
				// Draw Icon/Counter
				switch (xmi_y.getIconType()) {
				case XPMBMenuItemDef.ICON_TYPE_COUNTER:
					iAlpha = (int) ((255 * xmi_y.getIconAlpha()) * mContainer.getSubitemsAlpha());

					canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom, iAlpha,
							Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
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
					canvas.drawText(num, cP.left, cP.top + pxfd(18) - pParams.descent(), pParams);
					pParams.reset();
					canvas.restore();
					break;
				case XPMBMenuItemDef.ICON_TYPE_BITMAP:
					iAlpha = (int) ((255 * xmi_y.getIconAlpha()) * mContainer.getSubitemsAlpha());

					canvas.saveLayerAlpha(rILoc.left, rILoc.top, rILoc.right, rILoc.bottom, iAlpha,
							Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
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
				iAlpha = (int) (((255 * xmi_y.getLabelAlpha()) * mContainer.getSubitemsAlpha()) * mLayer
						.getOpacity());
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
					rTextBounds.offsetTo(rILoc.right + pxfd(10),
							(int) (rILoc.centerY() - pxfd(2) - pParams.descent()));
					canvas.drawText(strLabel_A, rTextBounds.left, rTextBounds.top, pParams);
					canvas.restore();
					// Line Separator
					int s_alpha = (int) ((255 * xmi_y.getSeparatorAlpha()) * mLayer.getOpacity());
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
					rTextBounds.offsetTo(rILoc.right + pxfd(10),
							(int) (rILoc.centerY() + pxfd(2) - pParams.ascent()));
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

			px_y += (xmi_y.getSize().y * xmi_y.getIconScale().y) + xmi_y.getMargins().bottom;
		}
	}

	protected UIAnimatorWorker getAnimatorWorker() {
		return aUIAnimatorW;
	}

	protected ValueAnimator getAnimator() {
		return aUIAnimator;
	}

	public void loadIn() {
	};

	public void processItem(XPMBMenuItem item) {
	};

	public void processkeyUp(int keyCode) {
	};

	public void processkeyDown(int keyCode) {
	};

	public void setListAnimator(int animator) {
		intAnimator = animator;
	};

	public int getListAnimator() {
		return intAnimator;
	};

	public boolean isInitialized() {
		return false;
	};
}
