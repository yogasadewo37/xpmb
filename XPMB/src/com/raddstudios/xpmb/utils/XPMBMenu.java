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

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_LinearLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_RelativeLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class XPMBMenu extends XPMB_MainMenu {

	private class XPMBMenuItem {
		private String strID = null, strIcon = null;
		private ArrayList<XPMBMenuSubitem> alSubitems = null;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		// private XPMB_RelativeLayout rlParentContainer = null;
		private XPMB_TableLayout tlChildContainer = null;
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

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		public XPMB_ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(XPMB_TextView parent) {
			tvParentLabel = parent;
		}

		public XPMB_TextView getParentLabel() {
			return tvParentLabel;
		}

		// public void setParentContainer(XPMB_RelativeLayout container) {
		// rlParentContainer = container;
		// }

		// public XPMB_RelativeLayout getParentContainer() {
		// return rlParentContainer;
		// }

		public void setChildContainer(XPMB_TableLayout container) {
			tlChildContainer = container;
		}

		public XPMB_TableLayout getChildContainer() {
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
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private XPMB_TableRow trParentContainer = null;

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

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		public XPMB_ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(XPMB_TextView label) {
			tvParentLabel = label;
		}

		public XPMB_TextView getParentLabel() {
			return tvParentLabel;
		}

		public void setParentContainer(XPMB_TableRow parent) {
			trParentContainer = parent;
		}

		public XPMB_TableRow getParentContainer() {
			return trParentContainer;
		}
	}

	private ArrayList<XPMBMenuItem> alItems = null;
	private int cMenuItem = 0;
	private XmlResourceParser xrpRes = null;
	private XPMB_LinearLayout tlRoot = null;
	private boolean firstBackPress = false;

	public XPMBMenu(XmlResourceParser source, Handler messageBus, ViewGroup rootView,
			XPMB_Activity root) {
		super(root, messageBus, rootView);
		xrpRes = source;
	}

	@Override
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

	private float motStX = 0;
	private long downTime = 0;
	private boolean isMoving = false;
	private OnTouchListener mTouchMenuListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			int action = arg1.getActionMasked();
			int pointerIndex = arg1.getActionIndex();
			int pointerId = arg1.getPointerId(pointerIndex);
			if (pointerId != 0) {
				return true;
			}

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				downTime = arg1.getEventTime();
				doCenterOnMenuItemPre();
				break;
			case MotionEvent.ACTION_MOVE:
				if (!isMoving && (arg1.getEventTime() - downTime) > 100) {
					motStX = arg1.getX(pointerId) * arg1.getXPrecision();
					isMoving = true;
					break;
				}
				if (isMoving) {
					float nX = (motStX + (arg1.getX(pointerId) * arg1.getXPrecision()));
					tlRoot.setX((tlRoot.getX() + nX) - pxFromDip(90));
					for (XPMBMenuItem xmi : alItems) {
						float nIX = xmi.getChildContainer().getX();
						xmi.getChildContainer().setX((int) ((nIX + nX) - pxFromDip(90)));
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				centerOnNearesMenutItem();
				doCenterOnMenuItemPos();
				motStX = 0;
				isMoving = false;
				break;
			}
			return true;
		}
	};

	private void doCenterOnMenuItemPre() {
		XPMBMenuItem xmi = alItems.get(cMenuItem);

		xmi.getParentView().setScaleX(0.7f);
		xmi.getParentView().setScaleY(0.7f);
		xmi.getParentLabel().setAlpha(0.0f);
		xmi.getChildContainer().setAlpha(0.0f);
	}

	private void doCenterOnMenuItemPos() {
		XPMBMenuItem xmi = alItems.get(cMenuItem);

		ObjectAnimator ao_mi_scx = ObjectAnimator.ofFloat(xmi.getParentView(), "ScaleX", 1.0f);
		ObjectAnimator ao_mi_scy = ObjectAnimator.ofFloat(xmi.getParentView(), "ScaleY", 1.0f);
		ObjectAnimator ao_mi_la = ObjectAnimator.ofFloat(xmi.getParentLabel(), "Alpha", 1.0f);
		ObjectAnimator ao_mi_cc = ObjectAnimator.ofFloat(xmi.getChildContainer(), "Alpha", 1.0f);
		AnimatorSet as_mi_comip = new AnimatorSet();
		as_mi_comip.playTogether(ao_mi_scx, ao_mi_scy, ao_mi_la, ao_mi_cc);
		as_mi_comip.setDuration(250);
		as_mi_comip.start();
	}

	private void centerOnNearesMenutItem() {
		float cPosX = tlRoot.getX();
		int destItem = ((int) (pxFromDip(56) - cPosX) / pxFromDip(90)) + 1;
		if (destItem < 0) {
			destItem = 0;
		} else if (destItem > (alItems.size() - 1)) {
			destItem = (alItems.size() - 1);
		}
		centerOnMenuItem(destItem);

	}

	private void centerOnMenuItem(int index) {

		float cPosX = tlRoot.getX();
		float destPos = pxFromDip(56) - (pxFromDip(90) * index);

		ObjectAnimator.ofFloat(tlRoot, "X", cPosX, destPos).setDuration(250).start();

		cMenuItem = index;
	}

	@Override
	public void parseInitLayout() {

		tlRoot = new XPMB_LinearLayout(getRootView().getContext());
		tlRoot.setOrientation(LinearLayout.HORIZONTAL);
		AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(
				pxFromDip(90 * alItems.size()), pxFromDip(80), pxFromDip(56), pxFromDip(48));
		tlRoot.setLayoutParams(rootP);

		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);

			XPMB_RelativeLayout cCont = new XPMB_RelativeLayout(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cCont.setId(getNextID());

			// Setup Item Container
			LinearLayout.LayoutParams cContP = new LinearLayout.LayoutParams(pxFromDip(80), pxFromDip(80));
			cCont.setLayoutParams(cContP);

			// Setup Item Icon
			RelativeLayout.LayoutParams cIconP = new RelativeLayout.LayoutParams(pxFromDip(80),
					pxFromDip(80));
			cIconP.addRule(RelativeLayout.CENTER_HORIZONTAL | RelativeLayout.ALIGN_PARENT_BOTTOM);
			cIcon.setLayoutParams(cIconP);
			cIcon.setScaleGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
			if (idx != 0) {
				cIcon.setScaleX(0.7f);
				cIcon.setScaleY(0.7f);
			}
			cIcon.setImageDrawable(getRootView().getResources().getDrawable(
					getRootView().getResources().getIdentifier("drawable/" + xmi.getIcon(), null,
							getRootView().getContext().getPackageName())));
			cIcon.setTag(idx);
			cIcon.setOnTouchListener(mTouchMenuListener);

			// Setup Item Label
			RelativeLayout.LayoutParams cLabelP = new RelativeLayout.LayoutParams(pxFromDip(90),
					pxFromDip(20));
			cLabelP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			cLabel.setLayoutParams(cLabelP);
			cLabel.setText(xmi.getID());
			cLabel.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setShadowLayer(8, 0, 0, Color.WHITE);
			if (idx > 0) {
				cLabel.setAlpha(0.0f);
			}
			cLabel.setTag(idx);
			cLabel.setOnTouchListener(mTouchMenuListener);

			// Add everything to their holder classes and containers
			cCont.addView(cIcon);
			cCont.addView(cLabel);
			xmi.setParentView(cIcon);
			xmi.setParentLabel(cLabel);
			// xmi.setParentContainer(cCont);
			tlRoot.addView(cCont);

			// Setup Subitems
			if (xmi.getNumSubItems() != 0) {
				XPMB_TableLayout cSubCont = new XPMB_TableLayout(getRootView().getContext());

				// Setup Subitems Root Container
				AbsoluteLayout.LayoutParams cSubContP = new AbsoluteLayout.LayoutParams(
						pxFromDip(416), pxFromDip(80 + (80 * xmi.getNumSubItems())), pxFromDip(56),
						pxFromDip(48));
				cSubCont.setLayoutParams(cSubContP);
				if (idx > 0) {
					cSubCont.setAlpha(0.0f);
				}

				for (XPMBMenuSubitem xsi : xmi.getSubitems()) {
					int idy = xmi.getIndexOf(xsi);

					XPMB_TableRow cSCont = new XPMB_TableRow(getRootView().getContext());
					XPMB_ImageView cSIcon = new XPMB_ImageView(getRootView().getContext());
					XPMB_TextView cSLabel = new XPMB_TextView(getRootView().getContext());
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
					cSIcon.setImageDrawable(getRootView().getResources().getDrawable(
							getRootView().getResources().getIdentifier("drawable/" + xsi.getIcon(),
									null, getRootView().getContext().getPackageName())));

					// Setup Subitem Label
					TableRow.LayoutParams cSLabelP = new TableRow.LayoutParams(pxFromDip(320),
							pxFromDip(80));
					cSIconP.column = 1;
					cSLabel.setLayoutParams(cSLabelP);
					cSLabel.setText(xsi.getID());
					cSLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					cSLabel.setTextAppearance(getRootView().getContext(),
							android.R.style.TextAppearance_Medium);
					cSLabel.setShadowLayer(8, 0, 0, Color.WHITE);
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
				getRootView().addView(cSubCont);
				xmi.setChildContainer(cSubCont);
			}
		}
		getRootView().addView(tlRoot);
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_LEFT:
			firstBackPress = false;
			moveLeft();
			break;
		case XPMB_Main.KEYCODE_RIGHT:
			firstBackPress = false;
			moveRight();
			break;
		case XPMB_Main.KEYCODE_UP:
			firstBackPress = false;
			moveUp();
			break;
		case XPMB_Main.KEYCODE_DOWN:
			firstBackPress = false;
			moveDown();
			break;
		case XPMB_Main.KEYCODE_CROSS:
			firstBackPress = false;
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			if (!firstBackPress) {
				firstBackPress = true;
				Toast tst = Toast.makeText(getRootActivity().getWindow().getContext(),
						getRootActivity().getString(R.string.strBackKeyHint), Toast.LENGTH_SHORT);
				tst.show();
			} else {
				getRootActivity().requestActivityEnd();
			}
			break;
		}
	}

	private void moveRight() {
		if (cMenuItem == (alItems.size() - 1)) {
			return;
		}

		final float initPosX = tlRoot.getX();
		final int curItem = cMenuItem;
		ValueAnimator va_mr = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_mr.setInterpolator(new DecelerateInterpolator());
		va_mr.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float pX = initPosX - (pxFromDip(80) * completion);
				float scaleO = 1.0f - (0.2f * completion);
				float alphaO = 1.0f - completion;
				float scaleI = 0.7f + (0.2f * completion);
				float alphaI = completion;


				tlRoot.setX(pX);
				alItems.get(curItem).getParentView().setScaleX(scaleO);
				alItems.get(curItem).getParentView().setScaleY(scaleO);
				alItems.get(curItem).getParentLabel().setAlpha(alphaO);
				alItems.get(curItem).getChildContainer().setAlpha(alphaO);
				alItems.get(curItem + 1).getParentView().setScaleX(scaleI);
				alItems.get(curItem + 1).getParentView().setScaleY(scaleI);
				alItems.get(curItem + 1).getParentLabel().setAlpha(alphaI);
				alItems.get(curItem + 1).getChildContainer().setAlpha(alphaI);
			}
		});

		va_mr.setDuration(550);
		getRootActivity().lockKeys(true);
		va_mr.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
				++cMenuItem;
			}

		}, 560);
	}

	private void moveLeft() {
		if (cMenuItem == 0) {
			return;
		}

		final float initPosX = tlRoot.getX();
		final int curItem = cMenuItem;
		ValueAnimator va_mr = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_mr.setInterpolator(new DecelerateInterpolator());
		va_mr.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float pX = initPosX + (pxFromDip(80) * completion);
				float scaleO = 1.0f - (0.2f * completion);
				float alphaO = 1.0f - completion;
				float scaleI = 0.7f + (0.2f * completion);
				float alphaI = completion;

				tlRoot.setX(pX);
				alItems.get(curItem).getParentView().setScaleX(scaleO);
				alItems.get(curItem).getParentView().setScaleY(scaleO);
				alItems.get(curItem).getParentLabel().setAlpha(alphaO);
				alItems.get(curItem).getChildContainer().setAlpha(alphaO);
				alItems.get(curItem - 1).getParentView().setScaleX(scaleI);
				alItems.get(curItem - 1).getParentView().setScaleY(scaleI);
				alItems.get(curItem - 1).getParentLabel().setAlpha(alphaI);
				alItems.get(curItem - 1).getChildContainer().setAlpha(alphaI);
			}
		});

		va_mr.setDuration(550);
		getRootActivity().lockKeys(true);
		va_mr.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
				--cMenuItem;
			}

		}, 560);
	}

	private void moveDown() {
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
		getRootActivity().lockKeys(true);
		ag_xmb_mu.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		alItems.get(cMenuItem).setSelectedSubItem(alItems.get(cMenuItem).getSelectedSubitem() + 1);
	}

	private void moveUp() {
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
		getRootActivity().lockKeys(true);
		ag_xmb_md.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		alItems.get(cMenuItem).setSelectedSubItem(alItems.get(cMenuItem).getSelectedSubitem() - 1);
	}

	private void execSelectedItem() {
		final XPMBMenuSubitem selItem = alItems.get(cMenuItem).getSubItem(
				alItems.get(cMenuItem).getSelectedSubitem());
		switch (selItem.getType()) {
		case XPMBMenuSubitem.TYPE_EXEC:
			Intent int_ex = new Intent("android.intent.action.MAIN");
			int_ex.setComponent(ComponentName.unflattenFromString(selItem.getExecString()));
			getRootActivity().startActivity(int_ex);
			break;
		case XPMBMenuSubitem.TYPE_SUBMENU:
			doPreExecute();
			getMessageBus().postDelayed(new Runnable() {

				@Override
				public void run() {
					getRootActivity().preloadSubmenu(selItem.getSubmenu());
				}

			}, 160);
			break;
		}
	}

	private void doPreExecute() {
		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		alAnims.add(ObjectAnimator.ofFloat(getRootActivity().findViewById(R.id.ivSubmenuShown),
				"Alpha", 1.0f));
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

		alAnims.add(ObjectAnimator.ofFloat(getRootActivity().findViewById(R.id.ivSubmenuShown),
				"Alpha", 0.0f));
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
		getRootActivity().lockKeys(true);
		as_ef_p.start();

		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);
	}

	@Override
	public void doCleanup() {
		tlRoot.removeAllViews();
		getRootView().removeView(tlRoot);
	}
}
