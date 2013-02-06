package com.raddstudios.xpmb.utils;

import android.os.Handler;
import android.util.TypedValue;
import android.view.ViewGroup;

public class XPMB_Layout {

	XPMB_Activity mRoot = null;
	Handler hMessageBus = null;
	int cID = 0xC0DD;

	public XPMB_Layout(XPMB_Activity root, Handler messageBus) {
		mRoot = root;
		hMessageBus = messageBus;
	}

	public void doInit() {
	}

	public void parseInitLayout(ViewGroup base) {
	}

	public void sendKeyDown(int keyCode) {

	}

	public void sendKeyUp(int keyCode) {

	}

	public int pxFromDip(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mRoot
				.getResources().getDisplayMetrics());
	}

	public void doCleanup(ViewGroup base) {
		
	}

	public void requestDestroy() {

	}

	protected int getNextID() {
		cID++;
		return cID;
	}

}
