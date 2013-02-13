package com.raddstudios.xpmb.utils.backports;

import android.content.Context;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.TableLayout;

@SuppressWarnings("deprecation")
public class XPMB_TableLayout extends TableLayout implements XPMB_View {

	private float mAlpha = 1.0f;

	public XPMB_TableLayout(Context context) {
		super(context);
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
		for (int v = 0; v < super.getChildCount(); v++) {
			View cView = super.getChildAt(v);
			if (cView instanceof XPMB_View) {
				((XPMB_View) cView).setAlpha(value);
			}
		}
		mAlpha = value;
	}

	@Override
	public float getAlpha() {
		return mAlpha;
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		if (child instanceof XPMB_View) {
			((XPMB_View) child).setAlpha(mAlpha);
		}
	}

	@Override
	public void setScaleGravity(int gravity) {
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

}
