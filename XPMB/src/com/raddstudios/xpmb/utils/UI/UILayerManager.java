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

package com.raddstudios.xpmb.utils.UI;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.raddstudios.xpmb.XPMBActivity;

@SuppressLint("ViewConstructor")
public class UILayerManager extends SurfaceView implements Runnable, SurfaceHolder.Callback,
		OnTouchListener {

	public interface UILayer_I {

		public void initialize();

		public void dispose();

		public void drawTo(Canvas canvas);

		public void setOpacity(float alpha);

		public float getOpacity();

		public void setVisibility(boolean visibility);

		public boolean isVisible();

		public void setDrawingConstraints(RectF constraints);

		public RectF getDrawingConstraints();

		public void sendKeyDown(int keyCode);

		public void sendKeyUp(int keyCode);

		public void sendKeyHold(int keyCode);

		public void sendClickEvent(Point clickedPoint);
	}

	// General Vars
	private Thread thDraw = null;
	private boolean bIsEnabled = true, bSurfaceExists = false, bModifyingList = false;
	private ArrayList<UILayer> alLayers = null;
	private Canvas mCanvas = null;
	private int intFocusedLayer = 0, intAlwaysOnTopLayer = -1;
	private RectF rConstraints = null;

	// Touch Events Vars
	private final int MOVING_DIR_VERT = 0, MOVING_DIR_HORZ = 1;
	private float motStX = 0, motStY = 0, dispX = 0, dispY = 0;
	private int polarity = 1;
	private boolean isMoving = false, isTouchEnabled = true;

	// Custom BG Vars
	private Rect bgRect = null;
	private Bitmap bmBG = null;
	private float fBG = 0.0f;
	private Paint pPaint = null;

	public UILayerManager(XPMBActivity root) {
		super(root.getBaseContext());

		Log.v(getClass().getSimpleName(), "UILayerManager():Start draw thread initialization.");
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		super.setZOrderOnTop(true);
		super.setOnTouchListener(this);

		alLayers = new ArrayList<UILayer>();
		rConstraints = new RectF();
		pPaint = new Paint();
		pPaint.setAntiAlias(true);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		root.getRootView().addView(this);
		Log.v(getClass().getSimpleName(),
				"UILayerManager():Finished draw thread initialization.");
	}

	@Override
	public void run() {
		int ly = 0;
		while (bIsEnabled && bSurfaceExists) {
			if (mCanvas == null) {
				mCanvas = getHolder().lockCanvas();
			}

			if (mCanvas != null && !bModifyingList) {
				mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				mCanvas.clipRect(rConstraints, Region.Op.REPLACE);

				if (bmBG != null && fBG > 0.0f) {
					mCanvas.saveLayerAlpha(rConstraints, (int) (255 * fBG),
							Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
					mCanvas.drawBitmap(bmBG, null, bgRect, pPaint);
					mCanvas.restore();
				}

				for (ly = 0; ly < alLayers.size(); ly++) {
					UILayer l = alLayers.get(ly);
					if (ly == intAlwaysOnTopLayer || !l.isVisible()) {
						continue;
					}
					// TODO: Optimize graphics performance before aplying
					// layered alpha
					// mCanvas.saveLayerAlpha(l.getDrawingConstraints(),
					// (int) (255 * l.getOpacity()),
					// Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
					l.drawTo(mCanvas);
					// mCanvas.restore();
				}
				if (intAlwaysOnTopLayer != -1) {
					alLayers.get(intAlwaysOnTopLayer).drawTo(mCanvas);
				}

				getHolder().unlockCanvasAndPost(mCanvas);
				mCanvas = null;
			}
		}
	}

	public void setAlwaysOnTopLayer(int layerIndex) {
		intAlwaysOnTopLayer = layerIndex;
	}

	public int addLayer(UILayer layer) {
		bModifyingList = true;
		layer.setDrawingConstraints(rConstraints);
		alLayers.add(layer);
		Log.d(getClass().getSimpleName(), "addLayer():Added layer '"
				+ layer.getClass().getSimpleName() + "'");
		bModifyingList = false;
		return alLayers.indexOf(layer);
	}

	public int getLayerIndex(UILayer layer) {
		return alLayers.indexOf(layer);
	}

	public UILayer getLayer(int index) {
		return alLayers.get(index);
	}

	public void removeLayer(UILayer layer) {
		bModifyingList = true;
		alLayers.remove(layer);
		Log.d(getClass().getSimpleName(), "removeLayer():Removed layer '"
				+ layer.getClass().getSimpleName() + "'");
		bModifyingList = false;
	}

	public void removeLayer(int layerIndex) {
		bModifyingList = true;
		UILayer layer = alLayers.get(layerIndex);
		alLayers.remove(layer);
		Log.d(getClass().getSimpleName(), "removeLayer():Removed layer '"
				+ layer.getClass().getSimpleName() + "'");
		bModifyingList = false;
	}

	public void setFocusOnLayer(int layer) {
		if (layer < 0 || layer >= alLayers.size()) {
			Log.e(getClass().getSimpleName(), "setFocusOnLayer():Error setting focus on layer #"
					+ layer + ". Layer index out of bounds");
			return;
		}
		intFocusedLayer = layer;
	}

	public void setFocusOnLayer(UILayer layer) {
		int l = alLayers.indexOf(layer);
		if (l == -1) {
			Log.e(getClass().getSimpleName(), "setFocusOnLayer(): Error setting focus on layer '"
					+ layer.getClass().getSimpleName() + "'. Not found in layer array.");
			return;
		}
		intFocusedLayer = l;
	}

	public UILayer getFocusedLayer() {
		return alLayers.get(intFocusedLayer);
	}

	public void updateAllLayersDrawingConstraints() {
		for (UILayer l : alLayers) {
			l.setDrawingConstraints(rConstraints);
		}
	}

	public void setBackgroundBitmap(Bitmap bg) {
		bmBG = bg;
	}

	public void setBackgroundOpacity(float opacity) {
		fBG = opacity;
	}

	public float getBackgroundOpacity() {
		return fBG;
	}

	public boolean isBackgroundBitmapSet() {
		return (bmBG != null);
	}

	@Override
	public boolean isEnabled() {
		return bIsEnabled;
	}

	public void setDrawingEnabled(boolean enabled) {
		bIsEnabled = enabled;
		if (bIsEnabled && bSurfaceExists) {
			thDraw = new Thread(this);
			thDraw.start();
			Log.v(getClass().getSimpleName(), "setDrawingEnabled():Starting drawing thread...");
		} else if (!bIsEnabled) {
			Log.v(getClass().getSimpleName(), "setDrawingEnabled():Stopping drawing thread...");
			boolean retry = true;

			while (retry) {
				try {
					thDraw.join();
					retry = false;
				}

				catch (Exception e) {
					retry = false;
					Log.e(getClass().getSimpleName(),
							"setDrawingEnabled():Error stopping drawing thread.");
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v(getClass().getSimpleName(), "surfaceChanged():New surface size is " + width + "w "
				+ height + "h");

		rConstraints = new RectF(0, 0, width, height);
		bgRect = new Rect((int) rConstraints.left, (int) rConstraints.top,
				(int) rConstraints.right, (int) rConstraints.bottom);
		updateAllLayersDrawingConstraints();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceCreated():New drawing surface available, "
				+ "starting drawing thread.");
		bSurfaceExists = true;
		setDrawingEnabled(true);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceDestroyed():Surface was destroyed, "
				+ "stopping drawing thread.");
		setDrawingEnabled(false);
	}

	public void enableTouchEvents(boolean enabled) {
		isTouchEnabled = enabled;
	}

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
						getFocusedLayer().sendKeyDown(XPMBActivity.KEYCODE_RIGHT);
						getFocusedLayer().sendKeyUp(XPMBActivity.KEYCODE_RIGHT);
					}
					if (dispX > 0) {
						getFocusedLayer().sendKeyDown(XPMBActivity.KEYCODE_LEFT);
						getFocusedLayer().sendKeyUp(XPMBActivity.KEYCODE_LEFT);
					}
					break;
				case MOVING_DIR_VERT:
					if (dispY < 0) {
						getFocusedLayer().sendKeyDown(XPMBActivity.KEYCODE_DOWN);
						getFocusedLayer().sendKeyUp(XPMBActivity.KEYCODE_DOWN);
					}
					if (dispY > 0) {
						getFocusedLayer().sendKeyDown(XPMBActivity.KEYCODE_UP);
						getFocusedLayer().sendKeyUp(XPMBActivity.KEYCODE_UP);
					}
					break;
				}
			} else {
				getFocusedLayer().sendClickEvent(
						new Point((int) arg1.getX(pointerId), (int) arg1.getY(pointerId)));
			}
			motStX = 0;
			motStY = 0;
			isMoving = false;
			break;
		}
		return true;
	}

	public void sendKeyDown(int keyCode) {
		getFocusedLayer().sendKeyDown(keyCode);
	}

	public void sendKeyUp(int keyCode) {
		getFocusedLayer().sendKeyUp(keyCode);
	}

	public void sendKeyHold(int keyCode) {
		getFocusedLayer().sendKeyHold(keyCode);
	}
}
