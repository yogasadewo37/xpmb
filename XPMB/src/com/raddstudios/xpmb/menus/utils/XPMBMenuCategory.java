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

import java.util.ArrayList;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class XPMBMenuCategory implements XPMBMenuItemDef {

	public static final int LIST_ANIM_NONE = 0, LIST_ANIM_FULL = 4, LIST_ANIM_HALF = 2,
			LIST_ANIM_HIGHLIGHT = 3;
	public static final int FILTER_SYSTEM_DUMMY = 0, FILTER_MEDIA_MUSIC = 1,
			FILTER_MEDIA_PICTURES = 2, FILTER_MEDIA_VIDEOS = 3, FILTER_SYSTEM_APPS = 4,
			FILTER_EMU_GBA = 5, FILTER_EMU_NES = 6, FILTER_EMU_SNES = 7;

	private ArrayList<XPMBMenuItemDef> alSubitems = null;
	private String strLabel = null, strIcon = null;
	private int intCurSubitem = 0, intListAnimator = LIST_ANIM_NONE,
			intListFilter = FILTER_SYSTEM_DUMMY;
	private PointF pfScale = null;
	private Rect rMargins = null, rLoc = null;
	private float fAlpha_i = 1.0f, fAlpha_l = 1.0f, fAlpha = 1.0f;
	private boolean bSubitemsVisible = false;

	public XPMBMenuCategory(String label) {
		strLabel = label;
		alSubitems = new ArrayList<XPMBMenuItemDef>();
		pfScale = new PointF(1.0f, 1.0f);
		rMargins = new Rect(0, 0, 0, 0);
		rLoc = new Rect(0, 0, 0, 0);
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

	public void addSubitem(XPMBMenuItemDef subitem) {
		alSubitems.add(subitem);
	}

	public void removeSubitem(XPMBMenuItemDef subitem) {
		alSubitems.remove(subitem);
	}

	public void removeSubitem(int index) {
		alSubitems.remove(index);
	}

	public void clearSubitems() {
		alSubitems.clear();
	}

	public XPMBMenuItemDef getSubitem(int index) {
		return alSubitems.get(index);
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

	public XPMBMenuItemDef[] getSubitems() {
		return alSubitems.toArray(new XPMBMenuItemDef[0]);
	}

	public int getIndexOf(XPMBMenuItemDef value) {
		return alSubitems.indexOf(value);
	}

	public void setItemFilter(String type) {
		if (type == null) {
			intListFilter = FILTER_SYSTEM_DUMMY;
		} else if (type.equalsIgnoreCase("com.xpmb.media.music")) {
			intListFilter = FILTER_MEDIA_MUSIC;
		} else if (type.equalsIgnoreCase("com.xpmb.media.pictures")) {
			intListFilter = FILTER_MEDIA_PICTURES;
		} else if (type.equalsIgnoreCase("com.xpmb.media.videos")) {
			intListFilter = FILTER_MEDIA_VIDEOS;
		} else if (type.equalsIgnoreCase("com.xpmb.emu.nes")) {
			intListFilter = FILTER_EMU_NES;
		} else if (type.equalsIgnoreCase("com.xpmb.emu.snes")) {
			intListFilter = FILTER_EMU_SNES;
		} else if (type.equalsIgnoreCase("com.xpmb.emu.gba")) {
			intListFilter = FILTER_EMU_GBA;
		} else if (type.equalsIgnoreCase("com.xpmb.system.apps")) {
			intListFilter = FILTER_SYSTEM_APPS;
		}
	}

	public int getItemFilter() {
		return intListFilter;
	}

	public void setListAnimator(String animator) {
		if (animator == null) {
			intListAnimator = LIST_ANIM_NONE;
		} else if (animator.equalsIgnoreCase("full")) {
			intListAnimator = LIST_ANIM_FULL;
		} else if (animator.equalsIgnoreCase("half")) {
			intListAnimator = LIST_ANIM_HALF;
		} else if (animator.equalsIgnoreCase("highlight")) {
			intListAnimator = LIST_ANIM_HIGHLIGHT;
		}
	}

	public int getListAnimator() {
		return intListAnimator;
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
		fAlpha = alpha;
	}

	@Override
	public float getSubitemsAlpha() {
		return fAlpha;
	}

	@Override
	public void setSubitemsVisibility(boolean visible) {
		bSubitemsVisible = visible;
	}

	@Override
	public boolean getSubitemsVisibility() {
		return bSubitemsVisible;
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
