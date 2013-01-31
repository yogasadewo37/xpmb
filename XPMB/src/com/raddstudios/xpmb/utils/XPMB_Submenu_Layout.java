package com.raddstudios.xpmb.utils;

import android.view.ViewGroup;

public interface XPMB_Submenu_Layout {
	public void doInit();

	public void parseInitLayout(ViewGroup base);

	public void moveToNextItem();

	public void moveToPrevItem();

	public void execSelectedItem();

}
