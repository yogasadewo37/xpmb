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

package com.raddstudios.xpmb.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Submenu_App;

public class XPMBSubmenu_App {

	class XPMBSubmenuItem_App {

		private Drawable drAppIcon = null;
		private String strAppName = null;
		private Intent intAppIntent = null;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;

		public XPMBSubmenuItem_App(String appName, Drawable appIcon,
				Intent appIntent) {
			strAppName = appName;
			drAppIcon = appIcon;
			intAppIntent = appIntent;
		}

		public String getAppName() {
			return strAppName;
		}

		public Drawable getAppIcon() {
			return drAppIcon;
		}

		public Intent getAppIntent() {
			return intAppIntent;
		}

		public void setParentView(ImageView parent) {
			ivParentView = parent;
		}

		public ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(TextView label) {
			tvParentLabel = label;
		}

		public TextView getParentLabel() {
			return tvParentLabel;
		}
	}

	private ArrayList<XPMBSubmenuItem_App> alItems = null;
	private XPMB_Submenu_App mRoot = null;
	private Handler hMBus = null;
	private int intSelItem = 0;

	public XPMBSubmenu_App(Handler messageBus, XPMB_Submenu_App root) {
		mRoot = root;
		hMBus = messageBus;

		alItems = new ArrayList<XPMBSubmenuItem_App>();
	}

	public void doInit() {
		PackageManager pm = mRoot.getPackageManager();
		Intent filter = new Intent(Intent.ACTION_MAIN);
		filter.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> ri = pm.queryIntentActivities(filter,
				PackageManager.GET_META_DATA);
		for (ResolveInfo r : ri) {
			alItems.add(new XPMBSubmenuItem_App(r.loadLabel(pm).toString(), r
					.loadIcon(pm), pm
					.getLaunchIntentForPackage(r.activityInfo.packageName)));
		}
	}

	private float pxFromDip(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				mRoot.getResources().getDisplayMetrics());
	}

	public void parseInitLayout(ViewGroup base) {
		if (alItems.size() == 0) {
			LayoutParams cItemParams = new LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(100));
			TextView cItem = new TextView(base.getContext());
			cItem.setText(mRoot.getText(R.string.strNoApps));
			cItem.setTextColor(Color.WHITE);
			cItem.setShadowLayer(16, 0, 0, Color.WHITE);
			cItem.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cItem.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cItem.setX(pxFromDip(48));
			cItem.setY(pxFromDip(110));
			base.addView(cItem, cItemParams);
			return;
		}

		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {
			// Setup Icon
			LayoutParams cItemParams = new LayoutParams((int) pxFromDip(50),
					(int) pxFromDip(50));
			ImageView cItem = new ImageView(base.getContext());
			cItem.setImageDrawable(alItems.get(mY).getAppIcon());
			cItem.setX(pxFromDip(48));
			cItem.setPivotX(0.0f);
			cItem.setPivotY(0.0f);
			if (mY == 0) {
				cItem.setY(pxFromDip(110));
				cItem.setScaleX(2.0f);
				cItem.setScaleY(2.0f);
			} else {
				cItem.setY(pxFromDip(226 + (50 * (mY - 1))));
			}
			// Setup Label
			LayoutParams cLabelParams = new LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(100));
			TextView cLabel = new TextView(base.getContext());
			cLabel.setText(alItems.get(mY).getAppName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cLabel.setX(pxFromDip(164));
			if (mY == 0) {
				cLabel.setY(pxFromDip(110));
			} else {
				cLabel.setY(pxFromDip(226 + (50 * (mY - 1))));
				cLabel.setAlpha(0.0f);
			}
			alItems.get(mY).setParentView(cItem);
			alItems.get(mY).setParentLabel(cLabel);
			base.addView(cItem, cItemParams);
			base.addView(cLabel, cLabelParams);
		}
	}

	public void moveToNextItem() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {

			ImageView ivCurItem = alItems.get(mY).getParentView();
			TextView tvCurLabel = alItems.get(mY).getParentLabel();
			float cY = ivCurItem.getY(), cY_l = tvCurLabel.getY();

			if (mY == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY - pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l - pxFromDip(91))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 1.0f,
						0.0f));
			} else if (mY == (intSelItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY - pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l - pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 0.0f,
						1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY - pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l - pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_mu = new AnimatorSet();
		ag_xmb_sm_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_mu.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_sm_mu.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	public void moveToPrevItem() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mY = 0;

		for (mY = 0; mY < alItems.size(); mY++) {

			ImageView ivCurItem = alItems.get(mY).getParentView();
			TextView tvCurLabel = alItems.get(mY).getParentLabel();
			float cY = ivCurItem.getY(), cY_l = tvCurLabel.getY();

			if (mY == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY + pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 2.0f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l + pxFromDip(116))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 1.0f,
						0.0f));
			} else if (mY == (intSelItem - 1)) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY + pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 1.0f,
						2.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l + pxFromDip(91))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 0.0f,
						1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "Y", cY,
						(cY + pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Y", cY_l,
						(cY_l + pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_md = new AnimatorSet();
		ag_xmb_sm_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_md.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_sm_md.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	public void runSelectedItem() {
		mRoot.startActivity(alItems.get(intSelItem).getAppIntent());
	}
}
