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
import android.animation.ValueAnimator;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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

		public Drawable getTrackAlbumCover() {
			return null;
		}
	}

	class XPMBSubmenuItem_MUSIC {

		private File fTrackPath = null;
		private XPMBSubmenuItem_MUSIC_Metadata xsimmMetadata = null;
		private ImageView ivParentView = null;
		private TextView tvParentLabel = null;
		private TableRow trParentContainer = null;

		public XPMBSubmenuItem_MUSIC(File trackPath, XPMBSubmenuItem_MUSIC_Metadata trackInfo) {
			fTrackPath = trackPath;
			xsimmMetadata = trackInfo;
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

		public void setParentContainer(TableRow container) {
			trParentContainer = container;
		}

		public TableRow getParentContainer() {
			return trParentContainer;
		}
	}

	private ArrayList<XPMBSubmenuItem_MUSIC> alItems = null;
	private XPMB_Activity mRoot = null;
	Handler hMessageBus = null;
	private int intSelItem = 0, intLastPlayed = 0;
	private TextView tv_no_music = null;
	private MediaPlayer mpPlayer = null;
	private RelativeLayout rlPlayerControls = null;
	private ImageView ivPlayStatus = null;
	private TextView tvCurPos = null, tvTotalLen = null;
	private ProgressBar pbTrackPos = null;
	private TableLayout tlRoot = null;
	private boolean isPlaying = false;

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
							if (mpPlayer != null && isPlaying) {
								if (mpPlayer.isPlaying()) {
									pbTrackPos.setProgress(mpPlayer.getCurrentPosition());
									tvCurPos.setText(getTimeString(mpPlayer.getCurrentPosition()));
									rlPlayerControls.setVisibility(View.VISIBLE);
									ivPlayStatus.setVisibility(View.VISIBLE);
								}
							} else {
								rlPlayerControls.setVisibility(View.INVISIBLE);
								ivPlayStatus.setVisibility(View.INVISIBLE);
							}
						}
					}
				});
			}
		}, 0, 100);
	}

	@Override
	public void doInit() {
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM };
		Cursor mCur = mRoot.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection, null, null, null);
		mCur.moveToFirst();

		while (mCur.isAfterLast() == false) {
			if (mCur.getString(0).startsWith(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
							.getAbsolutePath())) {
				alItems.add(new XPMBSubmenuItem_MUSIC(new File(mCur.getString(0)),
						new XPMBSubmenuItem_MUSIC_Metadata(mCur.getString(1), mCur.getString(2),
								mCur.getString(3))));
			}
			mCur.moveToNext();
		}
		mCur.close();
	}

	@Override
	public void parseInitLayout(ViewGroup base) {

		if (alItems.size() == 0) {
			tv_no_music = new TextView(base.getContext());
			AbsoluteLayout.LayoutParams lp_ng = new AbsoluteLayout.LayoutParams(
					(int) pxFromDip(320), (int) pxFromDip(100), pxFromDip(48), pxFromDip(128));
			tv_no_music.setLayoutParams(lp_ng);
			tv_no_music.setText(mRoot.getText(R.string.strNoMusic));
			tv_no_music.setTextColor(Color.WHITE);
			tv_no_music.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_music.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
			tv_no_music.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			base.addView(tv_no_music);
			return;
		}

		rlPlayerControls = new RelativeLayout(base.getContext());
		ivPlayStatus = new ImageView(base.getContext());
		tvCurPos = new TextView(base.getContext());
		tvTotalLen = new TextView(base.getContext());
		pbTrackPos = new ProgressBar(base.getContext(), null,
				android.R.attr.progressBarStyleHorizontal);
		rlPlayerControls.setId(getNextID());
		pbTrackPos.setId(getNextID());
		tvTotalLen.setId(getNextID());
		tvCurPos.setId(getNextID());
		ivPlayStatus.setId(getNextID());

		AbsoluteLayout.LayoutParams lp_iv_ps = new AbsoluteLayout.LayoutParams(pxFromDip(35),
				pxFromDip(35), pxFromDip(4), pxFromDip(282));
		ivPlayStatus.setLayoutParams(lp_iv_ps);
		ivPlayStatus.setImageDrawable(base.getContext().getResources()
				.getDrawable(R.drawable.ui_status_media_play));
		ivPlayStatus.setVisibility(View.INVISIBLE);
		rlPlayerControls.setLayoutParams(new AbsoluteLayout.LayoutParams(pxFromDip(224),
				pxFromDip(56), pxFromDip(342), pxFromDip(280)));
		rlPlayerControls.setVisibility(View.INVISIBLE);
		rlPlayerControls.setPadding(pxFromDip(2), 0, pxFromDip(2), 0);
		RelativeLayout.LayoutParams lp_sb = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, pxFromDip(16));
		lp_sb.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_BOTTOM);
		pbTrackPos.setLayoutParams(lp_sb);
		pbTrackPos.setMax(100);
		pbTrackPos.setProgress(0);
		RelativeLayout.LayoutParams lp_tv_tl = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp_tv_tl.addRule(RelativeLayout.ABOVE, pbTrackPos.getId());
		lp_tv_tl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		tvTotalLen.setLayoutParams(lp_tv_tl);
		tvTotalLen.setText(" / 00:00");
		tvTotalLen.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
		tvTotalLen.setTextColor(Color.WHITE);
		tvTotalLen.setShadowLayer(8, 4, 4, Color.BLACK);
		RelativeLayout.LayoutParams lp_tv_cp = new RelativeLayout.LayoutParams(pxFromDip(65),
				pxFromDip(30));
		lp_tv_cp.addRule(RelativeLayout.ALIGN_BOTTOM, tvTotalLen.getId());
		lp_tv_cp.addRule(RelativeLayout.LEFT_OF, tvTotalLen.getId());
		// lp_tv_cp.bottomMargin = pxFromDip(-2);
		tvCurPos.setLayoutParams(lp_tv_cp);
		tvCurPos.setText("00:00");
		tvCurPos.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Large);
		tvCurPos.setTextColor(0x0099EE);
		tvCurPos.setShadowLayer(8, 4, 4, Color.CYAN);

		rlPlayerControls.addView(pbTrackPos);
		rlPlayerControls.addView(tvTotalLen);
		rlPlayerControls.addView(tvCurPos);
		base.addView(ivPlayStatus);
		base.addView(rlPlayerControls);
		rlPlayerControls.requestLayout();

		tlRoot = new TableLayout(base.getContext());
		AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(pxFromDip(396),
				pxFromDip(32 + (60 * alItems.size())), pxFromDip(48), pxFromDip(122));
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_MUSIC xsi : alItems) {

			TableRow cItem = new TableRow(base.getContext());
			cItem.setId(getNextID());

			int idx = alItems.indexOf(xsi);
			ImageView cIcon = new ImageView(base.getContext());
			TextView cLabel = new TextView(base.getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());

			// Setup Icon
			// TODO load embedded or custom audio cover image
			TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(60),
					(int) pxFromDip(60));
			if (idx == 0) {
				cIconParams.topMargin = pxFromDip(16);
				cIconParams.bottomMargin = pxFromDip(16);
			}
			cIconParams.column = 0;
			cIcon.setLayoutParams(cIconParams);
			cIcon.setImageDrawable(base.getResources().getDrawable(
					R.drawable.ui_xmb_default_music_icon));
			cIcon.setPivotX(0.0f);
			cIcon.setPivotY(0.0f);
			// Setup Label
			TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(60));
			cLabelParams.leftMargin = pxFromDip(16);
			if (idx == 0) {
				cLabelParams.topMargin = pxFromDip(16);
				cLabelParams.bottomMargin = pxFromDip(16);
			}
			cLabelParams.column = 1;
			cLabel.setLayoutParams(cLabelParams);
			cLabel.setText(xsi.getTrackMetadata().getTrackName() + "\r\n"
					+ xsi.getTrackMetadata().getTrackAuthor());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			if (idx != 0) {
				cLabel.setAlpha(0.5f);
			}

			cItem.addView(cIcon);
			cItem.addView(cLabel);

			xsi.setParentView(cIcon);
			xsi.setParentLabel(cLabel);
			xsi.setParentContainer(cItem);

			tlRoot.addView(cItem);
		}
		base.addView(tlRoot);
	}

	@Override
	public void moveLeft() {
		if (mpPlayer != null && mpPlayer.isPlaying()) {
			if (mpPlayer.getCurrentPosition() - 2500 > 0) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() - 2500);
			}
			return;
		}
		mRoot.requestUnloadSubmenu();
	}

	@Override
	public void moveRight() {
		if (mpPlayer != null && mpPlayer.isPlaying()) {
			if (mpPlayer.getCurrentPosition() + 2500 < mpPlayer.getDuration()) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() + 2500);
			}
		}
	}

	@Override
	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		ArrayList<Animator> alAnims = new ArrayList<Animator>();

		final int intAnimItem = intSelItem;
		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "Y", tlRoot.getY() - pxFromDip(60)));
		ValueAnimator va_ci_bm = ValueAnimator.ofInt(pxFromDip(16), 0);
		va_ci_bm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_MUSIC cSub = alItems.get(intAnimItem);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_ni_bm = ValueAnimator.ofInt(0, pxFromDip(16));
		va_ni_bm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_MUSIC cSub = alItems.get(intAnimItem + 1);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});

		alAnims.add(va_ci_bm);
		alAnims.add(va_ni_bm);
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem).getParentLabel(), "Alpha", 0.5f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem + 1).getParentLabel(), "Alpha",
				1.0f));

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

		final int intAnimItem = intSelItem;
		alAnims.add(ObjectAnimator.ofFloat(tlRoot, "Y", tlRoot.getY() + pxFromDip(60)));
		ValueAnimator va_ci_bm = ValueAnimator.ofInt(pxFromDip(16), 0);
		va_ci_bm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_MUSIC cSub = alItems.get(intAnimItem);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});
		ValueAnimator va_ni_bm = ValueAnimator.ofInt(0, pxFromDip(16));
		va_ni_bm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_MUSIC cSub = alItems.get(intAnimItem - 1);
				TableRow.LayoutParams cIconP = (TableRow.LayoutParams) cSub.getParentView()
						.getLayoutParams();
				TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) cSub.getParentLabel()
						.getLayoutParams();
				cIconP.topMargin = (Integer) animation.getAnimatedValue();
				cIconP.bottomMargin = (Integer) animation.getAnimatedValue();
				cLabelP.topMargin = (Integer) animation.getAnimatedValue();
				cLabelP.bottomMargin = (Integer) animation.getAnimatedValue();
				cSub.getParentView().setLayoutParams(cIconP);
				cSub.getParentLabel().setLayoutParams(cLabelP);
			}
		});

		alAnims.add(va_ci_bm);
		alAnims.add(va_ni_bm);
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem).getParentLabel(), "Alpha", 0.5f));
		alAnims.add(ObjectAnimator.ofFloat(alItems.get(intSelItem - 1).getParentLabel(), "Alpha",
				1.0f));

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
					isPlaying = false;
				} else {
					mpPlayer.start();
					isPlaying = true;
				}
				return;
			} else {
				if (mpPlayer.isPlaying()) {
					mpPlayer.stop();
					isPlaying = false;
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
		tvTotalLen.setText(" / " + getTimeString(mpPlayer.getDuration()));
		mpPlayer.start();
		isPlaying = true;
		intLastPlayed = intSelItem;
	}

	private String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();

		int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
		int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

		buf.append(String.format("%02d", minutes)).append(":")
				.append(String.format("%02d", seconds));

		return buf.toString();
	}

	public void doCleanup(ViewGroup base) {
		if (alItems.size() == 0 && tv_no_music != null) {
			base.removeView(tv_no_music);
			return;
		}
		if (tlRoot != null) {
			tlRoot.removeAllViews();
			base.removeView(tlRoot);
		}
		if (ivPlayStatus != null) {
			base.removeView(ivPlayStatus);
		}
		if (rlPlayerControls != null) {
			base.removeView(rlPlayerControls);
		}
		if (mpPlayer != null) {
			if (mpPlayer.isPlaying()) {
				mpPlayer.stop();
				isPlaying = false;
			}
			mpPlayer.release();
		}
	}
}
