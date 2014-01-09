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

import com.raddstudios.xpmb.XPMBActivity;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;

public interface XPMBMenuItemDef {
	final static int ICON_TYPE_COUNTER = 0, ICON_TYPE_BITMAP = 1;
	
	Bundle storeInBundle();
	String getTypeDescriptor();
	void setLabel(String label);
	void setLabelB(String label);
	String getLabel();
	String getLabelB();
	void setLabelAlpha(float alpha);
	float getLabelAlpha();
	void setIconBitmapID(String icon);
	String getIconBitmapID();
	void preloadIconBitmap(XPMBActivity root);
	void setIconType(int type);
	int getIconType();
	void enableTwoLine(boolean enabled);
	boolean isTwoLines();
	void setIconScale(PointF scale);
	void setIconScaleX(float scx);
	void setIconScaleY(float scy);
	PointF getIconScale();
	void setIconAlpha(float alpha);
	float getIconAlpha();
	void setSeparatorAlpha(float alpha);
	float getSeparatorAlpha();
	void setSize(Point size);
	Point getSize();
	void setWidth(int width);
	void setHeight(int height);
	void setPosition(Point position);
	Point getPosition();
	void setPositionX(int x);
	void setPositionY(int y);
	Rect getMargins();
	void setMarginLeft(int left);
	void setMarginTop(int top);
	void setMarginRight(int right);
	void setMarginBottom(int bottom);
	Rect getComputedLocation();
}