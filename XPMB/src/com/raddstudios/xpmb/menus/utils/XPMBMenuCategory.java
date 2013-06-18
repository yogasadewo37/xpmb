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

public class XPMBMenuCategory extends XPMBMenuItem {

	public static final int LIST_ANIM_NONE = 0, LIST_ANIM_FULL = 4, LIST_ANIM_HALF = 2,
			LIST_ANIM_HIGHLIGHT = 3;
	public static final int MODULE_SYSTEM_DUMMY = 0, MODULE_MEDIA_MUSIC = 1,
			MODULE_MEDIA_PICTURES = 2, MODULE_MEDIA_VIDEOS = 3, MODULE_SYSTEM_APPS = 4,
			MODULE_EMU_GBA = 5, MODULE_EMU_NES = 6, MODULE_EMU_SNES = 7;

	private ArrayList<XPMBMenuItemDef> alSubitems = null;
	private int intCurSubitem = 0, intListAnimator = LIST_ANIM_NONE,
			intSubmodule = MODULE_SYSTEM_DUMMY;
	private float fSubAlpha = 1.0f;
	private Point pContainerPos = null;
	private boolean bSubitemsVisible = false;

	public XPMBMenuCategory(String label) {
		super(label);
		alSubitems = new ArrayList<XPMBMenuItemDef>();
		pContainerPos = new Point();
	}
	
	@Override
	public void setSubitemsAlpha(float alpha) {
		fSubAlpha = alpha;
	}

	@Override
	public float getSubitemsAlpha() {
		return fSubAlpha;
	}

	@Override
	public void setSubitemsVisibility(boolean visible){
		bSubitemsVisible = visible;
	}
	
	@Override
	public boolean getSubitemsVisibility(){
		return bSubitemsVisible;
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

	public void setSubmoduleIDFromString(String type) {
		if (type == null) {
			intSubmodule = MODULE_SYSTEM_DUMMY;
		} else if (type.equalsIgnoreCase("com.xpmb.media.music")) {
			intSubmodule = MODULE_MEDIA_MUSIC;
		} else if (type.equalsIgnoreCase("com.xpmb.media.pictures")) {
			intSubmodule = MODULE_MEDIA_PICTURES;
		} else if (type.equalsIgnoreCase("com.xpmb.media.videos")) {
			intSubmodule = MODULE_MEDIA_VIDEOS;
		} else if (type.equalsIgnoreCase("com.xpmb.emu.nes")) {
			intSubmodule = MODULE_EMU_NES;
		} else if (type.equalsIgnoreCase("com.xpmb.emu.snes")) {
			intSubmodule = MODULE_EMU_SNES;
		} else if (type.equalsIgnoreCase("com.xpmb.emu.gba")) {
			intSubmodule = MODULE_EMU_GBA;
		} else if (type.equalsIgnoreCase("com.xpmb.system.apps")) {
			intSubmodule = MODULE_SYSTEM_APPS;
		}
	}

	public int getSubmoduleID() {
		return intSubmodule;
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

	public void setSubitemsPosX(int pos) {
		pContainerPos.x = pos;
	}

	public void setSubitemsPosY(int pos) {
		pContainerPos.y = pos;
	}

	public Point getSubitemsPos() {
		return pContainerPos;
	}
}
