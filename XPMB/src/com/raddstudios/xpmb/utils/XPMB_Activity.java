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

import java.util.Hashtable;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;

public class XPMB_Activity extends Activity {

	public interface IntentFinishedListener {
		public void onFinished(Intent intent);
	}

	private Hashtable<String, Object> mObjectStore = null;
	private IntentFinishedListener cIntentWaitListener = null;

	public XPMB_Activity() {
		mObjectStore = new Hashtable<String, Object>();
	}

	public XPMB_ImageView getCustomBGView() {
		return null;
	}

	public void lockKeys(boolean locked) {
	}

	public void showLoadingAnim(boolean showAnim) {
	}

	public void preloadSubmenu(String submenu) {
	}

	public void requestUnloadSubmenu() {
	}

	public void requestActivityEnd() {
	}

	public boolean isActivityAvailable(Intent intent) {
		final PackageManager packageManager = getBaseContext().getPackageManager();
		ResolveInfo resolveInfo = packageManager.resolveActivity(intent,
				PackageManager.GET_ACTIVITIES);
		if (resolveInfo != null) {
			return true;
		}
		return false;
	}

	public void postIntentStartWait(IntentFinishedListener listener, Intent intent) {
		cIntentWaitListener = listener;
		startActivityForResult(intent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (cIntentWaitListener != null) {
			cIntentWaitListener.onFinished(data);
		}
	}

	public void putObjectInStore(String key, Object data) {
		mObjectStore.put(key, data);
	}

	public Object getObjectFromStore(String key) {
		return mObjectStore.get(key);
	}

	public void removeObjectFromStore(String key) {
		mObjectStore.remove(key);
	}
}
