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
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;

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

	public final int PLAYER_STATUS_STOPPED = 0, PLAYER_STATUS_PAUSED = 1,
			PLAYER_STATUS_PLAYING = 2;

	private ArrayList<XPMBSubmenuItem_MUSIC> alItems = null;
	private XPMB_Activity mRoot = null;
	Handler hMessageBus = null;
	private int intSelItem = 0, intLastPlayed = -1, intPlayerStatus = PLAYER_STATUS_STOPPED;
	private TextView tv_no_music = null;
	private MediaPlayer mpPlayer = null;
	private View vwPlayerControlsRoot = null;
	private RelativeLayout rlPlayerControls = null;
	private TextView tvCurPos = null, tvTotalLen = null;
	private ImageView ivPlayStatus = null;
	private ProgressBar pbTrackPos = null;
	private TableLayout tlRoot = null;

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
						if (mpPlayer != null) {
							try {
								if (intPlayerStatus == PLAYER_STATUS_PLAYING) {
									pbTrackPos.setProgress(mpPlayer.getCurrentPosition());
									tvCurPos.setText(getTimeString(mpPlayer.getCurrentPosition())
											+ " ");
								}
							} catch (Exception e) {
							}
						}
					}
				});
			}
		}, 0, 100);

		MediaPlayer mpTemp = (MediaPlayer) mRoot.getObjectFromStore("playerObj");
		if (mpTemp != null) {
			mpPlayer = mpTemp;
		}
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

	private float motStY = 0;
	private long downTime = 0;
	private boolean isMoving = false;
	private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			int action = arg1.getActionMasked();
			int pointerIndex = arg1.getActionIndex();
			int pointerId = arg1.getPointerId(pointerIndex);
			if (pointerId != 0) {
				return true;
			}

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (!isMoving) {
					downTime = arg1.getEventTime();
					doCenterOnItemPre();
					motStY = arg1.getY(pointerId) * arg1.getYPrecision();
					isMoving = true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (isMoving) {
					float nY = (motStY + (arg1.getY(pointerId) * arg1.getYPrecision()));
					tlRoot.setY((tlRoot.getY() + nY) - pxFromDip(60));
				}
				break;
			case MotionEvent.ACTION_UP:
				if ((arg1.getEventTime() - downTime) < 250) {
					execCustItem((Integer) arg0.getTag());
					centerOnItem((Integer) arg0.getTag());
					doCenterOnItemPos();
					motStY = 0;
					isMoving = false;
					break;
				}
				if (isMoving) {
					centerOnNearestItem();
					doCenterOnItemPos();
					motStY = 0;
					isMoving = false;
				}
				break;
			}
			return true;
		}
	};

	private void doCenterOnItemPre() {
		final int intAnimItem = intSelItem;
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
		va_ci_bm.setDuration(100);
		ObjectAnimator.ofFloat(alItems.get(intSelItem).getParentLabel(), "Alpha", 0.5f)
				.setDuration(100).start();
		va_ci_bm.start();
	}

	private void doCenterOnItemPos() {
		ValueAnimator va_ci_bm = ValueAnimator.ofInt(0, pxFromDip(16));
		va_ci_bm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				XPMBSubmenuItem_MUSIC cSub = alItems.get(intSelItem);
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
		va_ci_bm.setDuration(100);
		ObjectAnimator.ofFloat(alItems.get(intSelItem).getParentLabel(), "Alpha", 1.0f)
				.setDuration(100).start();
		va_ci_bm.start();
	}

	private void centerOnNearestItem() {
		float cPosY = tlRoot.getY();
		int destItem = ((int) (pxFromDip(122) - cPosY) / pxFromDip(60)) + 1;
		if (destItem < 0) {
			destItem = 0;
		} else if (destItem > (alItems.size() - 1)) {
			destItem = (alItems.size() - 1);
		}
		centerOnItem(destItem);

	}

	private void centerOnItem(int index) {

		float cPosY = tlRoot.getY();
		float destPos = pxFromDip(122) - (pxFromDip(60) * index);

		ObjectAnimator.ofFloat(tlRoot, "Y", cPosY, destPos).setDuration(250).start();

		intSelItem = index;
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

		vwPlayerControlsRoot = ((LayoutInflater) base.getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mediaplayer_control, base);
		rlPlayerControls = (RelativeLayout) vwPlayerControlsRoot.findViewById(R.id.playerc_l);
		tvCurPos = (TextView) vwPlayerControlsRoot.findViewById(R.id.tvCurPos);
		tvTotalLen = (TextView) vwPlayerControlsRoot.findViewById(R.id.tvTotalLen);
		pbTrackPos = (ProgressBar) vwPlayerControlsRoot.findViewById(R.id.pbCurPos);
		ivPlayStatus = (ImageView) vwPlayerControlsRoot.findViewById(R.id.ivPlayStatus);

		rlPlayerControls.setAlpha(0.0f);

		tlRoot = new TableLayout(base.getContext());
		AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(pxFromDip(396),
				pxFromDip(32 + (60 * alItems.size())), pxFromDip(48), pxFromDip(122));
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_MUSIC xsi : alItems) {

			TableRow cItem = new TableRow(base.getContext());
			cItem.setId(getNextID());

			int idy = alItems.indexOf(xsi);
			ImageView cIcon = new ImageView(base.getContext());
			TextView cLabel = new TextView(base.getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());

			// Setup Icon
			// TODO load embedded or custom audio cover image
			TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(60),
					(int) pxFromDip(60));
			if (idy == 0) {
				cIconParams.topMargin = pxFromDip(16);
				cIconParams.bottomMargin = pxFromDip(16);
			}
			cIconParams.column = 0;
			cIcon.setLayoutParams(cIconParams);
			cIcon.setTag(idy);
			cIcon.setImageDrawable(base.getResources().getDrawable(
					R.drawable.ui_xmb_default_music_icon));
			cIcon.setPivotX(0.0f);
			cIcon.setPivotY(0.0f);
			// cIcon.setOnClickListener(mClickListener);
			cIcon.setOnTouchListener(mTouchListener);

			// Setup Label
			TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(60));
			cLabelParams.leftMargin = pxFromDip(16);
			if (idy == 0) {
				cLabelParams.topMargin = pxFromDip(16);
				cLabelParams.bottomMargin = pxFromDip(16);
			}
			cLabelParams.column = 1;
			cLabel.setLayoutParams(cLabelParams);
			cLabel.setTag(idy);
			cLabel.setText(xsi.getTrackMetadata().getTrackName() + "\r\n"
					+ xsi.getTrackMetadata().getTrackAuthor());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(base.getContext(), android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			if (idy != 0) {
				cLabel.setAlpha(0.5f);
			}
			cLabel.setOnTouchListener(mTouchListener);

			cItem.addView(cIcon);
			cItem.addView(cLabel);

			xsi.setParentView(cIcon);
			xsi.setParentLabel(cLabel);
			xsi.setParentContainer(cItem);

			tlRoot.addView(cItem);
		}
		base.addView(tlRoot);

		if (mRoot.getObjectFromStore("playerStatus") != null) {
			intPlayerStatus = (Integer) mRoot.getObjectFromStore("playerStatus");
			switch (intPlayerStatus) {
			case PLAYER_STATUS_PLAYING:
				ivPlayStatus.setImageDrawable(mRoot.getResources().getDrawable(
						R.drawable.ui_status_media_play));
				pbTrackPos.setMax(mpPlayer.getDuration());
				tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
				doShowPlayerControls(true);
				break;
			case PLAYER_STATUS_PAUSED:
				ivPlayStatus.setImageDrawable(mRoot.getResources().getDrawable(
						R.drawable.ui_status_media_pause));
				pbTrackPos.setMax(mpPlayer.getDuration());
				tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
				rlPlayerControls.setAlpha(1.0f);
				doShowPlayerControls(false);
				break;
			}
		}
		if (mRoot.getObjectFromStore("selectionIndex") != null) {
			TableRow.LayoutParams cIconP = (TableRow.LayoutParams) alItems.get(0).getParentView()
					.getLayoutParams();
			TableRow.LayoutParams cLabelP = (TableRow.LayoutParams) alItems.get(0).getParentLabel()
					.getLayoutParams();
			cIconP.topMargin = 0;
			cIconP.bottomMargin = 0;
			cLabelP.topMargin = 0;
			cLabelP.bottomMargin = 0;
			alItems.get(0).getParentView().setLayoutParams(cIconP);
			alItems.get(0).getParentLabel().setLayoutParams(cLabelP);
			intSelItem = (Integer) mRoot.getObjectFromStore("selectionIndex");
			intLastPlayed = intSelItem;
			centerOnItem(intSelItem);
			doCenterOnItemPos();
		}
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_LEFT:
			moveLeft();
			break;
		case XPMB_Main.KEYCODE_RIGHT:
			moveRight();
			break;
		case XPMB_Main.KEYCODE_UP:
			moveUp();
			break;
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_CROSS:
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			mRoot.requestUnloadSubmenu();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_LEFT:
			execCustItem(intSelItem - 1);
			moveUp();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_RIGHT:
			execCustItem(intSelItem + 1);
			moveDown();
			break;
		}
	}

	private void moveLeft() {
		if (mpPlayer != null && mpPlayer.isPlaying()) {
			if (mpPlayer.getCurrentPosition() - 2500 > 0) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() - 2500);
			}
			return;
		}
		mRoot.requestUnloadSubmenu();
	}

	private void moveRight() {
		if (mpPlayer != null && mpPlayer.isPlaying()) {
			if (mpPlayer.getCurrentPosition() + 2500 < mpPlayer.getDuration()) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() + 2500);
			}
		}
	}

	private void moveDown() {
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

	private void doShowPlayerControls(boolean show) {
		if (show) {
			ObjectAnimator.ofFloat(rlPlayerControls, "Alpha", 1.0f).setDuration(200).start();
		} else {
			ObjectAnimator.ofFloat(rlPlayerControls, "Alpha", 0.0f).setDuration(800).start();
		}
	}

	private void execCustItem(int index) {
		if (mpPlayer != null) {
			if (index == intLastPlayed) {
				if (mpPlayer.isPlaying()) {
					intPlayerStatus = PLAYER_STATUS_PAUSED;
					ivPlayStatus.setImageDrawable(mRoot.getResources().getDrawable(
							R.drawable.ui_status_media_pause));
					mpPlayer.pause();
					doShowPlayerControls(false);
				} else {
					intPlayerStatus = PLAYER_STATUS_PLAYING;
					ivPlayStatus.setImageDrawable(mRoot.getResources().getDrawable(
							R.drawable.ui_status_media_play));
					mpPlayer.start();
					doShowPlayerControls(true);
				}
				return;
			} else {
				intPlayerStatus = PLAYER_STATUS_STOPPED;
				if (mpPlayer.isPlaying()) {
					mpPlayer.stop();
				}
				mpPlayer.release();

			}
		}
		mpPlayer = MediaPlayer.create(mRoot.getWindow().getContext(),
				Uri.fromFile(alItems.get(index).getTrackPath()));
		try {
			mpPlayer.prepare();
		} catch (Exception e) {
		}
		ivPlayStatus.setImageDrawable(mRoot.getResources().getDrawable(
				R.drawable.ui_status_media_play));
		pbTrackPos.setMax(mpPlayer.getDuration());
		tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
		mpPlayer.start();
		doShowPlayerControls(true);
		intPlayerStatus = PLAYER_STATUS_PLAYING;
		intLastPlayed = index;

	}

	public void execSelectedItem() {
		execCustItem(intSelItem);
	}

	private String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();

		int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
		int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

		buf.append(String.format("%02d", minutes)).append(":")
				.append(String.format("%02d", seconds));

		return buf.toString();
	}

	@Override
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
			mRoot.putObjectInStore("playerStatus", intPlayerStatus);
			mRoot.putObjectInStore("selectionIndex", intLastPlayed);
			mRoot.putObjectInStore("playerObj", mpPlayer);
		}
	}

	@Override
	public void requestDestroy() {
		intPlayerStatus = PLAYER_STATUS_STOPPED;
		if (mpPlayer != null) {
			if (mpPlayer.isPlaying()) {
				mpPlayer.stop();
			}
			mpPlayer.release();
		}
	}
}
