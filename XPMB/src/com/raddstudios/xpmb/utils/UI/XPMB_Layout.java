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

package com.raddstudios.xpmb.utils.UI;

import com.raddstudios.xpmb.utils.XPMB_Activity;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ViewConstructor")
public class XPMB_Layout extends UILayer {

	private XPMB_Activity mRoot = null;

	public XPMB_Layout(XPMB_Activity root) {
		super(root);
		mRoot = root;
	}

	protected Handler getMessageBus() {
		return mRoot.getMessageBus();
	}

	public ViewGroup getRootView() {
		return mRoot.getRootView();
	}

	public XPMB_Activity getRootActivity() {
		return mRoot;
	}

	public void doInit() {
	}

	public void parseInitLayout() {
	}

	public void sendKeyDown(int keyCode) {
	}

	public void sendKeyUp(int keyCode) {
	}

	public void sendKeyHold(int keyCode) {
	}

	public void sendClickEventToView(View v) {
	}
	
	protected int pxFromDip(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mRoot
				.getResources().getDisplayMetrics());
	}

	public void doCleanup() {
	}

	public void requestDestroy() {
	}
}
