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

package com.raddstudios.xpmb.menus.modules.media;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.XPMBMediaService;
import com.raddstudios.xpmb.menus.XPMBSideMenuItem;
import com.raddstudios.xpmb.menus.XPMBUIModule.XPMBUINotification;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemMusic;
import com.raddstudios.xpmb.utils.XPMBSettingsManager;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V1;

public class Module_Media_Music extends Modules_Base {

	private XPMBMediaService mpc = null;
	private boolean bInit = false, bIsPlaying = false, bLoaded = false;
	private int intLastPlayed = -1;
	private ArrayList<Long> alCoverKeys = null;
	private ProcessItemThread rProcessItem = null;

	private final String SETTINGS_BUNDLE_KEY = "module.media.music",
			SETTING_LAST_ITEM = "lastitem", SETTING_LAST_PLAYED = "lastplayed",
			SETTING_IS_PLAYING = "isplaying";

	@Override
	public String getModuleID() {
		return "com.xpmb.media.music";
	}

	private class ProcessItemThread implements Runnable {

		private XPMBMenuItemMusic f_item = null;

		public void setItem(XPMBMenuItemMusic item) {
			f_item = item;
		}

		@Override
		public void run() {
			mpc.stop();
			mpc.setMediaSource(f_item.getTrackPath());
			Log.v(getClass().getSimpleName(),
					"processItem():Start playing '" + f_item.getTrackName() + "'");
			mpc.play();
			bIsPlaying = true;
		}
	}

	private Runnable rLoadAlbumArts = new Runnable() {
		@Override
		public void run() {
			Long t = System.currentTimeMillis();

			Log.i(getClass().getSimpleName(), "loadAlbumArts():Started album art caching.");
			getRootActivity().setLoading(true);
			ArrayList<Long> loadedKeys = new ArrayList<Long>();

			for (long aid : alCoverKeys) {
				if (!getRootActivity().getThemeManager().assetExists("media.cover|albumid" + aid)) {
					Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
					Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, aid);
					File artFile = getImageFileFromUri(albumArtUri);

					if (artFile != null) {
						if (!artFile.exists()) {
							Log.e(getClass().getSimpleName(),
									"loadAlbumArts():Couldn't load album art for mediaID '" + aid
											+ "' (File not found), using list counter only.");
						} else {
							Long ct = System.currentTimeMillis();
							getRootActivity().getThemeManager().addCustomAsset(
									"media.cover|albumid" + aid,
									BitmapFactory.decodeFile(artFile.getAbsolutePath()));
							loadedKeys.add(aid);
							Log.i(getClass().getSimpleName(),
									"loadAlbumArts():Album art caching for ID '" + aid + "' took "
											+ (System.currentTimeMillis() - ct) + "ms.");
						}
					} else {
						Log.e(getClass().getSimpleName(),
								"loadAlbumArts():Couldn't load album art for mediaID '"
										+ aid
										+ "' (MediaStore returned no rows for path), using list counter only.");
					}
				}
				SystemClock.sleep(10);
			}
			for (int i = 0; i < getContainerCategory().getNumSubitems(); i++) {
				XPMBMenuItemMusic xmim = (XPMBMenuItemMusic) getContainerCategory().getSubitem(i);
				if (loadedKeys.contains(xmim.getAlbumArtID())) {
					xmim.setIconType(XPMBMenuItem.ICON_TYPE_BITMAP);
				}
			}
			Log.i(getClass().getSimpleName(),
					"loadAlbumArts():Album art caching took " + (System.currentTimeMillis() - t)
							+ "ms.");

			getRootActivity().setLoading(false);
		}
	};

	private class SMInfo extends XPMBSideMenuItem {
		@Override
		public int getIndex() {
			return 7;
		}

		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuInfo);
		}
	}

	public Module_Media_Music(XPMBActivity root) {
		super(root);

		alCoverKeys = new ArrayList<Long>();
		rProcessItem = new ProcessItemThread();
	}

	private OnCompletionListener oclListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			sendKeyDown(XPMBActivity.KEYCODE_SHOULDER_RIGHT);
		}
	};

	@Override
	public void initialize(UILayer parentLayer, FinishedListener listener) {
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		super.initialize(parentLayer, listener);
		super.setListAnimator(new SubmenuAnimator_V1(super.getContainerCategory(), this));
		reloadSettings();
		mpc = getRootActivity().getPlayerControl();
		if (!mpc.isInitialized()) {
			mpc.initialize(getRootActivity().getApplicationContext());
		}
		mpc.setOnCompletionListener(oclListener);
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	@Override
	protected void reloadSettings() {
		Bundle settings = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		// getContainerCategory().setSelectedSubitem(settings.getInt(SETTING_LAST_ITEM,
		// -1));
		intLastPlayed = settings.getInt(SETTING_LAST_PLAYED, -1);
		bIsPlaying = settings.getBoolean(SETTING_IS_PLAYING, false);
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>="
						+ String.valueOf(getContainerCategory().getSelectedSubitem()));
		Log.d(getClass().getSimpleName(), "reloadSettings():<Last Played Item>=" + intLastPlayed);
		Log.d(getClass().getSimpleName(), "reloadSettings():<MediaPlayer is running>=" + bIsPlaying);
	}

	@Override
	public void dispose() {
		Bundle saveData = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		// saveData.putInt(SETTING_LAST_ITEM,
		// getContainerCategory().getSelectedSubitem());
		saveData.putInt(SETTING_LAST_PLAYED, intLastPlayed);
		saveData.putBoolean(SETTING_IS_PLAYING, bIsPlaying);
		for (long aid : alCoverKeys) {
			getRootActivity().getThemeManager().removeCustomAsset("media.cover|albumid" + aid);
		}

		if (bLoaded) {
			Log.v(getClass().getSimpleName(), "dispose():Started saving module state.");
			File fMenuState = new File(getRootActivity().getCacheDir(), getModuleID() + ".state");
			try {
				FileOutputStream fos = new FileOutputStream(fMenuState);
				DataOutputStream dos = new DataOutputStream(fos);

				XPMBSettingsManager.writeBundleTo(getContainerCategory().storeInBundle(), dos);

				dos.close();

				Log.v(getClass().getSimpleName(), "dispose():Finished saving module state.");
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "dispose():Couldn't save module state");
			}
		}
		getContainerCategory().clearSubitems();
	}

	public File getImageFileFromUri(Uri uri) {
		File out = null;
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getRootActivity().getContentResolver().query(uri, projection, null, null,
				null);
		cursor.moveToFirst();
		if (cursor.getCount() != 0) {
			out = new File(cursor.getString(0));
		}
		cursor.close();
		return out;
	}

	@Override
	public void loadIn() {
		super.loadIn();
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}

		if (bInit && !bLoaded) {
			File fMenuState = new File(getRootActivity().getCacheDir(), getModuleID() + ".state");
			if (fMenuState.exists()) {
				try {
					FileInputStream fis = new FileInputStream(fMenuState);
					DataInputStream dis = new DataInputStream(fis);

					long t = System.currentTimeMillis();
					Log.i(getClass().getSimpleName(),
							"doInit():Started reading module state cache.");
					super.setContainerCategory(new XPMBMenuCategory(XPMBSettingsManager
							.readBundleFrom(dis)));
					Log.i(getClass().getSimpleName(),
							"doInit():Finished reading module state cache. Took "
									+ (System.currentTimeMillis() - t) + "ms.");

					dis.close();

					for (int id = 0; id < getContainerCategory().getNumSubitems(); id++) {
						XPMBMenuItemMusic xmim = (XPMBMenuItemMusic) getContainerCategory()
								.getSubitem(id);
						xmim.setIconType(XPMBMenuItem.ICON_TYPE_COUNTER);
						if (!alCoverKeys.contains(xmim.getAlbumArtID())) {
							alCoverKeys.add(xmim.getAlbumArtID());
						}
					}

					bLoaded = true;

					new Thread(rLoadAlbumArts).start();
					Log.v(getClass().getSimpleName(), "doInit():Finished module initialization.");
					return;
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "doInit():Couldn't read module state");
				}
			}
		}
		if (bLoaded) {
			Log.i(getClass().getSimpleName(), "loadIn():Module already loaded. Skipping process.");
			return;
		}
		long startT = System.currentTimeMillis();
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM };
		Cursor mCur = getRootActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		mCur.moveToFirst();

		int y = 0;
		while (mCur.isAfterLast() == false) {
			if (mCur.getString(0).startsWith(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
							.getAbsolutePath())) {

				long t = System.currentTimeMillis();
				String strTrackPath = mCur.getString(0);
				String strTrackName = mCur.getString(1);
				String strTrackArtist = mCur.getString(2);
				long albumId = mCur.getLong(3);
				String strTrackAlbum = mCur.getString(4);

				Log.d(getClass().getSimpleName(), "loadIn():Found track Name='" + strTrackName
						+ "' Artist='" + strTrackArtist + "' at '" + strTrackPath + "' ID #" + y);

				XPMBMenuItemMusic xmi = new XPMBMenuItemMusic(strTrackPath);
				xmi.setAlbumArtID(albumId);
				xmi.setTrackName(strTrackName);
				xmi.setAuthorName(strTrackArtist);
				xmi.setAlbumName(strTrackAlbum);

				xmi.setLabel(strTrackName);
				xmi.setLabelB(strTrackArtist);
				xmi.setIconBitmapID("media.cover|albumid" + albumId);
				xmi.enableTwoLine(true);
				xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_COUNTER);
				if (!alCoverKeys.contains(albumId)) {
					alCoverKeys.add(albumId);
				}

				// xmi.setIconBitmapID("theme.icon|icon_music_album_default");
				xmi.setWidth(pxfd(64));
				xmi.setHeight(pxfd(64));

				getContainerCategory().addSubitem(xmi);
				y++;
				Log.i(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
						+ ". Process took " + (System.currentTimeMillis() - t) + "ms.");
			}
			mCur.moveToNext();
		}
		mCur.close();
		getListAnimator().initializeItems();

		bLoaded = true;
		new Thread(rLoadAlbumArts).start();
		Log.i(getClass().getSimpleName(), "loadIn():Track list load finished. Process took "
				+ (System.currentTimeMillis() - startT) + "ms.");
	}

	@Override
	public void processItem(XPMBMenuItemDef item) {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. You shouldn't even be calling this method.");
			return;
		}
		rProcessItem.setItem((XPMBMenuItemMusic) item);
		new Thread(rProcessItem).start();
		intLastPlayed = getContainerCategory().getSelectedSubitem();
		showNotification();
	}

	@Override
	public boolean isInitialized() {
		return bInit;
	}

	@Override
	public void sendKeyDown(int keyCode) {

		switch (keyCode) {
		case XPMBActivity.KEYCODE_SHOULDER_LEFT:
			if (intLastPlayed == 0 || !bIsPlaying) {
				break;
			}
			intLastPlayed--;
			centerOnItem(intLastPlayed);
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(intLastPlayed));
			break;
		case XPMBActivity.KEYCODE_UP:
			moveUp();
			break;
		case XPMBActivity.KEYCODE_SHOULDER_RIGHT:
			if (intLastPlayed == getContainerCategory().getNumSubitems() - 1 || !bIsPlaying) {
				break;
			}
			intLastPlayed++;
			centerOnItem(intLastPlayed);
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(intLastPlayed));
			break;
		case XPMBActivity.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMBActivity.KEYCODE_LEFT:
		case XPMBActivity.KEYCODE_CIRCLE:
			getFinishedListener().onFinished(this);
			break;
		case XPMBActivity.KEYCODE_CROSS:
			if (getContainerCategory().getSelectedSubitem() == intLastPlayed) {
				if (bIsPlaying) {
					mpc.pause();
					bIsPlaying = false;
					hideNotification();
				} else {
					mpc.play();
					bIsPlaying = true;
					showNotification();
				}
			} else {
				processItem(getContainerCategory().getSubitem(
						getContainerCategory().getSelectedSubitem()));
			}
			break;
		case XPMBActivity.KEYCODE_TRIANGLE:
			getRootActivity().setupSideMenu(new XPMBSideMenuItem[] { new SMInfo() }, 7);
			getRootActivity().showSideMenu(this);
			break;
		}
	}

	private void hideNotification() {
		getRootActivity().getMainUILayer().removeNotification(0);
	}

	private void showNotification() {
		hideNotification();
		getRootActivity().getMainUILayer().putNotification(new XPMBUINotification() {
			@Override
			public String getIconKey() {
				return "theme.icon|icon_music";
			}

			@Override
			public String getText() {
				return ((XPMBMenuItemMusic) getContainerCategory().getSubitem(intLastPlayed))
						.getTrackName()
						+ " - "
						+ ((XPMBMenuItemMusic) getContainerCategory().getSubitem(intLastPlayed))
								.getAuthorName();
			}
		});
	}
}
