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

package com.raddstudios.xpmb.menus.utils.filters;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.utils.XPMBMenuCategory;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItem;
import com.raddstudios.xpmb.menus.utils.XPMBMenuItemDef;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.FinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Activity.MediaPlayerControl;
import com.raddstudios.xpmb.utils.backports.XPMBMenu_View;

public class Filter_Media_Music extends SurfaceView implements Filter_Base, SurfaceHolder.Callback {

	private ContentResolver cr = null;
	private Hashtable<String, Bitmap> bmlStorage = null;
	private XPMBMenuCategory dest = null;
	private XPMBMenu_View container = null;
	private XPMB_Activity act = null;
	private MediaPlayerControl mpc = null;
	private boolean bInit = false;
	private DrawThread mDrwTh = null;
	private FinishedListener flListener = null;
	private int intAnimator = 0;
	private ArrayList<String> alCoverKeys = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1;

	private class UIAnimatorWorker implements AnimatorUpdateListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
		private int pX = 0, pY = 0, destX = 0, destY = 0;
		private ValueAnimator mOwner = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				pY = dest.getSubitem(0).getPosition().y;
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				destY = 96;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				pY = dest.getSubitem(0).getPosition().y;
				intAnimItem = dest.getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				if (intAnimItem > 0)
					destY = -96;
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			int dispA = 0, dispB = 0, marginA = 0, marginB = 0;
			float alphaA = 0.0f, alphaB = 0.0f, scaleA = 0.0f, scaleB = 0.0f;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
				dispA = (int) (destY * completion);
				if (intAnimType == ANIM_MENU_MOVE_UP) {
					marginA = (int) (16 - (16 * completion));
					marginB = (int) (16 * completion);
				} else {
					marginA = (int) (16 * completion);
					marginB = (int) (16 - 16 * completion);
				}

				for (int y = 0; y < dest.getNumSubItems(); y++) {
					XPMBMenuItemDef xmid = dest.getSubitem(y);

					xmid.setPositionY(pY + (96 * y) + dispA);
					if (y == intAnimItem) {
						xmid.setMargins(new Rect(0, marginA, 0, marginA));
					} else if (y == intNextItem) {
						xmid.setMargins(new Rect(0, marginB, 0, marginB));
					}
				}
				break;
			case ANIM_NONE:
			default:
				break;
			}
			// requestRedraw();
		}
	};

	private class DrawThread extends Thread {
		boolean mRun;
		Filter_Media_Music mMenuView;

		public DrawThread(Context ctx, Filter_Media_Music sView) {
			mRun = false;
			mMenuView = sView;
		}

		void setRunning(boolean bRun) {
			mRun = bRun;
		}

		@Override
		public void run() {
			super.run();

			while (mRun) {
				mMenuView.requestRedraw();
			}
		}
	}

	public Filter_Media_Music(Context context) {
		super(context);
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		this.setZOrderOnTop(true);
		alCoverKeys = new ArrayList<String>();
		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
	}

	@SuppressWarnings("unchecked")
	public void initialize(XPMBMenu_View owner, XPMBMenuCategory root, XPMB_Activity resources,
			FinishedListener finishedL) {
		cr = resources.getContentResolver();
		act = resources;
		flListener = finishedL;
		mpc = resources.getPlayerControl();
		if (!mpc.isInitialized()) {
			mpc.initialize();
		}
		bmlStorage = (Hashtable<String, Bitmap>) resources.getStorage().getCollection(
				XPMB_Main.GRAPH_ASSETS_COL_KEY);
		container = owner;
		dest = root;
		bInit = true;
	}

	@Override
	public void deInitialize() {
		dest.clearSubitems();
	}

	@Override
	public void loadIn() {
		if (!bInit) {
			return;
		}
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_ID };
		Cursor mCur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
				null);
		mCur.moveToFirst();

		while (mCur.isAfterLast() == false) {
			if (mCur.getString(0).startsWith(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
							.getAbsolutePath())) {

				long albumId = mCur.getLong(3);
				String strAlbumId = "media.cover|" + String.valueOf(albumId);

				XPMBMenuItem xmi = new XPMBMenuItem(mCur.getString(1) + "\r\n" + mCur.getString(2));
				// xmi.setIcon(strAlbumId);
				xmi.setIcon("theme.icon|icon_music_album_default");
				xmi.setData(new File(mCur.getString(0)));
				xmi.setPosition(new Point(184, 176 + (96 * dest.getNumSubItems())));
				if (dest.getNumSubItems() == 0) {
					xmi.setMargins(new Rect(0, 16, 0, 16));
				}

				try {
					/*
					 * if (!bmlStorage.containsKey(strAlbumId)) { Uri
					 * sArtworkUri =
					 * Uri.parse("content://media/external/audio/albumart"); Uri
					 * albumArtUri = ContentUris.withAppendedId(sArtworkUri,
					 * albumId); bmlStorage.put(strAlbumId,
					 * MediaStore.Images.Media.getBitmap(cr, albumArtUri));
					 * alCoverKeys.add(strAlbumId); }
					 */
				} catch (Exception e) {
					xmi.setIcon("theme.icon|icon_music_album_default");
				}

				dest.addSubitem(xmi);
			}
			mCur.moveToNext();
		}
		mCur.close();
	}

	@Override
	public void processItem(XPMBMenuItem item) {
		if (!bInit) {
			return;
		}

		mpc.setMediaSource(((File) item.getData()).getAbsolutePath());
		mpc.play();
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
			mpc.stop();
			if (dest.getSelectedSubitem() == 0) {
				break;
			}
			mpc.setMediaSource(((File) dest.getSubitem(dest.getSelectedSubitem() - 1))
					.getAbsolutePath());
			mpc.play();
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_RIGHT:
			mpc.stop();
			if (dest.getSelectedSubitem() == dest.getNumSubItems() - 1) {
				break;
			}
			mpc.setMediaSource(((File) dest.getSubitem(dest.getSelectedSubitem() + 1))
					.getAbsolutePath());
			mpc.play();
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			flListener.onFinished(null);
			break;
		case XPMB_Main.KEYCODE_CROSS:
			processItem((XPMBMenuItem) dest.getSubitem(dest.getSelectedSubitem()));
			break;
		}
	}

	private boolean drawing = false;
	private Paint pParams = new Paint();
	private Rect rTextBounds = new Rect();
	// private long lastFrameTime = 0, maxFrameTime = 0, avgTime = 0;
	private int px_i_i = 0, py_i_i = 0, px_i_l = 0, py_i_l = 0, textH = 0;

	public void requestRedraw() {
		if (drawing) {
			return;
		}
		Canvas mcanvas = getHolder().lockCanvas();

		if (mcanvas != null) {
			processDraw(mcanvas);
			getHolder().unlockCanvasAndPost(mcanvas);
		}
	}

	private Rect getAlignedAndScaledRect(int left, int top, int width, int height, float scaleX,
			float scaleY, int gravity) {
		int sizeX = (int) (width * scaleX);
		int sizeY = (int) (height * scaleY);
		Rect in = new Rect(left, top, left + width, top + height);
		Rect out = new Rect(0, 0, 0, 0);

		Gravity.apply(gravity, sizeX, sizeY, in, out);

		return out;
	}

	private void processDraw(Canvas canvas) {
		// TODO: Take in account the actual orientation of the device

		drawing = true;
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

		// Process subitems
		for (int y = 0; y < dest.getNumSubItems(); y++) {
			XPMBMenuItemDef xmi_y = dest.getSubitem(y);

			// Setup Icon
			String strIcon_i = xmi_y.getIcon();
			float alpha_i_i = (255 * xmi_y.getIconAlpha()) * dest.getSubitemsAlpha(), scale_x_i = xmi_y
					.getIconScale().x, scale_y_i = xmi_y.getIconScale().y;
			int size_x_i = bmlStorage.get(strIcon_i).getWidth(), size_y_i = bmlStorage.get(
					strIcon_i).getHeight();
			pParams.setAlpha((int) (alpha_i_i * container.getOpacity()));
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			// Draw Icon
			px_i_i = xmi_y.getPosition().x + xmi_y.getMargins().left;
			py_i_i = xmi_y.getPosition().y + xmi_y.getMargins().top;
			if (y > 0) {
				py_i_i += (dest.getSubitem(y - 1).getMargins().bottom);
			}
			canvas.drawBitmap(
					bmlStorage.get(strIcon_i),
					null,
					getAlignedAndScaledRect(px_i_i, py_i_i, size_x_i, size_y_i, scale_x_i,
							scale_y_i, Gravity.CENTER), pParams);
			pParams.reset();

			// Setup Label
			String strLabel_i = xmi_y.getLabel();
			float alpha_i_l = (255 * xmi_y.getLabelAlpha()) * dest.getSubitemsAlpha();
			pParams.setFlags(Paint.ANTI_ALIAS_FLAG);
			pParams.setTextSize(24);
			pParams.setColor(Color.WHITE);
			pParams.setAlpha((int) (alpha_i_l * container.getOpacity()));
			pParams.setTextAlign(Align.LEFT);
			pParams.setShadowLayer(4, 0, 0, Color.WHITE);
			pParams.getTextBounds(strLabel_i, 0, strLabel_i.length(), rTextBounds);
			// textW = rTextBounds.right - rTextBounds.left;
			textH = (int) (Math.abs(pParams.getFontMetrics().ascent) + Math.abs(pParams
					.getFontMetrics().bottom));
			// Draw Label
			px_i_l = px_i_i + 128;
			py_i_l = (int) (py_i_i + (64 + (textH / 2) - pParams.getFontMetrics().descent));
			canvas.drawText(strLabel_i, px_i_l, py_i_l, pParams);
			pParams.reset();
		}
		drawing = false;
	}

	public void moveUp() {
		if (dest.getSelectedSubitem() == 0 || dest.getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		dest.setSelectedSubItem(dest.getSelectedSubitem() - 1);
	}

	public void moveDown() {
		if (dest.getSelectedSubitem() == dest.getNumSubItems() - 1 || dest.getNumSubItems() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		dest.setSelectedSubItem(dest.getSelectedSubitem() + 1);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mDrwTh = new DrawThread(getContext(), this);
		mDrwTh.setRunning(true);
		mDrwTh.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		mDrwTh.setRunning(false);
		boolean retry = true;

		while (retry) {
			try {
				mDrwTh.join();
				retry = false;
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
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
