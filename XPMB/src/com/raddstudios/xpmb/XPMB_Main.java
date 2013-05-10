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

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.raddstudios.xpmb.menus.XPMBMenu;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;

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
	private Handler hMessageBus = null;
	private boolean bLockedKeys = false, firstInitDone = false;
	private AnimationDrawable bmAnim = null;
	AudioManager amVolControl = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hMessageBus = new Handler();
		if (!checkForSupportedDevice()) {
			Toast tst = Toast.makeText(getWindow().getContext(),
					getString(R.string.strIncorrectDevice), Toast.LENGTH_SHORT);
			tst.show();

			hMessageBus.postDelayed(new Runnable() {

				@Override
				public void run() {
					finish();
				}

			}, 2500);
			return;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.xpmb_main);
		findViewById(R.id.main_l).setOnTouchListener(mTouchListener);

		amVolControl = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);

		setupAnimations();

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				hMessageBus.post(new Runnable() {

					@Override
					public void run() {
						updateTimeLabel();
					}

				});
			}

		}, 0, 10000);

		if (firstInitDone && mMenu != null) {
			return;
		}

		getStorage().createCollection(GRAPH_ASSETS_COL_KEY);
		getStorage().createCollection(SETTINGS_COL_KEY);
		mMenu = new XPMBMenu(getResources().getXml(R.xml.xmb_layout), hMessageBus,
				(ViewGroup) findViewById(R.id.main_l), this);
	}

	private static final int MOVING_DIR_VERT = 0, MOVING_DIR_HORZ = 1;
	private float motStX = 0, motStY = 0, dispX = 0, dispY = 0;
	private int polarity = 1;
	private boolean isMoving = false;
	private boolean isTouchEnabled = true;
	private View mTouchedView = null;

	@Override
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

	@Override
	public void enableTouchEvents(boolean enabled) {
		isTouchEnabled = enabled;
	}

	private boolean checkForSupportedDevice() {
		return true;
		// return new
		// File("/system/framework/xperiaplaycertified.jar").exists();
	}

	private void setupAnimations() {

		bmAnim = new AnimationDrawable();
		Bitmap drwAnimSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ui_loading);
		int bmSizeX = drwAnimSrc.getWidth(), bmSizeY = drwAnimSrc.getHeight(), bmFrameSzx = bmSizeX
				/ bmSizeY;
		for (int dp = 0; dp < bmFrameSzx; dp++) {
			bmAnim.addFrame(
					new BitmapDrawable(getResources(), Bitmap.createBitmap(drwAnimSrc,
							bmSizeY * dp, 0, bmSizeY, bmSizeY)), 50);
		}

		bmAnim.setOneShot(false);
		drwAnimSrc = null;
		((ImageView) findViewById(R.id.ivLoadAnim)).setImageDrawable(bmAnim);
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatInfoReceiver, filter);
		if (!firstInitDone && mMenu != null) {
			showLoadingAnim(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					mMenu.doInit();
					hMessageBus.post(new Runnable() {

						@Override
						public void run() {
							mMenu.parseInitLayout();
							showLoadingAnim(false);
							firstInitDone = true;
						}

					});
				}

			}).start();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
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
		if (mMenu != null) {
			mMenu.doCleanup();
			mMenu.requestDestroy();
		}
		finish();
	}

	@Override
	public void onDestroy() {
		requestUnloadSubmenu();
		if (mMenu != null) {
			mMenu.doCleanup();
		}
		super.onDestroy();
	}

	@Override
	public XPMB_ImageView getCustomBGView() {
		return (XPMB_ImageView) findViewById(R.id.ivCustomBG);
	}

	@Override
	public void lockKeys(boolean locked) {
		bLockedKeys = locked;
	}

	@Override
	public void showLoadingAnim(boolean show) {
		ImageView iv_la = (ImageView) findViewById(R.id.ivLoadAnim);
		if (iv_la != null) {
			if (show) {
				iv_la.setVisibility(View.VISIBLE);
				bmAnim.start();
			} else {
				bmAnim.stop();
				iv_la.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void updateTimeLabel() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		TextView lb_ct = (TextView) findViewById(R.id.lbCurTime);
		if (lb_ct != null) {
			lb_ct.setText(today.format("%d/%m %H:%M "));
		}
	}

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			int percent = (level * 100) / scale;

			ImageView iv_bt = (ImageView) findViewById(R.id.ivBattStatus);
			if (iv_bt != null) {
				if (percent > 80) {
					iv_bt.setImageDrawable(getResources().getDrawable(R.drawable.ui_batt_100));
				} else if (percent < 81 && percent > 60) {
					iv_bt.setImageDrawable(getResources().getDrawable(R.drawable.ui_batt_080));
				} else if (percent < 61 && percent > 40) {
					iv_bt.setImageDrawable(getResources().getDrawable(R.drawable.ui_batt_060));
				} else if (percent < 41 && percent > 20) {
					iv_bt.setImageDrawable(getResources().getDrawable(R.drawable.ui_batt_040));
				} else if (percent < 21 && percent > 4) {
					iv_bt.setImageDrawable(getResources().getDrawable(R.drawable.ui_batt_020));
				} else if (percent < 5) {
					iv_bt.setImageDrawable(getResources().getDrawable(R.drawable.ui_batt_000));
				}
			}
		}
	};
}
