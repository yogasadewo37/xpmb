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
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.utils.XPMBSubmenu_GBA.XPMBSubmenuItem_GBA;

@SuppressWarnings("deprecation")
public class XPMBMenu extends XPMB_Layout {

	private class XPMBMenuItem {
		private String strID = null, strIcon = null;
		private ArrayList<XPMBMenuSubitem> alSubitems = null;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;
		private RelativeLayout rlParentContainer = null;
		private TableLayout tlChildContainer = null;
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

		public void setParentLabel(TextView parent) {
			tvParentLabel = parent;
		}

		public TextView getParentLabel() {
			return tvParentLabel;
		}

		public void setParentContainer(RelativeLayout container) {
			rlParentContainer = container;
		}

		public RelativeLayout getParentContainer() {
			return rlParentContainer;
		}

		public void setChildContainer(TableLayout container) {
			tlChildContainer = container;
		}

		public TableLayout getChildContainer() {
			return tlChildContainer;
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
		private String strID = null, strExecString = null, strIcon = null, strSubmenu;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;
		private TableRow trParentContainer = null;

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

		public void setParentContainer(TableRow parent) {
			trParentContainer = parent;
		}

		public TableRow getParentContainer() {
			return trParentContainer;
		}
	}

	private Handler hMBus = null;
	private ArrayList<XPMBMenuItem> alItems = null;
	private int cMenuItem = 0;
	private XPMB_Activity mRoot = null;
	private XmlResourceParser xrpRes = null;
	private LinearLayout tlRoot = null;

	public XPMBMenu(XmlResourceParser source, Handler messageBus, XPMB_Activity root) {
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
						cItem = new XPMBMenuItem(xrpRes.getAttributeValue(null, "id"));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cItem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
					}
					if (cName.equals("subitem")) {
						XPMBMenuSubitem cSubitem = new XPMBMenuSubitem(xrpRes.getAttributeValue(
								null, "id"), getSubitemTypeFromString(xrpRes.getAttributeValue(
								null, "type")));
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

		tlRoot = new LinearLayout(base.getContext());
		tlRoot.setOrientation(LinearLayout.HORIZONTAL);
		AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(
				pxFromDip(90 * alItems.size()), pxFromDip(80), pxFromDip(56), pxFromDip(48));
		tlRoot.setLayoutParams(rootP);

		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);

			RelativeLayout cCont = new RelativeLayout(base.getContext());
			ImageView cIcon = new ImageView(base.getContext());
			TextView cLabel = new TextView(base.getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cCont.setId(getNextID());

			// Setup Item Container
			LayoutParams cContP = new LayoutParams(pxFromDip(80), pxFromDip(80));
			cCont.setLayoutParams(cContP);

			// Setup Item Icon
			RelativeLayout.LayoutParams cIconP = new RelativeLayout.LayoutParams(pxFromDip(80),
					pxFromDip(80));
			cIconP.addRule(RelativeLayout.CENTER_HORIZONTAL | RelativeLayout.ALIGN_PARENT_BOTTOM);
			cIcon.setScaleType(ScaleType.CENTER_INSIDE);
			if (idx != 0) {
				cIcon.setScaleX(0.7f);
				cIcon.setScaleY(0.7f);
			}
			cIcon.setPivotX(pxFromDip(40));
			cIcon.setPivotY(pxFromDip(80));
			cIcon.setScaleType(ScaleType.CENTER_INSIDE);
			cIcon.setImageDrawable(base.getResources().getDrawable(
					base.getResources().getIdentifier("drawable/" + xmi.getIcon(), null,
							base.getContext().getPackageName())));

			// Setup Item Label
			RelativeLayout.LayoutParams cLabelP = new RelativeLayout.LayoutParams(pxFromDip(90),
					pxFromDip(20));
			cLabelP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			cLabel.setLayoutParams(cLabelP);
			cLabel.setText(xmi.getID());
			cLabel.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			cLabel.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			if (idx > 0) {
				cLabel.setAlpha(0.0f);
			}

			// Add everything to their holder classes and containers
			cCont.addView(cIcon);
			cCont.addView(cLabel);
			xmi.setParentView(cIcon);
			xmi.setParentLabel(cLabel);
			xmi.setParentContainer(cCont);
			tlRoot.addView(cCont);

			// Setup Subitems
			if (xmi.getNumSubItems() != 0) {
				TableLayout cSubCont = new TableLayout(base.getContext());

				// Setup Subitems Root Container
				AbsoluteLayout.LayoutParams cSubContP = new AbsoluteLayout.LayoutParams(
						pxFromDip(416), pxFromDip(80 + (80 * xmi.getNumSubItems())),
						pxFromDip(56 + (80 * idx)), pxFromDip(48));
				cSubCont.setLayoutParams(cSubContP);
				if (idx > 0) {
					cSubCont.setAlpha(0.0f);
				}

				for (XPMBMenuSubitem xsi : xmi.getSubitems()) {
					int idy = xmi.getIndexOf(xsi);

					TableRow cSCont = new TableRow(base.getContext());
					ImageView cSIcon = new ImageView(base.getContext());
					TextView cSLabel = new TextView(base.getContext());
					cSIcon.setId(getNextID());
					cSLabel.setId(getNextID());
					cSCont.setId(getNextID());

					// Setup Subitem Container
					TableLayout.LayoutParams cSContP = new TableLayout.LayoutParams(pxFromDip(416),
							pxFromDip(80));
					if (idy == 0) {
						cSContP.topMargin = pxFromDip(80);
					}
					cSCont.setLayoutParams(cSContP);

					// Setup Subitem Icon
					TableRow.LayoutParams cSIconP = new TableRow.LayoutParams((int) pxFromDip(80),
							(int) pxFromDip(80));
					cSIconP.column = 0;
					cSIcon.setLayoutParams(cSIconP);
					cSIcon.setImageDrawable(base.getResources().getDrawable(
							base.getResources().getIdentifier("drawable/" + xsi.getIcon(), null,
									base.getContext().getPackageName())));

					// Setup Subitem Label
					TableRow.LayoutParams cSLabelP = new TableRow.LayoutParams(pxFromDip(320),
							pxFromDip(80));
					cSIconP.column = 1;
					cSLabel.setLayoutParams(cSLabelP);
					cSLabel.setText(xsi.getID());
					cSLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					cSLabel.setTextAppearance(base.getContext(),
							android.R.style.TextAppearance_Medium);
					cSLabel.setShadowLayer(16, 0, 0, Color.WHITE);
					if (idx != 0 && idy > 0) {
						cSLabel.setAlpha(0.0f);
					} else if (idx == 0 && idy > 0) {
						cSLabel.setAlpha(0.5f);
					}

					// Add everything to their holder classes and containers
					cSCont.addView(cSIcon);
					cSCont.addView(cSLabel);
					cSubCont.addView(cSCont);
					xsi.setParentView(cSIcon);
					xsi.setParentLabel(cSLabel);
					xsi.setParentContainer(cSCont);

				}
				base.addView(cSubCont);
				xmi.setChildContainer(cSubCont);
			}
		}
		base.addView(tlRoot);
	}

	@Override
	public void moveRight() {
		if (cMenuItem == (alItems.size() - 1)) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "X", tlRoot.getX() - pxFromDip(80)));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentView(), "ScaleY", 0.7f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentView(), "ScaleX", 0.7f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentLabel(), "Alpha", 0.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getChildContainer(), "Alpha",
				0.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem + 1).getParentView(), "ScaleX",
				1.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem + 1).getParentView(), "ScaleY",
				1.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem + 1).getParentLabel(), "Alpha",
				1.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem + 1).getChildContainer(), "Alpha",
				1.0f));
		for (XPMBMenuItem xmi : alItems) {
			alAnims.add(ObjectAnimator.ofFloat(xmi.getChildContainer(), "X", xmi
					.getChildContainer().getX() - pxFromDip(80)));

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

		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "X", tlRoot.getX() + pxFromDip(80)));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentView(), "ScaleY", 0.7f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentView(), "ScaleX", 0.7f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentLabel(), "Alpha", 0.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getChildContainer(), "Alpha",
				0.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem - 1).getParentView(), "ScaleX",
				1.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem - 1).getParentView(), "ScaleY",
				1.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem - 1).getParentLabel(), "Alpha",
				1.0f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem - 1).getChildContainer(), "Alpha",
				1.0f));
		for (XPMBMenuItem xmi : alItems) {
			alAnims.add(ObjectAnimator.ofFloat(xmi.getChildContainer(), "X", xmi
					.getChildContainer().getX() + pxFromDip(80)));
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
		if (alItems.get(cMenuItem).getSelectedSubitem() == (alItems.get(cMenuItem).getNumSubItems() - 1)) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		final int intAnimItem = alItems.get(cMenuItem).getSelectedSubitem();
		if (cMenuItem > 0) {
			alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getSubItem(intAnimItem)
					.getParentLabel(), "Alpha", 0.0f));
			alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getSubItem(intAnimItem + 1)
					.getParentLabel(), "Alpha", 1.0f));
		}
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getChildContainer(), "Y", alItems
				.get(cMenuItem).getChildContainer().getY()
				- pxFromDip(80)));
		ValueAnimator va_ci_tm = ValueAnimator.ofInt(pxFromDip(80), 0);
		va_ci_tm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBMenuSubitem cSub = alItems.get(cMenuItem).getSubItem(intAnimItem);
				TableLayout.LayoutParams cSubP = (TableLayout.LayoutParams) cSub
						.getParentContainer().getLayoutParams();
				cSubP.topMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentContainer().setLayoutParams(cSubP);
			}
		});
		ValueAnimator va_ni_tm = ValueAnimator.ofInt(0, pxFromDip(80));
		va_ni_tm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBMenuSubitem cSub = alItems.get(cMenuItem).getSubItem(intAnimItem + 1);
				TableLayout.LayoutParams cSubP = (TableLayout.LayoutParams) cSub
						.getParentContainer().getLayoutParams();
				cSubP.topMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentContainer().setLayoutParams(cSubP);
			}
		});
		alAnims.add(va_ci_tm);
		alAnims.add(va_ni_tm);

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

		alItems.get(cMenuItem).setSelectedSubItem(alItems.get(cMenuItem).getSelectedSubitem() + 1);
	}

	@Override
	public void moveUp() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		final int intAnimItem = alItems.get(cMenuItem).getSelectedSubitem();
		if (cMenuItem > 0) {
			alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getSubItem(intAnimItem)
					.getParentLabel(), "Alpha", 0.0f));
			alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getSubItem(intAnimItem - 1)
					.getParentLabel(), "Alpha", 1.0f));
		}
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getChildContainer(), "Y", alItems
				.get(cMenuItem).getChildContainer().getY()
				+ pxFromDip(80)));
		ValueAnimator va_ci_tm = ValueAnimator.ofInt(pxFromDip(80), 0);
		va_ci_tm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBMenuSubitem cSub = alItems.get(cMenuItem).getSubItem(intAnimItem);
				TableLayout.LayoutParams cSubP = (TableLayout.LayoutParams) cSub
						.getParentContainer().getLayoutParams();
				cSubP.topMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentContainer().setLayoutParams(cSubP);
			}
		});
		ValueAnimator va_pi_tm = ValueAnimator.ofInt(0, pxFromDip(80));
		va_pi_tm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBMenuSubitem cSub = alItems.get(cMenuItem).getSubItem(intAnimItem - 1);
				TableLayout.LayoutParams cSubP = (TableLayout.LayoutParams) cSub
						.getParentContainer().getLayoutParams();
				cSubP.topMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentContainer().setLayoutParams(cSubP);
			}
		});
		alAnims.add(va_ci_tm);
		alAnims.add(va_pi_tm);

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

		alItems.get(cMenuItem).setSelectedSubItem(alItems.get(cMenuItem).getSelectedSubitem() - 1);
	}

	@Override
	public void execSelectedItem() {
		final XPMBMenuSubitem selItem = alItems.get(cMenuItem).getSubItem(
				alItems.get(cMenuItem).getSelectedSubitem());
		switch (selItem.getType()) {
		case XPMBMenuSubitem.TYPE_EXEC:
			Intent int_ex = new Intent("android.intent.action.MAIN");
			int_ex.setComponent(ComponentName.unflattenFromString(selItem.getExecString()));
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

		alAnims.add(ObjectAnimator.ofFloat(mRoot.findViewById(R.id.ivSubmenuShown), "Alpha", 1.0f));
		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "X", tlRoot.getX() - pxFromDip(96)));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getChildContainer(), "X", alItems
				.get(cMenuItem).getChildContainer().getX()
				- pxFromDip(96)));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentLabel(), "Alpha", 0.0f));
		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);

			if (idx != cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(xmi.getParentView(), "Alpha", 0.0f));
			}
		}
		for (XPMBMenuSubitem xms : alItems.get(cMenuItem).getSubitems()) {
			int idy = alItems.get(cMenuItem).getIndexOf(xms);

			if (idy == alItems.get(cMenuItem).getSelectedSubitem()) {
				alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(), "Alpha", 0.0f));
				alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(), "Alpha", 0.0f));

			} else {
				alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(), "Alpha", 0.5f));
				if (cMenuItem == 0) {
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(), "Alpha", 0.0f));
				}
			}
		}

		AnimatorSet as_ef_p = new AnimatorSet();
		as_ef_p.playTogether((Collection<Animator>) alAnims);
		as_ef_p.setDuration(150);
		as_ef_p.start();
	}

	@Override
	public void postExecuteFinished() {
		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		alAnims.add(ObjectAnimator.ofFloat(mRoot.findViewById(R.id.ivSubmenuShown), "Alpha", 0.0f));
		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "X", tlRoot.getX() + pxFromDip(96)));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getChildContainer(), "X", alItems
				.get(cMenuItem).getChildContainer().getX()
				+ pxFromDip(96)));
		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);

			if (idx != cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(xmi.getParentView(), "Alpha", 1.0f));
			}
		}
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(cMenuItem).getParentLabel(), "Alpha", 1.0f));
		for (XPMBMenuSubitem xms : alItems.get(cMenuItem).getSubitems()) {
			int idy = alItems.get(cMenuItem).getIndexOf(xms);

			if (idy == alItems.get(cMenuItem).getSelectedSubitem()) {
				alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(), "Alpha", 1.0f));
				alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(), "Alpha", 1.0f));

			} else {
				alAnims.add(ObjectAnimator.ofFloat(xms.getParentView(), "Alpha", 1.0f));
				if (cMenuItem == 0) {
					alAnims.add(ObjectAnimator.ofFloat(xms.getParentLabel(), "Alpha", 0.5f));
				}
			}
		}

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
			base.removeView(xmi.getChildContainer());
		}
		base.removeView(tlRoot);
	}
}
