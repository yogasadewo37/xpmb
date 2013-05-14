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

package com.raddstudios.xpmb.menus.modules;

import android.graphics.Canvas;

import com.raddstudios.xpmb.menus.XPMBMenu_View;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;

public interface Modules_Base {
	public void initialize(XPMBMenu_View owner, XPMBMenuCategory dest, FinishedListener finishedL);

	public void deInitialize();
	
	public void drawTo(Canvas dest);

	public void loadIn();

	public void processItem(XPMBMenuItem item);

	public void processkeyUp(int keyCode);

	public void processkeyDown(int keyCode);

	public void setListAnimator(int animator);

	public int getListAnimator();

	public boolean isInitialized();
}
