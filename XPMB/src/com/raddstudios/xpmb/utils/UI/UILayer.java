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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;

import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.utils.UI.UILayerManager.UILayer_I;

public class UILayer implements UILayer_I {

	public static final int TYPE_VERTICAL = 0, TYPE_HORIZONTAL = 1;

	private XPMBActivity mRoot = null;
	private RectF rConstraints = null;
	private float fOpacity = 1.0f;
	private boolean bVisible = false;
	private Handler hBus = null;

	public UILayer(XPMBActivity root) {
		mRoot = root;
		hBus = new Handler(root.getMainLooper());
		rConstraints = new RectF();
	}

	@Override
	public void initialize() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setVisibility(boolean visibility) {
		bVisible = visibility;
	}

	@Override
	public boolean isVisible() {
		return bVisible;
	}

	protected Handler getMessageBus() {
		return hBus;
	}

	@Override
	public void drawTo(Canvas canvas) {
	}

	public Bitmap flipBitmap(Bitmap src) {
		Matrix m = new Matrix();
		m.preScale(-1, 1);
		Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
		dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		return dst;
	}

	public int pxfd(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mRoot
				.getResources().getDisplayMetrics());
	}

	protected XPMBActivity getRootActivity() {
		return mRoot;
	}

	public void getRectFromTextBounds(Rect bounds, Paint p) {
		bounds.right = bounds.right - bounds.left;
		bounds.bottom = (int) (-p.ascent() + p.descent());
		bounds.left = 0;
		bounds.top = 0;
	}

	protected Rect getScaledRect(Rect source, float scaleX, float scaleY, int gravity) {
		Rect scaledRect = new Rect(0, 0, (int) (source.width() * scaleX),
				(int) (source.height() * scaleY));

		gravitateRect(source, scaledRect, gravity);
		return scaledRect;
	}

	protected void drawText(String text, Rect destRect, Paint p, Canvas destCanvas) {
		Rect tRect = new Rect();
		p.getTextBounds(text, 0, text.length(), tRect);
		tRect.top = (int) p.ascent();
		tRect.bottom = (int) p.descent();
		destCanvas.drawText(text, destRect.left - tRect.left, destRect.top - tRect.top, p);
	}

	public static void gravitateRect(Rect base, Rect source, int gravity) {
		switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
		case Gravity.CENTER_HORIZONTAL:
			source.offsetTo(base.centerX() - (source.width() / 2), source.top);
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
			source.offsetTo(source.left, base.centerY() - (source.height() / 2));
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
	public void setDrawingConstraints(RectF constraints) {
		rConstraints = constraints;
	}

	@Override
	public RectF getDrawingConstraints() {
		return rConstraints;
	}

	public int getMaxItemsOnScreen(int type, int itemSize, int spacingPre, int spacingPos) {
		switch (type) {
		case TYPE_VERTICAL:
			return (int) (rConstraints.height() / (spacingPre + itemSize + spacingPos));
		case TYPE_HORIZONTAL:
			return (int) (rConstraints.width() / (spacingPre + itemSize + spacingPos));
		default:
			return 0;
		}
	}

	@Override
	public void sendKeyDown(int keyCode) {
	}

	@Override
	public void sendKeyUp(int keyCode) {
	}

	@Override
	public void sendKeyHold(int keyCode) {
	}

	@Override
	public void sendClickEvent(Point clickedPoint) {
	}

	@Override
	public void setOpacity(float alpha) {
		fOpacity = alpha;
	}

	@Override
	public float getOpacity() {
		return fOpacity;
	}

}
