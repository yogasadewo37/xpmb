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

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.raddstudios.xpmb.utils.ROMInfo;
import com.raddstudios.xpmb.utils.XPMBSubmenu_GBA;

public class XPMB_Submenu_GBA extends Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = 19, KEYCODE_DOWN = 20,
			KEYCODE_LEFT = 21, KEYCODE_RIGHT = 22, KEYCODE_CROSS = 23,
			KEYCODE_CIRCLE = 4, KEYCODE_SQUARE = 99, KEYCODE_TRIANGLE = 100,
			KEYCODE_SELECT = 109, KEYCODE_START = 108, KEYCODE_MENU = 82,
			KEYCODE_SHOULDER_LEFT = 102, KEYCODE_SHOULDER_RIGHT = 103,
			KEYCODE_VOLOUME_DOWN = 25, KEYCODE_VOLUME_UP = 24;

	private boolean showingSideMenu = false, bLockedKeys = false,
			firstInitDone = false;
	private AnimationDrawable bmAnim = null;
	final Handler hMessageBus = new Handler();
	private XPMBSubmenu_GBA mSubmenu = null;
	public ROMInfo ridROMInfoDat = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		setContentView(R.layout.xpmb_submenu);

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
			mSubmenu = new XPMBSubmenu_GBA(new File(
					Environment.getExternalStorageDirectory(), "GBA"),
					hMessageBus, this);
		}

		super.onCreate(savedInstanceState);
	}

	private boolean checkForSupportedDevice() {
		return new File("/system/framework/xperiaplaycertified.jar").exists();
	}

	private void setupAnimations() {
		bmAnim = new AnimationDrawable();
		Bitmap drwAnimSrc = BitmapFactory.decodeResource(getResources(),
				R.drawable.ui_loading);
		for (int dp = 0; (dp * 128) < drwAnimSrc.getWidth(); dp++) {
			bmAnim.addFrame(
					new BitmapDrawable(getResources(), Bitmap.createBitmap(
							drwAnimSrc, dp * 128, 0, 128, 128)), 50);
		}

		bmAnim.setOneShot(false);
		drwAnimSrc = null;
		ImageView iv_la = ((ImageView) findViewById(R.id.ivLoadAnim));
		iv_la.setImageDrawable(bmAnim);
	}

	private void updateTimeLabel() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		TextView lb_ct = (TextView) findViewById(R.id.lbCurTime);
		if (lb_ct != null) {
			lb_ct.setText(today.format("%d/%m %H:%M "));
		}
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
					ridROMInfoDat = new ROMInfo(getResources().getXml(
							R.xml.rominfo_gba), ROMInfo.TYPE_CRC);
					mSubmenu.doInit();
					hMessageBus.post(new Runnable() {

						@Override
						public void run() {
							mSubmenu.parseInitLayout((ViewGroup) findViewById(R.id.absl_submenu_root));
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

	public void LockKeys(boolean locked) {
		bLockedKeys = locked;
	}

	public void showLoadingAnim(boolean show) {
		ImageView iv_la = (ImageView) findViewById(R.id.ivLoadAnim);
		if (iv_la != null) {
			if (show) {
				bmAnim.start();
				iv_la.setVisibility(View.VISIBLE);
			} else {
				bmAnim.stop();
				iv_la.setVisibility(View.INVISIBLE);
			}
		}
	}

	private float pxFromDip(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				getResources().getDisplayMetrics());
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		switch (keyCode) {
		case KEYCODE_UP:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mSubmenu.moveToPrevItem();
			}
			break;
		case KEYCODE_DOWN:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mSubmenu.moveToNextItem();
			}
			break;
		case KEYCODE_TRIANGLE:
			ImageView iv_sm = (ImageView) findViewById(R.id.ivSideMenu);
			if (!showingSideMenu) {
				iv_sm.setVisibility(View.VISIBLE);
				ObjectAnimator sm_tx_s = ObjectAnimator.ofFloat(iv_sm,
						"TranslationX", pxFromDip(145), 0);
				sm_tx_s.setDuration(150);
				sm_tx_s.start();
				showingSideMenu = true;
			} else {
				ObjectAnimator sm_tx_h = ObjectAnimator.ofFloat(iv_sm,
						"TranslationX", 0, pxFromDip(164));
				sm_tx_h.setDuration(150);
				sm_tx_h.start();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						ImageView iv_sm = (ImageView) findViewById(R.id.ivSideMenu);
						iv_sm.setVisibility(View.INVISIBLE);
					}

				}, 150);
				showingSideMenu = false;
			}
			break;
		case KEYCODE_CROSS:
			mSubmenu.runSelectedItem();
			break;
		case KEYCODE_LEFT:
		case KEYCODE_CIRCLE:
			if (showingSideMenu) {
				ObjectAnimator sm_tx_h = ObjectAnimator.ofFloat(
						findViewById(R.id.ivSideMenu), "TranslationX", 0,
						pxFromDip(164));
				sm_tx_h.setDuration(150);
				sm_tx_h.start();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						ImageView iv_sm = (ImageView) findViewById(R.id.ivSideMenu);
						iv_sm.setVisibility(View.INVISIBLE);
					}

				}, 150);
				showingSideMenu = false;
				break;
			}
			finish();
			break;
		default:
			break;
		}
		return true;
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
					iv_bt.setImageDrawable(getResources().getDrawable(
							R.drawable.ui_batt_100));
				} else if (percent < 81 && percent > 60) {
					iv_bt.setImageDrawable(getResources().getDrawable(
							R.drawable.ui_batt_080));
				} else if (percent < 61 && percent > 40) {
					iv_bt.setImageDrawable(getResources().getDrawable(
							R.drawable.ui_batt_060));
				} else if (percent < 41 && percent > 20) {
					iv_bt.setImageDrawable(getResources().getDrawable(
							R.drawable.ui_batt_040));
				} else if (percent < 21 && percent > 4) {
					iv_bt.setImageDrawable(getResources().getDrawable(
							R.drawable.ui_batt_020));
				} else if (percent < 5) {
					iv_bt.setImageDrawable(getResources().getDrawable(
							R.drawable.ui_batt_000));
				}
			}
		}
	};
}
