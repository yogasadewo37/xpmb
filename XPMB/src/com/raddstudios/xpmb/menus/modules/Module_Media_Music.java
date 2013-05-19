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

package com.raddstudios.xpmb.menus.modules;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBMenu_View;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Activity.MediaPlayerControl;
import com.raddstudios.xpmb.utils.XPMB_Activity.ObjectCollections;
import com.raddstudios.xpmb.utils.XPMB_Layout;

public class Module_Media_Music extends XPMB_Layout implements Modules_Base{

	private ContentResolver cr = null;
	private ObjectCollections mStor = null;
	private XPMBMenuCategory dest = null;
	private XPMBMenu_View container = null;
	private MediaPlayerControl mpc = null;
	private boolean bInit = false, bIsPlaying = false;
	private FinishedListener flListener = null;
	private int intAnimator = 0, intLastItem = -1, intLastPlayed = -1, intMaxItemsOnScreen = 1;
	private ArrayList<String> alCoverKeys = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	private final String SETTING_LAST_ITEM = "mediaplayer.lastitem",
			SETTING_LAST_PLAYED = "mediaplayer.lastplayed",
			SETTING_IS_PLAYING = "mediaplayer.isplaying";

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_MENU_CENTER_ON_ITEM = 2, ANIM_STARTPLAYING = 3, ANIM_STOPPLAYING = 4;

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		private int intAnimType = -1, intAnimItem = -1, intNextItem = -1;
		private int[] iaParams = null;
		private Point[] apInitValues = null;
		private ValueAnimator mOwner = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setParams(int[] params) {
			iaParams = params;
		}

		private void setInitialValues() {
			apInitValues = new Point[dest.getNumSubItems()];
			for (int i = 0; i < dest.getNumSubItems(); i++) {
				apInitValues[i] = dest.getSubitem(i).getPosition();
			}
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				break;
			case ANIM_MENU_CENTER_ON_ITEM:
				mOwner.setDuration(250);
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = iaParams[0];
				break;
			}
			setInitialValues();
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			int dispA = 0, marginA = 0;
			float alphaA = 0.0f, alphaB = 0.0f, alphaC = 0.0f, alphaD = 0.0f;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_MENU_CENTER_ON_ITEM:
				dispA = (int) (((intNextItem - intAnimItem) * -96) * completion);
				if (dispA > 0) {
					marginA = (int) (24 * completion);
				} else {
					marginA = (int) (-24 * completion);
				}
				alphaA = 1.0f - completion;
				alphaB = completion;
				alphaC = 1.0f - (0.5f * completion);
				alphaD = 0.5f + (0.5f * completion);

				for (int y = 0; y < dest.getNumSubItems(); y++) {
					XPMBMenuItemDef xmid = dest.getSubitem(y);

					if (y == intAnimItem || y == intNextItem) {
						xmid.setPositionY(apInitValues[y].y + dispA + marginA);
						if (y == intAnimItem) {
							xmid.setSeparatorAlpha(alphaA);
							xmid.setLabelAlpha(alphaC);
						} else {
							xmid.setSeparatorAlpha(alphaB);
							xmid.setLabelAlpha(alphaD);
						}
					} else {
						xmid.setPositionY(apInitValues[y].y + dispA);
					}
				}
				break;
			case ANIM_NONE:
			default:
				break;
			}
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
			apInitValues = null;
			iaParams = null;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			apInitValues = null;
			iaParams = null;
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}
	};
	
	public Module_Media_Music(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView);

		// alCoverKeys = new ArrayList<String>();
		cr = getRootActivity().getContentResolver();
		mpc = getRootActivity().getPlayerControl();
		if (!mpc.isInitialized()) {
			mpc.initialize();
		}
		mpc.setOnCompletionListener(oclListener);
		mStor = getRootActivity().getStorage();
		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
	}

	private OnCompletionListener oclListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			processkeyDown(XPMB_Main.KEYCODE_SHOULDER_RIGHT);
		}
	};

	public void initialize(XPMBMenu_View owner, XPMBMenuCategory root, FinishedListener finishedL) {
		Log.v(getClass().getSimpleName(), "initialize():Start module initialization.");
		flListener = finishedL;
		container = owner;
		dest = root;
		reloadSettings();
		intMaxItemsOnScreen = ( / 96) + 1;
		Log.v(getClass().getSimpleName(),
				"initialize():Max vertical items on screen: " + String.valueOf(intMaxItemsOnScreen));
		bInit = true;
		Log.v(getClass().getSimpleName(), "initialize():Finished module initialization.");
	}

	private void reloadSettings() {
		intLastItem = (Integer) mStor.getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, -1);
		intLastPlayed = (Integer) mStor.getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_PLAYED,
				-1);
		bIsPlaying = (Boolean) mStor.getObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_IS_PLAYING,
				false);
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Selected Item>=" + String.valueOf(intLastItem));
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<Last Played Item>=" + String.valueOf(intLastPlayed));
		Log.d(getClass().getSimpleName(),
				"reloadSettings():<MediaPlayer is running>=" + String.valueOf(bIsPlaying));
	}

	@Override
	public void deInitialize() {
		mStor.putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_ITEM, intLastItem);
		mStor.putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_LAST_PLAYED, intLastPlayed);
		mStor.putObject(XPMB_Main.SETTINGS_COL_KEY, SETTING_IS_PLAYING, bIsPlaying);

		dest.clearSubitems();
		// for (String ck : alCoverKeys) {
		// mStor.removeObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, ck);
		// }
		// alCoverKeys.clear();
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
				long albumId = mCur.getLong(3);
				String strAlbumId = String.valueOf(albumId);
				String strTrackName = mCur.getString(1);
				String strTrackArtist = mCur.getString(2);
				String strTrackPath = mCur.getString(0);

				Log.d(getClass().getSimpleName(), "loadIn():Found track Name='" + strTrackName
						+ "' Artist='" + strTrackArtist + "' at '" + strTrackPath + "' ID #" + y);

				XPMBMenuItem xmi = new XPMBMenuItem(strTrackName);
				xmi.setLabelB(strTrackArtist);
				xmi.enableTwoLine(true);
				// TODO: post-load cover images to decrease loading times for
				// covers

				xmi.setIcon("theme.icon|icon_music_album_default");
				xmi.setData(new File(strTrackPath));
				xmi.setPositionX(184);
				if (intLastItem != -1) {
					if (y < intLastItem) {
						xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y) - 24);
						xmi.setSeparatorAlpha(0.0f);
						xmi.setLabelAlpha(0.5f);
					} else if (y > intLastItem) {
						xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y) + 24);
						xmi.setSeparatorAlpha(0.0f);
						xmi.setLabelAlpha(0.5f);
					} else {
						xmi.setPositionY((-96 * intLastItem) + 208 + (96 * y));
					}
				} else {
					xmi.setPositionY(208 + (96 * y));
					if (y > 0) {
						xmi.setPositionY(xmi.getPosition().y + 24);
						xmi.setSeparatorAlpha(0.0f);
						xmi.setLabelAlpha(0.5f);
					}
				}
				Log.v(getClass().getSimpleName(),
						"loadin():Item #" + y + " is at [" + xmi.getPosition().x + ","
								+ xmi.getPosition().y + "].");
				xmi.setWidth(96);
				xmi.setHeight(96);

				if (mStor.getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, "media.cover|" + strAlbumId) == null) {
					Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
					Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
					File artFile = getImageFileFromUri(albumArtUri);

					if (artFile != null) {
						if (!artFile.exists()) {
							Log.e(getClass().getSimpleName(),
									"loadIn():Couldn't load album art for mediaID '" + strAlbumId
											+ "' (File not found), using stock album art.");
						} else {
							Long ct = System.currentTimeMillis();
							mStor.putObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, "media.cover|"
									+ strAlbumId,
									BitmapFactory.decodeFile(artFile.getAbsolutePath()));
							xmi.setIcon("media.cover|" + strAlbumId);
							Log.i(getClass().getSimpleName(), "loadIn():Album art caching for ID '"
									+ strAlbumId + "' took " + (System.currentTimeMillis() - ct)
									+ "ms.");
						}
					} else {
						Log.e(getClass().getSimpleName(),
								"loadIn():Couldn't load album art for mediaID '"
										+ strAlbumId
										+ "' (MediaStore returned no rows for path), using stock album art.");
					}
				} else {
					xmi.setIcon("media.cover|" + strAlbumId);
					Log.w(getClass().getSimpleName(), "loadIn():Album art for mediaID '"
							+ strAlbumId + "' already cached, skipping cache process.");

				}

				dest.addSubitem(xmi);
				y++;
				Log.i(getClass().getSimpleName(), "loadIn():Item loading completed for item #" + y
						+ ". Process took " + (System.currentTimeMillis() - t) + "ms.");
			}
			mCur.moveToNext();
		}
		mCur.close();
		Log.i(getClass().getSimpleName(), "loadIn():Track list load finished. Process took "
				+ (System.currentTimeMillis() - startT) + "ms.");
	}

	@Override
	public void processItem(XPMBMenuItem item) {
		if (!bInit) {
			Log.e(getClass().getSimpleName(),
					"loadIn():Module not initialized. You shouldn't even be calling this method.");
			return;
		}
		final XPMBMenuItem f_item = item;
		new Thread(new Runnable() {

			@Override
			public void run() {
				mpc.stop();
				mpc.setMediaSource(((File) f_item.getData()).getAbsolutePath());
				Log.v(getClass().getSimpleName(),
						"processItem():Start playing '"
								+ ((File) f_item.getData()).getAbsolutePath() + "'");
				mpc.play();
				bIsPlaying = true;
			}
		}).run();
	}

	@Override
	public boolean isInitialized() {
		return bInit;
	}

	@Override
	public void processkeyUp(int keyCode) {
	}

	@Override
	public void processkeyDown(int keyCode) {

		switch (keyCode) {
		case XPMB_Main.KEYCODE_SHOULDER_LEFT:
			if (intLastPlayed == 0) {
				break;
			}
			centerOnItem(intLastPlayed - 1);
			processItem((XPMBMenuItem) dest.getSubitem(intLastPlayed - 1));
			intLastPlayed--;
			break;
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_RIGHT:
			if (intLastPlayed == dest.getNumSubItems() - 1) {
				break;
			}
			centerOnItem(intLastPlayed + 1);
			processItem((XPMBMenuItem) dest.getSubitem(intLastPlayed + 1));
			intLastPlayed++;
			break;
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_LEFT:
		case XPMB_Main.KEYCODE_CIRCLE:
			flListener.onFinished(null);
			break;
		case XPMB_Main.KEYCODE_CROSS:
			if (dest.getSelectedSubitem() == intLastPlayed) {
				if (bIsPlaying) {
					mpc.pause();
					bIsPlaying = false;
				} else {
					mpc.play();
					bIsPlaying = true;
				}
			} else {
				processItem((XPMBMenuItem) dest.getSubitem(dest.getSelectedSubitem()));
				intLastPlayed = dest.getSelectedSubitem();
			}
			break;
		}
	}

	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect();
	private int px_i_l = 0, py_i_l = 0, textH = 0;

	private Rect getAlignedAndScaledRect(int left, int top, int width, int height, float scaleX,
			float scaleY, int gravity) {
		int sizeX = (int) (width * scaleX);
		int sizeY = (int) (height * scaleY);
		Rect in = new Rect(left, top, left + width, top + height);
		Rect out = new Rect(0, 0, 0, 0);

		Gravity.apply(gravity, sizeX, sizeY, in, out);

		return out;
	}

	public void drawTo(Canvas canvas) {
		// TODO: Take in account the actual orientation and DPI of the device

		//canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

		// Process subitems
		for (int y = (intLastItem - intMaxItemsOnScreen / 2); y < (intLastItem + intMaxItemsOnScreen); y++) {
			if (y < 0) {
				continue;
			} else if (y > dest.getNumSubItems() - 1) {
				break;
			}
			XPMBMenuItemDef xmi_y = dest.getSubitem(y);

			// Setup Icon
			String strIcon_i = xmi_y.getIcon();
			float alpha_i_i = (255 * xmi_y.getIconAlpha()) * dest.getSubitemsAlpha(), scale_x_i = xmi_y
					.getIconScale().x, scale_y_i = xmi_y.getIconScale().y;
			Rect rLoc = xmi_y.getComputedLocation();
			pParams.setAlpha((int) (alpha_i_i * container.getOpacity()));
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			// Draw Icon
			canvas.drawBitmap(
					(Bitmap) mStor.getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY, strIcon_i, null),
					null,
					getAlignedAndScaledRect(rLoc.left, rLoc.top, rLoc.width(), rLoc.height(),
							scale_x_i, scale_y_i, Gravity.CENTER), pParams);
			pParams.reset();

			// Setup Label
			String strLabel_i = xmi_y.getLabel();
			String strLabel_i_b = xmi_y.getLabelB();
			float alpha_i_l = (255 * xmi_y.getLabelAlpha()) * dest.getSubitemsAlpha();
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			pParams.setTextSize(28);
			pParams.setColor(Color.WHITE);
			pParams.setAlpha((int) (alpha_i_l * container.getOpacity()));
			pParams.setTextAlign(Align.LEFT);
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
				pParams.setShadowLayer(4, 0, 0, Color.WHITE);
			}
			pParams.getTextBounds(strLabel_i, 0, strLabel_i.length(), rTextBounds);
			// textW = rTextBounds.right - rTextBounds.left;
			textH = (int) (Math.abs(pParams.getFontMetrics().ascent) + Math.abs(pParams
					.getFontMetrics().bottom));
			// Draw Label
			px_i_l = rLoc.right + 16;

			if (!xmi_y.isTwoLines()) {
				py_i_l = (int) (rLoc.top + (xmi_y.getSize().y / 2) + (textH / 2) - pParams
						.getFontMetrics().descent);
				canvas.drawText(strLabel_i, px_i_l, py_i_l, pParams);
			} else {
				// Text A
				py_i_l = (int) (rLoc.top + (textH / 4) + textH - pParams.getFontMetrics().descent);
				canvas.drawText(strLabel_i, px_i_l, py_i_l, pParams);
				pParams.setAlpha((int) ((255 * xmi_y.getSeparatorAlpha()) * container.getOpacity()));
				// Line Separator
				py_i_l += (pParams.getFontMetrics().descent + 4);
				canvas.drawLine(px_i_l, py_i_l, canvas.getWidth() - 4, py_i_l, pParams);
				// Text B
				pParams.setAlpha((int) (alpha_i_l * container.getOpacity()));
				py_i_l = (int) (rLoc.bottom - (textH / 4) - (textH / 2) + pParams.getFontMetrics().descent);
				canvas.drawText(strLabel_i_b, px_i_l, py_i_l, pParams);
			}
			pParams.reset();
		}
	}

	public void moveUp() {
		if (dest.getSelectedSubitem() == 0 || dest.getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		dest.setSelectedSubItem(dest.getSelectedSubitem() - 1);
		intLastItem = dest.getSelectedSubitem();
	}

	public void moveDown() {
		if (dest.getSelectedSubitem() == dest.getNumSubItems() - 1 || dest.getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		dest.setSelectedSubItem(dest.getSelectedSubitem() + 1);
		intLastItem = dest.getSelectedSubitem();
	}

	public void centerOnItem(int index) {
		if (index < 0 || index >= dest.getNumSubItems()) {
			return;
		}

		aUIAnimatorW.setParams(new int[] { index });
		aUIAnimatorW.setAnimationType(ANIM_MENU_CENTER_ON_ITEM);
		aUIAnimator.start();

		dest.setSelectedSubItem(index);
		intLastItem = index;
	}

	@Override
	public void setListAnimator(int animator) {
		intAnimator = animator;
	}

	@Override
	public int getListAnimator() {
		return intAnimator;
	}
}
