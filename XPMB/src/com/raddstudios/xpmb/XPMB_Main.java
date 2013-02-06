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
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.raddstudios.xpmb.utils.XPMBMenu;
import com.raddstudios.xpmb.utils.XPMBSubmenu_APP;
import com.raddstudios.xpmb.utils.XPMBSubmenu_GBA;
import com.raddstudios.xpmb.utils.XPMBSubmenu_MUSIC;
import com.raddstudios.xpmb.utils.XPMBSubmenu_NES;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Layout;
import com.raddstudios.xpmb.utils.XPMB_MainMenu;

public class XPMB_Main extends XPMB_Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = 19, KEYCODE_DOWN = 20, KEYCODE_LEFT = 21,
			KEYCODE_RIGHT = 22, KEYCODE_CROSS = 23, KEYCODE_CIRCLE = 4, KEYCODE_SQUARE = 99,
			KEYCODE_TRIANGLE = 100, KEYCODE_SELECT = 109, KEYCODE_START = 108, KEYCODE_MENU = 82,
			KEYCODE_SHOULDER_LEFT = 102, KEYCODE_SHOULDER_RIGHT = 103, KEYCODE_VOLOUME_DOWN = 25,
			KEYCODE_VOLUME_UP = 24;

	private XPMB_MainMenu mMenu = null;
	private XPMB_Layout mSub = null;
	private Handler hMessageBus = null;
	private boolean showingSubmenu = false, bLockedKeys = false, firstInitDone = false;
	private AnimationDrawable bmAnim = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

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
			super.onCreate(savedInstanceState);
			return;
		}

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.xpmb_main);

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

		mMenu = new XPMBMenu(getResources().getXml(R.xml.xmb_layout), hMessageBus, this);

		super.onCreate(savedInstanceState);
	}

	private boolean checkForSupportedDevice() {
		return new File("/system/framework/xperiaplaycertified.jar").exists();
	}

	private void setupAnimations() {

		bmAnim = new AnimationDrawable();
		Bitmap drwAnimSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ui_loading);
		for (int dp = 0; (dp * 128) < drwAnimSrc.getWidth(); dp++) {
			bmAnim.addFrame(
					new BitmapDrawable(getResources(), Bitmap.createBitmap(drwAnimSrc, dp * 128, 0,
							128, 128)), 50);
		}

		bmAnim.setOneShot(false);
		drwAnimSrc = null;
		((ImageView) findViewById(R.id.ivLoadAnim)).setImageDrawable(bmAnim);
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatInfoReceiver, filter);
		if (!firstInitDone) {
			showLoadingAnim(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					mMenu.doInit();
					hMessageBus.post(new Runnable() {

						@Override
						public void run() {
							mMenu.parseInitLayout((ViewGroup) findViewById(R.id.main_l));
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
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		if (showingSubmenu) {
			mSub.sendKeyUp(keyCode);
		} else {
			mMenu.sendKeyUp(keyCode);
		}
		return true;
	}

	@Override
	public void requestActivityEnd() {
		if (mSub != null) {
			mSub.doCleanup((ViewGroup) findViewById(R.id.main_l));
			mSub.requestDestroy();
		}
		if (mMenu != null) {
			mMenu.doCleanup((ViewGroup) findViewById(R.id.main_l));
			mMenu.requestDestroy();
		}
		finish();
	}

	@Override
	public void onDestroy() {
		requestUnloadSubmenu();
		mMenu.doCleanup((ViewGroup) findViewById(R.id.main_l));
		super.onDestroy();
	}

	@Override
	public void requestUnloadSubmenu() {
		if (showingSubmenu) {
			unloadSubmenu();
			mMenu.postExecuteFinished();
			showingSubmenu = false;
		}
	}

	@Override
	public ImageView getCustomBGView() {
		return (ImageView) findViewById(R.id.ivCustomBG);
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

	@Override
	public void preloadSubmenu(String submenu) {
		if (!showingSubmenu) {
			if (submenu.equals("XPMB_Submenu_APP")) {
				mSub = new XPMBSubmenu_APP(this, hMessageBus);
			} else if (submenu.equals("XPMB_Submenu_GBA")) {
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

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					mSub = new XPMBSubmenu_GBA(this, hMessageBus, new File(
							Environment.getExternalStorageDirectory(), "GBA"));
				}
			} else if (submenu.equals("XPMB_Submenu_NES")) {
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

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					mSub = new XPMBSubmenu_NES(this, hMessageBus, new File(
							Environment.getExternalStorageDirectory(), "NES"));
				}

			} else if (submenu.equals("XPMB_Submenu_MUSIC")) {
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

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					mSub = new XPMBSubmenu_MUSIC(this, hMessageBus);
				}

			}
			if (mSub == null) {
				System.err.println("XPMB_Main::preloadSubmenu() : can't load submenu '" + submenu
						+ "'");
				return;
			}
			showLoadingAnim(true);
			lockKeys(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					mSub.doInit();

					hMessageBus.post(new Runnable() {

						@Override
						public void run() {
							mSub.parseInitLayout((ViewGroup) findViewById(R.id.main_l));
							showLoadingAnim(false);
							showingSubmenu = true;
							lockKeys(false);
						}

					});
				}

			}).start();
		}
	}

	private void unloadSubmenu() {
		mSub.doCleanup((ViewGroup) findViewById(R.id.main_l));
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
