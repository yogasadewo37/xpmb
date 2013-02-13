package com.raddstudios.xpmb.utils.backports;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

@SuppressWarnings("deprecation")
public class XPMB_RelativeLayout extends View implements XPMB_View {

	private float mAlpha = 1.0f, mScaleX = 1.0f, mScaleY = 1.0f;
	private int scaleGravity;
	private Bitmap bmObject = null;
	private Canvas cvObject = null;
	private Paint cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private FrameLayout mContainer = null;
	private RelativeLayout mItem = null;

	public XPMB_RelativeLayout(Context context) {
		super(context);
		mContainer = new FrameLayout(context);
		mItem = new RelativeLayout(context);
		mContainer.addView(mItem);
	}

	@Override
	public void onDraw(Canvas canvas) {
		cvObject.drawColor(0x00FFFFFF, Mode.CLEAR);
		mContainer.draw(cvObject);
		cPaint.setAlpha((int) (255 * mAlpha));
		canvas.drawBitmap(bmObject, 0, 0, cPaint);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mContainer.measure(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mContainer.layout(left, top, right, bottom);
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public void setX(float x) {
		if (super.getLayoutParams() != null) {
			if (super.getLayoutParams() instanceof AbsoluteLayout.LayoutParams) {
				AbsoluteLayout.LayoutParams allp = (AbsoluteLayout.LayoutParams) super
						.getLayoutParams();
				allp.x = (int) x;
				super.setLayoutParams(allp);
			}
		}
	}

	@Override
	public float getX() {
		if (super.getLayoutParams() != null) {
			if (super.getLayoutParams() instanceof AbsoluteLayout.LayoutParams) {
				AbsoluteLayout.LayoutParams allp = (AbsoluteLayout.LayoutParams) super
						.getLayoutParams();
				return allp.x;
			}
		}
		return 0;
	}

	@Override
	public void setY(float y) {
		if (super.getLayoutParams() != null) {
			if (super.getLayoutParams() instanceof AbsoluteLayout.LayoutParams) {
				AbsoluteLayout.LayoutParams allp = (AbsoluteLayout.LayoutParams) super
						.getLayoutParams();
				allp.y = (int) y;
				super.setLayoutParams(allp);
			}
		}
	}

	@Override
	public float getY() {
		if (super.getLayoutParams() != null) {
			if (super.getLayoutParams() instanceof AbsoluteLayout.LayoutParams) {
				AbsoluteLayout.LayoutParams allp = (AbsoluteLayout.LayoutParams) super
						.getLayoutParams();
				return allp.y;
			}
		}
		return 0;
	}
	
	public void addView(View child){
		mItem.addView(child);
		super.invalidate();
	}
	
	public void removeView(View child){
		mItem.removeView(child);
		super.invalidate();
	}

	@Override
	public void setAlpha(float value) {
		mAlpha = value;
		super.invalidate();
	}

	@Override
	public float getAlpha() {
		return mAlpha;
	}

	private void resetBitmap(int width, int height) {
		bmObject = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		cvObject = new Canvas(bmObject);
	}

	@Override
	public void setLayoutParams(LayoutParams params) {
		super.setLayoutParams(params);
		mContainer.setLayoutParams(new ViewGroup.LayoutParams(params));
		FrameLayout.LayoutParams fllp = new FrameLayout.LayoutParams(params);
		fllp.height = (int) (params.height * mScaleY);
		fllp.width = (int) (params.height * mScaleX);
		fllp.gravity = scaleGravity;
		mItem.setLayoutParams(fllp);
		resetBitmap(params.width, params.height);
		super.requestLayout();
		super.invalidate();
	}
	
	@Override
	public void setTopMargin(int top) {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).topMargin = top;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setBottomMargin(int bottom) {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).bottomMargin = bottom;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setLeftMargin(int left) {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).leftMargin = left;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setRightMargin(int right) {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).rightMargin = right;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public int getTopMargin() {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).topMargin;
		}
		return 0;
	}

	@Override
	public int getBottomMargin() {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).bottomMargin;
		}
		return 0;
	}

	@Override
	public int getLeftMargin() {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).leftMargin;
		}
		return 0;
	}

	@Override
	public int getRightMargin() {
		LayoutParams lp = (LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).rightMargin;
		}
		return 0;
	}

	@Override
	public void setScaleX(float scale) {
		mScaleX = scale;
		if (super.getLayoutParams() != null && mItem.getLayoutParams() != null) {
			FrameLayout.LayoutParams fllp = (FrameLayout.LayoutParams) mItem.getLayoutParams();
			ViewGroup.LayoutParams vglp = super.getLayoutParams();
			fllp.width = (int) (vglp.width * mScaleX);
			mItem.setLayoutParams(fllp);
			mContainer.requestLayout();
			super.requestLayout();
			super.invalidate();
		}
	}

	@Override
	public float getScaleX() {
		return mScaleX;
	}

	@Override
	public void setScaleY(float scale) {
		mScaleY = scale;
		if (super.getLayoutParams() != null && mItem.getLayoutParams() != null) {
			FrameLayout.LayoutParams fllp = (FrameLayout.LayoutParams) mItem.getLayoutParams();
			ViewGroup.LayoutParams vglp = super.getLayoutParams();
			fllp.height = (int) (vglp.height * mScaleY);
			mItem.setLayoutParams(fllp);
			mContainer.requestLayout();
			super.requestLayout();
			super.invalidate();
		}
	}

	@Override
	public float getScaleY() {
		return mScaleY;
	}

	@Override
	public void setScaleGravity(int gravity) {
		scaleGravity = gravity;
		if(mItem.getLayoutParams() != null){
			FrameLayout.LayoutParams fllp = (FrameLayout.LayoutParams) mItem.getLayoutParams();
			fllp.gravity = scaleGravity;
			mItem.setLayoutParams(fllp);
			super.requestLayout();
			super.invalidate();
		}
	}
}
