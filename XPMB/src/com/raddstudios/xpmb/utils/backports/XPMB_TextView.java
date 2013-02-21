package com.raddstudios.xpmb.utils.backports;

import com.raddstudios.xpmb.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class XPMB_TextView extends TextView implements XPMB_View {

	private float mAlpha = 1.0f, mScaleX = 1.0f, mScaleY = 1.0f;
	private int baseWidth = 0, baseHeight = 0;

	public XPMB_TextView(Context context) {
		super(context);
	}

	public XPMB_TextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_TextView, 0, 0);

		try {
			mAlpha = a.getFloat(
					R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_TextView_alpha, 1.0f);
			mScaleX = a.getFloat(
					R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_TextView_scaleX, 1.0f);
			mScaleX = a.getFloat(
					R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_TextView_scaleY, 1.0f);
		} finally {
			a.recycle();
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), (int) (255 * mAlpha),
				Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		super.onDraw(canvas);
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

	@Override
	public void setAlpha(float value) {
		mAlpha = value;
		super.invalidate();
	}

	@Override
	public float getAlpha() {
		return mAlpha;
	}

	@Override
	public void setLayoutParams(ViewGroup.LayoutParams params) {
		super.setLayoutParams(params);
	}

	@Override
	public void resetScaleBase() {
		baseWidth = super.getLayoutParams().width;
		baseHeight = super.getLayoutParams().height;
	}

	private void updateScaledLayoutParams(ViewGroup.LayoutParams params) {
		if (params.width != ViewGroup.LayoutParams.MATCH_PARENT
				|| params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
			params.width = (int) (baseWidth * mScaleX);
		}
		if (params.height != ViewGroup.LayoutParams.MATCH_PARENT
				|| params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
			params.height = (int) (baseHeight * mScaleY);
		}
		super.setLayoutParams(params);
	}

	@Override
	public void setScaleX(float scale) {
		mScaleX = scale;
		updateScaledLayoutParams(super.getLayoutParams());
	}

	@Override
	public float getScaleX() {
		return mScaleX;
	}

	@Override
	public void setScaleY(float scale) {
		mScaleY = scale;
		updateScaledLayoutParams(super.getLayoutParams());
	}

	@Override
	public float getScaleY() {
		return mScaleY;
	}

	@Override
	public void setTopMargin(int top) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).topMargin = top;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setBottomMargin(int bottom) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).bottomMargin = bottom;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setLeftMargin(int left) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).leftMargin = left;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setRightMargin(int right) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).rightMargin = right;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public int getTopMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).topMargin;
		}
		return 0;
	}

	@Override
	public int getBottomMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).bottomMargin;
		}
		return 0;
	}

	@Override
	public int getLeftMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).leftMargin;
		}
		return 0;
	}

	@Override
	public int getRightMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).rightMargin;
		}
		return 0;
	}
}
