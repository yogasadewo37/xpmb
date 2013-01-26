package com.raddstudios.xpmb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.raddstudios.xpmb.utils.ROMInfo;

//TODO Implement a correct XML Reader for getting info from the ROM Database

public class GBA_launcher extends Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = 19, KEYCODE_DOWN = 20,
			KEYCODE_LEFT = 21, KEYCODE_RIGHT = 22, KEYCODE_CROSS = 23,
			KEYCODE_CIRCLE = 4, KEYCODE_SQUARE = 99, KEYCODE_TRIANGLE = 100,
			KEYCODE_SELECT = 109, KEYCODE_START = 108, KEYCODE_MENU = 82,
			KEYCODE_SHOULDER_LEFT = 102, KEYCODE_SHOULDER_RIGHT = 103,
			KEYCODE_VOLOUME_DOWN = 25, KEYCODE_VOLUME_UP = 24;

	ArrayList<ROMInfo> foundGames;

	int cGame = -1;
	boolean firstBackPress = false, showingSideMenu = false;
	AnimationDrawable bmAnim = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!Build.PRODUCT.equalsIgnoreCase("R800i")
				&& !Build.PRODUCT.equalsIgnoreCase("R800a")
				&& !Build.PRODUCT.equalsIgnoreCase("R800x")) {
			Toast tst = Toast.makeText(getWindow().getContext(),
					getString(R.string.strIncorrectDevice), Toast.LENGTH_SHORT);
			tst.show();

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					finish();
				}

			}, 2500);
			super.onCreate(savedInstanceState);
			return;
		}

		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		setContentView(R.layout.gba_launcher);

		initializeTriggeredEvents();

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
		((ImageView) findViewById(R.id.ivLoadAnim)).setImageDrawable(bmAnim);
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatInfoReceiver, filter);
		
		final Handler mHandler = new Handler();
		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHandler.post(new Runnable() {

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

		foundGames = new ArrayList<ROMInfo>();
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			File storPt = new File(Environment.getExternalStorageDirectory(),
					"GBA");
			storPt.mkdir();
			if (!storPt.isDirectory()) {
				// TODO Handle storagePath exists and is not a directory
				// or cannot create directory
			}
			try {
				File[] storPtCont = storPt.listFiles();
				for (File f : storPtCont) {
					if (f.getName().endsWith(".zip")) {
						ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
						Enumeration<? extends ZipEntry> ze = zf.entries();
						while (ze.hasMoreElements()) {
							ZipEntry zef = ze.nextElement();
							if (zef.getName().endsWith(".gba")
									|| zef.getName().endsWith(".GBA")) {
								InputStream fi = zf.getInputStream(zef);
								fi.skip(0xAC);
								String gameCode = "";
								gameCode += (char) fi.read();
								gameCode += (char) fi.read();
								gameCode += (char) fi.read();
								gameCode += (char) fi.read();
								fi.close();
								String gameCRC = Long
										.toHexString(zef.getCrc())
										.toUpperCase(
												getResources()
														.getConfiguration().locale);
								foundGames.add(new ROMInfo(f.getAbsolutePath(),
										gameCode, gameCRC));
								break;
							}
						}
						zf.close();
					}
				}
			} catch (Exception e) {
				// TODO Handle errors when loading found ROMs
				e.printStackTrace();
			}

			new Thread(new Runnable() {

				@Override
				public void run() {
					((ImageView) findViewById(R.id.ivLoadAnim))
							.setVisibility(View.VISIBLE);
					updateFoundGames();
					((ImageView) findViewById(R.id.ivLoadAnim))
							.setVisibility(View.INVISIBLE);
					((ImageView) findViewById(R.id.ivCover))
							.setImageDrawable(getCorrectCover(0));
					if (foundGames.size() > 1) {
						ImageView iv_nv = ((ImageView) findViewById(R.id.ivNextCover));
						iv_nv.setImageDrawable(getCorrectCover(1));
						iv_nv.setVisibility(View.VISIBLE);
					}
				}

			}).run();

			if (foundGames.size() != 0) {
				cGame = 0;
			}
			updateButtonStatuses();
			reloadGameInfo();
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (bmAnim != null) {
			if (hasFocus) {
				bmAnim.start();
			} else {
				bmAnim.stop();
			}
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KEYCODE_UP:
			firstBackPress = false;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				selectPrevGame();
			}
			break;
		case KEYCODE_DOWN:
			firstBackPress = false;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				selectNextGame();
			}
			break;
		case KEYCODE_TRIANGLE:
			firstBackPress = false;
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
			firstBackPress = false;
			runSelectedGame();
			break;
		case KEYCODE_CIRCLE:
			if (showingSideMenu) {
				firstBackPress = false;
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
			if (!firstBackPress) {
				firstBackPress = true;
				Toast tst = Toast.makeText(getWindow().getContext(),
						getString(R.string.strBackKeyHint), Toast.LENGTH_SHORT);
				tst.show();
			} else {
				return super.onKeyUp(keyCode, event);
			}
			break;
		default:
			firstBackPress = false;
			break;
		}
		return true;
		// return super.onKeyUp(keyCode, event);
	}

	// TODO Find use for this method or give it a GTFO
	// private void triggerOnClickEvent(View v) {
	// v.onKeyDown(KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_DOWN,
	// 0));
	// v.onKeyUp(KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_UP, 0));
	// }

	private void initializeTriggeredEvents() {

		ImageView iv = ((ImageView) findViewById(R.id.ivRunGame));
		if (iv != null) {

			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					runSelectedGame();
				}

			});
		}
		iv = ((ImageView) findViewById(R.id.ivNextGame));
		if (iv != null) {
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectNextGame();
					updateButtonStatuses();
				}

			});
		}
		iv = ((ImageView) findViewById(R.id.ivPrevGame));
		if (iv != null) {
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectPrevGame();
					updateButtonStatuses();
				}

			});
		}
	}

	private void updateButtonStatuses() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (cGame == 0 || cGame == -1) {
				((ImageView) findViewById(R.id.ivPrevGame)).setEnabled(false);
				((ImageView) findViewById(R.id.ivPrevGame)).setAlpha(0.2f);
			} else {
				((ImageView) findViewById(R.id.ivPrevGame)).setEnabled(true);
				((ImageView) findViewById(R.id.ivPrevGame)).setAlpha(1.0f);
			}
			if (cGame == (foundGames.size() - 1) || cGame == -1) {
				((ImageView) findViewById(R.id.ivNextGame)).setEnabled(false);
				((ImageView) findViewById(R.id.ivNextGame)).setAlpha(0.2f);
			} else {
				((ImageView) findViewById(R.id.ivNextGame)).setEnabled(true);
				((ImageView) findViewById(R.id.ivNextGame)).setAlpha(1.0f);
			}
		}
	}

	private void reloadGameInfo() {
		if (cGame == -1) {
			return;
		}
		((TextView) findViewById(R.id.lbGameName)).setText(foundGames
				.get(cGame).getGameName());
		reloadGameBG();
	}

	private void reloadGameBG() {
		ImageView iv_bg = (ImageView) findViewById(R.id.ivGameBackground);
		if (iv_bg.getDrawable() != null) {
			ObjectAnimator rbg_a_pre = ObjectAnimator.ofFloat(iv_bg, "Alpha",
					1.0f, 0.0f);
			rbg_a_pre.setDuration(250);
			rbg_a_pre.start();
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				ImageView iv_bg = (ImageView) findViewById(R.id.ivGameBackground);
				iv_bg.setAlpha(0.0f);
				iv_bg.setImageDrawable(new BitmapDrawable(getResources(),
						foundGames.get(cGame).getGameBackground()));
				if (iv_bg.getDrawable() == null) {
					return;
				}
				ObjectAnimator rbg_a_pos = ObjectAnimator.ofFloat(iv_bg,
						"Alpha", 0.0f, 1.0f);
				rbg_a_pos.setDuration(250);
				rbg_a_pos.start();
			}

		}, 250);
	}

	private void runSelectedGame() {
		if (cGame == -1) {
			return;
		}
		ObjectAnimator zsx = ObjectAnimator.ofFloat(findViewById(R.id.ivCover),
				"ScaleX", 1, 1.15f);
		ObjectAnimator zsy = ObjectAnimator.ofFloat(findViewById(R.id.ivCover),
				"ScaleY", 1, 1.15f);
		AnimatorSet animzoom = new AnimatorSet();
		animzoom.play(zsx).with(zsy);
		animzoom.setDuration(500);
		animzoom.start();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent("android.intent.action.VIEW");
				intent.setComponent(ComponentName
						.unflattenFromString("com.androidemu.gba/.EmulatorActivity"));
				intent.setData(Uri.fromFile(new File(foundGames.get(cGame)
						.getROMPath())));
				intent.setFlags(0x10000000);
				startActivity(intent);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						((ImageView) findViewById(R.id.ivCover)).setScaleX(1);
						((ImageView) findViewById(R.id.ivCover)).setScaleY(1);
					}
				}, 50);

			}
		}, 501);
	}

	private float pxFromDip(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				getResources().getDisplayMetrics());
	}

	private void selectNextGame() {
		if (cGame < (foundGames.size() - 1)) {
			++cGame;
			switch (getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				// Setup Cover Scale and Translate
				ImageView iv_cv = (ImageView) findViewById(R.id.ivCover);
				ImageView iv_pv = (ImageView) findViewById(R.id.ivPrevCover);
				ImageView iv_nv = (ImageView) findViewById(R.id.ivNextCover);
				ImageView iv_fv = (ImageView) findViewById(R.id.ivNextFill);
				// --Main cover
				ObjectAnimator ng_sx_c_l = ObjectAnimator.ofFloat(iv_cv,
						"ScaleX", 1, 0.5f);
				ObjectAnimator ng_sy_c_l = ObjectAnimator.ofFloat(iv_cv,
						"ScaleY", 1, 0.5f);
				ObjectAnimator ng_ty_c_l = ObjectAnimator.ofFloat(iv_cv,
						"TranslationY", 0, pxFromDip(-166));
				ObjectAnimator ng_pvy_c_l = ObjectAnimator.ofFloat(iv_cv,
						"PivotY", iv_cv.getPivotY(), iv_cv.getHeight());
				// --Previous cover
				ObjectAnimator ng_ty_p_l = ObjectAnimator.ofFloat(iv_pv,
						"TranslationY", 0, pxFromDip(-91));
				// --Next cover
				ObjectAnimator ng_sx_n_l = ObjectAnimator.ofFloat(iv_nv,
						"ScaleX", 1.0f, 2.0f);
				ObjectAnimator ng_sy_n_l = ObjectAnimator.ofFloat(iv_nv,
						"ScaleY", 1.0f, 2.0f);
				ObjectAnimator ng_ty_n_l = ObjectAnimator.ofFloat(iv_nv,
						"TranslationY", 0, pxFromDip(-91));
				// --Filler cover
				ObjectAnimator ng_ty_f_l = null;
				if (cGame < (foundGames.size() - 1)) {
					iv_fv.setPivotY(0);
					iv_fv.setVisibility(View.VISIBLE);
					iv_fv.setImageDrawable(getCorrectCover(cGame + 1));
					ng_ty_f_l = ObjectAnimator.ofFloat(iv_fv, "TranslationY",
							0, pxFromDip(-91));
					ng_ty_f_l.setDuration(300);
				}
				AnimatorSet ngas_l_sctr = new AnimatorSet();
				ngas_l_sctr.playTogether(ng_sx_c_l, ng_sy_c_l, ng_ty_c_l,
						ng_pvy_c_l, ng_ty_p_l, ng_sx_n_l, ng_sy_n_l, ng_ty_n_l);
				ngas_l_sctr.setDuration(300);

				// Setup Text Fade-out-then-in
				ObjectAnimator ng_a_n_l_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 1, 0);
				ObjectAnimator ng_a_n_l_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 0, 1);
				ng_a_n_l_pre.setDuration(150);
				ng_a_n_l_pos.setDuration(150);
				AnimatorSet ng_a_l = new AnimatorSet();
				ng_a_l.playSequentially(ng_a_n_l_pre, ng_a_n_l_pos);

				// Run!
				ngas_l_sctr.start();
				if (cGame < (foundGames.size() - 1) && ng_ty_f_l != null) {
					ng_ty_f_l.start();
				}
				ng_a_l.start();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						reloadGameInfo();
					}

				}, 150);

				// Clean Up
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						ImageView iv_cv = (ImageView) findViewById(R.id.ivCover);
						ImageView iv_nv = (ImageView) findViewById(R.id.ivNextCover);
						ImageView iv_pv = (ImageView) findViewById(R.id.ivPrevCover);
						ImageView iv_fv = (ImageView) findViewById(R.id.ivNextFill);
						Drawable cv_dr = iv_nv.getDrawable(), pv_dr = iv_cv
								.getDrawable(), nv_dr = iv_fv.getDrawable();
						iv_cv.setScaleX(1);
						iv_cv.setScaleY(1);
						iv_cv.setTranslationY(0);
						iv_cv.setPivotY(iv_cv.getHeight() / 2);
						iv_cv.setImageDrawable(cv_dr);
						iv_pv.setTranslationY(0);
						iv_pv.setImageDrawable(pv_dr);
						if (cGame == (foundGames.size() - 1)) {
							iv_nv.setVisibility(View.INVISIBLE);
						}
						if (cGame > 0) {
							iv_pv.setVisibility(View.VISIBLE);
						}
						iv_nv.setScaleX(1.0f);
						iv_nv.setScaleY(1.0f);
						iv_nv.setTranslationY(0);
						iv_nv.setImageDrawable(nv_dr);
						iv_fv.setVisibility(View.INVISIBLE);
						iv_fv.setTranslationY(0);
					}

				}, 320);
				break;
			default:
				ObjectAnimator ngac_p_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.ivCover), "Alpha", 1, 0);
				ObjectAnimator ngan_p_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 1, 0);
				ObjectAnimator ngad_p_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameDescription), "Alpha", 1, 0);
				AnimatorSet nga_p_pre = new AnimatorSet();
				nga_p_pre.play(ngac_p_pre).with(ngan_p_pre).with(ngad_p_pre);
				nga_p_pre.setDuration(200);
				nga_p_pre.start();
				ObjectAnimator ngac_p_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.ivCover), "Alpha", 0, 1);
				ObjectAnimator ngan_p_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 0, 1);
				ObjectAnimator ngad_p_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameDescription), "Alpha", 0, 1);
				AnimatorSet nga_p_pos = new AnimatorSet();
				nga_p_pos.play(ngac_p_pos).with(ngan_p_pos).with(ngad_p_pos);
				nga_p_pos.setDuration(300);
				nga_p_pos.setStartDelay(200);
				nga_p_pos.start();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						reloadGameInfo();
					}

				}, 200);
				break;
			}
		}
	}

	private void selectPrevGame() {
		if (cGame > 0) {
			--cGame;
			switch (getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				// Setup Cover Scale and Translate
				ImageView iv_cv = (ImageView) findViewById(R.id.ivCover);
				ImageView iv_nv = (ImageView) findViewById(R.id.ivNextCover);
				ImageView iv_pv = (ImageView) findViewById(R.id.ivPrevCover);
				ImageView iv_fv = (ImageView) findViewById(R.id.ivPrevFill);
				// --Main cover
				ObjectAnimator pg_sx_c_l = ObjectAnimator.ofFloat(iv_cv,
						"ScaleX", 1, 0.5f);
				ObjectAnimator pg_sy_c_l = ObjectAnimator.ofFloat(iv_cv,
						"ScaleY", 1, 0.5f);
				ObjectAnimator pg_ty_c_l = ObjectAnimator.ofFloat(iv_cv,
						"TranslationY", 0, pxFromDip(166));
				ObjectAnimator ng_pvy_c_l = ObjectAnimator.ofFloat(iv_cv,
						"PivotY", iv_cv.getPivotY(), 0);
				// --Next cover
				ObjectAnimator pg_ty_n_l = ObjectAnimator.ofFloat(iv_nv,
						"TranslationY", 0, pxFromDip(91));
				// --Previous cover
				ObjectAnimator ng_sx_p_l = ObjectAnimator.ofFloat(iv_pv,
						"ScaleX", 1.0f, 2.0f);
				ObjectAnimator pg_sy_p_l = ObjectAnimator.ofFloat(iv_pv,
						"ScaleY", 1.0f, 2.0f);
				ObjectAnimator pg_ty_p_l = ObjectAnimator.ofFloat(iv_pv,
						"TranslationY", 0, pxFromDip(91));
				// --Filler cover
				ObjectAnimator pg_ty_f_l = null;
				if (cGame > 0) {
					iv_fv.setPivotY(iv_fv.getHeight());
					iv_fv.setVisibility(View.VISIBLE);
					iv_fv.setImageDrawable(getCorrectCover(cGame - 1));
					pg_ty_f_l = ObjectAnimator.ofFloat(iv_fv, "TranslationY",
							0, pxFromDip(91));
					pg_ty_f_l.setDuration(300);
				}
				AnimatorSet pg_as_l_sctr = new AnimatorSet();
				pg_as_l_sctr.playTogether(pg_sx_c_l, pg_sy_c_l, pg_ty_c_l,
						ng_pvy_c_l, pg_ty_n_l, ng_sx_p_l, pg_sy_p_l, pg_ty_p_l);
				pg_as_l_sctr.setDuration(300);

				// Setup Text Fade-out-then-in
				ObjectAnimator ng_a_n_l_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 1, 0);
				ObjectAnimator ng_a_n_l_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 0, 1);
				ng_a_n_l_pre.setDuration(150);
				ng_a_n_l_pos.setDuration(150);
				AnimatorSet ng_a_l = new AnimatorSet();
				ng_a_l.playSequentially(ng_a_n_l_pre, ng_a_n_l_pos);

				// Run!
				pg_as_l_sctr.start();
				if (cGame > 0 && pg_ty_f_l != null) {
					pg_ty_f_l.start();
				}
				ng_a_l.start();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						reloadGameInfo();
					}

				}, 150);

				// Clean Up
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						ImageView iv_cv = (ImageView) findViewById(R.id.ivCover);
						ImageView iv_nv = (ImageView) findViewById(R.id.ivNextCover);
						ImageView iv_pv = (ImageView) findViewById(R.id.ivPrevCover);
						ImageView iv_fv = (ImageView) findViewById(R.id.ivPrevFill);
						Drawable cv_dr = iv_pv.getDrawable(), nv_dr = iv_cv
								.getDrawable(), pv_dr = iv_fv.getDrawable();
						iv_cv.setImageDrawable(cv_dr);
						iv_cv.setScaleX(1.0f);
						iv_cv.setScaleY(1.0f);
						iv_cv.setTranslationY(0);
						iv_cv.setPivotY(iv_cv.getHeight() / 2);
						iv_nv.setImageDrawable(nv_dr);
						iv_nv.setTranslationY(0);
						if (cGame == 0) {
							iv_pv.setVisibility(View.INVISIBLE);
						}
						if (cGame < foundGames.size()) {
							iv_nv.setVisibility(View.VISIBLE);
						}
						iv_pv.setImageDrawable(pv_dr);
						iv_pv.setScaleX(1.0f);
						iv_pv.setScaleY(1.0f);
						iv_pv.setTranslationY(0);
						iv_fv.setVisibility(View.INVISIBLE);
						iv_fv.setTranslationY(0);
					}

				}, 320);
				break;
			default:
				ObjectAnimator ngac_p_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.ivCover), "Alpha", 1, 0);
				ObjectAnimator ngan_p_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 1, 0);
				ObjectAnimator ngad_p_pre = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameDescription), "Alpha", 1, 0);
				AnimatorSet nga_p_pre = new AnimatorSet();
				nga_p_pre.play(ngac_p_pre).with(ngan_p_pre).with(ngad_p_pre);
				nga_p_pre.setDuration(200);
				nga_p_pre.start();
				ObjectAnimator ngac_p_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.ivCover), "Alpha", 0, 1);
				ObjectAnimator ngan_p_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameName), "Alpha", 0, 1);
				ObjectAnimator ngad_p_pos = ObjectAnimator.ofFloat(
						findViewById(R.id.lbGameDescription), "Alpha", 0, 1);
				AnimatorSet nga_p_pos = new AnimatorSet();
				nga_p_pos.play(ngac_p_pos).with(ngan_p_pos).with(ngad_p_pos);
				nga_p_pos.setDuration(300);
				nga_p_pos.setStartDelay(200);
				nga_p_pos.start();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						reloadGameInfo();
					}

				}, 200);
				break;
			}
		}
	}

	private Drawable getCorrectCover(int gameIndex) {
		if (foundGames.get(gameIndex).getGameCover() == null) {
			return getResources().getDrawable(R.drawable.ui_cover_not_found);
		}
		return new BitmapDrawable(getResources(), foundGames.get(gameIndex)
				.getGameCover());
	}

	private void updateFoundGames() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getResources().openRawResource(R.raw.rominfo)));
			Hashtable<String, String> mpROMInfo = new Hashtable<String, String>();
			String l = null;
			while ((l = br.readLine()) != null) {
				String cCRC = l.substring(l.indexOf("crc=") + 5,
						l.indexOf(" md5=") - 1);
				mpROMInfo.put(cCRC, l);

			}
			br.close();

			for (ROMInfo r : foundGames) {
				l = mpROMInfo.get(r.getGameCRC());
				String romName = l.substring(l.indexOf(" name=") + 7,
						l.indexOf(".gba\""));
				if (romName.indexOf('(') != -1) {
					String romRegions = romName.substring(
							romName.indexOf('(') + 1, romName.indexOf(')'));
					r.setGameRegions(romRegions);
				}
				if (romName.indexOf('(', romName.indexOf(')')) != -1) {
					String romLanguages = romName.substring(romName.indexOf(
							'(', romName.indexOf(')')));
					romLanguages = romLanguages.substring(
							romLanguages.indexOf('(') + 1,
							romLanguages.indexOf(')'));
					r.setGameLanguages(romLanguages);
				}
				if (romName.indexOf('(') != -1) {

					r.setGameName(romName.substring(0, romName.indexOf('(') - 1));
				} else {
					r.setGameName(romName.substring(0, romName.indexOf(".gba")));
				}
				File resStor = new File(new File(
						Environment.getExternalStorageDirectory(), "GBA"),
						"Resources");
				if (resStor.exists()) {
					File fExtRes = new File(resStor, r.getGameCode()
							+ "-CV.jpg");
					if (fExtRes.exists()) {
						r.setGameCover(BitmapFactory
								.decodeStream(new FileInputStream(fExtRes)));
					}
					fExtRes = new File(resStor, r.getGameCode() + "-BG.jpg");
					if (fExtRes.exists()) {
						r.setGameBackground(BitmapFactory
								.decodeStream(new FileInputStream(fExtRes)));
					}
					fExtRes = new File(resStor, "META_DESC");
					if (fExtRes.exists()) {
						BufferedReader ebr = new BufferedReader(
								new InputStreamReader(new FileInputStream(
										fExtRes)));
						String el = null;
						while ((el = ebr.readLine()) != null) {
							if (el.startsWith(r.getGameCode())) {
								r.setGameDescription(el.substring(el.indexOf(r
										.getGameCode()) + 5));
							}
						}
						ebr.close();
					}
				}
			}
		} catch (Exception e) {
			// TODO handle error when updating data from rominfo.db
			e.printStackTrace();
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
