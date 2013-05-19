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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Layout;

@SuppressLint("ViewConstructor")
public class XPMB_UI extends XPMB_Layout implements Runnable, SurfaceHolder.Callback {

	public interface UILayer {
		public void drawTo(Canvas canvas);
	}

	private Thread thDraw = null;
	private boolean bIsEnabled = true, bSurfaceExists = false;
	private ArrayList<UILayer> alLayers = null;

	public XPMB_UI(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView);

		Log.v(getClass().getSimpleName(), "XPMB_UI():Start draw thread initialization.");
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		this.setZOrderOnTop(true);

		thDraw = new Thread(this);
		alLayers = new ArrayList<UILayer>();
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		rootView.addView(this);
		Log.v(getClass().getSimpleName(), "XPMB_UI():Finished draw thread initialization.");
	}

	@Override
	public void run() {
		while (bIsEnabled) {
			if (bSurfaceExists) {
				Canvas mcanvas = getHolder().lockCanvas();

				for (UILayer l : alLayers) {
					l.drawTo(mcanvas);
				}

				getHolder().unlockCanvasAndPost(mcanvas);
			}
		}
	}

	public int addLayer(UILayer layer) {
		alLayers.add(layer);
		return alLayers.indexOf(layer);
	}

	public int getLayerIndex(UILayer layer) {
		return alLayers.indexOf(layer);
	}

	public void removeLayer(UILayer layer) {
		alLayers.remove(layer);
	}

	public void removeLayer(int layerIndex) {
		alLayers.remove(layerIndex);
	}

	public boolean isEnabled() {
		return bIsEnabled;
	}

	public void setDrawingEnabled(boolean enabled) {
		bIsEnabled = enabled;
		if (bIsEnabled) {
			thDraw.start();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v(getClass().getSimpleName(), "surfaceChanged():New surface size is " + width + "w "
				+ height + "h");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceCreated():New drawing surface available, "
				+ "starting drawing thread.");
		bSurfaceExists = true;
		thDraw.start();
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
				e.printStackTrace();
			}
		}
	}

}
