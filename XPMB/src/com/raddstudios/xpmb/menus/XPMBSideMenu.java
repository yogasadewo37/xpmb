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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.utils.XPMB_Activity;

public class XPMBSideMenu extends Modules_Base {

	private class SideMenuAnimator extends ValueAnimator implements AnimatorUpdateListener,
			AnimatorListener {

		private final int ANIM_NONE = -1, ANIM_CENTER_ON_ITEM = 0;
		public static final int ANIM_SHOW_SIDEMENU = 1, ANIM_HIDE_SIDEMENU = 2;

		// Common Vars
		private int pInitPosY = 0, intAnimType = -1, intAnimItem = -1, intNextItem = -1;

		// Animator Vars
		private int dispA = 0;

		public SideMenuAnimator() {
			super.setFloatValues(0.0f, 1.0f);
			super.setInterpolator(new DecelerateInterpolator());
			super.addListener(this);
			super.addUpdateListener(this);
		}

		public void setAnimation(int animation) {
			if (super.isStarted()) {
				super.end();
			}
			super.setDuration(300);
			intAnimType = animation;
		}

		public void setNextItem(int nextItem) {
			if (nextItem == intSelItem) {
				intAnimType = ANIM_NONE;
				return;
			}
			if (super.isStarted()) {
				super.end();
			}
			pInitPosY = rSelection.top;

			switch (intAnimType) {
			case ANIM_CENTER_ON_ITEM:
				super.setDuration(250);
				intAnimItem = intSelItem;
				intNextItem = nextItem;
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			switch (intAnimType) {
			case ANIM_CENTER_ON_ITEM:
				dispA = (int) ((pxfd(16) * (intNextItem - intAnimItem)) * completion);
				rSelection.offsetTo(rSelection.left, pInitPosY + dispA);
				break;
			case ANIM_HIDE_SIDEMENU:
				dispA = (int) (pxfd(188) - (pxfd(188) * completion));
				fAlpha = 1.0f - completion;
				px_x = (int) (getDrawingConstraints().right - dispA);
				break;
			case ANIM_SHOW_SIDEMENU:
				dispA = (int) (pxfd(188) * completion);
				fAlpha = completion;
				px_x = (int) (getDrawingConstraints().right - dispA);
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
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}
	}

	// TODO: Finish Side Menu code

	private XPMBSideMenuItem[] alItems = null;
	private Rect rSelection = null, rCurItemD = null, rIcon = null;;
	private int px_x = 0, px_y = 0, intSzy = 0, intSelItem = 0;
	float fAlpha = 0.0f;
	private Paint pParams = new Paint();
	private Bitmap bmBG = null, bmIco = null;
	private SideMenuAnimator mAnimator = null;

	public XPMBSideMenu(XPMB_Activity root) {
		super(root);
		alItems = new XPMBSideMenuItem[15];
		rCurItemD = new Rect();
		rIcon = new Rect();
		bmBG = (Bitmap) root.getStorage().getObject(XPMB_Activity.GRAPH_ASSETS_COL_KEY,
				"theme.icon|ui_sidemenu_bg");
		mAnimator = new SideMenuAnimator();
	}

	@Override
	public void drawTo(Canvas canvas) {
		if (!isInitialized()) {
			return;
		}
		rCurItemD.set(px_x, (int) getDrawingConstraints().top, px_x + pxfd(188),
				(int) (getDrawingConstraints().top + pxfd(320)));
		rSelection.offsetTo(px_x, rSelection.top);

		pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
		canvas.drawBitmap(bmBG, null, rCurItemD, pParams);

		canvas.saveLayerAlpha(new RectF(rCurItemD), (int) (255 * fAlpha),
				Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		pParams.setTextSize(pxfd(16));
		pParams.setColor(Color.WHITE);
		pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
		for (int i = 0; i < 15; i++) {
			rCurItemD.set(px_x, px_y, (int) getDrawingConstraints().right, px_y + intSzy);
			if (alItems[i] != null) {
				if (alItems[i].getIconBitmapID() != null) {
					bmIco = (Bitmap) getRootActivity().getStorage().getObject(
							XPMB_Activity.GRAPH_ASSETS_COL_KEY, alItems[i].getIconBitmapID());
					rIcon.offsetTo(rCurItemD.left + pxfd(2), rCurItemD.top + pxfd(2));

					canvas.drawBitmap(bmIco, null, rIcon, pParams);
					canvas.drawText(alItems[i].getLabel(), rIcon.right + pxfd(2),
							rCurItemD.centerY() + (pParams.ascent() / 2), pParams);

				} else {
					canvas.drawText(alItems[i].getLabel(), px_x,
							rCurItemD.centerY() + (pParams.ascent() / 2), pParams);
				}
				if (i == intSelItem) {
				}
			}
			px_y += intSzy;
		}
		pParams.setStyle(Style.STROKE);
		pParams.setColor(Color.CYAN);
		pParams.setStrokeWidth(pxfd(2));
		canvas.drawRoundRect(new RectF(rSelection), pxfd(2), pxfd(2), pParams);
		canvas.restore();
		pParams.reset();
	}

	public void setItemSlot(int slot, XPMBSideMenuItem item) {
		alItems[slot] = item;
	}

	public void clearItemSlots() {
		for (int i = 0; i < 15; i++) {
			alItems[i] = null;
		}
	}

	public void moveUp() {
		if ((intSelItem - 1) >= 0) {
			for (int nextItem = intSelItem; nextItem >= 0; nextItem--) {
				if (alItems[nextItem] != null) {
					mAnimator.setNextItem(nextItem);
					mAnimator.start();
					intSelItem = nextItem;
					break;
				}
			}
		}
	}

	public void moveDown() {
		if ((intSelItem + 1) < 15) {
			for (int nextItem = intSelItem; nextItem < 15; nextItem++) {
				if (alItems[nextItem] != null) {
					mAnimator.setNextItem(nextItem);
					mAnimator.start();
					intSelItem = nextItem;
					break;
				}
			}
		}
	}

	public void show() {
		mAnimator.setAnimation(SideMenuAnimator.ANIM_SHOW_SIDEMENU);
		mAnimator.start();
	}

	public void hide() {
		mAnimator.setAnimation(SideMenuAnimator.ANIM_HIDE_SIDEMENU);
		mAnimator.start();
	}

	@Override
	public void sendKeyDown(int keyCode) {
		switch (keyCode) {
		case XPMB_Activity.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Activity.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Activity.KEYCODE_CROSS:
			execSelectedItem();
			break;
		case XPMB_Activity.KEYCODE_TRIANGLE:
		case XPMB_Activity.KEYCODE_CIRCLE:

			break;
		}
	}

	public void execSelectedItem() {
		alItems[intSelItem].executeAction();
	}

	@Override
	public void setDrawingConstraints(RectF constraints) {
		super.setDrawingConstraints(constraints);
		intSzy = (int) (getDrawingConstraints().height() / 15);
		px_x = (int) (constraints.right - pxfd(188));
		px_y = (int) getDrawingConstraints().top;
		rSelection = new Rect(0, 0, (int) getDrawingConstraints().right, pxfd(16));
		rIcon = new Rect(0, 0, intSzy - pxfd(4), intSzy - pxfd(4));
	}
}
