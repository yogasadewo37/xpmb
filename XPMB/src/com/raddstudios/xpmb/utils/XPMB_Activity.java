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

import java.util.ArrayList;
import java.util.Hashtable;

import com.raddstudios.xpmb.menus.XPMBUIModule;
import com.raddstudios.xpmb.utils.UI.XPMB_UILayerManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

@SuppressLint("Registered")
public class XPMB_Activity extends Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = 19, KEYCODE_DOWN = 20, KEYCODE_LEFT = 21,
			KEYCODE_RIGHT = 22, KEYCODE_CROSS = 23, KEYCODE_CIRCLE = 4, KEYCODE_SQUARE = 99,
			KEYCODE_TRIANGLE = 100, KEYCODE_SELECT = 109, KEYCODE_START = 108, KEYCODE_MENU = 82,
			KEYCODE_SHOULDER_LEFT = 102, KEYCODE_SHOULDER_RIGHT = 103, KEYCODE_VOLUME_DOWN = 25,
			KEYCODE_VOLUME_UP = 24;
	public static final String GRAPH_ASSETS_COL_KEY = "com.raddstudios.graphassets",
			SETTINGS_COL_KEY = "com.raddstudios.settings";
	
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

	public final class ObjectCollections {
		private Hashtable<String, Hashtable<String, Object>> mCollection = null;
		private Hashtable<String, ArrayList<Object>> mList = null;

		public ObjectCollections() {
			mCollection = new Hashtable<String, Hashtable<String, Object>>();
			mList = new Hashtable<String, ArrayList<Object>>();
		}

		public void createList(String name) {
			mList.put(name, new ArrayList<Object>());
		}

		public void createCollection(String name) {
			mCollection.put(name, new Hashtable<String, Object>());
		}

		public void removeList(String name) {
			ArrayList<Object> cArray = mList.get(name);
			if (cArray != null) {
				cArray.clear();
			}
			mList.remove(name);
		}

		public void removeCollection(String name) {
			Hashtable<String, Object> cHash = mCollection.get(name);
			if (cHash != null) {
				cHash.clear();
			}
			mCollection.remove(name);
		}

		public ArrayList<?> getList(String name) {
			return mList.get(name);
		}

		public Hashtable<String, ?> getCollection(String name) {
			return mCollection.get(name);
		}

		public void putObject(String collection, String key, Object value) {
			Hashtable<String, Object> cHash = mCollection.get(collection);
			if (cHash != null) {
				cHash.put(key, value);
			}
		}

		public Object getObject(String collection, String key) {
			return getObject(collection, key, null);
		}

		public Object getObject(String collection, String key, Object defValue) {
			Hashtable<String, Object> cHash = mCollection.get(collection);
			if (cHash != null) {
				if (cHash.get(key) != null) {
					return cHash.get(key);
				} else {
					return defValue;
				}
			}
			return defValue;
		}

		public void copyObject(String srcCollection, String srcKey, String destCollection,
				String destKey) {
			Object itm = mCollection.get(srcCollection).get(srcKey);
			mCollection.get(destCollection).put(destKey, itm);
		}

		public Object removeObject(String collection, String key) {
			Hashtable<String, Object> cHash = mCollection.get(collection);
			if (cHash != null) {
				return cHash.remove(key);
			}
			return null;
		}

		public void release() {
			mCollection.clear();
			mList.clear();
		}
	}

	private MediaPlayerControl mpMedia = null;
	private ObjectCollections ocCollections = null;
	private RelativeLayout rlRootView = null;
	private XPMB_UILayerManager xuLayerManager = null;
	private Handler hMessageBus = null;

	private FinishedListener cIntentWaitListener = null;

	public XPMB_Activity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mpMedia = new MediaPlayerControl();
		ocCollections = new ObjectCollections();
		rlRootView = new RelativeLayout(getBaseContext());
		rlRootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		hMessageBus = new Handler(Looper.getMainLooper());
		xuLayerManager = new XPMB_UILayerManager(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mpMedia.release();
		ocCollections.release();
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

	public void showLoadingAnim(boolean visible) {
		((XPMBUIModule) xuLayerManager.getLayer(0)).setLoadingAnimationVisible(visible);
	}

	public void lockKeys(boolean locked) {
	}

	public void requestActivityEnd() {
	}
	
	public boolean isExtStorageRW(){
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

	public ObjectCollections getStorage() {
		return ocCollections;
	}

	public MediaPlayerControl getPlayerControl() {
		return mpMedia;
	}
}
