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

	//TODO Fix for incorrect scaling behavior (temporal workaround)
	public void resetScaleBase();
	
	public void setScaleX(float scale);

	public float getScaleX();

	public void setScaleY(float scale);

	public float getScaleY();
}
