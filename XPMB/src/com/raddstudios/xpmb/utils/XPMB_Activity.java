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

package com.raddstudios.xpmb.utils;

import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_GBA;
import com.raddstudios.xpmb.menus.modules.games.Module_Emu_NES;
import com.raddstudios.xpmb.menus.modules.media.Module_Media_Music;
import com.raddstudios.xpmb.menus.modules.system.Module_System_Apps;
import com.raddstudios.xpmb.utils.UI.ThemeLoader;
import com.raddstudios.xpmb.utils.UI.XPMB_UILayerManager;

@SuppressLint("Registered")
public class XPMB_Activity extends Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = 19, KEYCODE_DOWN = 20, KEYCODE_LEFT = 21,
			KEYCODE_RIGHT = 22, KEYCODE_CROSS = 23, KEYCODE_CIRCLE = 4, KEYCODE_SQUARE = 99,
			KEYCODE_TRIANGLE = 100, KEYCODE_SELECT = 109, KEYCODE_START = 108, KEYCODE_MENU = 82,
			KEYCODE_SHOULDER_LEFT = 102, KEYCODE_SHOULDER_RIGHT = 103, KEYCODE_VOLUME_DOWN = 25,
			KEYCODE_VOLUME_UP = 24;
	public static final String SETTINGS_KEY_SYSTEM = "com.raddstudios.settings.global";

	public static final String MODULE_SYSTEM_DUMMY = "module.system.dummy",
			MODULE_MEDIA_MUSIC = "module.media.music",
			MODULE_MEDIA_PICTURES = "module.media.pictures",
			MODULE_MEDIA_VIDEOS = "module.media.video", MODULE_SYSTEM_APPS = "module.media.apps",
			MODULE_EMU_GBA = "module.emu.gba", MODULE_EMU_NES = "module.emu.nes",
			MODULE_EMU_SNES = "module.emu.snes";

	private final String APP_SETTINGS_BUNDLE_KEY = "com.raddstudios",
			SETTINGS_SYSTEM_INITIALIZED = "system.initialized";

	public interface FinishedListener {
		public void onFinished(Object data);
	}

	public final class MediaPlayerControl implements MediaPlayer.OnPreparedListener,
			MediaPlayer.OnErrorListener {
		private MediaPlayer mMediaPlayer = null;
		public static final int STATE_NOT_INITIALIZED = -1, STATE_PLAYING = 0, STATE_STOPPED = 1,
				STATE_PAUSED = 2;

		private int intPlayerStatus = STATE_NOT_INITIALIZED;
		private boolean bInit = false;

		public MediaPlayerControl() {
			mMediaPlayer = new MediaPlayer();
		}

		public void initialize() {
			Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
			mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			mMediaPlayer.setOnPreparedListener(this);
			bInit = true;
			Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
		}

		public void setOnCompletionListener(OnCompletionListener listener) {
			mMediaPlayer.setOnCompletionListener(listener);
		}

		public void play() {
			if (intPlayerStatus == STATE_PAUSED || intPlayerStatus == STATE_STOPPED) {
				mMediaPlayer.start();
				intPlayerStatus = STATE_PLAYING;
			}
		}

		public void pause() {
			if (intPlayerStatus == STATE_PLAYING) {
				mMediaPlayer.pause();
				intPlayerStatus = STATE_PAUSED;
			}
		}

		public void stop() {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				pause();
				mMediaPlayer.seekTo(0);
				intPlayerStatus = STATE_STOPPED;
			}
		}

		public void seekTo(int msec) {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				mMediaPlayer.seekTo(msec);
			}
		}

		public int getCurrentPosition() {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				return mMediaPlayer.getCurrentPosition();
			}
			return 0;
		}

		public int getDuration() {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				return mMediaPlayer.getDuration();
			}
			return 0;
		}

		public int getPlayerStatus() {
			return intPlayerStatus;
		}

		public void setMediaSource(String url) {
			intPlayerStatus = STATE_NOT_INITIALIZED;
			try {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.reset();
				mMediaPlayer.setDataSource(url);
				mMediaPlayer.prepareAsync();
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "setMediaSource():" + e.getMessage());
			}
		}

		public void release() {
			mMediaPlayer.release();
			Log.v(getClass().getSimpleName(), "release():MediaPlayer released.");
		}

		public boolean isInitialized() {
			return bInit;
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			mp.reset();
			intPlayerStatus = STATE_NOT_INITIALIZED;
			Log.e(getClass().getSimpleName(), "release():MediaPlayer threw an error.");
			return false;
		}

		@Override
		public void onPrepared(MediaPlayer player) {
			player.start();
			intPlayerStatus = STATE_PLAYING;
		}
	}

	private MediaPlayerControl mpMedia = null;
	private RelativeLayout rlRootView = null;
	private XPMB_UILayerManager xuLayerManager = null;
	private Handler hMessageBus = null;
	private ThemeLoader mTheme = null;
	private Bundle mSettings = null;
	private Hashtable<String, Modules_Base> alModules = null;

	private FinishedListener cIntentWaitListener = null;

	public XPMB_Activity() {
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
		mpMedia = new MediaPlayerControl();
		rlRootView = new RelativeLayout(getBaseContext());
		rlRootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		hMessageBus = new Handler(Looper.getMainLooper());
		xuLayerManager = new XPMB_UILayerManager(this);
		mTheme = new ThemeLoader();
		if (savedInstanceState == null) {
			mSettings = new Bundle();
		} else {
			mSettings = savedInstanceState.getBundle(APP_SETTINGS_BUNDLE_KEY);
		}
		alModules = new Hashtable<String, Modules_Base>();
		initializeModules();
	}
	
	@Override
	public void onResume() {
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBundle(APP_SETTINGS_BUNDLE_KEY, mSettings);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mpMedia.release();
	}

	public XPMB_UILayerManager getDrawingLayerManager() {
		return xuLayerManager;
	}

	public ViewGroup getRootView() {
		return rlRootView;
	}

	public Handler getMessageBus() {
		return hMessageBus;
	}

	public ThemeLoader getThemeManager() {
		return mTheme;
	}

	private void initializeModules() {
		alModules.put(MODULE_MEDIA_MUSIC, new Module_Media_Music(this));
		alModules.put(MODULE_EMU_GBA, new Module_Emu_GBA(this));
		alModules.put(MODULE_EMU_NES, new Module_Emu_NES(this));
		alModules.put(MODULE_SYSTEM_APPS, new Module_System_Apps(this));
	}

	public Modules_Base getModule(String id) {
		return alModules.get(id);
	}

	public Bundle getSettingBundle(String key) {
		if (!mSettings.containsKey(key)) {
			mSettings.putBundle(key, new Bundle());
		}
		return mSettings.getBundle(key);
	}

	public void showLoadingAnim(boolean visible) {
		((XPMBUIModule) xuLayerManager.getLayer(0)).setLoadingAnimationVisible(visible);
	}

	public void lockKeys(boolean locked) {
	}

	public void requestActivityEnd() {
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
		final PackageManager packageManager = getBaseContext().getPackageManager();
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
		startActivityForResult(intent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (cIntentWaitListener != null) {
			cIntentWaitListener.onFinished(data);
		}
	}

	public MediaPlayerControl getPlayerControl() {
		return mpMedia;
	}
}
