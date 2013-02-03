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

import org.xmlpull.v1.XmlPullParser;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.raddstudios.xpmb.R;

@SuppressWarnings("deprecation")
public class XPMBMenu extends XPMB_Layout {

	private class XPMBMenuItem {
		private String strID = null, strIcon = null;
		private ArrayList<XPMBMenuSubitem> alSubitems = null;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;
		private int intCurSubitem = 0;

		public XPMBMenuItem(String id) {
			strID = id;
			alSubitems = new ArrayList<XPMBMenuSubitem>();
		}

		public String getID() {
			return strID;
		}

		public void setIcon(String icon) {
			strIcon = icon;
		}

		public String getIcon() {
			return strIcon;
		}

		public void addSubItem(XPMBMenuSubitem subitem) {
			alSubitems.add(subitem);
		}

		public XPMBMenuSubitem getSubItem(int index) {
			return alSubitems.get(index);
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

		public int getNumSubItems() {
			return alSubitems.size();
		}

		public void setSelectedSubItem(int subitem) {
			intCurSubitem = subitem;
		}

		public int getSelectedSubitem() {
			return intCurSubitem;
		}

		public XPMBMenuSubitem[] getSubitems() {
			return alSubitems.toArray(new XPMBMenuSubitem[0]);
		}

		public int getIndexOf(XPMBMenuSubitem value) {
			return alSubitems.indexOf(value);
		}
	}

	private class XPMBMenuSubitem {
		static final int TYPE_DUMMY = 0, TYPE_EXEC = 1, TYPE_SUBMENU = 2;

		private int intType = TYPE_DUMMY;
		private String strID = null, strExecString = null, strIcon = null,
				strSubmenu;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;

		public XPMBMenuSubitem(String id, int type) {
			intType = type;
			strID = id;
		}

		public int getType() {
			return intType;
		}

		public String getID() {
			return strID;
		}

		public void setExecString(String execString) {
			strExecString = execString;
		}

		public void setSubmenu(String submenu) {
			strSubmenu = submenu;
		}

		public String getSubmenu() {
			return strSubmenu;
		}

		public String getExecString() {
			return strExecString;
		}

		public void setIcon(String icon) {
			strIcon = icon;
		}

		public String getIcon() {
			return strIcon;
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

	private Handler hMBus = null;
	private ArrayList<XPMBMenuItem> alItems = null;
	private int cMenuItem = 0;
	private XPMB_Activity mRoot = null;
	XmlResourceParser xrpRes = null;

	public XPMBMenu(XmlResourceParser source, Handler messageBus,
			XPMB_Activity root) {
		super(root, messageBus);
		hMBus = messageBus;
		mRoot = root;
		xrpRes = source;
	}

	public void doInit() {

		try {
			int eventType = xrpRes.getEventType();
			boolean done = false;
			XPMBMenuItem cItem = null;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					alItems = new ArrayList<XPMBMenuItem>();
					break;
				case XmlResourceParser.START_TAG:
					cName = xrpRes.getName();
					if (cName.equals("item")) {
						cItem = new XPMBMenuItem(xrpRes.getAttributeValue(null,
								"id"));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cItem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
					}
					if (cName.equals("subitem")) {
						XPMBMenuSubitem cSubitem = new XPMBMenuSubitem(
								xrpRes.getAttributeValue(null, "id"),
								getSubitemTypeFromString(xrpRes
										.getAttributeValue(null, "type")));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubitem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
						cAtt = xrpRes.getAttributeValue(null, "exec");
						if (cAtt != null) {
							cSubitem.setExecString(cAtt);
						}
						cAtt = xrpRes.getAttributeValue(null, "submenu");
						if (cAtt != null) {
							cSubitem.setSubmenu(cAtt);
						}
						cItem.addSubItem(cSubitem);
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = xrpRes.getName();
					if (cName.equals("item")) {
						alItems.add(cItem);
					}
					break;
				}
				eventType = xrpRes.next();
			}
			xrpRes.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getSubitemTypeFromString(String type) {
		if (type == null) {
			return XPMBMenuSubitem.TYPE_DUMMY;
		}
		if (type.equalsIgnoreCase("exec")) {
			return XPMBMenuSubitem.TYPE_EXEC;
		}
		if (type.equalsIgnoreCase("submenu")) {
			return XPMBMenuSubitem.TYPE_SUBMENU;
		}
		return XPMBMenuSubitem.TYPE_DUMMY;
	}

	public void parseInitLayout(ViewGroup base) {
		int cId = 0xC0DE;

		int mX = 0, mY = 0;

		for (mX = 0; mX < alItems.size(); mX++) {
			// Setup Item Icon
			ImageView cItem = new ImageView(base.getContext());
			LayoutParams cItemParams = new LayoutParams(pxFromDip(80),
					pxFromDip(80), pxFromDip(56 + (80 * mX)), pxFromDip(48));
			cItem.setLayoutParams(cItemParams);
			if (mX != 0) {
				cItem.setScaleX(0.7f);
				cItem.setScaleY(0.7f);
			}
			cItem.setPivotX(pxFromDip(40));
			cItem.setPivotY(pxFromDip(80));
			cItem.setScaleType(ScaleType.CENTER_INSIDE);
			String cIcon = "drawable/" + alItems.get(mX).getIcon();
			Drawable cDrawable = base.getResources().getDrawable(
					base.getResources().getIdentifier(cIcon, null,
							base.getContext().getPackageName()));
			cItem.setImageDrawable(cDrawable);
			cItem.setId(cId);
			++cId;
			// Setup Item Label
			TextView cLabel = new TextView(base.getContext());
			LayoutParams cLabelParams = new LayoutParams(pxFromDip(90),
					pxFromDip(20), pxFromDip(56 + ((80 * mX) - 5)),
					pxFromDip(108));
			cLabel.setLayoutParams(cLabelParams);
			cLabel.setText(alItems.get(mX).getID());
			cLabel.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);
			cLabel.setTextAppearance(base.getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			if (mX > 0) {
				cLabel.setAlpha(0.0f);
			}
			cLabel.setId(cId);
			++cId;
			alItems.get(mX).setParentView(cItem);
			alItems.get(mX).setParentLabel(cLabel);
			base.addView(cItem);
			base.addView(cLabel);
			// Setup Subitems
			for (mY = 0; mY < alItems.get(mX).getNumSubItems(); mY++) {
				// Setup Subitem Icon
				ImageView cSubitem = new ImageView(base.getContext());
				LayoutParams cSubitemParams = new LayoutParams(pxFromDip(80),
						pxFromDip(80), pxFromDip(56 + (80 * mX)),
						pxFromDip(128 + (80 * mY)));
				cSubitem.setLayoutParams(cSubitemParams);
				String cSubicon = "drawable/"
						+ alItems.get(mX).getSubItem(mY).getIcon();
				Drawable cSubdrawable = base.getResources().getDrawable(
						base.getResources().getIdentifier(cSubicon, null,
								base.getContext().getPackageName()));
				cSubitem.setImageDrawable(cSubdrawable);
				if (mX > 0) {
					cSubitem.setAlpha(0.0f);
				}
				cSubitem.setId(cId);
				++cId;
				// Setup Subitem Label
				TextView cSublabel = new TextView(base.getContext());
				LayoutParams cSublabelParams = new LayoutParams(pxFromDip(320),
						pxFromDip(80), pxFromDip(152 + (80 * mX)),
						pxFromDip(128 + (80 * mY)));
				cSublabel.setLayoutParams(cSublabelParams);
				cSublabel.setText(alItems.get(mX).getSubItem(mY).getID());
				cSublabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				cSublabel.setTextAppearance(base.getContext(),
						android.R.style.TextAppearance_Medium);
				cSublabel.setShadowLayer(16, 0, 0, Color.WHITE);
				if (mX > 0 | mY > 0) {
					cSublabel.setAlpha(0.0f);
				}
				cSublabel.setId(cId);
				++cId;
				alItems.get(mX).getSubItem(mY).setParentView(cSubitem);
				alItems.get(mX).getSubItem(mY).setParentLabel(cSublabel);
				base.addView(cSubitem);
				base.addView(cSublabel);
			}
		}
	}

	@Override
	public void moveRight() {
		if (cMenuItem == (alItems.size() - 1)) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);
			ImageView iv_i = xmi.getParentView();
			TextView tv_l = xmi.getParentLabel();

			alAnims.add(ObjectAnimator.ofFloat(iv_i, "X", iv_i.getX(),
					iv_i.getX() - pxFromDip(80)));
			alAnims.add(ObjectAnimator.ofFloat(tv_l, "X", tv_l.getX(),
					tv_l.getX() - pxFromDip(80)));

			if (idx == cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleX", 1.0f, 0.7f));
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleY", 1.0f, 0.7f));
				alAnims.add(ObjectAnimator.ofFloat(tv_l, "Alpha", 1.0f, 0.0f));
			} else if (idx == (cMenuItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleX", 0.7f, 1.0f));
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleY", 0.7f, 1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tv_l, "Alpha", 0.0f, 1.0f));
			}

			for (XPMBMenuSubitem xms : xmi.getSubitems()) {
				ImageView iv_s_i = xms.getParentView();
				TextView tv_s_l = xms.getParentLabel();

				alAnims.add(ObjectAnimator.ofFloat(iv_s_i, "X", iv_s_i.getX(),
						iv_s_i.getX() - pxFromDip(80)));
				alAnims.add(ObjectAnimator.ofFloat(tv_s_l, "X", tv_s_l.getX(),
						tv_s_l.getX() - pxFromDip(80)));
				if (idx == cMenuItem) {
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(),
							"Alpha", 1.0f, 0.0f));
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(),
							"Alpha", 1.0f, 0.0f));
				} else if (idx == (cMenuItem + 1)) {
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(),
							"Alpha", 0.0f, 1.0f));
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(),
							"Alpha", 0.0f, 1.0f));

				}
			}
		}

		AnimatorSet ag_xmb_ml = new AnimatorSet();
		ag_xmb_ml.playTogether((Collection<Animator>) alAnims);
		ag_xmb_ml.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_ml.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);
		++cMenuItem;
	}

	@Override
	public void moveLeft() {
		if (cMenuItem == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);
			ImageView iv_i = xmi.getParentView();
			TextView tv_l = xmi.getParentLabel();

			alAnims.add(ObjectAnimator.ofFloat(iv_i, "X", iv_i.getX(),
					iv_i.getX() + pxFromDip(80)));
			alAnims.add(ObjectAnimator.ofFloat(tv_l, "X", tv_l.getX(),
					tv_l.getX() + pxFromDip(80)));
			if (idx == cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleX", 1.0f, 0.7f));
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleY", 1.0f, 0.7f));
				alAnims.add(ObjectAnimator.ofFloat(tv_l, "Alpha", 1.0f, 0.0f));
			} else if (idx == cMenuItem - 1) {
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleX", 0.7f, 1.0f));
				alAnims.add(ObjectAnimator.ofFloat(iv_i, "ScaleY", 0.7f, 1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tv_l, "Alpha", 0.0f, 1.0f));
			}

			for (XPMBMenuSubitem xms : xmi.getSubitems()) {
				ImageView iv_s_i = xms.getParentView();
				TextView tv_s_l = xms.getParentLabel();

				alAnims.add(ObjectAnimator.ofFloat(iv_s_i, "X", iv_s_i.getX(),
						iv_s_i.getX() + pxFromDip(80)));
				alAnims.add(ObjectAnimator.ofFloat(tv_s_l, "X", tv_s_l.getX(),
						tv_s_l.getX() + pxFromDip(80)));

				if (idx == cMenuItem) {
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(),
							"Alpha", 1.0f, 0.0f));
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(),
							"Alpha", 1.0f, 0.0f));
				} else if (idx == (cMenuItem - 1)) {
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(),
							"Alpha", 0.0f, 1.0f));
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(),
							"Alpha", 0.0f, 1.0f));

				}
			}
		}

		AnimatorSet ag_xmb_mr = new AnimatorSet();
		ag_xmb_mr.playTogether((Collection<Animator>) alAnims);
		ag_xmb_mr.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_mr.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);
		--cMenuItem;
	}

	@Override
	public void moveDown() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == (alItems.get(
				cMenuItem).getNumSubItems() - 1)) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBMenuSubitem xms : alItems.get(cMenuItem).getSubitems()) {
			int idx = alItems.get(cMenuItem).getIndexOf(xms);
			ImageView iv_s_i = xms.getParentView();
			TextView tv_s_l = xms.getParentLabel();

			if (idx == alItems.get(cMenuItem).getSelectedSubitem()) {
				alAnims.add(ObjectAnimator.ofFloat(iv_s_i, "Y", iv_s_i.getY(),
						iv_s_i.getY() - pxFromDip(160)));
				alAnims.add(ObjectAnimator.ofFloat(tv_s_l, "Y", tv_s_l.getY(),
						tv_s_l.getY() - pxFromDip(160)));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_s_i, "Y", iv_s_i.getY(),
						iv_s_i.getY() - pxFromDip(80)));
				alAnims.add(ObjectAnimator.ofFloat(tv_s_l, "Y", tv_s_l.getY(),
						tv_s_l.getY() - pxFromDip(80)));
			}
		}

		AnimatorSet ag_xmb_mu = new AnimatorSet();
		ag_xmb_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_mu.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_mu.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		alItems.get(cMenuItem).setSelectedSubItem(
				alItems.get(cMenuItem).getSelectedSubitem() + 1);
	}

	@Override
	public void moveUp() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBMenuSubitem xms : alItems.get(cMenuItem).getSubitems()) {
			int idx = alItems.get(cMenuItem).getIndexOf(xms);
			ImageView iv_s_i = xms.getParentView();
			TextView tv_s_l = xms.getParentLabel();

			if (idx == alItems.get(cMenuItem).getSelectedSubitem() - 1) {
				alAnims.add(ObjectAnimator.ofFloat(iv_s_i, "Y", iv_s_i.getY(),
						iv_s_i.getY() + pxFromDip(160)));
				alAnims.add(ObjectAnimator.ofFloat(tv_s_l, "Y", tv_s_l.getY(),
						tv_s_l.getY() + pxFromDip(160)));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_s_i, "Y", iv_s_i.getY(),
						iv_s_i.getY() + pxFromDip(80)));
				alAnims.add(ObjectAnimator.ofFloat(tv_s_l, "Y", tv_s_l.getY(),
						tv_s_l.getY() + pxFromDip(80)));
			}
		}

		AnimatorSet ag_xmb_md = new AnimatorSet();
		ag_xmb_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_md.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_md.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		alItems.get(cMenuItem).setSelectedSubItem(
				alItems.get(cMenuItem).getSelectedSubitem() - 1);
	}

	@Override
	public void execSelectedItem() {
		final XPMBMenuSubitem selItem = alItems.get(cMenuItem).getSubItem(
				alItems.get(cMenuItem).getSelectedSubitem());
		switch (selItem.getType()) {
		case XPMBMenuSubitem.TYPE_EXEC:
			Intent int_ex = new Intent("android.intent.action.MAIN");
			int_ex.setComponent(ComponentName.unflattenFromString(selItem
					.getExecString()));
			mRoot.startActivity(int_ex);
			break;
		case XPMBMenuSubitem.TYPE_SUBMENU:
			doPreExecute();
			hMBus.postDelayed(new Runnable() {

				@Override
				public void run() {
					mRoot.preloadSubmenu(selItem.getSubmenu());
				}

			}, 160);
			break;
		}
	}

	private void doPreExecute() {
		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);
			ImageView cItem = xmi.getParentView();
			TextView cLabel = xmi.getParentLabel();

			alAnims.add(ObjectAnimator.ofFloat(cItem, "X", cItem.getX(),
					(cItem.getX() - pxFromDip(96))));
			alAnims.add(ObjectAnimator.ofFloat(cLabel, "X", cLabel.getX(),
					(cLabel.getX() - pxFromDip(96))));
			if (idx != cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(cItem, "Alpha", 1.0f, 0.0f));
			}
			if (idx == cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(cLabel, "Alpha", 1.0f, 0.0f));
				for (XPMBMenuSubitem xms : xmi.getSubitems()) {
					int idx_s = xmi.getIndexOf(xms);
					ImageView cSubItem = xms.getParentView();
					TextView cSubLabel = xms.getParentLabel();

					alAnims.add(ObjectAnimator.ofFloat(cSubItem, "X",
							cSubItem.getX(), (cSubItem.getX() - pxFromDip(96))));
					alAnims.add(ObjectAnimator.ofFloat(cSubLabel, "X",
							cSubLabel.getX(),
							(cSubLabel.getX() - pxFromDip(96))));
					alAnims.add(ObjectAnimator.ofFloat(cSubLabel, "Alpha",
							1.0f, 0.0f));

					if (idx_s == xmi.getSelectedSubitem()) {
						alAnims.add(ObjectAnimator.ofFloat(cSubItem, "Alpha",
								1.0f, 0.0f));
					} else {
						alAnims.add(ObjectAnimator.ofFloat(cSubItem, "Alpha",
								1.0f, 0.6f));
					}
				}
			}
		}

		alAnims.add(ObjectAnimator.ofFloat(
				mRoot.findViewById(R.id.ivSubmenuShown), "Alpha", 0.0f, 1.0f));

		AnimatorSet as_ef_p = new AnimatorSet();
		as_ef_p.playTogether((Collection<Animator>) alAnims);
		as_ef_p.setDuration(150);
		as_ef_p.start();
	}

	@Override
	public void postExecuteFinished() {
		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);
			ImageView cItem = xmi.getParentView();
			TextView cLabel = xmi.getParentLabel();

			alAnims.add(ObjectAnimator.ofFloat(cItem, "X", cItem.getX(),
					(cItem.getX() + pxFromDip(96))));
			alAnims.add(ObjectAnimator.ofFloat(cLabel, "X", cLabel.getX(),
					(cLabel.getX() + pxFromDip(96))));
			if (idx != cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(cItem, "Alpha", 0.0f, 1.0f));
			}
			if (idx == cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(cLabel, "Alpha", 0.0f, 1.0f));
				for (XPMBMenuSubitem xms : xmi.getSubitems()) {
					int idx_s = xmi.getIndexOf(xms);
					ImageView cSubItem = xms.getParentView();
					TextView cSubLabel = xms.getParentLabel();

					alAnims.add(ObjectAnimator.ofFloat(cSubItem, "X",
							cSubItem.getX(), (cSubItem.getX() + pxFromDip(96))));
					alAnims.add(ObjectAnimator.ofFloat(cSubLabel, "X",
							cSubLabel.getX(),
							(cSubLabel.getX() + pxFromDip(96))));
					alAnims.add(ObjectAnimator.ofFloat(cSubLabel, "Alpha",
							0.0f, 1.0f));

					if (idx_s == xmi.getSelectedSubitem()) {
						alAnims.add(ObjectAnimator.ofFloat(cSubItem, "Alpha",
								0.0f, 1.0f));
					} else {
						alAnims.add(ObjectAnimator.ofFloat(cSubItem, "Alpha",
								0.6f, 1.0f));
					}
				}
			}
		}

		alAnims.add(ObjectAnimator.ofFloat(
				mRoot.findViewById(R.id.ivSubmenuShown), "Alpha", 1.0f, 0.0f));

		AnimatorSet as_ef_p = new AnimatorSet();
		as_ef_p.playTogether((Collection<Animator>) alAnims);
		as_ef_p.setDuration(150);
		mRoot.lockKeys(true);
		as_ef_p.start();

		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);
	}

	@Override
	public void doCleanup(ViewGroup base) {
		for (XPMBMenuItem xmi : alItems) {
			base.removeView(xmi.getParentView());
			base.removeView(xmi.getParentLabel());
			for (Object xms : xmi.getSubitems()) {
				base.removeView(((XPMBMenuSubitem) xms).getParentView());
				base.removeView(((XPMBMenuSubitem) xms).getParentLabel());
			}
		}
	}
}
