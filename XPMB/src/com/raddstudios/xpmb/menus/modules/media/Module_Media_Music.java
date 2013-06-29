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

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBSideMenuItem;
import com.raddstudios.xpmb.menus.modules.Modules_Base;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Activity.MediaPlayerControl;
import com.raddstudios.xpmb.utils.UI.UILayer;
import com.raddstudios.xpmb.utils.UI.animators.SubmenuAnimator_V1;

public class Module_Media_Music extends Modules_Base {

	private ContentResolver cr = null;
	private MediaPlayerControl mpc = null;
	private boolean bInit = false, bIsPlaying = false;
	private int intLastPlayed = -1;
	// private ArrayList<String> alCoverKeys = null;
	private ProcessItemThread rProcessItem = null;

	private final String SETTINGS_BUNDLE_KEY = "module.media.music",
			SETTING_LAST_ITEM = "lastitem", SETTING_LAST_PLAYED = "lastplayed",
			SETTING_IS_PLAYING = "isplaying";

	private class ProcessItemThread implements Runnable {

		private XPMBMenuItemDef f_item = null;

		public void setItem(XPMBMenuItemDef item) {
			f_item = item;
		}

		@Override
		public void run() {
			mpc.stop();
			mpc.setMediaSource(((File) f_item.getData()).getAbsolutePath());
			Log.v(getClass().getSimpleName(),
					"processItem():Start playing '" + ((File) f_item.getData()).getAbsolutePath()
							+ "'");
			mpc.play();
			bIsPlaying = true;
		}
	}

	private class SMInfo extends XPMBSideMenuItem {
		@Override
		public String getLabel() {
			return getRootActivity().getString(R.string.strSideMenuInfo);
		}
	}

	public Module_Media_Music(XPMB_Activity root) {
		super(root);

		// alCoverKeys = new ArrayList<String>();
		cr = getRootActivity().getContentResolver();
		mpc = getRootActivity().getPlayerControl();
		if (!mpc.isInitialized()) {
			mpc.initialize();
		}
		mpc.setOnCompletionListener(oclListener);
		rProcessItem = new ProcessItemThread();
	}

	private OnCompletionListener oclListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			sendKeyDown(XPMB_Main.KEYCODE_SHOULDER_RIGHT);
		}
	};

	@Override
	public void initialize(UILayer parentLayer, XPMBMenuCategory container,
			FinishedListener listener) {
		super.initialize(parentLayer, container, listener);
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		super.setListAnimator(new SubmenuAnimator_V1(container, this));
		reloadSettings();
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
	public void deInitialize() {
		Bundle saveData = getRootActivity().getSettingBundle(SETTINGS_BUNDLE_KEY);
		saveData.putInt(SETTING_LAST_ITEM, getContainerCategory().getSelectedSubitem());
		saveData.putInt(SETTING_LAST_PLAYED, intLastPlayed);
		saveData.putBoolean(SETTING_IS_PLAYING, bIsPlaying);
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
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. Refusing to load any item.");
			return;
		}
		long startT = System.currentTimeMillis();
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_ID };
		Cursor mCur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
				null);
		mCur.moveToFirst();

		int y = 0;
		while (mCur.isAfterLast() == false) {
			if (mCur.getString(0).startsWith(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
							.getAbsolutePath())) {

				long t = System.currentTimeMillis();
				// long albumId = mCur.getLong(3);
				// String strAlbumId = String.valueOf(albumId);
				String strTrackName = mCur.getString(1);
				String strTrackArtist = mCur.getString(2);
				String strTrackPath = mCur.getString(0);

				Log.d(getClass().getSimpleName(), "loadIn():Found track Name='" + strTrackName
						+ "' Artist='" + strTrackArtist + "' at '" + strTrackPath + "' ID #" + y);

				XPMBMenuItem xmi = new XPMBMenuItem(strTrackName);
				xmi.setLabelB(strTrackArtist);
				xmi.enableTwoLine(true);
				xmi.setIconType(XPMBMenuItemDef.ICON_TYPE_COUNTER);
				// TODO: post-load cover images to decrease loading times

				xmi.setIconBitmapID("theme.icon|icon_music_album_default");
				xmi.setData(new File(strTrackPath));

				xmi.setWidth(pxfd(64));
				xmi.setHeight(pxfd(64));

				/*
				 * if (mStor.getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY,
				 * "media.cover|" + strAlbumId) == null) { Uri sArtworkUri =
				 * Uri.parse("content://media/external/audio/albumart"); Uri
				 * albumArtUri = ContentUris.withAppendedId(sArtworkUri,
				 * albumId); File artFile = getImageFileFromUri(albumArtUri);
				 * 
				 * if (artFile != null) { if (!artFile.exists()) {
				 * Log.e(getClass().getSimpleName(),
				 * "loadIn():Couldn't load album art for mediaID '" + strAlbumId
				 * + "' (File not found), using stock album art."); } else {
				 * Long ct = System.currentTimeMillis();
				 * mStor.putObject(XPMB_Main.GRAPH_ASSETS_COL_KEY,
				 * "media.cover|" + strAlbumId,
				 * BitmapFactory.decodeFile(artFile.getAbsolutePath()));
				 * xmi.setIcon("media.cover|" + strAlbumId);
				 * Log.i(getClass().getSimpleName(),
				 * "loadIn():Album art caching for ID '" + strAlbumId +
				 * "' took " + (System.currentTimeMillis() - ct) + "ms."); } }
				 * else { Log.e(getClass().getSimpleName(),
				 * "loadIn():Couldn't load album art for mediaID '" + strAlbumId
				 * +
				 * "' (MediaStore returned no rows for path), using stock album art."
				 * ); } } else { xmi.setIcon("media.cover|" + strAlbumId);
				 * Log.w(getClass().getSimpleName(),
				 * "loadIn():Album art for mediaID '" + strAlbumId +
				 * "' already cached, skipping cache process.");
				 * 
				 * }
				 */

				getContainerCategory().addSubitem(xmi);
				y++;
				Log.i(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
						+ ". Process took " + (System.currentTimeMillis() - t) + "ms.");
			}
			mCur.moveToNext();
		}
		mCur.close();
		getListAnimator().initializeItems();
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
		rProcessItem.setItem(item);
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
		case XPMB_Main.KEYCODE_SHOULDER_LEFT:
			if (intLastPlayed == 0) {
				break;
			}
			centerOnItem(intLastPlayed - 1);
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(intLastPlayed - 1));
			intLastPlayed--;
			break;
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_RIGHT:
			if (intLastPlayed == getContainerCategory().getNumSubitems() - 1) {
				break;
			}
			centerOnItem(intLastPlayed + 1);
			processItem((XPMBMenuItem) getContainerCategory().getSubitem(intLastPlayed + 1));
			intLastPlayed++;
			break;
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_LEFT:
		case XPMB_Main.KEYCODE_CIRCLE:
			getFinishedListener().onFinished(null);
			break;
		case XPMB_Main.KEYCODE_CROSS:
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
		}
	}
}
