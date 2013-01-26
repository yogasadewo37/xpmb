package com.raddstudios.xpmb.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.xmlpull.v1.XmlPullParser;

import com.raddstudios.xpmb.XPMB_Main;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class XPMBMenu {

	private class XPMBMenuItem {
		private String strID = null, strIcon = null;
		private ArrayList<XPMBMenuSubitem> alSubitems = null;
		private ImageView ivParentView = null;
		private TextView ivParentLabel = null;
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
			ivParentLabel = label;
		}

		public TextView getParentLabel() {
			return ivParentLabel;
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
	}

	private class XPMBMenuSubitem {
		static final int TYPE_DUMMY = 0, TYPE_EXEC = 1;

		private int intType = TYPE_DUMMY;
		private String strID = null, strExecString = null, strIcon = null;
		private ImageView ivParentView = null;
		private TextView ivParentLabel = null;

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
			ivParentLabel = label;
		}

		public TextView getParentLabel() {
			return ivParentLabel;
		}
	}

	private Handler hMBus = null;
	private ViewGroup vgParent = null;
	private ArrayList<XPMBMenuItem> alItems = null;
	private int cMenuItem = 0, cMenuSubitem = 0;
	private XPMB_Main mRoot = null;

	public XPMBMenu(XmlResourceParser layout, Handler messageBus,
			ViewGroup parentView, XPMB_Main root) {
		hMBus = messageBus;
		vgParent = parentView;
		mRoot = root;

		try {
			int eventType = layout.getEventType();
			boolean done = false;
			XPMBMenuItem cItem = null;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					alItems = new ArrayList<XPMBMenuItem>();
					break;
				case XmlResourceParser.START_TAG:
					cName = layout.getName();
					if (cName.equals("item")) {
						cItem = new XPMBMenuItem(layout.getAttributeValue(null,
								"id"));
						String cAtt = layout.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cItem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
					}
					if (cName.equals("subitem")) {
						XPMBMenuSubitem cSubitem = new XPMBMenuSubitem(
								layout.getAttributeValue(null, "id"),
								getSubitemTypeFromString(layout
										.getAttributeValue(null, "type")));
						String cAtt = layout.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubitem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
						cAtt = layout.getAttributeValue(null, "exec");
						if (cAtt != null) {
							cSubitem.setExecString(cAtt);
						}
						cItem.addSubItem(cSubitem);
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = layout.getName();
					if (cName.equals("item")) {
						alItems.add(cItem);
					}
					break;
				}
				eventType = layout.next();
			}

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
		return XPMBMenuSubitem.TYPE_DUMMY;
	}

	public void parseInitLayout() {

		int mX = 0, mY = 0;

		for (mX = 0; mX < alItems.size(); mX++) {
			// Setup Icon
			LayoutParams cItemParams = new LayoutParams((int) pxFromDip(70),
					(int) pxFromDip(70));
			ImageView cItem = new ImageView(vgParent.getContext());
			cItem.setX(pxFromDip(86 + (16 * mX) + (70 * mX)));
			cItem.setY(pxFromDip(48));
			cItem.setPivotX(pxFromDip(35));
			cItem.setPivotY(pxFromDip(55));
			String cIcon = "drawable/" + alItems.get(mX).getIcon();
			Drawable cDrawable = vgParent.getResources().getDrawable(
					vgParent.getResources().getIdentifier(cIcon, null,
							vgParent.getContext().getPackageName()));
			cItem.setImageDrawable(cDrawable);
			if (mX > 0) {
				cItem.setScaleX(0.7f);
				cItem.setScaleY(0.7f);
			}
			// Setup label
			LayoutParams cLabelParams = new LayoutParams((int) pxFromDip(70),
					(int) pxFromDip(16));
			TextView cLabel = new TextView(vgParent.getContext());
			cLabel.setText(alItems.get(mX).getID());
			cLabel.setGravity(Gravity.CENTER_HORIZONTAL);
			cLabel.setX(pxFromDip(86 + (16 * mX) + (70 * mX)));
			cLabel.setY(pxFromDip(104));
			cLabel.setTextAppearance(vgParent.getContext(),
					android.R.style.TextAppearance_Small);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			if (mX > 0) {
				cLabel.setAlpha(0.0f);
			}
			alItems.get(mX).setParentView(cItem);
			alItems.get(mX).setParentLabel(cLabel);
			vgParent.addView(cItem, cItemParams);
			vgParent.addView(cLabel, cLabelParams);
			// Setup Subitems
			for (mY = 0; mY < alItems.get(mX).getNumSubItems(); mY++) {
				// Set Subitem Icon
				LayoutParams cSubitemParams = new LayoutParams(
						(int) pxFromDip(70), (int) pxFromDip(70));
				ImageView cSubitem = new ImageView(vgParent.getContext());
				cSubitem.setX(pxFromDip(86 + (16 * mX) + (70 * mX)));
				cSubitem.setY(pxFromDip(118 + (70 * mY)));
				String cSubicon = "drawable/"
						+ alItems.get(mX).getSubItem(mY).getIcon();
				Drawable cSubdrawable = vgParent.getResources().getDrawable(
						vgParent.getResources().getIdentifier(cSubicon, null,
								vgParent.getContext().getPackageName()));
				cSubitem.setImageDrawable(cSubdrawable);
				if (mX > 0) {
					cSubitem.setAlpha(0.0f);
				}
				// Set Subitem Label
				LayoutParams cSublabelParams = new LayoutParams(
						(int) pxFromDip(240), (int) pxFromDip(70));
				TextView cSublabel = new TextView(vgParent.getContext());
				cSublabel.setText(alItems.get(mX).getSubItem(mY).getID());
				cSublabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				cSublabel.setX(pxFromDip(156 + (16 * mX) + (70 * mX)));
				cSublabel.setY(pxFromDip(118 + (70 * mY)));
				cSublabel.setTextAppearance(vgParent.getContext(),
						android.R.style.TextAppearance_Medium);
				cSublabel.setShadowLayer(16, 0, 0, Color.WHITE);
				if (mX > 0 | mY > 0) {
					cSublabel.setAlpha(0.0f);
				}
				alItems.get(mX).getSubItem(mY).setParentView(cSubitem);
				alItems.get(mX).getSubItem(mY).setParentLabel(cSublabel);
				vgParent.addView(cSubitem, cSubitemParams);
				vgParent.addView(cSublabel, cSublabelParams);
			}
		}
	}

	private float pxFromDip(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				vgParent.getResources().getDisplayMetrics());
	}

	public void moveToNextItem() {
		if (cMenuItem == (alItems.size() - 1)) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mX = 0, mY = 0;

		for (mX = 0; mX < alItems.size(); mX++) {
			ImageView ivCurItem = alItems.get(mX).getParentView();
			TextView tvCurLabel = alItems.get(mX).getParentLabel();
			float cX = ivCurItem.getX();

			alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "X", cX,
					(cX - pxFromDip(86))));
			alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "X", cX,
					(cX - pxFromDip(86))));
			if (mX == cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 1.0f,
						0.7f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 1.0f,
						0.7f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 1.0f,
						0.0f));
			}
			if (mX == (cMenuItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 0.7f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 0.7f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 0.0f,
						1.0f));
			}
			for (mY = 0; mY < alItems.get(mX).getNumSubItems(); mY++) {
				ImageView ivCurSubitem = alItems.get(mX).getSubItem(mY)
						.getParentView();
				TextView tvCurSublabel = alItems.get(mX).getSubItem(mY)
						.getParentLabel();
				float cSubX = ivCurSubitem.getX(), cSublX = tvCurSublabel
						.getX();

				alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "X", cSubX,
						(cSubX - pxFromDip(86))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "X", cSublX,
						(cSublX - pxFromDip(86))));
				if (mX == cMenuItem) {
					alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Alpha",
							1.0f, 0.0f));
					alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Alpha",
							1.0f, 0.0f));
				}
				if (mX == cMenuItem + 1) {
					alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Alpha",
							0.0f, 1.0f));
					alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Alpha",
							0.0f, 1.0f));
				}
			}
		}

		AnimatorSet ag_xmb_ml = new AnimatorSet();
		ag_xmb_ml.playTogether((Collection<Animator>) alAnims);
		ag_xmb_ml.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_ml.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);
		++cMenuItem;
	}

	public void moveToPrevItem() {
		if (cMenuItem == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mX = 0, mY;

		for (mX = 0; mX < alItems.size(); mX++) {
			ImageView ivCurItem = alItems.get(mX).getParentView();
			TextView tvCurLabel = alItems.get(mX).getParentLabel();
			float cX = ivCurItem.getX();

			alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "X", cX,
					(cX + pxFromDip(86))));
			alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "X", cX,
					(cX + pxFromDip(86))));
			if (mX == cMenuItem) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 1.0f,
						0.7f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 1.0f,
						0.7f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 1.0f,
						0.0f));
			}
			if (mX == (cMenuItem - 1)) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleX", 0.7f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(ivCurItem, "ScaleY", 0.7f,
						1.0f));
				alAnims.add(ObjectAnimator.ofFloat(tvCurLabel, "Alpha", 0.0f,
						1.0f));
			}
			for (mY = 0; mY < alItems.get(mX).getNumSubItems(); mY++) {
				ImageView ivCurSubitem = alItems.get(mX).getSubItem(mY)
						.getParentView();
				TextView tvCurSublabel = alItems.get(mX).getSubItem(mY)
						.getParentLabel();
				float cSubX = ivCurSubitem.getX(), cSublX = tvCurSublabel
						.getX();

				alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "X", cSubX,
						(cSubX + pxFromDip(86))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "X", cSublX,
						(cSublX + pxFromDip(86))));
				if (mX == cMenuItem) {
					alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Alpha",
							1.0f, 0.0f));
					alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Alpha",
							1.0f, 0.0f));
				}
				if (mX == cMenuItem - 1) {
					alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Alpha",
							0.0f, 1.0f));
					alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Alpha",
							0.0f, 1.0f));
				}
			}
		}

		AnimatorSet ag_xmb_mr = new AnimatorSet();
		ag_xmb_mr.playTogether((Collection<Animator>) alAnims);
		ag_xmb_mr.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_mr.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);
		--cMenuItem;
	}

	public void moveToNextSubitem() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == (alItems.get(
				cMenuItem).getNumSubItems() - 1)) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mY = 0;

		for (mY = 0; mY < alItems.get(cMenuItem).getNumSubItems(); mY++) {
			ImageView ivCurSubitem = alItems.get(cMenuItem).getSubItem(mY)
					.getParentView();
			TextView tvCurSublabel = alItems.get(cMenuItem).getSubItem(mY)
					.getParentLabel();
			float cY = ivCurSubitem.getY();

			if (mY == alItems.get(cMenuItem).getSelectedSubitem()) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Y", cY,
						(cY - pxFromDip(140))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Y", cY,
						(cY - pxFromDip(140))));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Y", cY,
						(cY - pxFromDip(70))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Y", cY,
						(cY - pxFromDip(70))));
			}
		}

		AnimatorSet ag_xmb_mu = new AnimatorSet();
		ag_xmb_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_mu.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_mu.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);

		alItems.get(cMenuItem).setSelectedSubItem(
				alItems.get(cMenuItem).getSelectedSubitem() + 1);
	}

	public void moveToPrevSubitem() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();
		int mY = 0;

		for (mY = 0; mY < alItems.get(cMenuItem).getNumSubItems(); mY++) {
			ImageView ivCurSubitem = alItems.get(cMenuItem).getSubItem(mY)
					.getParentView();
			TextView tvCurSublabel = alItems.get(cMenuItem).getSubItem(mY)
					.getParentLabel();
			float cY = ivCurSubitem.getY();

			if (mY == alItems.get(cMenuItem).getSelectedSubitem() - 1) {
				alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Y", cY,
						(cY + pxFromDip(140))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Y", cY,
						(cY + pxFromDip(140))));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(ivCurSubitem, "Y", cY,
						(cY + pxFromDip(70))));
				alAnims.add(ObjectAnimator.ofFloat(tvCurSublabel, "Y", cY,
						(cY + pxFromDip(70))));
			}
		}

		AnimatorSet ag_xmb_md = new AnimatorSet();
		ag_xmb_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_md.setDuration(150);
		mRoot.LockKeys(true);
		ag_xmb_md.start();
		hMBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.LockKeys(false);
			}

		}, 160);

		alItems.get(cMenuItem).setSelectedSubItem(
				alItems.get(cMenuItem).getSelectedSubitem() - 1);
	}

	public void executeSelectedSubitem() {
		final XPMBMenuSubitem selItem = alItems.get(cMenuItem).getSubItem(
				alItems.get(cMenuItem).getSelectedSubitem());
		switch (selItem.getType()) {
		case XPMBMenuSubitem.TYPE_EXEC:
			mRoot.showLoadingAnim(true);
			hMBus.postDelayed(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent("android.intent.action.VIEW");
					intent.setComponent(ComponentName
							.unflattenFromString(selItem.getExecString()));
					intent.setFlags(0x10200000);
					mRoot.startActivityForResult(intent,
							XPMB_Main.RESULT_RUN_APP_FINISHED);
				}

			}, 500);
		}
	}
}
