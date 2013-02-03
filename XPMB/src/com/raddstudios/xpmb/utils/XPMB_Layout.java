package com.raddstudios.xpmb.utils;

import android.os.Handler;
import android.util.TypedValue;
import android.view.ViewGroup;

public class XPMB_Layout {

	XPMB_Activity mRoot = null;
	Handler hMessageBus = null;

	public XPMB_Layout(XPMB_Activity root, Handler messageBus) {
		mRoot = root;
		hMessageBus = messageBus;
	}

	public void doInit() {
	}

	public void parseInitLayout(ViewGroup base) {
	}

	public void moveDown() {
	}

	public void moveUp() {
	}

	public void moveLeft() {
	}

	public void moveRight() {
	}

	public void execSelectedItem() {
	}

	public int pxFromDip(int dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				mRoot.getResources().getDisplayMetrics());
	}
	
	public void postExecuteFinished(){
		
	}

	public void doCleanup(ViewGroup base) {

	}

}
