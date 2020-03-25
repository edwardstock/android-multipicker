package com.edwardstock.multipicker.picker.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

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
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.video.VideoListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class PreviewVideoPagerItem extends PreviewPagerItem implements Player.EventListener {
    @BindView(R2.id.video_view) PlayerView playerView;
    @BindView(R2.id.exo_play) View playBtn;
    @BindView(R2.id.mp_timing_view) ViewGroup timingView;
    @BindView(R2.id.exo_pause) View pauseView;
    private SimpleExoPlayer mPlayer;
    private DataSource.Factory mSourceFactory = new FileDataSourceFactory();
    private MediaSource mMediaSource;
    private boolean mPrepared = false;
    private int mTimingViewHeight = -1;
    private boolean mEnded = false;
    private long mLastPosition = 0;
    private boolean mPlayerCreated = false;
    private DefaultTrackSelector mTrackSelector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
    public void onStart() {
        super.onStart();
        createPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        createPlayer();
        if (mPlayer != null) {
            mPlayer.seekTo(mLastPosition);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
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
                mEnded = true;
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
        playBtn.setOnClickListener(v -> {
            // This is the MediaSource representing the media to be played.
            mMediaSource = new ExtractorMediaSource.Factory(mSourceFactory).createMediaSource(getMediaFile().getUri());
            // Prepare the player with the source.
            createPlayer();
            if (!mPrepared) {
                mPlayer.prepare(mMediaSource);
                mPrepared = true;
            }
            if (mEnded) {
                mPlayer.seekTo(0);
                mEnded = false;
            }
            mPlayer.setPlayWhenReady(true);
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mp_page_preview_video_item;
    }

    @Override
    protected void onPageInactive() {
        super.onPageInactive();
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    private void releasePlayer() {
        safeActivity(act -> {
            act.runOnUiThread(() -> {
                if (mPlayer != null) {
                    mPlayer.setPlayWhenReady(false);
                    mPlayer.release();
                    mPlayer = null;
                    mTrackSelector = null;
                    playerView.setPlayer(null);
                    mPlayerCreated = false;
                    mPrepared = false;
                    mEnded = false;
                    mMediaSource = null;
                }
            });
        });
    }

    private void createPlayer() {
        safeActivity(act -> {
            act.runOnUiThread(() -> {
                if (mPlayer != null || mPlayerCreated) {
                    return;
                }
                Timber.d("Create player");
                // 1. Create a default TrackSelector
                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
                mTrackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);

                // 2. Create the player
                mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), mTrackSelector);
                mPlayer.addListener(this);

                playerView.setPlayer(mPlayer);
                playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
                    @Override
                    public void onVisibilityChange(int visibility) {
                        if (visibility == View.VISIBLE) {
                            actionGroup.setVisibility(View.VISIBLE);
                            actionGroup.animate()
                                    .translationY(0)
                                    .setDuration(300)
                                    .setInterpolator(new DecelerateInterpolator(1.2f))
                                    .start();
                        } else {
                            actionGroup.setVisibility(View.VISIBLE);
                            actionGroup.animate()
                                    .translationY(mTimingViewHeight)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .setDuration(350)
                                    .start();
                        }
                    }
                });

                mPlayer.addVideoListener(new VideoListener() {
                    @Override
                    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                        safeActivity(act -> {
                            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                        });
                    }

                    @Override
                    public void onRenderedFirstFrame() {

                    }
                });
                mPlayerCreated = true;
            });
        });
    }
}
