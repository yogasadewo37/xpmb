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

package com.raddstudios.xpmb.menus.utils.filters;

import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.backports.XPMBMenu_View;

public interface Filter_Base {
	public void initialize(XPMBMenu_View owner, XPMBMenuCategory dest, XPMB_Activity resources,
			FinishedListener finishedL);

	public void deInitialize();

	public void loadIn();

	public void processItem(XPMBMenuItem item);

	public void processkeyUp(int keyCode);

	public void processkeyDown(int keyCode);
	
	public void setListAnimator(int animator);
	
	public int getListAnimator();

	public boolean isInitialized();
}
