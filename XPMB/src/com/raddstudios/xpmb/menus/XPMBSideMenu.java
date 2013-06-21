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

package com.raddstudios.xpmb.menus;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.UI.UILayer;

public class XPMBSideMenu extends UILayer {

	public interface SideMenuItemAction {
		public void execute();
	}

	public class SideMenuItem {

		private String strLabel = null, strIconBitmapID = null;
		private SideMenuItemAction mAction = null;

		public void setLabel(String label) {
			strLabel = label;
		}

		public String getLabel() {
			return strLabel;
		}

		public void setIconBitmapID(String iconBitmapID) {
			strIconBitmapID = iconBitmapID;
		}

		public String getIconBitmapID() {
			return strIconBitmapID;
		}

		public boolean hasChildren() {
			return false;
		}

		public void setExecuteAction(SideMenuItemAction action) {
			mAction = action;
		}

		public void executeAction() {
			if (mAction != null) {
				mAction.execute();
			}
		}
	}

	//TODO: Finish Side Menu code
	
	SideMenuItem[] alItems = null;

	public XPMBSideMenu(XPMB_Activity root) {
		super(root);
		alItems = new SideMenuItem[15];
	}

	int px_x = (int) (getDrawingConstraints().right - pxfd(188)), px_y = 0,
			intSzy = (int) (getDrawingConstraints().height() / 15);
	Rect rCurItem = new Rect();
	Paint pParams = new Paint();

	@Override
	public void drawTo(Canvas canvas) {
		pParams.setTextSize(pxfd(16));
		pParams.setColor(Color.WHITE);
		pParams.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);

		for (int i = 0; i < 15; i++) {
			rCurItem.set(px_x, px_y, (int) getDrawingConstraints().right,
					(int) getDrawingConstraints().bottom);
			if (alItems[i] != null) {
				if (alItems[i].getIconBitmapID() != null) {
					canvas.drawText(alItems[i].getLabel(), px_x,
							rCurItem.centerY() + (pParams.ascent() / 2), pParams);

				} else {
					canvas.drawText(alItems[i].getLabel(), px_x,
							rCurItem.centerY() + (pParams.ascent() / 2), pParams);
				}
			}
			px_y += intSzy;
		}
		pParams.reset();
	}

	public void setItemSlot(int slot, SideMenuItem item) {
		alItems[slot] = item;
	}

	public void clearItemSlots() {
		for (int i = 0; i < 15; i++) {
			alItems[i] = null;
		}
	}
}
