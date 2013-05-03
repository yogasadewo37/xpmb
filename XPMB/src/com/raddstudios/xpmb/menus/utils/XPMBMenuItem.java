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

	private String strLabel = null, strIcon = null;
	private Object oData = null;
	private PointF pfScale = null;
	private Rect rMargins = null, rLoc = null;
	private float fAlpha_i = 1.0f, fAlpha_l = 1.0f;

	public XPMBMenuItem(String label) {
		strLabel = label;
		pfScale = new PointF(1.0f, 1.0f);
		rMargins = new Rect(0, 0, 0, 0);
		rLoc = new Rect(0, 0, 0, 0);
	}

	public void setData(Object data) {
		oData = data;
	}

	public Object getData() {
		return oData;
	}

	@Override
	public void setLabel(String label) {
		strLabel = label;
	}

	@Override
	public String getLabel() {
		return strLabel;
	}

	@Override
	public void setIcon(String icon) {
		strIcon = icon;
	}

	@Override
	public String getIcon() {
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
	public void setSubitemsAlpha(float alpha) {
	}

	@Override
	public float getSubitemsAlpha() {
		return 1.0f;
	}

	@Override
	public void setSubitemsVisibility(boolean visible) {
	}

	@Override
	public boolean getSubitemsVisibility() {
		return false;
	}

	@Override
	public void setPosition(Point position) {
		rLoc.left = position.x;
		rLoc.top = position.y;
	}

	@Override
	public Point getPosition() {
		return new Point(rLoc.left, rLoc.top);
	}

	@Override
	public void setMargins(Rect margins) {
		rMargins = margins;
	}

	@Override
	public Rect getMargins() {
		return rMargins;
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
	public Point getSize() {
		return new Point(rLoc.right - rLoc.left, rLoc.bottom - rLoc.top);
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
	public void setWidth(int width) {
		if (rLoc.left != 0) {
			rLoc.right = rLoc.left + width;
		} else {
			rLoc.right = width;
		}
	}

	@Override
	public void setHeight(int height) {
		if (rLoc.top != 0) {
			rLoc.bottom = rLoc.top + height;
		} else {
			rLoc.bottom = height;
		}
	}

	@Override
	public void setPositionX(int x) {
		int w = rLoc.right - rLoc.left;
		rLoc.left = x;
		rLoc.right = x + w;
	}

	@Override
	public void setPositionY(int y) {
		int h = rLoc.bottom - rLoc.top;
		rLoc.top = y;
		rLoc.bottom = y + h;
	}
}
