package com.edwardstock.multipicker.picker.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.MediaFile;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.video.VideoListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.view.View.TRANSLATION_Y;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class PreviewVideoPagerItem extends PreviewPagerItem implements Player.EventListener {
    @BindView(R2.id.video_view) PlayerView playerView;
    @BindView(R2.id.mp_button_play) View playBtn;
    @BindView(R2.id.mp_timing_view) ViewGroup timingView;
    private SimpleExoPlayer mPlayer;
    private DataSource.Factory mSourceFactory = new FileDataSourceFactory();
    private MediaSource mMediaSource;
    private int mTimingViewHeight = -1;
    private PreviewerActivity.SystemUiVisibilityListener mSystemUiVisibilityListener = new PreviewerActivity.SystemUiVisibilityListener() {
        @Override
        public void onChangeVisibleState(boolean visibleNow) {
            if (visibleNow) {
                safeActivity(act -> {

                    AnimatorSet set = new AnimatorSet();
                    ObjectAnimator[] anims = new ObjectAnimator[2];

                    anims[0] = ObjectAnimator.ofFloat(actionGroup, TRANSLATION_Y, -(act.getNavigationBarHeight() + mTimingViewHeight));
                    anims[0] = ObjectAnimator.ofFloat(timingView, TRANSLATION_Y, -act.getNavigationBarHeight());
//                    anims[0] = actionGroup.animate()
//                            .translationY(-(act.getNavigationBarHeight() + mTimingViewHeight));
//
//                    anims[1] = timingView.animate()
//                            .translationY(-act.getNavigationBarHeight());

                    set.playTogether(anims);
                });
            } else {
                AnimatorSet set = new AnimatorSet();
                ObjectAnimator[] anims = new ObjectAnimator[2];

                anims[0] = ObjectAnimator.ofFloat(actionGroup, TRANSLATION_Y, mTimingViewHeight);
                anims[0] = ObjectAnimator.ofFloat(timingView, TRANSLATION_Y, 0);

                set.playTogether(anims);
//                actionGroup.animate()
//                        .translationY(mTimingViewHeight)
//                        .start();
//
//                timingView.animate()
//                        .translationY(0)
//                        .start();
            }
        }
    };
    private boolean mPlaying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.mSystemUiVisibilityListener = mSystemUiVisibilityListener;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        timingView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mTimingViewHeight = timingView.getHeight();
                timingView.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPlayer.stop();
        mPlayer.release();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String s = String.valueOf(playbackState);
        switch (playbackState) {
            case Player.STATE_IDLE:
                progress.setVisibility(View.GONE);
                s = "IDLE";
                break;
            case Player.STATE_BUFFERING:
                s = "BUFFERING";
                progress.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_READY:
                progress.setVisibility(View.GONE);
                s = "READY";
                break;
            case Player.STATE_ENDED:
                progress.setVisibility(View.GONE);
                s = "ENDED";
                break;
        }
        Timber.d("Player state: %s", s);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    protected void initMedia(MediaFile mediaFile) {
        // This is the MediaSource representing the media to be played.
        mMediaSource = new ExtractorMediaSource.Factory(mSourceFactory).createMediaSource(getMediaFile().getUri());
        // Prepare the player with the source.
        createPlayer();

        playBtn.setOnClickListener(v -> {
            if (!mPlaying) {
                Timber.d("Prepare");
                mPlayer.prepare(mMediaSource);
                mPlayer.setPlayWhenReady(true);
            } else {
                mPlayer.setPlayWhenReady(false);
            }
            mPlaying = !mPlaying;

        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mp_page_preview_video_item;
    }

    private void createPlayer() {
        if (mPlayer != null) {
            return;
        }
        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        mPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        mPlayer.addListener(this);
//        mStartedPositionResolver = true;

        playerView.setPlayer(mPlayer);
        mPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//                presenter.readyToPlay();
                safeActivity(act -> {
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                });
            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });
    }
}
