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

import java.util.Iterator;
import java.util.Vector;

import android.graphics.Point;
import android.os.Bundle;

import com.raddstudios.xpmb.XPMBActivity;

public class XPMBMenuCategory extends XPMBMenuItem {

	public static final int LIST_ANIM_NONE = 0, LIST_ANIM_FULL = 4, LIST_ANIM_HALF = 2,
			LIST_ANIM_HIGHLIGHT = 3;

	private Vector<XPMBMenuItemDef> alSubitems = null;
	private int intCurSubitem = 0, intListAnimator = LIST_ANIM_NONE;
	private String strSubmodule = XPMBActivity.MODULE_SYSTEM_DUMMY;
	private float fSubAlpha = 1.0f;
	private Point pContainerPos = null;
	private boolean bSubitemsVisible = false;
	
	private final String BD_INTCURSUBITEM = "intCurSubitem", BD_INTLISTANIMATOR = "intListAnimator",
			BD_STRSUBMODULE = "strSubmodule", BD_FSUBALPHA = "fSubAlpha", BD_PCONTAINERPOS = "pContainerPos",
			BD_BSUBITEMSVISIBLE = "bSubitemsVisible";

	public XPMBMenuCategory(String label) {
		super(label);
		alSubitems = new Vector<XPMBMenuItemDef>();
		pContainerPos = new Point();
	}
	
	public XPMBMenuCategory(Bundle source){
		super(source);
		intCurSubitem = source.getInt(BD_INTCURSUBITEM);
		intListAnimator = source.getInt(BD_INTLISTANIMATOR);
		strSubmodule = source.getString(BD_STRSUBMODULE);
		fSubAlpha = source.getFloat(BD_FSUBALPHA);
		pContainerPos = getPointFromBundle(source,BD_PCONTAINERPOS);
		bSubitemsVisible = source.getBoolean(BD_BSUBITEMSVISIBLE);
	}
	
	@Override
	public Bundle storeInBundle(){
		Bundle s = super.storeInBundle();
		
		s.putInt(BD_INTCURSUBITEM, intCurSubitem);
		s.putInt(BD_INTLISTANIMATOR, intListAnimator);
		s.putString(BD_STRSUBMODULE, strSubmodule);
		s.putFloat(BD_FSUBALPHA,fSubAlpha);
		storePointInBundle(s,pContainerPos,BD_PCONTAINERPOS);
		s.putBoolean(BD_BSUBITEMSVISIBLE, bSubitemsVisible);
		
		return s;
	}

	public void setSubitemsAlpha(float alpha) {
		fSubAlpha = alpha;
	}

	public float getSubitemsAlpha() {
		return fSubAlpha;
	}

	public void setSubitemsVisibility(boolean visible) {
		bSubitemsVisible = visible;
	}

	public boolean getSubitemsVisibility() {
		return bSubitemsVisible;
	}

	public void addSubitem(XPMBMenuItemDef subitem) {
		alSubitems.add(subitem);
	}
	
	public void addSubitem(int index, XPMBMenuItemDef subitem){
		alSubitems.add(index, subitem);
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

	public int getNumSubitems() {
		return alSubitems.size();
	}

	public void setSelectedSubitem(int subitem) {
		intCurSubitem = subitem;
	}

	public int getSelectedSubitem() {
		return intCurSubitem;
	}

	public Iterator<XPMBMenuItemDef> getSubitems() {
		return alSubitems.iterator();
	}

	public int getIndexOf(XPMBMenuItemDef value) {
		return alSubitems.indexOf(value);
	}

	public void setSubmoduleID(String id) {
		strSubmodule = id;
	}

	public String getSubmoduleID() {
		return strSubmodule;
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
