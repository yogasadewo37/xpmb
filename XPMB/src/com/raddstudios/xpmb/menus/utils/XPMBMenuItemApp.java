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

package com.raddstudios.xpmb.menus.utils;

import java.net.URISyntaxException;

import com.raddstudios.xpmb.XPMBActivity;
import com.raddstudios.xpmb.utils.UI.UILayer;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

public class XPMBMenuItemApp extends XPMBMenuItem {

	public static final String TYPE_DESC = "menuitem.system.app";

	private Intent mIntent = null;

	private final String BD_MINTENT = "mIntent";

	public XPMBMenuItemApp(String label) {
		super(label);
	}

	public XPMBMenuItemApp(Bundle source) {
		super(source);

		mIntent = getIntentFromBundle(source, BD_MINTENT);
	}

	public Intent getIntent() {
		return mIntent;
	}

	@Override
	public Bundle storeInBundle() {
		Bundle s = super.storeInBundle();
		storeIntentInBundle(s, mIntent, BD_MINTENT);
		return s;
	}

	@Override
	public String getTypeDescriptor() {
		return XPMBMenuItemApp.TYPE_DESC;
	}

	@Override
	public void preloadIconBitmap(XPMBActivity root) {
		if (getIconBitmapID() != null && mIntent != null) {
			if (!root.getThemeManager().assetExists(getIconBitmapID())) {
				long dt = System.currentTimeMillis();
				Bitmap bmAsset = null;
				try {
					bmAsset = ((BitmapDrawable) root.getBaseContext().getPackageManager()
							.getActivityIcon(mIntent)).getBitmap();
				} catch (NameNotFoundException e) {
					Log.w(getClass().getSimpleName(),
							"preloadIconBitmap():Couldn't load Icon for app '"
									+ mIntent.getPackage() + "'");
					return;
				}
				Bitmap bmModAsset = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
				Canvas bmModAssetCanvas = new Canvas(bmModAsset);
				Rect bA = new Rect(0, 0, 96, 96), bB = new Rect(0, 0, bmAsset.getWidth(),
						bmAsset.getHeight());
				UILayer.gravitateRect(bA, bB, Gravity.CENTER);
				bmModAssetCanvas.drawBitmap(bmAsset, null, bB, new Paint());
				root.getThemeManager().addCustomAsset(getIconBitmapID(), bmModAsset);
				Log.i(getClass().getSimpleName(), "loadIn():Icon asset loading for app '"
						+ getLabel() + "' done. Took " + (System.currentTimeMillis() - dt) + "ms.");
			}
			super.setIconType(XPMBMenuItemDef.ICON_TYPE_BITMAP);
		}
	}

	private void storeIntentInBundle(Bundle dest, Intent src, String baseKey) {
		dest.putString(baseKey + "_datauri", src.toUri(Intent.URI_INTENT_SCHEME));
	}

	private Intent getIntentFromBundle(Bundle src, String baseKey) {

		try {
			return Intent.parseUri(src.getString(baseKey + "_datauri"), Intent.URI_INTENT_SCHEME);
		} catch (URISyntaxException e) {
			Log.e(getClass().getSimpleName(),
					"getIntentFromBundle():Error parsing Uri from Bundle. Returning empty Intent.");
		}
		return new Intent();
	}

	public void setIntent(Intent dest) {
		mIntent = dest;
	}

	public void setVersion(String version) {
		if (version == null) {
			super.enableTwoLine(false);
		} else {
			super.enableTwoLine(true);
			super.setLabelB(version);
		}
	}

	public String getVersion() {
		return super.getLabelB();
	}
}
