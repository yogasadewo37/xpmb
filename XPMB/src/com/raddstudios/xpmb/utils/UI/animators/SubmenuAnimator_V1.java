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

package com.raddstudios.xpmb.utils.UI.animators;

import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.menus.modules.Modules_Base.ListAnimator;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.UI.UILayer;

public class SubmenuAnimator_V1 extends ValueAnimator implements AnimatorUpdateListener,
		AnimatorListener, ListAnimator {

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_MENU_CENTER_ON_ITEM = 2;

	//Common Vars
	private int pInitPosY = 0, intAnimType = -1, intAnimItem = -1, intNextItem = -1;
	private UILayer mOwner = null;
	private XPMBMenuCategory mList = null;

	//Animator Vars
	private int dispA = 0, marginA = 0, marginB = 0;
	private float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f;

	public SubmenuAnimator_V1(XPMBMenuCategory list, UILayer owner) {
		super();
		super.setFloatValues(0.0f, 1.0f);
		super.setInterpolator(new DecelerateInterpolator());
		super.addListener(this);
		super.addUpdateListener(this);
		mOwner = owner;
		mList = list;
	}

	public void setNextItem(int nextItem) {
		if (nextItem == mList.getSelectedSubitem()) {
			intAnimType = ANIM_NONE;
			return;
		}
		if (super.isStarted()) {
			super.end();
		}
		if (nextItem == mList.getSelectedSubitem() + 1) {
			intAnimType = ANIM_MENU_MOVE_DOWN;
		} else if (nextItem == mList.getSelectedSubitem() - 1) {
			intAnimType = ANIM_MENU_MOVE_UP;
		} else {
			intAnimType = ANIM_MENU_CENTER_ON_ITEM;
		}
		pInitPosY = mList.getSubitemsPos().y;

		switch (intAnimType) {
		case ANIM_MENU_MOVE_UP:
			super.setDuration(250);
			intAnimItem = mList.getSelectedSubitem();
			intNextItem = nextItem;
			break;
		case ANIM_MENU_MOVE_DOWN:
			super.setDuration(250);
			intAnimItem = mList.getSelectedSubitem();
			intNextItem = nextItem;
			break;
		case ANIM_MENU_CENTER_ON_ITEM:
			super.setDuration(250);
			intAnimItem = mList.getSelectedSubitem();
			intNextItem = nextItem;
			break;
		}
	}

	@Override
	public void onAnimationUpdate(ValueAnimator arg0) {
		float completion = (Float) arg0.getAnimatedValue();

		switch (intAnimType) {
		case ANIM_MENU_MOVE_UP:
		case ANIM_MENU_MOVE_DOWN:
		case ANIM_MENU_CENTER_ON_ITEM:

			dispA = (int) (((intNextItem - intAnimItem) * mOwner.pxfd(-64)) * completion);
			alphaA = 1.0f - completion;
			alphaB = completion;
			alphaC = 1.0f - (0.5f * completion);
			alphaD = 0.5f + (0.5f * completion);
			marginA = (int) (mOwner.pxfd(16) * completion);
			marginB = (mOwner.pxfd(16) - marginA);
			mList.setSubitemsPosY(pInitPosY + dispA);

			for (int y = 0; y < mList.getNumSubitems(); y++) {
				XPMBMenuItemDef xmid = mList.getSubitem(y);

				if (y == intAnimItem) {
					xmid.setSeparatorAlpha(alphaA);
					xmid.setLabelAlpha(alphaC);
					xmid.setMarginTop(marginB);
					xmid.setMarginBottom(marginB);
				} else if (y == intNextItem) {
					xmid.setSeparatorAlpha(alphaB);
					xmid.setLabelAlpha(alphaD);
					xmid.setMarginTop(marginA);
					xmid.setMarginBottom(marginA);
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
		case ANIM_MENU_CENTER_ON_ITEM:
			mList.setSubitemsPosY(pInitPosY + ((intNextItem - intAnimItem) * mOwner.pxfd(-64)));
		}
	}

	@Override
	public void onAnimationRepeat(Animator arg0) {
	}

	@Override
	public void onAnimationStart(Animator arg0) {
	}

	public void initializeItems() {
		if (mList.getSelectedSubitem() == -1){
			mList.setSelectedSubitem(0);
		}
		mList.setSubitemsPosY(mOwner.pxfd(122) - (mOwner.pxfd(64) * mList.getSelectedSubitem()));
		for (int i = 0; i < mList.getNumSubitems(); i++) {
			XPMBMenuItemDef xmi = mList.getSubitem(i);
			if (i == mList.getSelectedSubitem()) {
				xmi.setMarginTop(mOwner.pxfd(16));
				xmi.setMarginBottom(mOwner.pxfd(16));
			} else {
				xmi.setSeparatorAlpha(0.0f);
				xmi.setLabelAlpha(0.5f);
			}
		}
	}
	
	@Override
	public void resetContainer(XPMBMenuCategory container){
		mList = container;
	}
	
	@Override
	public void start(){
		super.start();
	}
};
