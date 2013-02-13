package com.raddstudios.xpmb.utils.backports;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class XPMB_TextView extends View implements XPMB_View {

	private float mAlpha = 1.0f;

	private Bitmap bmObject = null;
	private Canvas cvObject = null;
	private Paint cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private TextView mItem = null;

	public XPMB_TextView(Context context) {
		super(context);
		mItem = new TextView(context);
	}

	@Override
	public void onDraw(Canvas canvas) {
		cvObject.drawColor(0x00FFFFFF, Mode.CLEAR);
		mItem.draw(cvObject);
		cPaint.setAlpha((int) (255 * mAlpha));
		canvas.drawBitmap(bmObject, 0, 0, cPaint);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mItem.measure(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mItem.layout(left, top, right, bottom);
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

	public void setText(CharSequence text) {
		mItem.setText(text);
	}

	public void setTextColor(int color) {
		mItem.setTextColor(color);
	}

	public void setTextAppearance(Context context, int resid) {
		mItem.setTextAppearance(context, resid);
	}

	public void setShadowLayer(float radius, float dx, float dy, int color) {
		mItem.setShadowLayer(radius, dx, dy, color);
	}

	public void setGravity(int gravity) {
		mItem.setGravity(gravity);
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
		mItem.setLayoutParams(new ViewGroup.LayoutParams(params));
		resetBitmap(params.width, params.height);
		super.requestLayout();
	}

	@Override
	public void setScaleX(float scale) {
	}

	@Override
	public float getScaleX() {
		return 1.0f;
	}

	@Override
	public void setScaleY(float scale) {
	}

	@Override
	public float getScaleY() {
		return 1.0f;
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
	public void setScaleGravity(int gravity) {
	}
}
