package com.raddstudios.xpmb.utils;

import java.util.Hashtable;

import android.app.Activity;

import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;

public class XPMB_Activity extends Activity {
	private Hashtable<String, Object> mObjectStore = null;

	public XPMB_Activity() {
		mObjectStore = new Hashtable<String, Object>();
	}

	public XPMB_ImageView getCustomBGView() {
		return null;
	}

	public void lockKeys(boolean locked) {
	}

	public void showLoadingAnim(boolean showAnim) {
	}

	public void preloadSubmenu(String submenu) {
	}

	public void requestUnloadSubmenu() {
	}

	public void requestActivityEnd() {
	}

	public void putObjectInStore(String key, Object data) {
		mObjectStore.put(key, data);
	}

	public Object getObjectFromStore(String key) {
		return mObjectStore.get(key);
	}
}
