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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.raddstudios.xpmb.R;

@SuppressWarnings("deprecation")
public class XPMBSubmenu_MUSIC extends XPMB_Layout {

	class XPMBSubmenuItem_MUSIC_Metadata {
		private String strName = null, strAuthor = null, strAlbum = null;

		public XPMBSubmenuItem_MUSIC_Metadata(String name, String author, String album) {
			strName = name;
			strAuthor = author;
			strAlbum = album;
		}

		public String getTrackName() {
			return strName;
		}

		public String getTrackAuthor() {
			return strAuthor;
		}

		public String getTrackAlbum() {
			return strAlbum;
		}
	}

	class XPMBSubmenuItem_MUSIC {

		private File fTrackPath = null;
		private XPMBSubmenuItem_MUSIC_Metadata xsimmMetadata = null;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;

		public XPMBSubmenuItem_MUSIC(File trackPath) {
			fTrackPath = trackPath;
			MediaMetadataRetriever mmrMeta = new MediaMetadataRetriever();
			mmrMeta.setDataSource(trackPath.getAbsolutePath());
			xsimmMetadata = new XPMBSubmenuItem_MUSIC_Metadata(
					mmrMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
					mmrMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
					mmrMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
			mmrMeta.release();
			mmrMeta = null;
		}

		public File getTrackPath() {
			return fTrackPath;
		}

		public XPMBSubmenuItem_MUSIC_Metadata getTrackMetadata() {
			return xsimmMetadata;
		}

		public void setParentView(ImageView parent) {
			ivParentView = parent;
		}

		public ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(TextView label) {
			tvParentLabel = label;
		}

		public TextView getParentLabel() {
			return tvParentLabel;
		}
	}

	private ArrayList<XPMBSubmenuItem_MUSIC> alItems = null;
	private XPMB_Activity mRoot = null;
	Handler hMessageBus = null;
	private int intSelItem = 0, intLastPlayed = 0;
	private TextView tv_no_music = null;
	private MediaPlayer mpPlayer = null;
	private ProgressBar pbTrackPos = null;

	public XPMBSubmenu_MUSIC(XPMB_Activity root, Handler messageBus) {
		super(root, messageBus);
		mRoot = root;
		hMessageBus = messageBus;

		alItems = new ArrayList<XPMBSubmenuItem_MUSIC>();

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				hMessageBus.post(new Runnable() {

					@Override
					public void run() {
						if (pbTrackPos != null) {
							if (mpPlayer != null) {
								if (mpPlayer.isPlaying()) {
									pbTrackPos.setVisibility(View.VISIBLE);
									pbTrackPos.setProgress(mpPlayer.getCurrentPosition());
								}
							} else {
								pbTrackPos.setProgress(0);
								pbTrackPos.setVisibility(View.INVISIBLE);
							}
						}
					}
				});
			}
		}, 0, 100);
	}

	private void loadFilesIn(File root) {
		if (new File(root, ".nomedia").exists()) {
			return;
		}
		int count = 0;
		for (File f : root.listFiles()) {
			if(count == 10){
				break;
			}
			if (f.isDirectory()) {
				loadFilesIn(f);
			} else {
				if (isSupportedExtension(f.getName().substring(f.getName().length() - 4,
						f.getName().length()))) {
					alItems.add(new XPMBSubmenuItem_MUSIC(f));
					System.out.println("XPMB_Submenu_Music::loadFilesIn : found '"
							+ f.getAbsolutePath() + "'");
				}
			}
			++count;
		}
	}

	private boolean isSupportedExtension(String ext) {
		return (ext.equalsIgnoreCase(".mp3") || ext.equalsIgnoreCase(".m4a") || ext
				.equalsIgnoreCase(".ogg"));
	}

	@Override
	public void doInit() {
		loadFilesIn(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
	}

	@Override
	public void parseInitLayout(ViewGroup base) {
		int cId = 0xC0DE;

		if (alItems.size() == 0) {
			tv_no_music = new TextView(base.getContext());
			LayoutParams lp_ng = new LayoutParams((int) pxFromDip(320), (int) pxFromDip(100),
					pxFromDip(48), pxFromDip(128));
			tv_no_music.setLayoutParams(lp_ng);
			tv_no_music.setText(mRoot.getText(R.string.strNoMusic));
			tv_no_music.setTextColor(Color.WHITE);
			tv_no_music.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_music.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
			tv_no_music.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			base.addView(tv_no_music);
			return;
		}

		pbTrackPos = new ProgressBar(base.getContext(), null,
				android.R.attr.progressBarStyleHorizontal);
		LayoutParams lp_sb = new LayoutParams(pxFromDip(256), pxFromDip(16), pxFromDip(310),
				pxFromDip(300));
		pbTrackPos.setLayoutParams(lp_sb);
		pbTrackPos.setVisibility(View.INVISIBLE);
		base.addView(pbTrackPos);

		for (XPMBSubmenuItem_MUSIC xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView cItem = new ImageView(base.getContext());
			TextView cLabel = new TextView(base.getContext());

			// Setup Icon
			// TODO load embedded or custom audio cover image
			cItem.setImageDrawable(base.getResources().getDrawable(
					R.drawable.ui_xmb_default_music_icon));
			cItem.setPivotX(0.0f);
			cItem.setPivotY(0.0f);
			if (idx == 0) {
				LayoutParams cItemParams = new LayoutParams((int) pxFromDip(60),
						(int) pxFromDip(60), pxFromDip(48), pxFromDip(138));
				cItem.setLayoutParams(cItemParams);
			} else {
				LayoutParams cItemParams = new LayoutParams((int) pxFromDip(60),
						(int) pxFromDip(60), pxFromDip(48), pxFromDip(214 + (60 * (idx - 1))));
				cItem.setLayoutParams(cItemParams);
			}
			cItem.setId(cId);
			++cId;
			// Setup Label
			cLabel.setText(xsi.getTrackMetadata().getTrackName() + "\r\n"
					+ xsi.getTrackMetadata().getTrackAuthor());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

			if (idx == 0) {
				LayoutParams cLabelParams = new LayoutParams((int) pxFromDip(320),
						(int) pxFromDip(60), pxFromDip(124), pxFromDip(138));
				cLabel.setLayoutParams(cLabelParams);
			} else {
				LayoutParams cLabelParams = new LayoutParams((int) pxFromDip(320),
						(int) pxFromDip(60), pxFromDip(124), pxFromDip(214 + (60 * (idx - 1))));
				cLabel.setLayoutParams(cLabelParams);
				cLabel.setAlpha(0.5f);
			}
			cLabel.setId(cId);
			++cId;

			xsi.setParentView(cItem);
			xsi.setParentLabel(cLabel);
			base.addView(cItem);
			base.addView(cLabel);
		}
	}

	@Override
	public void moveLeft() {
		if (mpPlayer != null && mpPlayer.isPlaying()) {
			if (mpPlayer.getCurrentPosition() - 1000 > 0) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() - 1000);
			}
			return;
		}
		mRoot.requestUnloadSubmenu();
	}

	@Override
	public void moveRight() {
		if (mpPlayer != null && mpPlayer.isPlaying()) {
			if (mpPlayer.getCurrentPosition() + 1000 < mpPlayer.getDuration()) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() + 1000);
			}
		}
	}

	@Override
	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBSubmenuItem_MUSIC xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView iv_c_i = xsi.getParentView();
			TextView tv_c_l = xsi.getParentLabel();

			if (idx == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 1.0f, 0.5f));
			} else if (idx == (intSelItem + 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 0.5f, 1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() - pxFromDip(60))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() - pxFromDip(60))));
			}
		}

		AnimatorSet ag_xmb_sm_mu = new AnimatorSet();
		ag_xmb_sm_mu.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_mu.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_sm_mu.start();
		hMessageBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	@Override
	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		for (XPMBSubmenuItem_MUSIC xsi : alItems) {
			int idx = alItems.indexOf(xsi);
			ImageView iv_c_i = xsi.getParentView();
			TextView tv_c_l = xsi.getParentLabel();

			if (idx == intSelItem) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 1.0f, 0.5f));
			} else if (idx == (intSelItem - 1)) {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(76))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Alpha", 0.5f, 1.0f));
			} else {
				alAnims.add(ObjectAnimator.ofFloat(iv_c_i, "Y", iv_c_i.getY(),
						(iv_c_i.getY() + pxFromDip(60))));
				alAnims.add(ObjectAnimator.ofFloat(tv_c_l, "Y", tv_c_l.getY(),
						(tv_c_l.getY() + pxFromDip(60))));
			}
		}

		AnimatorSet ag_xmb_sm_md = new AnimatorSet();
		ag_xmb_sm_md.playTogether((Collection<Animator>) alAnims);
		ag_xmb_sm_md.setDuration(150);
		mRoot.lockKeys(true);
		ag_xmb_sm_md.start();
		hMessageBus.postDelayed(new Runnable() {

			@Override
			public void run() {
				mRoot.lockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	@Override
	public void execSelectedItem() {
		if (mpPlayer != null) {
			if (intSelItem == intLastPlayed) {
				if (mpPlayer.isPlaying()) {
					mpPlayer.pause();
				} else {
					mpPlayer.start();
				}
				return;
			} else {
				if (mpPlayer.isPlaying()) {
					mpPlayer.stop();
				}
				mpPlayer.release();

			}
		}
		mpPlayer = MediaPlayer.create(mRoot.getWindow().getContext(),
				Uri.fromFile(alItems.get(intSelItem).getTrackPath()));
		try {
			mpPlayer.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pbTrackPos.setMax(mpPlayer.getDuration());
		mpPlayer.start();
		intLastPlayed = intSelItem;
	}

	public void doCleanup(ViewGroup base) {
		if (alItems.size() == 0 && tv_no_music != null) {
			base.removeView(tv_no_music);
			return;
		}
		if (pbTrackPos != null) {
			base.removeView(pbTrackPos);
		}
		pbTrackPos = null;
		if (mpPlayer != null) {
			if (mpPlayer.isPlaying()) {
				mpPlayer.stop();
			}
			mpPlayer.release();
		}
		for (XPMBSubmenuItem_MUSIC xig : alItems) {
			base.removeView(xig.getParentView());
			base.removeView(xig.getParentLabel());
		}
	}
}
