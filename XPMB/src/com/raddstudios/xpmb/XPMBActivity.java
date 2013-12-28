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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.raddstudios.xpmb.menus.XPMBMenuModule;
import com.raddstudios.xpmb.menus.XPMBSideMenu;
import com.raddstudios.xpmb.menus.XPMBSideMenuItem;
import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_GBA;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_NES;
import com.raddstudios.xpmb.menus.modules.media.Module_Media_Music;
import com.raddstudios.xpmb.menus.modules.system.Module_System_Apps;
import com.raddstudios.xpmb.utils.XPMBSettingsManager;
import com.raddstudios.xpmb.utils.UI.GraphicAssetsManager;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.UILayerManager;

@SuppressLint("Registered")
public class XPMBActivity extends Activity {

	// XPERIA Play's physical button Key Codes (stored again here just for
	// convenience)
	public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP,
			KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN, KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT,
			KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT,
			KEYCODE_CROSS = KeyEvent.KEYCODE_DPAD_CENTER, KEYCODE_CIRCLE = KeyEvent.KEYCODE_BACK,
			KEYCODE_SQUARE = KeyEvent.KEYCODE_BUTTON_X,
			KEYCODE_TRIANGLE = KeyEvent.KEYCODE_BUTTON_Y,
			KEYCODE_SELECT = KeyEvent.KEYCODE_BUTTON_SELECT,
			KEYCODE_START = KeyEvent.KEYCODE_BUTTON_START, KEYCODE_MENU = KeyEvent.KEYCODE_MENU,
			KEYCODE_SHOULDER_LEFT = KeyEvent.KEYCODE_BUTTON_L1,
			KEYCODE_SHOULDER_RIGHT = KeyEvent.KEYCODE_BUTTON_R1,
			KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN,
			KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;

	public static final String SETTINGS_BUNDLE_GLOBAL = "com.raddstudios.settings.storage",
			SETTINGS_GLOBAL_COPIED_MENUITEM = "com.raddstudios.settings.copiedmenuitem",
			SETTINGS_GLOBAL_COPIED_MENUITEM_TYPE = "com.raddstudios.settings.copiedmenuitem.type";
	private final String SETTINGS_BUNDLE_KEY = "main.settings", SETTING_NO_TITLE = "notitle",
			SETTING_SHOW_SYSWALLPAPER = "usesyswallpaper", SETTING_KEEP_SCREEN = "keppscreen",
			SETTING_SYSTEM_INITIALIZED = "initialized";

	public static final String MODULE_SYSTEM_DUMMY = "com.xpmb.system.dummy",
			MODULE_MEDIA_MUSIC = "com.xpmb.media.music",
			MODULE_MEDIA_PICTURES = "com.xpmb.media.pictures",
			MODULE_MEDIA_VIDEOS = "com.xpmb.media.video",
			MODULE_SYSTEM_APPS = "com.xpmb.system.apps", MODULE_EMU_GBA = "com.xpmb.emu.gba",
			MODULE_EMU_NES = "com.xpmb.emu.nes", MODULE_EMU_SNES = "com.xpmb.emu.snes";

	public interface FinishedListener {
		public void onFinished(Object data);
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

	private XPMBMediaService mpMedia = null;
	private RelativeLayout rlRootView = null;
	private UILayerManager xuLayerManager = null;
	private Handler hMessageBus = null;
	private GraphicAssetsManager mTheme = null;
	private Hashtable<String, Modules_Base> alModules = null;
	private XPMBSideMenu mSideMenu = null;
	private boolean sideMenuOpen = false;
	private int iLoading = 0;
	private UILayer ulSideMenuOwner = null;
	private XPMBSettingsManager xsm = null;

	private XPMBMenuModule mMenu = null;
	private XPMBUIModule mBaseLayer = null;
	private boolean bLockedKeys = false, bInit = false;
	private AudioManager amVolControl = null;
	private Intent itMediaService = null;

	private FinishedListener cIntentWaitListener = null;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			mpMedia = ((XPMBMediaService.MyBinder) binder).getService();
			Log.i(XPMBActivity.class.getSimpleName(),
					"onServiceConnected():Connected to XPMBMediaService [");
		}

		public void onServiceDisconnected(ComponentName className) {
			mpMedia = null;
			Log.i(XPMBActivity.class.getSimpleName(),
					"onServiceDisconnected():Disconnected from XPMBMediaService");
		}
	};

	public XPMBActivity() {
		super();
	}

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

		itMediaService = new Intent(this, XPMBMediaService.class);
		xsm = new XPMBSettingsManager();
		if (savedInstanceState == null) {
			xsm.readFromFile(new File(getBaseContext().getFilesDir(), SETTINGS_BUNDLE_GLOBAL));
		} else {
			xsm.setRootBundle(savedInstanceState.getBundle(SETTINGS_BUNDLE_GLOBAL));
		}
		reloadSettings();

		rlRootView = new RelativeLayout(getBaseContext());
		rlRootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		hMessageBus = new Handler(Looper.getMainLooper());
		xuLayerManager = new UILayerManager(this);
		mTheme = new GraphicAssetsManager();
		alModules = new Hashtable<String, Modules_Base>();

		setContentView(getRootView());
		this.setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);

		// Setup services
		amVolControl = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);

		initializeStorage();
		initializeThemeManager();

		mBaseLayer = new XPMBUIModule(this);
		mBaseLayer.initialize();
		mBaseLayer.setVisibility(true);
		mSideMenu = new XPMBSideMenu(this);
		getDrawingLayerManager().setAlwaysOnTopLayer(getDrawingLayerManager().addLayer(mBaseLayer));
		mMenu = new XPMBMenuModule(this);
		mMenu.setVisibility(true);
		bInit = getSettingBundle(SETTINGS_BUNDLE_KEY).getBoolean(SETTING_SYSTEM_INITIALIZED, false);
	}

	Runnable rInitialize = new Runnable() {

		@Override
		public void run() {
			setLoading(true);
			mMenu.initialize();
			initializeModules();
			setLoading(false);
		}

	};

	@Override
	public void onStart() {
		if (!bInit) {
			new Thread(rInitialize).start();
		}
		super.onStart();
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatInfoReceiver, filter);
		startService(itMediaService);
		bindService(itMediaService, mConnection, Context.BIND_AUTO_CREATE);
		if (!getDrawingLayerManager().isEnabled()) {
			getDrawingLayerManager().setDrawingEnabled(true);
		}
		xuLayerManager.addLayer(mMenu);
		xuLayerManager.setFocusOnLayer(mMenu);
		super.onResume();
	}

	@Override
	public void onPause() {
		if (getDrawingLayerManager().isEnabled()) {
			getDrawingLayerManager().setDrawingEnabled(false);
		}
		unregisterReceiver(mBatInfoReceiver);
		unbindService(mConnection);
		xuLayerManager.removeLayer(mMenu);
		xuLayerManager.setFocusOnLayer(mBaseLayer);
		super.onPause();
	}

	private void reloadSettings() {
		Bundle settings = getSettingBundle(SETTINGS_BUNDLE_KEY);
		// Setup window params
		if (settings.getBoolean(SETTING_NO_TITLE, true)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (settings.getBoolean(SETTING_SHOW_SYSWALLPAPER, true)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
		if (settings.getBoolean(SETTING_KEEP_SCREEN, true)) {
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

	public void setupSideMenu(XPMBSideMenuItem[] items, int defaultItem) {
		if (!sideMenuOpen) {
			mSideMenu.clearItemSlots();
			for (XPMBSideMenuItem xsmi : items) {
				mSideMenu.setItemSlot(xsmi.getIndex(), xsmi);
			}
			mSideMenu.setSelectedItem(defaultItem);
		}
	}

	public void showSideMenu(UILayer root) {
		if (!sideMenuOpen) {
			ulSideMenuOwner = root;
			xuLayerManager.addLayer(mSideMenu);
			xuLayerManager.setFocusOnLayer(mSideMenu);
			mSideMenu.setVisibility(true);
			mSideMenu.show();
			sideMenuOpen = true;
		}
	}

	public void hideSideMenu() {
		if (sideMenuOpen) {
			xuLayerManager.setFocusOnLayer(ulSideMenuOwner);
			mSideMenu.setVisibility(false);
			xuLayerManager.removeLayer(mSideMenu);
			sideMenuOpen = false;
		}
	}

	@Override
	public void onStop() {
		dispose();
		xsm.getSettingBundle(SETTINGS_BUNDLE_KEY).putBoolean(SETTING_SYSTEM_INITIALIZED, false);
		xsm.writeToFile(new File(getBaseContext().getFilesDir(), SETTINGS_BUNDLE_GLOBAL));
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// mpMedia.release();
		super.onDestroy();
	}

	public void lockKeys(boolean locked) {
		bLockedKeys = locked;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBundle(SETTINGS_BUNDLE_GLOBAL, xsm.getRootBundle());
	}

	public UILayerManager getDrawingLayerManager() {
		return xuLayerManager;
	}

	public ViewGroup getRootView() {
		return rlRootView;
	}

	public Handler getMessageBus() {
		return hMessageBus;
	}

	public GraphicAssetsManager getThemeManager() {
		return mTheme;
	}

	private void initializeModules() {
		setLoading(true);
		while (this.getPlayerControl() == null) {
			// TODO: Wait for service connection before attempting to load any
			// module. This could cause an infinite loop!
			continue;
		}
		alModules.put(MODULE_MEDIA_MUSIC, new Module_Media_Music(this));
		alModules.get(MODULE_MEDIA_MUSIC).initialize(mMenu, mMenu);
		alModules.put(MODULE_EMU_GBA, new Module_Emu_GBA(this));
		alModules.get(MODULE_EMU_GBA).initialize(mMenu, mMenu);
		alModules.put(MODULE_EMU_NES, new Module_Emu_NES(this));
		alModules.get(MODULE_EMU_NES).initialize(mMenu, mMenu);
		alModules.put(MODULE_SYSTEM_APPS, new Module_System_Apps(this));
		alModules.get(MODULE_SYSTEM_APPS).initialize(mMenu, mMenu);
		setLoading(false);
	}

	public Modules_Base getModule(String id) {
		return alModules.get(id);
	}

	public void showModule(String id) {
		if (alModules.containsKey(id)) {
			xuLayerManager.addLayer(alModules.get(id));
			xuLayerManager.setFocusOnLayer(alModules.get(id));
			alModules.get(id).setVisibility(true);
		}
	}

	public void hideModule(String id) {
		if (alModules.containsKey(id)) {
			xuLayerManager.removeLayer(alModules.get(id));
			alModules.get(id).setVisibility(false);
		}
	}

	public Bundle getSettingBundle(String key) {
		return xsm.getSettingBundle(key);
	}

	public void setLoading(boolean loading) {
		if (loading) {
			iLoading++;
		} else if (iLoading > 0) {
			iLoading--;
		}
		if (iLoading > 0) {
			mBaseLayer.setLoadingAnimationVisible(true);
		} else {
			mBaseLayer.setLoadingAnimationVisible(false);
		}
	}

	public XPMBUIModule getMainUILayer() {
		return mBaseLayer;
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

	// TODO: Fix the need to use this function
	// Normally, as this is a launcher, we should not call this procedure.
	// As we aren't finished yet, we can't use this launcher as a day-to-day
	// replacement one, that's the reason to be for this procedure.
	public void requestActivityEnd() {
		//onStop();
		//onDestroy();
		finish();
	}

	public boolean isExtStorageRW() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		return (mExternalStorageAvailable && mExternalStorageWriteable);
	}

	public boolean isActivityAvailable(Intent intent) {
		final PackageManager packageManager = getPackageManager();
		ResolveInfo resolveInfo = packageManager.resolveActivity(intent,
				PackageManager.GET_ACTIVITIES);
		if (resolveInfo != null) {
			return true;
		}
		if (intent.getAction() != Intent.ACTION_MAIN) {
			return true;
		}
		return false;
	}

	public void postIntentStartWait(FinishedListener listener, Intent intent) {
		cIntentWaitListener = listener;
		try {
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException ane) {
			Log.e(getClass().getSimpleName(),
					"postIntentStartWait(): Couldn't find the requested activity '"
							+ intent.getPackage() + "'");
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (cIntentWaitListener != null) {
			cIntentWaitListener.onFinished(data);
		}
	}

	public XPMBMediaService getPlayerControl() {
		return mpMedia;
	}

	private void dispose() {
		alModules.get(MODULE_MEDIA_MUSIC).dispose();
		alModules.get(MODULE_EMU_GBA).dispose();
		alModules.get(MODULE_EMU_NES).dispose();
		alModules.get(MODULE_SYSTEM_APPS).dispose();
		mMenu.dispose();
	}
}
