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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Gravity;

import com.raddstudios.xpmb.utils.XPMB_UILayerManager.UILayer_I;

public class UILayer implements UILayer_I {
	private XPMB_Activity mRoot = null;
	private Rect rConstraints = null;

	public UILayer(XPMB_Activity root) {
		mRoot = root;
		rConstraints = new Rect(0, 0, getRootActivity().getRootView().getWidth(), getRootActivity()
				.getRootView().getHeight());
	}

	@Override
	public void drawTo(Canvas canvas) {
	}

	@Override
	public void setDrawingConstraints(Rect constraints) {
		rConstraints = constraints;
	}

	protected int pxfd(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mRoot
				.getResources().getDisplayMetrics());
	}

	protected XPMB_Activity getRootActivity() {
		return mRoot;
	}

	public Rect getBoundsFromTextRect(Rect source) {
		Rect out = new Rect();
		out.right = source.right - source.left;
		out.bottom = Math.abs(source.top) + Math.abs(source.bottom);
		return out;
	}

	protected Rect getScaledRect(Rect source, float scaleX, float scaleY, int gravity) {
		Rect scaledRect = new Rect(0, 0, (int) (source.width() * scaleX),
				(int) (source.height() * scaleY));

		centerRect(source, scaledRect, gravity);
		return scaledRect;
	}

	protected void centerRect(Rect base, Rect source, int gravity) {
		switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
		case Gravity.CENTER_HORIZONTAL:
			source.offsetTo(base.left + ((base.width() - source.width()) / 2), source.top);
			break;
		case Gravity.LEFT:
			source.offsetTo(base.left, source.top);
			break;
		case Gravity.RIGHT:
			source.offsetTo(base.right - source.width(), source.top);
			break;
		}
		switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
		case Gravity.CENTER_VERTICAL:
			source.offsetTo(source.left, base.top + ((base.height() - source.height()) / 2));
			break;
		case Gravity.TOP:
			source.offsetTo(source.left, base.top);
			break;
		case Gravity.BOTTOM:
			source.offsetTo(source.left, base.bottom - source.height());
			break;
		}
	}

	@Override
	public Rect getDrawingConstraints() {
		return rConstraints;
	}

}
