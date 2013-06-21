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

package com.raddstudios.xpmb.menus;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.format.Time;
import android.view.Gravity;

import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.UI.UILayer;

public class XPMBUIModule extends UILayer {

	private XPMB_Activity mRoot = null;
	private RectF rConstraints = null;
	private Rect bmRect = null;
	private String strBatteryIcon = "theme.icon|icon_batt_status_000";
	private boolean bLoadingAnim = false, bBatteryIndicator = true, bDateTimeLabel = true;
	private Paint pPaint = null;
	private Time tClock = null;
	private String strDateFormat = "%d/%m %H:%M", strFormattedDate = "00/00 00:00";
	private int intLoadAnimFrames = 0, intLoadAnimCurFrame = 0;
	private Timer tUpdateAnims = null;

	public XPMBUIModule(XPMB_Activity root) {
		super(root);
		mRoot = root;
		bmRect = new Rect();
		setDrawingConstraints(new RectF(0, 0, root.getRootView().getWidth(), root.getRootView()
				.getHeight()));
		pPaint = new Paint();
		tClock = new Time(Time.getCurrentTimezone());
		Bitmap drwAnimSrc = (Bitmap) root.getStorage().getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY,
				"theme.icon|ui_load_anim");
		intLoadAnimFrames = drwAnimSrc.getWidth() / drwAnimSrc.getHeight();
		tUpdateAnims = new Timer();
		tUpdateAnims.scheduleAtFixedRate(ttUpdateLoadAnim, 0, 30);
		tUpdateAnims.scheduleAtFixedRate(ttUpdateDateTime, 0, 10000);
	}

	@Override
	public void drawTo(Canvas canvas) {

		if (bBatteryIndicator) {
			Bitmap bmBatt = (Bitmap) mRoot.getStorage().getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY,
					strBatteryIcon);
			pPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

			canvas.drawBitmap(bmBatt, null, bmRect, pPaint);
			pPaint.reset();
		}
		if (bDateTimeLabel) {
			pPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			pPaint.setColor(Color.WHITE);
			pPaint.setTextAlign(Align.RIGHT);
			pPaint.setTextSize(pxfd(19));
			pPaint.setShadowLayer(pxfd(2), pxfd(1), pxfd(1), Color.BLACK);
			int intTime_x = (int) rConstraints.right - pxfd(42), intTime_y = (int) (rConstraints.top
					- pPaint.ascent() + pxfd(6));
			canvas.drawText(strFormattedDate, intTime_x, intTime_y, pPaint);
			pPaint.reset();
		}
		if (bLoadingAnim) {
			Bitmap bmAnim = (Bitmap) mRoot.getStorage().getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY,
					"theme.icon|ui_load_anim");
			int intAnim_x = (int) rConstraints.right - pxfd(37), intAnim_y = (int) rConstraints.bottom
					- pxfd(37), intAnim_bx = bmAnim.getHeight() * intLoadAnimCurFrame;
			pPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			canvas.drawBitmap(bmAnim, new Rect(intAnim_bx, 0, intAnim_bx + bmAnim.getHeight(),
					bmAnim.getHeight()), new Rect(intAnim_x, intAnim_y, intAnim_x + pxfd(32),
					intAnim_y + pxfd(32)), pPaint);
			pPaint.reset();
		}
	}

	@Override
	public void setDrawingConstraints(RectF constraints) {
		rConstraints = constraints;
		Bitmap bm_batt = (Bitmap) mRoot.getStorage().getObject(XPMB_Main.GRAPH_ASSETS_COL_KEY,
				strBatteryIcon);
		Rect bm_rect = new Rect(0, 0, bm_batt.getWidth(), bm_batt.getHeight()), br = new Rect(
				(int) rConstraints.right - pxfd(37), (int) rConstraints.top,
				(int) rConstraints.right - pxfd(37) + pxfd(32), (int) rConstraints.top + pxfd(32));
		bmRect = getScaledRect(br, bm_rect.width() / br.width(), bm_rect.height() / br.height(),
				Gravity.CENTER);
	}

	public void setLoadingAnimationVisible(boolean visible) {
		bLoadingAnim = visible;
	}

	public void setBatteryIndicatorStatus(int level, boolean charging) {
		if (level > 80) {
			strBatteryIcon = "theme.icon|icon_batt_status_100";
		} else if (level > 60) {
			strBatteryIcon = "theme.icon|icon_batt_status_080";
		} else if (level > 40) {
			strBatteryIcon = "theme.icon|icon_batt_status_060";
		} else if (level > 20) {
			strBatteryIcon = "theme.icon|icon_batt_status_040";
		} else if (level > 4) {
			strBatteryIcon = "theme.icon|icon_batt_status_020";
		} else {
			strBatteryIcon = "theme.icon|icon_batt_status_000";
		}
		if (charging) {
			strBatteryIcon = strBatteryIcon + "c";
		}
	}

	private TimerTask ttUpdateLoadAnim = new TimerTask() {
		@Override
		public void run() {
			if (intLoadAnimCurFrame < (intLoadAnimFrames - 1)) {
				intLoadAnimCurFrame++;
			} else {
				intLoadAnimCurFrame = 0;
			}
		}
	};

	private TimerTask ttUpdateDateTime = new TimerTask() {
		@Override
		public void run() {
			tClock.setToNow();
			strFormattedDate = tClock.format(strDateFormat);
		}
	};

	public void setBatteryIndicatorVisible(boolean visible) {
		bBatteryIndicator = visible;
	}

	public void setDateTimeLabelVisible(boolean dateVisible, boolean timeVisible) {
		bDateTimeLabel = (dateVisible || timeVisible);
		if (dateVisible && timeVisible) {
			strDateFormat = "%d/%m %H:%M";
		} else if (dateVisible) {
			strDateFormat = "%d/%m %H:%M";
		} else if (timeVisible) {
			strDateFormat = "%d/%m %H:%M";
		}
	}

}
