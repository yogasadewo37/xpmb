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
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.raddstudios.xpmb.R;

@SuppressWarnings("deprecation")
public class XPMBSubmenu_APP extends XPMB_Layout {

	class XPMBSubmenuItem_APP {

		private Drawable drAppIcon = null;
		private String strAppName = null;
		private Intent intAppIntent = null;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;

		public XPMBSubmenuItem_APP(String appName, Drawable appIcon,
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

	private ArrayList<XPMBSubmenuItem_APP> alItems = null;
	private XPMB_Activity mRoot = null;
	Handler hMessageBus = null;
	private int intSelItem = 0;
	private TextView tv_no_app = null;

	public XPMBSubmenu_APP(XPMB_Activity root, Handler messageBus) {
		super(root, messageBus);
		mRoot = root;
		hMessageBus = messageBus;

		alItems = new ArrayList<XPMBSubmenuItem_APP>();
	}

	public void doInit() {
		PackageManager pm = mRoot.getPackageManager();
		Intent filter = new Intent(Intent.ACTION_MAIN);
		filter.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> ri = pm.queryIntentActivities(filter,
				PackageManager.GET_META_DATA);
		for (ResolveInfo r : ri) {
			alItems.add(new XPMBSubmenuItem_APP(r.loadLabel(pm).toString(), r
					.loadIcon(pm), pm
					.getLaunchIntentForPackage(r.activityInfo.packageName)));
		}
	}

	@Override
	public void parseInitLayout(ViewGroup base) {
		int cId = 0xC0DE;

		if (alItems.size() == 0) {
			tv_no_app = new TextView(base.getContext());
			LayoutParams lp_ng = new LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(100), pxFromDip(48), pxFromDip(128));
			tv_no_app.setLayoutParams(lp_ng);
			tv_no_app.setText(mRoot.getText(R.string.strNoApps));
			tv_no_app.setTextColor(Color.WHITE);
			tv_no_app.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_app.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_app.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			base.addView(tv_no_app);
			return;
		}
		
		for (XPMBSubmenuItem_APP xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView cItem = new ImageView(base.getContext());
			TextView cLabel = new TextView(base.getContext());

			// Setup Icon
			cItem.setImageDrawable(xsi.getAppIcon());
			cItem.setPivotX(0.0f);
			cItem.setPivotY(0.0f);
			if (idx == 0) {
				cItem.setScaleX(2.56f);
				cItem.setScaleY(2.56f);
				LayoutParams cItemParams = new LayoutParams(
						(int) pxFromDip(50), (int) pxFromDip(50),
						pxFromDip(48), pxFromDip(104));
				cItem.setLayoutParams(cItemParams);
			} else {
				LayoutParams cItemParams = new LayoutParams(
						(int) pxFromDip(50), (int) pxFromDip(50),
						pxFromDip(48), pxFromDip(248 + (50 * (idx - 1))));
				cItem.setLayoutParams(cItemParams);
			}
			cItem.setId(cId);
			++cId;
			// Setup Label
			cLabel.setText(xsi.getAppName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

			if (idx == 0) {
				LayoutParams cLabelParams = new LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(128),
						pxFromDip(184), pxFromDip(104));
				cLabel.setLayoutParams(cLabelParams);
			} else {
				LayoutParams cLabelParams = new LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(128),
						pxFromDip(184),
						pxFromDip((248 + (50 * (idx - 1)) - 39)));
				cLabel.setLayoutParams(cLabelParams);
				cLabel.setAlpha(0.0f);
			}
			cLabel.setId(cId);
			++cId;

			xsi.setParentView(cItem);
			xsi.setParentLabel(cLabel);
			base.addView(cItem);
			base.addView(cLabel);
		}
	}

	@Override
	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBSubmenuItem_APP xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView iv_c_i = xsi.getParentView();
			TextView tv_c_l = xsi.getParentLabel();

			if (idx == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 1.0f, 0.0f));
			} else if (idx == (intSelItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(144))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 0.0f, 1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_mu = new AnimatorSet();
		ag_xmb_sm_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_mu.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_sm_mu.start();
		hMessageBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	@Override
	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBSubmenuItem_APP xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView iv_c_i = xsi.getParentView();
			TextView tv_c_l = xsi.getParentLabel();

			if (idx == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(144))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 2.56f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 1.0f, 0.0f));
			} else if (idx == (intSelItem - 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(66))));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleX", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "ScaleY", 1.0f,
						2.56f));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(105))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 0.0f, 1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(50))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(50))));
			}
		}

		AnimatorSet ag_xmb_sm_md = new AnimatorSet();
		ag_xmb_sm_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_md.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_sm_md.start();
		hMessageBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	@Override
	public void execSelectedItem() {
		mRoot.startActivity(alItems.get(intSelItem).getAppIntent());
	}

	public void doCleanup(ViewGroup base) {
		if (alItems.size() == 0 && tv_no_app != null) {
			base.removeView(tv_no_app);
			return;
		}
		for (XPMBSubmenuItem_APP xig : alItems) {
			base.removeView(xig.getParentView());
			base.removeView(xig.getParentLabel());
		}
	}
}
