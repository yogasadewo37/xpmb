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

package com.raddstudios.xpmb.menus.utils;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;

public class XPMBMenuItem implements XPMBMenuItemDef {

	private String strLabelA = null, strLabelB = null, strIcon = null;
	private PointF pfScale = null;
	private Rect rLoc = null, rMargins = null;
	private float fAlpha_i = 1.0f, fAlpha_l = 1.0f, fAlpha_s = 1.0f;
	private boolean bTwoLines = false;
	private int intIconType = 0;

	public static final String TYPE_DESC = "menuitem.generic";

	private final String BD_STRLABELA = "strLabelA", BD_STRLABELB = "strLabelB",
			BD_STRICON = "strIcon", BD_PFSCALE = "pfScale", BD_RLOC = "rLoc",
			BD_RMARGINS = "rMargins", BD_FALPHAI = "fAlpha_i", BD_FALPHAL = "fAlpha_l",
			BD_FALPHAS = "fAlpha_s", BD_BTWOLINES = "bTwoLines", BD_INTICONTYPE = "intIconType";

	public XPMBMenuItem(String label) {
		strLabelA = label;
		pfScale = new PointF(1.0f, 1.0f);
		rLoc = new Rect();
		rMargins = new Rect();
	}

	public XPMBMenuItem(Bundle src) {
		strLabelA = src.getString(BD_STRLABELA);
		strLabelB = src.getString(BD_STRLABELB);
		strIcon = src.getString(BD_STRICON);
		pfScale = getPointFFromBundle(src, BD_PFSCALE);
		rLoc = getRectFromBundle(src, BD_RLOC);
		rMargins = getRectFromBundle(src, BD_RMARGINS);
		fAlpha_i = src.getFloat(BD_FALPHAI);
		fAlpha_l = src.getFloat(BD_FALPHAL);
		fAlpha_s = src.getFloat(BD_FALPHAS);
		bTwoLines = src.getBoolean(BD_BTWOLINES);
		intIconType = src.getInt(BD_INTICONTYPE);
	}

	@Override
	public String getTypeDescriptor() {
		return XPMBMenuItem.TYPE_DESC;
	}

	public Bundle storeInBundle() {
		Bundle o = new Bundle();

		o.putString(BD_STRLABELA, strLabelA);
		o.putString(BD_STRLABELB, strLabelB);
		o.putString(BD_STRICON, strIcon);
		storePointFInBundle(o, pfScale, BD_PFSCALE);
		storeRectInBundle(o, rLoc, BD_RLOC);
		storeRectInBundle(o, rMargins, BD_RMARGINS);
		o.putFloat(BD_FALPHAI, fAlpha_i);
		o.putFloat(BD_FALPHAL, fAlpha_l);
		o.putFloat(BD_FALPHAS, fAlpha_s);
		o.putBoolean(BD_BTWOLINES, bTwoLines);
		o.putInt(BD_INTICONTYPE, intIconType);

		return o;
	}

	protected void storePointInBundle(Bundle dest, Point src, String baseKey) {
		dest.putInt(baseKey + "_x", src.x);
		dest.putInt(baseKey + "_y", src.y);
	}

	protected Point getPointFromBundle(Bundle src, String baseKey) {
		Point o = new Point();
		o.x = src.getInt(baseKey + "_x");
		o.y = src.getInt(baseKey + "_y");
		return o;
	}

	protected void storePointFInBundle(Bundle dest, PointF src, String baseKey) {
		dest.putFloat(baseKey + "_x", src.x);
		dest.putFloat(baseKey + "_y", src.y);
	}

	protected PointF getPointFFromBundle(Bundle src, String baseKey) {
		PointF o = new PointF();
		o.x = src.getFloat(baseKey + "_x");
		o.y = src.getFloat(baseKey + "_y");
		return o;
	}

	protected void storeRectInBundle(Bundle dest, Rect src, String baseKey) {
		dest.putInt(baseKey + "_l", src.left);
		dest.putInt(baseKey + "_t", src.top);
		dest.putInt(baseKey + "_r", src.right);
		dest.putInt(baseKey + "_b", src.bottom);
	}

	protected Rect getRectFromBundle(Bundle src, String baseKey) {
		Rect o = new Rect();
		o.left = src.getInt(baseKey + "_l");
		o.top = src.getInt(baseKey + "_t");
		o.right = src.getInt(baseKey + "_r");
		o.bottom = src.getInt(baseKey + "_b");
		return o;
	}

	protected void storeRectFInBundle(Bundle dest, RectF src, String baseKey) {
		dest.putFloat(baseKey + "_l", src.left);
		dest.putFloat(baseKey + "_t", src.top);
		dest.putFloat(baseKey + "_r", src.right);
		dest.putFloat(baseKey + "_b", src.bottom);
	}

	protected RectF getRectFFromBundle(Bundle src, String baseKey) {
		RectF o = new RectF();
		o.left = src.getFloat(baseKey + "_l");
		o.top = src.getFloat(baseKey + "_t");
		o.right = src.getFloat(baseKey + "_r");
		o.bottom = src.getFloat(baseKey + "_b");
		return o;
	}

	@Override
	public void setLabel(String label) {
		strLabelA = label;
	}

	@Override
	public String getLabel() {
		return strLabelA;
	}

	@Override
	public void setIconBitmapID(String icon) {
		strIcon = icon;
	}

	@Override
	public String getIconBitmapID() {
		return strIcon;
	}

	@Override
	public void setIconAlpha(float alpha) {
		fAlpha_i = alpha;
	}

	@Override
	public float getIconAlpha() {
		return fAlpha_i;
	}

	@Override
	public void setLabelAlpha(float alpha) {
		fAlpha_l = alpha;
	}

	@Override
	public float getLabelAlpha() {
		return fAlpha_l;
	}

	@Override
	public void setPosition(Point position) {
		rLoc.offsetTo(position.x, position.y);
	}

	@Override
	public Point getPosition() {
		return new Point(rLoc.left, rLoc.top);
	}

	@Override
	public void setIconScale(PointF scale) {
		pfScale = scale;
	}

	@Override
	public PointF getIconScale() {
		return pfScale;
	}

	@Override
	public void setSize(Point size) {
		setWidth(size.x);
		setHeight(size.y);
	}

	@Override
	public Rect getComputedLocation() {
		return rLoc;
	}

	@Override
	public void setIconScaleX(float scx) {
		pfScale.x = scx;
	}

	@Override
	public void setIconScaleY(float scy) {
		pfScale.y = scy;
	}

	@Override
	public Point getSize() {
		return new Point(rLoc.width(), rLoc.height());
	}

	@Override
	public void setWidth(int width) {
		rLoc.right = rLoc.left + width;
	}

	@Override
	public void setHeight(int height) {
		rLoc.bottom = rLoc.top + height;
	}

	@Override
	public void setPositionX(int x) {
		rLoc.offsetTo(x, rLoc.top);
	}

	@Override
	public void setPositionY(int y) {
		rLoc.offsetTo(rLoc.left, y);
	}

	@Override
	public void setLabelB(String label) {
		strLabelB = label;
	}

	@Override
	public void enableTwoLine(boolean enabled) {
		bTwoLines = enabled;
	}

	@Override
	public String getLabelB() {
		return strLabelB;
	}

	@Override
	public boolean isTwoLines() {
		return bTwoLines;
	}

	@Override
	public void setSeparatorAlpha(float alpha) {
		fAlpha_s = alpha;
	}

	@Override
	public float getSeparatorAlpha() {
		return fAlpha_s;
	}

	@Override
	public Rect getMargins() {
		return rMargins;
	}

	@Override
	public void setMarginLeft(int left) {
		rMargins.left = left;
	}

	@Override
	public void setMarginTop(int top) {
		rMargins.top = top;
	}

	@Override
	public void setMarginRight(int right) {
		rMargins.right = right;
	}

	@Override
	public void setMarginBottom(int bottom) {
		rMargins.bottom = bottom;
	}

	@Override
	public void setIconType(int type) {
		intIconType = type;
	}

	@Override
	public int getIconType() {
		return intIconType;
	}
}
