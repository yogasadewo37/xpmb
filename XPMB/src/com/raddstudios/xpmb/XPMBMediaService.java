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

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemMusic;

public class XPMBMediaService extends Service implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

	private MediaPlayer mMediaPlayer = null;

	public static final int STATE_NOT_INITIALIZED = -1, STATE_PLAYING = 0, STATE_STOPPED = 1,
			STATE_PAUSED = 2;
	public static final int ONFINISH_STOP = 0, ONFINISH_NEXT = 1, ONFINISH_LOOP = 2;

	private int intPlayerStatus = STATE_NOT_INITIALIZED, intCurIndex = -1, intRandIndex = -1,
			intOnFinishedAction = ONFINISH_STOP;
	private XPMBMenuCategory xmcPlaylist = null;
	private boolean bInit = false, randomize = false;
	private ArrayList<Integer> alRandom = null;
	private FinishedListener oclFinished = null;
	private RSetNextItem rniNextItem = null;

	private final IBinder mBinder = new MyBinder();

	private class RSetNextItem implements Runnable {
		private int intNextItem = -1;

		public void setNextIndex(int next) {
			intNextItem = next;
		}

		public void run() {
			setMediaSource(intNextItem);
		}
	};

	public XPMBMediaService() {
		mMediaPlayer = new MediaPlayer();
		alRandom = new ArrayList<Integer>();
		rniNextItem = new RSetNextItem();
	}

	public void initialize(Context c) {
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		mMediaPlayer.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	public void setOnCompletionListener(FinishedListener listener) {
		oclFinished = listener;
	}

	public void play() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return;
		}
		if (intPlayerStatus == STATE_PAUSED || intPlayerStatus == STATE_STOPPED) {
			mMediaPlayer.start();
			intPlayerStatus = STATE_PLAYING;
		}
	}

	public void pause() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return;
		}
		if (intPlayerStatus == STATE_PLAYING) {
			mMediaPlayer.pause();
			intPlayerStatus = STATE_PAUSED;
		}
	}

	public void stop() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return;
		}
		if (intPlayerStatus != STATE_NOT_INITIALIZED) {
			pause();
			mMediaPlayer.seekTo(0);
			intPlayerStatus = STATE_STOPPED;
		}
	}

	public int next() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return -1;
		}
		if (randomize) {
			if (alRandom.size() < xmcPlaylist.getNumSubitems()) {
				if (intRandIndex < (alRandom.size() - 1)) {
					int rNext = rangedRandom(0, xmcPlaylist.getNumSubitems() - 1);
					while (alRandom.contains(rNext)) {
						rNext = rangedRandom(0, xmcPlaylist.getNumSubitems() - 1);
					}
					alRandom.add(rNext);
					rniNextItem.setNextIndex(rNext);
					new Thread(rniNextItem).start();
					intRandIndex++;
				} else {
					intRandIndex++;
					rniNextItem.setNextIndex(alRandom.get(intRandIndex));
					new Thread(rniNextItem).start();
				}
				return intRandIndex;
			}
		} else {
			if (intCurIndex < (xmcPlaylist.getNumSubitems() - 1)) {
				rniNextItem.setNextIndex(intCurIndex + 1);
				new Thread(rniNextItem).start();
				return intCurIndex + 1;
			}
		}
		return intCurIndex;
	}

	public int previous() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return -1;
		}
		if (randomize) {
			if (alRandom.size() > 0 && intRandIndex > 0) {
				intRandIndex--;
				rniNextItem.setNextIndex(alRandom.get(intRandIndex));
				new Thread(rniNextItem).start();
				return intRandIndex;
			}
		} else {
			if (intCurIndex > 0) {
				rniNextItem.setNextIndex(intCurIndex - 1);
				new Thread(rniNextItem).start();
				return intCurIndex - 1;
			}
		}
		return intCurIndex;
	}

	public void seekTo(int msec) {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return;
		}
		if (intPlayerStatus != STATE_NOT_INITIALIZED) {
			mMediaPlayer.seekTo(msec);
		}
	}

	public int getCurrentPosition() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return 0;
		}
		if (intPlayerStatus != STATE_NOT_INITIALIZED) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public int getDuration() {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return 0;
		}
		if (intPlayerStatus != STATE_NOT_INITIALIZED) {
			return mMediaPlayer.getDuration();
		}
		return 0;
	}

	public int getPlayerStatus() {
		return intPlayerStatus;
	}

	public void setMediaPlaylist(XPMBMenuCategory source) {
		xmcPlaylist = source;
	}

	public void clearMediaPlaylist() {
		xmcPlaylist = null;
		alRandom.clear();
		intRandIndex = -1;
		intCurIndex = -1;
	}

	public void setMediaSource(int index) {
		if (xmcPlaylist == null || xmcPlaylist.getNumSubitems() == 0) {
			return;
		}
		XPMBMenuItemMusic xmim = (XPMBMenuItemMusic) xmcPlaylist.getSubitem(index);
		setMediaSource(xmim.getTrackPath());
		intCurIndex = index;
		if (randomize) {
			intRandIndex = alRandom.indexOf(index);
			if (intRandIndex == -1) {
				intRandIndex = alRandom.size();
				alRandom.add(index);
			}
		}
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

	public int getCurIndex() {
		return intCurIndex;
	}

	public void setRandomize(boolean random) {
		randomize = random;
	}

	public boolean isRandomizing() {
		return randomize;
	}
	
	public boolean isPlaying(){
		return (intPlayerStatus == STATE_PLAYING);
	}

	public void setOnFinishedBehavior(int behavior) {
		intOnFinishedAction = behavior;
	}

	public int getOnFinishedBehavior() {
		return intOnFinishedAction;
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

	@Override
	public void onCompletion(MediaPlayer mp) {
		switch (intOnFinishedAction) {
		case ONFINISH_NEXT:
			int nItem = next();
			if (oclFinished != null) {
				oclFinished.onFinished(nItem);
			}
			break;
		case ONFINISH_STOP:
		default:
			alRandom.clear();
			intRandIndex = -1;
			intCurIndex = -1;
			break;
		}
	}

	@Override
	public void onDestroy() {
		release();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		XPMBMediaService getService() {
			return XPMBMediaService.this;
		}
	}

	private int rangedRandom(int min, int max) {
		return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
	}

}
