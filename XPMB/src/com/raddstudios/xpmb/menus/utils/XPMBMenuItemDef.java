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


public interface XPMBMenuItemDef {
	void setLabel(String label);
	String getLabel();
	void setLabelAlpha(float alpha);
	float getLabelAlpha();
	void setIcon(String icon);
	String getIcon();
	void setIconScale(PointF scale);
	PointF getIconScale();
	void setIconAlpha(float alpha);
	float getIconAlpha();
	void setSubitemsAlpha(float alpha);
	float getSubitemsAlpha();
	void setSubitemsVisibility(boolean visible);
	boolean getSubitemsVisibility();
	void setSize(Point size);
	Point getSize();
	void setPosition(Point position);
	Point getPosition();
	Rect getComputedLocation();
	void setMargins(Rect margins);
	Rect getMargins();
}