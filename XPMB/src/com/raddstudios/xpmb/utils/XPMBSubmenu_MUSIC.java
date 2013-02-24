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
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_RelativeLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class XPMBSubmenu_MUSIC extends XPMB_Layout {

	class XPMBSubmenuItem_MUSIC_Metadata {
		private String strName = null, strAuthor = null, strAlbum = null;
		private Drawable drwAlbumCover = null;

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

		public void setTrackAlbumCover(Drawable cover) {
			drwAlbumCover = cover;
		}

		public Drawable getTrackAlbumCover() {
			return drwAlbumCover;
		}
	}

	class XPMBSubmenuItem_MUSIC {

		private File fTrackPath = null;
		private XPMBSubmenuItem_MUSIC_Metadata xsimmMetadata = null;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private XPMB_TableRow trParentContainer = null;

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

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		public XPMB_ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(XPMB_TextView label) {
			tvParentLabel = label;
		}

		public XPMB_TextView getParentLabel() {
			return tvParentLabel;
		}

		public void setParentContainer(XPMB_TableRow container) {
			trParentContainer = container;
		}

		public XPMB_TableRow getParentContainer() {
			return trParentContainer;
		}
	}

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_ITEM = 2, ANIM_SHOW_MEDIA_CONTROLS = 3, ANIM_HIDE_MEDIA_CONTROLS = 4;

	private class UIAnimatorWorker implements AnimatorUpdateListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
		private float pY = 0, destY = 0;
		private ValueAnimator mOwner = null;
		private float[] mArgs = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setArguments(float[] arguments) {
			mArgs = arguments;
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;
			intAnimItem = intSelItem;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				pY = tlRoot.getY();
				intNextItem = intAnimItem - 1;
				destY = pxFromDip(122) - (pxFromDip(60) * intNextItem);
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				pY = tlRoot.getY();
				intNextItem = intAnimItem + 1;
				destY = pxFromDip(122) - (pxFromDip(60) * intNextItem);
				break;
			case ANIM_CENTER_ON_ITEM:
				mOwner.setDuration(250);
				pY = tlRoot.getY();
				intNextItem = (int) mArgs[0];
				destY = pxFromDip(122) - (pxFromDip(60) * intNextItem);
				break;
			case ANIM_SHOW_MEDIA_CONTROLS:
				mOwner.setDuration(200);
				rlPlayerControls.setVisibility(View.VISIBLE);
				break;
			case ANIM_HIDE_MEDIA_CONTROLS:
				mOwner.setDuration(800);
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			float posY = 0, alphaO = 0, alphaI = 0;
			int marginO = 0, marginI = 0;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_CENTER_ON_ITEM:
				posY = destY - pY;
				alphaO = 1.0f - (0.5f * completion);
				alphaI = 0.5f + (0.5f * completion);
				marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setY(pY + (posY * completion));
				alItems.get(intAnimItem).getParentContainer().setTopMargin(marginO);
				alItems.get(intAnimItem).getParentContainer().setBottomMargin(marginO);
				alItems.get(intAnimItem).getParentLabel().setAlpha(alphaO);
				alItems.get(intNextItem).getParentContainer().setTopMargin(marginI);
				alItems.get(intNextItem).getParentContainer().setBottomMargin(marginI);
				alItems.get(intNextItem).getParentLabel().setAlpha(alphaI);
				break;
			case ANIM_SHOW_MEDIA_CONTROLS:
				alphaI = completion;

				rlPlayerControls.setAlpha(alphaI);
				break;
			case ANIM_HIDE_MEDIA_CONTROLS:
				alphaO = 1.0f - completion;

				rlPlayerControls.setAlpha(alphaO);
				if (completion == 1.0f) {
					rlPlayerControls.setVisibility(View.INVISIBLE);
				}
			case ANIM_NONE:
			default:
				break;
			}
		}
	};

	private final int SCROLL_DIR_UP = 0, SCROLL_DIR_DOWN = 1;

	private class RapidScroller extends TimerTask {

		private int mDirection = 0;
		private boolean bEnabled = false;

		public void setScrollDirection(int direction) {
			mDirection = direction;
		}

		public void setEnabled(boolean enabled) {
			bEnabled = enabled;
		}

		public boolean isEnabled() {
			return bEnabled;
		}

		@Override
		public void run() {
			if (bEnabled) {
				switch (mDirection) {
				case SCROLL_DIR_UP:
					getMessageBus().post(new Runnable() {
						@Override
						public void run() {
							moveUp();
						}
					});
					break;
				case SCROLL_DIR_DOWN:
					getMessageBus().post(new Runnable() {
						@Override
						public void run() {
							moveDown();
						}
					});
					break;
				}
			}
		}
	};

	public final int PLAYER_STATUS_STOPPED = 0, PLAYER_STATUS_PAUSED = 1,
			PLAYER_STATUS_PLAYING = 2;

	private ArrayList<XPMBSubmenuItem_MUSIC> alItems = null;
	private int intSelItem = 0, intLastPlayed = -1, intPlayerStatus = PLAYER_STATUS_STOPPED;

	private XPMB_TextView tv_no_music = null;
	private MediaPlayer mpPlayer = null;
	private View vwPlayerControlsRoot = null;
	private XPMB_RelativeLayout rlPlayerControls = null;
	private XPMB_TextView tvCurPos = null, tvTotalLen = null;
	private XPMB_ImageView ivPlayStatus = null;
	private ProgressBar pbTrackPos = null;
	private XPMB_TableLayout tlRoot = null;
	private Hashtable<Long, Drawable> albumCovers = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;
	private RapidScroller ttFastScroll = null;

	@SuppressWarnings("unchecked")
	public XPMBSubmenu_MUSIC(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView);

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				getMessageBus().post(new Runnable() {

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
		ttFastScroll = new RapidScroller();
		new Timer().scheduleAtFixedRate(ttFastScroll, 0, 50);

		MediaPlayer mpTemp = (MediaPlayer) getRootActivity().getObjectFromStore("playerObj");
		if (mpTemp != null) {
			mpPlayer = mpTemp;
		}
		albumCovers = (Hashtable<Long, Drawable>) getRootActivity().getObjectFromStore(
				"albumCovers");
		alItems = (ArrayList<XPMBSubmenuItem_MUSIC>) getRootActivity().getObjectFromStore(
				"submenu_music_alItems");
		tlRoot = (XPMB_TableLayout) getRootActivity().getObjectFromStore("submenu_music_tlRoot");
	}

	@Override
	public void doInit() {
		if (albumCovers == null) {
			albumCovers = new Hashtable<Long, Drawable>();
		}
		if (alItems != null) {
			return;
		}

		alItems = new ArrayList<XPMBSubmenuItem_MUSIC>();
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID };
		Cursor mCur = getRootActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		mCur.moveToFirst();

		while (mCur.isAfterLast() == false) {
			if (mCur.getString(0).startsWith(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
							.getAbsolutePath())) {
				alItems.add(new XPMBSubmenuItem_MUSIC(new File(mCur.getString(0)),
						new XPMBSubmenuItem_MUSIC_Metadata(mCur.getString(1), mCur.getString(2),
								mCur.getString(3))));

				try {
					long albumId = mCur.getLong(4);

					if (!albumCovers.containsKey(albumId)) {
						Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
						Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
						albumCovers.put(
								albumId,
								new BitmapDrawable(MediaStore.Images.Media.getBitmap(getRootView()
										.getContext().getContentResolver(), albumArtUri)));
					}
					alItems.get(alItems.size() - 1).getTrackMetadata()
							.setTrackAlbumCover(albumCovers.get(albumId));

				} catch (Exception exception) {
					alItems.get(alItems.size() - 1)
							.getTrackMetadata()
							.setTrackAlbumCover(
									getRootView().getResources().getDrawable(
											R.drawable.ui_xmb_default_music_icon));
				}
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
				downTime = arg1.getEventTime();
				doCenterOnItemPre();
				break;
			case MotionEvent.ACTION_MOVE:
				if (!isMoving && (arg1.getEventTime() - downTime) > 100) {
					motStY = arg1.getY(pointerId);
					isMoving = true;
					break;
				}
				if (isMoving) {
					float nY = (motStY + arg1.getY(pointerId));
					tlRoot.setY((int) (tlRoot.getY() + (nY - pxFromDip(76))));
				}
				break;
			case MotionEvent.ACTION_UP:
				if ((arg1.getEventTime() - downTime) < 100) {
					execCustItem((Integer) arg0.getTag());
					centerOnItem((Integer) arg0.getTag());
				} else {
					centerOnNearestItem();
				}
				motStY = 0;
				isMoving = false;
				break;
			}
			return true;
		}
	};

	private void doCenterOnItemPre() {
		alItems.get(intSelItem).getParentContainer().setTopMargin(0);
		alItems.get(intSelItem).getParentContainer().setBottomMargin(0);
		alItems.get(intSelItem).getParentLabel().setAlpha(0.5f);
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
		aUIAnimatorW.setArguments(new float[] { index });
		aUIAnimatorW.setAnimationType(ANIM_CENTER_ON_ITEM);
		aUIAnimator.start();
		intSelItem = index;
	}

	@Override
	public void parseInitLayout() {

		if (alItems.size() == 0) {
			tv_no_music = new XPMB_TextView(getRootView().getContext());
			AbsoluteLayout.LayoutParams lp_ng = new AbsoluteLayout.LayoutParams(
					(int) pxFromDip(320), (int) pxFromDip(100), pxFromDip(48), pxFromDip(128));
			tv_no_music.setLayoutParams(lp_ng);
			tv_no_music.setText(getRootActivity().getText(R.string.strNoMusic));
			tv_no_music.setTextColor(Color.WHITE);
			tv_no_music.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_music.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_music.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			getRootView().addView(tv_no_music);
			return;
		}

		vwPlayerControlsRoot = ((LayoutInflater) getRootView().getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mediaplayer_control,
				getRootView());
		rlPlayerControls = (XPMB_RelativeLayout) vwPlayerControlsRoot.findViewById(R.id.playerc_l);
		tvCurPos = (XPMB_TextView) vwPlayerControlsRoot.findViewById(R.id.tvCurPos);
		tvTotalLen = (XPMB_TextView) vwPlayerControlsRoot.findViewById(R.id.tvTotalLen);
		pbTrackPos = (ProgressBar) vwPlayerControlsRoot.findViewById(R.id.pbCurPos);
		ivPlayStatus = (XPMB_ImageView) vwPlayerControlsRoot.findViewById(R.id.ivPlayStatus);

		rlPlayerControls.setAlpha(0.0f);

		if (tlRoot == null) {
			tlRoot = new XPMB_TableLayout(getRootView().getContext());
			AbsoluteLayout.LayoutParams rootP = new AbsoluteLayout.LayoutParams(pxFromDip(396),
					pxFromDip(32 + (60 * alItems.size())), pxFromDip(48), pxFromDip(122));
			tlRoot.setLayoutParams(rootP);

			for (XPMBSubmenuItem_MUSIC xsi : alItems) {
				int idy = alItems.indexOf(xsi);
				XPMB_TableRow cItem = new XPMB_TableRow(getRootView().getContext());
				XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
				XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
				cItem.setId(getNextID());
				cIcon.setId(getNextID());
				cLabel.setId(getNextID());

				// Setup Item Container
				TableLayout.LayoutParams cItemParams = new TableLayout.LayoutParams(pxFromDip(396),
						pxFromDip(60));
				if (idy == 0) {
					cItemParams.topMargin = pxFromDip(16);
					cItemParams.bottomMargin = pxFromDip(16);
				}
				cItem.setLayoutParams(cItemParams);

				// Setup Icon
				TableRow.LayoutParams cIconParams = new TableRow.LayoutParams((int) pxFromDip(60),
						(int) pxFromDip(60));
				cIconParams.column = 0;
				cIcon.setLayoutParams(cIconParams);
				cIcon.setTag(idy);
				cIcon.setImageDrawable(xsi.getTrackMetadata().getTrackAlbumCover());
				cIcon.setOnTouchListener(mTouchListener);

				// Setup Label
				TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams(
						(int) pxFromDip(320), (int) pxFromDip(60));
				cLabelParams.leftMargin = pxFromDip(16);
				cLabelParams.column = 1;
				cLabel.setLayoutParams(cLabelParams);
				cLabel.setTag(idy);
				cLabel.setText(xsi.getTrackMetadata().getTrackName() + "\r\n"
						+ xsi.getTrackMetadata().getTrackAuthor());
				cLabel.setTextColor(Color.WHITE);
				cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
				cLabel.setTextAppearance(getRootView().getContext(),
						android.R.style.TextAppearance_Medium);
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
		}

		getRootView().addView(tlRoot);

		if (getRootActivity().getObjectFromStore("playerStatus") != null) {
			intPlayerStatus = (Integer) getRootActivity().getObjectFromStore("playerStatus");
			switch (intPlayerStatus) {
			case PLAYER_STATUS_PLAYING:
				ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
						R.drawable.ui_status_media_play));
				pbTrackPos.setMax(mpPlayer.getDuration());
				tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
				doShowPlayerControls(true);
				break;
			case PLAYER_STATUS_PAUSED:
				ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
						R.drawable.ui_status_media_pause));
				pbTrackPos.setMax(mpPlayer.getDuration());
				tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
				rlPlayerControls.setAlpha(1.0f);
				doShowPlayerControls(false);
				break;
			}
		}
		if (getRootActivity().getObjectFromStore("selectionIndex") != null) {
			// alItems.get(0).getParentContainer().setTopMargin(0);
			// alItems.get(0).getParentContainer().setBottomMargin(0);
			// alItems.get(0).getParentLabel().setAlpha(0.5f);
			intSelItem = (Integer) getRootActivity().getObjectFromStore("selectionIndex");
			intLastPlayed = (Integer) getRootActivity().getObjectFromStore("lastIndex");
			// centerOnItem(intSelItem);
			// doCenterOnItemPos();
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
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveUp();
			}
			break;
		case XPMB_Main.KEYCODE_DOWN:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveDown();
			}
			break;
		case XPMB_Main.KEYCODE_CROSS:
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			getRootActivity().requestUnloadSubmenu();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_LEFT:
			if (intSelItem == 0 || alItems.size() == 0) {
				break;
			} else {
				execCustItem(intSelItem - 1);
				centerOnItem(intLastPlayed);
			}
			break;
		case XPMB_Main.KEYCODE_SHOULDER_RIGHT:
			if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
				break;
			} else {
				execCustItem(intSelItem + 1);
				centerOnItem(intLastPlayed);
			}
			break;
		}
	}

	@Override
	public void sendKeyHold(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_UP:
			ttFastScroll.setScrollDirection(SCROLL_DIR_UP);
			ttFastScroll.setEnabled(true);
			break;
		case XPMB_Main.KEYCODE_DOWN:
			ttFastScroll.setScrollDirection(SCROLL_DIR_DOWN);
			ttFastScroll.setEnabled(true);
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
		getRootActivity().requestUnloadSubmenu();
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

		// getRootActivity().lockKeys(true);
		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		// getMessageBus().postDelayed(new Runnable() {

		// @Override
		// public void run() {
		// getRootActivity().lockKeys(false);
		// }

		// }, 160);
		++intSelItem;
	}

	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		// getRootActivity().lockKeys(true);
		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		// getMessageBus().postDelayed(new Runnable() {

		// @Override
		// public void run() {
		// getRootActivity().lockKeys(false);
		// }

		// }, 160);

		--intSelItem;
	}

	private void doShowPlayerControls(boolean show) {
		if (show) {
			aUIAnimatorW.setAnimationType(ANIM_SHOW_MEDIA_CONTROLS);
		} else {
			aUIAnimatorW.setAnimationType(ANIM_HIDE_MEDIA_CONTROLS);
		}
		aUIAnimator.start();
	}

	private void execCustItem(int index) {
		if (mpPlayer != null) {
			if (index == intLastPlayed) {
				if (intPlayerStatus == PLAYER_STATUS_PLAYING) {
					intPlayerStatus = PLAYER_STATUS_PAUSED;
					ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
							R.drawable.ui_status_media_pause));
					mpPlayer.pause();
					doShowPlayerControls(false);
				} else {
					intPlayerStatus = PLAYER_STATUS_PLAYING;
					ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
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
		mpPlayer = MediaPlayer.create(getRootActivity().getWindow().getContext(),
				Uri.fromFile(alItems.get(index).getTrackPath()));
		mpPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				if ((intLastPlayed + 1) < alItems.size()) {
					doCenterOnItemPre();
					execCustItem(intLastPlayed + 1);
					centerOnItem(intLastPlayed);
				} else {
					doShowPlayerControls(false);
				}
			}
		});

		ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
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
	public void doCleanup() {
		if (alItems.size() == 0 && tv_no_music != null) {
			getRootView().removeView(tv_no_music);
			return;
		}
		if (alItems != null && alItems.size() > 0) {
			getRootActivity().putObjectInStore("submenu_music_alItems", alItems);
		}
		if (tlRoot != null) {
			getRootActivity().putObjectInStore("submenu_music_tlRoot", tlRoot);
			getRootView().removeView(tlRoot);
		}
		if (ivPlayStatus != null) {
			getRootView().removeView(ivPlayStatus);
		}
		if (rlPlayerControls != null) {
			getRootView().removeView(rlPlayerControls);
		}
		if (mpPlayer != null) {
			getRootActivity().putObjectInStore("playerStatus", intPlayerStatus);
			getRootActivity().putObjectInStore("selectionIndex", intSelItem);
			getRootActivity().putObjectInStore("lastIndex", intLastPlayed);
			getRootActivity().putObjectInStore("playerObj", mpPlayer);
		}
		if (albumCovers != null && albumCovers.size() > 0) {
			getRootActivity().putObjectInStore("albumCovers", albumCovers);
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
		if (albumCovers != null) {
			albumCovers.clear();
		}
		if (tlRoot != null) {
			tlRoot.removeAllViews();
		}
		if (alItems != null) {
			alItems.clear();
		}
		getRootActivity().removeObjectFromStore("submemu_music_tlRoot");
		getRootActivity().removeObjectFromStore("submemu_music_alItems");
		getRootActivity().removeObjectFromStore("playerStatus");
		getRootActivity().removeObjectFromStore("selectionIndex");
		getRootActivity().removeObjectFromStore("lastIndex");
		getRootActivity().removeObjectFromStore("playerObj");
		getRootActivity().removeObjectFromStore("albumCovers");

		tlRoot = null;
		rlPlayerControls = null;
		alItems = null;
		mpPlayer = null;
		albumCovers = null;
	}
}
