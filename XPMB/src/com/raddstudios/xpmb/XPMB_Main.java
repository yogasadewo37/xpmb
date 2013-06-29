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
import java.util.zip.ZipFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.raddstudios.xpmb.menus.XPMBMenuModule;
import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_GBA;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_NES;
import com.raddstudios.xpmb.menus.modules.media.Module_Media_Music;
import com.raddstudios.xpmb.menus.modules.system.Module_System_Apps;
import com.raddstudios.xpmb.utils.XPMB_Activity;

public class XPMB_Main extends XPMB_Activity {

	private XPMBMenuModule mMenu = null;
	private XPMBUIModule mBaseLayer = null;
	private boolean bLockedKeys = false, firstInitDone = false;
	private AudioManager amVolControl = null;

	private final String SETTINGS_BUNDLE_KEY = "main.global", SETTING_NO_TITLE = "notitle",
			SETTING_SHOW_SYSWALLPAPER = "usesystemwallpaper", SETTING_KEEP_SCREEN = "keppscreenon";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Call this always after super.onCreate() and before
		// setContentView()!!!
		reloadSettings();
		
		setContentView(getRootView());

		// Setup services
		amVolControl = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);

		initializeStorage();
		initializeThemeManager();

		mBaseLayer = new XPMBUIModule(this);
		getDrawingLayerManager().addLayer(mBaseLayer);
		mMenu = new XPMBMenuModule(this);
	}

	private void reloadSettings() {
		Bundle settings = getSettingBundle(SETTINGS_BUNDLE_KEY);
		// Setup window params
		if (settings.getBoolean(SETTING_NO_TITLE)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (settings.getBoolean(SETTING_SHOW_SYSWALLPAPER)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
		if (settings.getBoolean(SETTING_KEEP_SCREEN)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	private void initializeStorage() {
		File fRootStoragePath = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/XPMB");
		if (!fRootStoragePath.exists()) {
			Log.w(getClass().getSimpleName(),
					"onCreate():Root storage path not found. Creating directory.");
			fRootStoragePath.mkdirs();
		}
	}

	private void initializeThemeManager() {
		File defTheme = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/XPMB/themes/XPMB.zip");
		if (!defTheme.exists()) {
			if (!defTheme.getParentFile().exists()) {
				Log.w(getClass().getSimpleName(),
						"onCreate():Theme path not found. Creating directory.");
				defTheme.getParentFile().mkdir();
			}
			long t = System.currentTimeMillis();
			Log.w(getClass().getSimpleName(),
					"onCreate():Default theme file not found. Dumping default theme into '"
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
		try {
			getThemeManager().reloadTheme(
					new ZipFile(new File(Environment.getExternalStorageDirectory().getPath()
							+ "/XPMB/themes/XPMB.zip"), ZipFile.OPEN_READ));
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"onCreate():Error loading theme file. Now expect NullPointerExceptions");
		}
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
					getDrawingLayerManager().setFocusOnLayer(mMenu);
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
		getDrawingLayerManager().sendKeyDown(keyCode);
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

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		getDrawingLayerManager().sendKeyUp(keyCode);
		return true;
	}

	public boolean onKeyLongPress(int keyCode, KeyEvent vent) {
		if (bLockedKeys) {
			return true;
		}
		getDrawingLayerManager().sendKeyHold(keyCode);
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
		super.requestActivityEnd();
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
			boolean isCharging = (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0);
			int percent = (level * 100) / scale;

			Log.i(getClass().getSimpleName(),
					"onReceive():Received battery status change broadcast. Level is " + percent
							+ "%, plugged=" + isCharging + ".");

			((XPMBUIModule) getDrawingLayerManager().getLayer(0)).setBatteryIndicatorStatus(
					percent, isCharging);
		}
	};
}
