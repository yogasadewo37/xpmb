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

package com.raddstudios.xpmb.utils;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

@SuppressLint("ViewConstructor")
public class XPMB_UILayerManager extends SurfaceView implements Runnable, SurfaceHolder.Callback {

	public interface UILayer_I {
		public void drawTo(Canvas canvas);

		public void setDrawingConstraints(Rect constraints);
		
		public Rect getDrawingConstraints();
	}

	private Thread thDraw = null;
	private boolean bIsEnabled = true, bSurfaceExists = false, bModifyingList = false;
	private ArrayList<UILayer> alLayers = null;
	private Canvas mCanvas = null;

	public XPMB_UILayerManager(XPMB_Activity root) {
		super(root.getBaseContext());

		Log.v(getClass().getSimpleName(), "XPMB_UILayerManager():Start draw thread initialization.");
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		this.setZOrderOnTop(true);

		thDraw = new Thread(this);
		alLayers = new ArrayList<UILayer>();
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		root.getRootView().addView(this);
		Log.v(getClass().getSimpleName(),
				"XPMB_UILayerManager():Finished draw thread initialization.");
	}

	@Override
	public void run() {
		int ly = 0;
		while (bIsEnabled) {
			if (bSurfaceExists) {
				if (mCanvas == null){
					mCanvas = getHolder().lockCanvas();
				}

				if (mCanvas != null && !bModifyingList) {
					mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

					for (ly = 0; ly < alLayers.size(); ly++) {
						alLayers.get(ly).drawTo(mCanvas);
					}

					getHolder().unlockCanvasAndPost(mCanvas);
					mCanvas = null;
				}

			}
		}
	}

	public int addLayer(UILayer layer) {
		bModifyingList = true;
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

	public void setDrawingConstraints(Rect constraints) {
		for (UILayer l : alLayers) {
			l.setDrawingConstraints(constraints);
		}
	}

	@Override
	public boolean isEnabled() {
		return bIsEnabled;
	}

	public void setDrawingEnabled(boolean enabled) {
		bIsEnabled = enabled;
		if (bIsEnabled && bSurfaceExists && !thDraw.isAlive()) {
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
		setDrawingConstraints(new Rect(0, 0, width, height));
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceCreated():New drawing surface available, "
				+ "starting drawing thread.");
		bSurfaceExists = true;
		if (bIsEnabled && !thDraw.isAlive()) {
			thDraw.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceDestroyed():Surface was destroyed, "
				+ "stopping drawing thread.");
		bIsEnabled = false;
		bSurfaceExists = false;
		boolean retry = true;

		while (retry) {
			try {
				thDraw.join();
				retry = false;
			}

			catch (Exception e) {
				Log.e(getClass().getSimpleName(),
						"surfaceDestroyed():Error stopping drawing thread.");
				e.printStackTrace();
			}
		}
	}

}
