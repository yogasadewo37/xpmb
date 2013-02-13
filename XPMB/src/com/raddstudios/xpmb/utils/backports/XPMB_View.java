package com.raddstudios.xpmb.utils.backports;

public interface XPMB_View {
	public void setX(float x);

	public void setY(float y);

	public float getX();

	public float getY();

	public void setTopMargin(int top);

	public void setBottomMargin(int bottom);

	public void setLeftMargin(int left);

	public void setRightMargin(int right);

	public int getTopMargin();

	public int getBottomMargin();

	public int getLeftMargin();

	public int getRightMargin();

	public void setAlpha(float value);

	public float getAlpha();

	public void setScaleX(float scale);

	public float getScaleX();

	public void setScaleY(float scale);

	public float getScaleY();
	
	public void setScaleGravity(int gravity);
}
