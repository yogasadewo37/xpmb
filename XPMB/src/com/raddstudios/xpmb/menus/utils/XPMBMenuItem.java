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

public class XPMBMenuItem implements XPMBMenuItemDef {

	private String strLabelA = null, strLabelB = null, strIcon = null;
	private Object oData = null;
	private PointF pfScale = null;
	private Rect rLoc = null, rMargins = null;
	private float fAlpha_i = 1.0f, fAlpha_l = 1.0f, fAlpha_s = 1.0f;
	private boolean bTwoLines = false;
	private int iIconType = 0;

	public XPMBMenuItem(String label) {
		strLabelA = label;
		pfScale = new PointF(1.0f, 1.0f);
		rLoc = new Rect();
		rMargins = new Rect();
	}

	public void setData(Object data) {
		oData = data;
	}

	public Object getData() {
		return oData;
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
		iIconType = type;
	}

	@Override
	public int getIconType() {
		return iIconType;
	}
}
