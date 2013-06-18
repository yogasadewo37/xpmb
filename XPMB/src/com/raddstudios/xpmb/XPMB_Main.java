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

package com.raddstudios.xpmb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.zip.ZipFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import com.raddstudios.xpmb.menus.XPMBMenu;
import com.raddstudios.xpmb.menus.XPMB_BaseUILayer;
import com.raddstudios.xpmb.utils.ThemeLoader;
import com.raddstudios.xpmb.utils.XPMB_Activity;

public class XPMB_Main extends XPMB_Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = 19, KEYCODE_DOWN = 20, KEYCODE_LEFT = 21,
			KEYCODE_RIGHT = 22, KEYCODE_CROSS = 23, KEYCODE_CIRCLE = 4, KEYCODE_SQUARE = 99,
			KEYCODE_TRIANGLE = 100, KEYCODE_SELECT = 109, KEYCODE_START = 108, KEYCODE_MENU = 82,
			KEYCODE_SHOULDER_LEFT = 102, KEYCODE_SHOULDER_RIGHT = 103, KEYCODE_VOLUME_DOWN = 25,
			KEYCODE_VOLUME_UP = 24;
	public static final String GRAPH_ASSETS_COL_KEY = "com.raddstudios.graphassets",
			SETTINGS_COL_KEY = "com.raddstudios.settings";

	private XPMBMenu mMenu = null;
	private XPMB_BaseUILayer mBaseLayer = null;
	private boolean bLockedKeys = false, firstInitDone = false;
	AudioManager amVolControl = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(getClass().getSimpleName(), "onCreate():Initializing XPMB...");
		Log.d(getClass().getSimpleName(), "onCreate():Reported Android version is ["
				+ Build.VERSION.RELEASE + "]");
		Log.d(getClass().getSimpleName(), "onCreate():Reported locale is ["
				+ Locale.getDefault().getDisplayLanguage() + "]");
		Log.d(getClass().getSimpleName(), "onCreate():Reported board name is [" + Build.BOARD + "]");

		if (!isExtStorageRW()) {
			Log.e(getClass().getSimpleName(),
					"onCreate():Error initializing XPMB. External storage is not available.");
			finish();
		}

		// Setup services
		amVolControl = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);

		// Setup window params
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		// ^^ These 2 or 3 should be optional
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getRootView().setOnTouchListener(mTouchListener);
		setContentView(getRootView());

		File fRootStoragePath = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/XPMB");
		if (!fRootStoragePath.exists()) {
			Log.w(getClass().getSimpleName(),
					"onCreate():Root storage path not found. Creating directory.");
			fRootStoragePath.mkdirs();
		}
		File defTheme = new File(fRootStoragePath.getAbsolutePath() + "/themes/XPMB.zip");
		if (!defTheme.exists()) {
			if (!defTheme.getParentFile().exists()) {
				Log.w(getClass().getSimpleName(),
						"onCreate():Theme path not found. Creating directory.");
				defTheme.getParentFile().mkdir();
			}
			long t = System.currentTimeMillis();
			Log.w(getClass().getSimpleName(),
					"onCreate():Default theme file not found. Creating default theme into '"
							+ defTheme.getAbsolutePath() + "'...");

			FileOutputStream oFile = null;
			try {
				oFile = new FileOutputStream(defTheme);
				int cByte = 0;
				InputStream fi = getResources().openRawResource(R.raw.xpmb);

				byte[] buf = new byte[1024 * 64];
				while ((cByte = fi.read(buf)) > 0) {
					oFile.write(buf, 0, cByte);
				}
				oFile.close();
				Log.i(getClass().getSimpleName(),
						"onCreate():Default theme creation finished. Process took "
								+ (System.currentTimeMillis() - t) + "ms");
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(),
						"onCreate():Error creating default theme. Process took "
								+ (System.currentTimeMillis() - t) + "ms");
			}
		}

		if (firstInitDone && mMenu != null) {
			Log.v(getClass().getSimpleName(), "onCreate():Already initialized. Skipping process.");
			return;
		}

		getStorage().createCollection(GRAPH_ASSETS_COL_KEY);
		getStorage().createCollection(SETTINGS_COL_KEY);
		try {
			new ThemeLoader((Hashtable<String, Bitmap>) getStorage().getCollection(
					XPMB_Main.GRAPH_ASSETS_COL_KEY)).reloadTheme(new ZipFile(new File(Environment
					.getExternalStorageDirectory().getPath() + "/XPMB/themes/XPMB.zip"),
					ZipFile.OPEN_READ));
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"onCreate():Error loading theme file. Now expect NullPointerExceptions");
		}
		mBaseLayer = new XPMB_BaseUILayer(this);
		getDrawingLayerManager().addLayer(mBaseLayer);
		mMenu = new XPMBMenu(this);
	}

	private static final int MOVING_DIR_VERT = 0, MOVING_DIR_HORZ = 1;
	private float motStX = 0, motStY = 0, dispX = 0, dispY = 0;
	private int polarity = 1;
	private boolean isMoving = false;
	private boolean isTouchEnabled = true;
	private View mTouchedView = null;

	// @Override
	public void setTouchedChildView(View v) {
		mTouchedView = v;
	}

	private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			if (!isTouchEnabled) {
				return true;
			}
			int action = arg1.getActionMasked();
			int pointerIndex = arg1.getActionIndex();
			int pointerId = arg1.getPointerId(pointerIndex);
			if (pointerId != 0) {
				return true;
			}

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (!isMoving) {
					motStX = arg1.getX(pointerId);
					motStY = arg1.getY(pointerId);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				dispX = arg1.getX(pointerId) - motStX;
				dispY = arg1.getY(pointerId) - motStY;
				float absX = Math.abs(dispX),
				absY = Math.abs(dispY);
				if (!isMoving && (absX > 50 || absY > 50)) {
					if (absX > 50) {
						polarity = MOVING_DIR_HORZ;
					}
					if (absY > 50) {
						polarity = MOVING_DIR_VERT;
					}
					isMoving = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (arg1.getEventTime() - arg1.getDownTime() > 150) {
					switch (polarity) {
					case MOVING_DIR_HORZ:
						if (dispX < 0) {
							onKeyDown(KEYCODE_RIGHT, new KeyEvent(KeyEvent.ACTION_DOWN,
									KEYCODE_RIGHT));
							onKeyUp(KEYCODE_RIGHT, new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_RIGHT));
						}
						if (dispX > 0) {
							onKeyDown(KEYCODE_LEFT,
									new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_LEFT));
							onKeyUp(KEYCODE_LEFT, new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_LEFT));
						}
						break;
					case MOVING_DIR_VERT:
						if (dispY < 0) {
							onKeyDown(KEYCODE_DOWN,
									new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_DOWN));
							onKeyUp(KEYCODE_DOWN, new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_DOWN));
						}
						if (dispY > 0) {
							onKeyDown(KEYCODE_UP, new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_UP));
							onKeyUp(KEYCODE_UP, new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_UP));
						}
						break;
					}
				} else {
					if (mTouchedView != null) {
						mMenu.sendClickEventToView(mTouchedView);
						mTouchedView = null;
					}
				}
				motStX = 0;
				motStY = 0;
				isMoving = false;
				break;
			}
			return true;
		}
	};

	// @Override
	public void enableTouchEvents(boolean enabled) {
		isTouchEnabled = enabled;
	}

	// TODO: Optimize onResume() to speed up resuming process.
	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatInfoReceiver, filter);
		if (!getDrawingLayerManager().isEnabled()) {
			getDrawingLayerManager().setDrawingEnabled(true);
		}
		if (!firstInitDone && mMenu != null) {
			showLoadingAnim(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					mMenu.doInit();
					showLoadingAnim(false);
				}

			}).start();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (getDrawingLayerManager().isEnabled()) {
			getDrawingLayerManager().setDrawingEnabled(false);
		}
		unregisterReceiver(mBatInfoReceiver);
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		mMenu.sendKeyDown(keyCode);
		event.startTracking();

		switch (keyCode) {
		case KEYCODE_VOLUME_UP:
			amVolControl.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_SHOW_UI);
			break;
		case KEYCODE_VOLUME_DOWN:
			amVolControl.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_SHOW_UI);
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		mMenu.sendKeyUp(keyCode);
		return true;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent vent) {
		if (bLockedKeys) {
			return true;
		}
		mMenu.sendKeyHold(keyCode);
		return true;
	}

	// Normally, as this is a launcher, we should not call this procedure.
	// As we aren't finished yet, we can't use this launcher as a day-to-day
	// replacement one,
	// that's the reason to be for this procedure.
	@Override
	public void requestActivityEnd() {
		if (getDrawingLayerManager().isEnabled()) {
			getDrawingLayerManager().setDrawingEnabled(false);
		}
		if (mMenu != null) {
			mMenu.doCleanup();
			mMenu.requestDestroy();
		}
		finish();
	}

	@Override
	public void onDestroy() {
		Log.w(getClass().getSimpleName(), "onDestroy():Method was called.");
		if (mMenu != null) {
			mMenu.doCleanup();
		}
		super.onDestroy();
	}

	@Override
	public void lockKeys(boolean locked) {
		bLockedKeys = locked;
	}

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			boolean isCharging = (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) == 1);
			int percent = (level * 100) / scale;

			Log.i(getClass().getSimpleName(),
					"onReceive():Received battery status change broadcast. Level is " + percent
							+ "%, plugged=" + isCharging + ".");

			((XPMB_BaseUILayer) getDrawingLayerManager().getLayer(0)).setBatteryIndicatorStatus(
					percent, isCharging);
		}
	};
}
