package com.raddstudios.xpmb.utils;

import android.os.Handler;
import android.util.TypedValue;
import android.view.ViewGroup;

public class XPMB_Layout {

	private XPMB_Activity mRoot = null;
	private ViewGroup mRootView = null;
	private Handler hMessageBus = null;
	private int cID = 0xC0DD;

	public XPMB_Layout(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		mRoot = root;
		hMessageBus = messageBus;
		mRootView = rootView;
	}

	protected Handler getMessageBus() {
		return hMessageBus;
	}

	protected ViewGroup getRootView() {
		return mRootView;
	}

	protected XPMB_Activity getRootActivity() {
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

	protected int pxFromDip(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mRoot
				.getResources().getDisplayMetrics());
	}

	public void doCleanup() {
	}

	public void requestDestroy() {
	}

	protected int getNextID() {
		cID++;
		return cID;
	}
}
