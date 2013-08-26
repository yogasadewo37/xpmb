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

import java.io.File;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.XPMBActivity.FinishedListener;
import com.raddstudios.xpmb.XPMBMediaService;
import com.raddstudios.xpmb.menus.XPMBSideMenuItem;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemMusic;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V1;

public class Module_Media_Music extends Modules_Base {

	private ContentResolver cr = null;
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
		cr = getRootActivity().getContentResolver();
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
		super.initialize(parentLayer, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
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
		getContainerCategory().setSelectedSubitem(settings.getInt(SETTING_LAST_ITEM, -1));
		intLastPlayed = settings.getInt(SETTING_LAST_PLAYED, -1);
		bIsPlaying = settings.getBoolean(SETTING_IS_PLAYING, false);
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>="
						+ String.valueOf(getContainerCategory().getSelectedSubitem()));
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Last Played Item>=" + String.valueOf(intLastPlayed));
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<MediaPlayer is running>=" + String.valueOf(bIsPlaying));
	}

	@Override
	public void dispose() {
		Bundle saveData = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		saveData.putInt(SETTING_LAST_ITEM, getContainerCategory().getSelectedSubitem());
		saveData.putInt(SETTING_LAST_PLAYED, intLastPlayed);
		saveData.putBoolean(SETTING_IS_PLAYING, bIsPlaying);
		getContainerCategory().clearSubitems();
		for (long aid : alCoverKeys) {
			getRootActivity().getThemeManager().removeCustomAsset("media.cover|albumid" + aid);
		}
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
	public void loadIn(XPMBMenuCategory dest) {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}
		if (bLoaded) {
			Log.i(getClass().getSimpleName(), "loadIn():Module already loaded. Skipping process.");
			return;
		}
		super.loadIn(dest);
		reloadSettings();
		super.setListAnimator(new SubmenuAnimator_V1(dest, this));
		long startT = System.currentTimeMillis();
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM };
		Cursor mCur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
				null);
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

				//xmi.setIconBitmapID("theme.icon|icon_music_album_default");
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
		Log.i(getClass().getSimpleName(), "loadIn():Track list load finished. Process took "
				+ (System.currentTimeMillis() - startT) + "ms.");
		new Thread(rLoadAlbumArts).start();
	}

	private Runnable rLoadAlbumArts = new Runnable() {
		@Override
		public void run() {
			Long t = System.currentTimeMillis();

			Log.i(getClass().getSimpleName(), "loadAlbumArts():Started album art caching.");
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
		}
	};

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
	}

	@Override
	public boolean isInitialized() {
		return bInit;
	}

	@Override
	public void sendKeyDown(int keyCode) {

		switch (keyCode) {
		case XPMBActivity.KEYCODE_SHOULDER_LEFT:
			if (intLastPlayed == 0) {
				break;
			}
			centerOnItem(intLastPlayed - 1);
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(intLastPlayed - 1));
			intLastPlayed--;
			break;
		case XPMBActivity.KEYCODE_UP:
			moveUp();
			break;
		case XPMBActivity.KEYCODE_SHOULDER_RIGHT:
			if (intLastPlayed == getContainerCategory().getNumSubitems() - 1) {
				break;
			}
			centerOnItem(intLastPlayed + 1);
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(intLastPlayed + 1));
			intLastPlayed++;
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
				} else {
					mpc.play();
					bIsPlaying = true;
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
}
