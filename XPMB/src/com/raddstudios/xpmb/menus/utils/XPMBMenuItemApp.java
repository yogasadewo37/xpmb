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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class XPMBMenuItemApp extends XPMBMenuItem {

	public static final String TYPE_DESC = "menuitem.system.app";
	
	private Intent mIntent = null;

	private final String BD_MINTENT = "mIntent";

	public XPMBMenuItemApp(String label) {
		super(label);
		super.setTypeDescriptor(TYPE_DESC);
	}

	public XPMBMenuItemApp(Bundle source) {
		super(source);

		mIntent = getIntentFromBundle(source, BD_MINTENT);
	}

	public Intent getIntent() {
		Intent o = null;
		return o;
	}
	
	@Override
	public Bundle storeInBundle() {
		Bundle s = super.storeInBundle();
		storeIntentInBundle(s, mIntent, BD_MINTENT);
		return s;
	}

	private void storeIntentInBundle(Bundle dest, Intent src, String baseKey) {
		dest.putString(baseKey + "_datauri", src.toUri(Intent.URI_INTENT_SCHEME));
	}

	private Intent getIntentFromBundle(Bundle src, String baseKey) {
		Intent o = new Intent();

		Uri data = Uri.parse(src.getString(baseKey + "_datauri"));
		o.setData(data);

		return o;
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
