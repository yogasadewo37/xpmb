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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class XPMBMediaService extends Service implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener {
	
	private MediaPlayer mMediaPlayer = null;
	
	public static final int STATE_NOT_INITIALIZED = -1, STATE_PLAYING = 0, STATE_STOPPED = 1,
			STATE_PAUSED = 2;

	private int intPlayerStatus = STATE_NOT_INITIALIZED;
	private boolean bInit = false;

	private final IBinder mBinder = new MyBinder();

	public XPMBMediaService() {
		mMediaPlayer = new MediaPlayer();
	}

	public void initialize(Context c) {
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		mMediaPlayer.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
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

}
