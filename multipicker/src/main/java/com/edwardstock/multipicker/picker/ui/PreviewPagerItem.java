package com.edwardstock.multipicker.picker.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;

import java.lang.ref.WeakReference;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_DIR;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_MEDIA_FILE;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public abstract class PreviewPagerItem extends Fragment {
    private final static String EXTRA_POS = "EXTRA_POS";
    @BindView(R2.id.mp_selection_action_add) ImageView btnAdd;
    @BindView(R2.id.mp_selection_action_send) ImageView btnSend;
    @BindView(R2.id.mp_progress) ProgressBar progress;
    @BindView(R2.id.layout_actions) ViewGroup actionGroup;
    private MediaFile mMediaFile;
    private Dir mDir;
    private WeakReference<PreviewerActivity> mActivity;

    protected enum Orient {
        Portrait,
        PortraitReverse,
        Landscape,
        LandscapeReverse
    }

    public static PreviewPagerItem newInstance(int pos, MediaFile file, Dir dir) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MEDIA_FILE, file);
        args.putParcelable(EXTRA_DIR, dir);
        args.putInt(EXTRA_POS, pos);

        PreviewPagerItem fragment;

        if (file.isVideo()) {
            fragment = new PreviewVideoPagerItem();
        } else {
            fragment = new PreviewImagePagerItem();
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = new WeakReference<>(((PreviewerActivity) context));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public Orient getScreenOrientation(Context context) {
        final int screenOrientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (screenOrientation) {
            case Surface.ROTATION_0:
                return Orient.Portrait;
            case Surface.ROTATION_180:
                return Orient.PortraitReverse;
            case Surface.ROTATION_90:
                return Orient.Landscape;
            case Surface.ROTATION_270:
                return Orient.LandscapeReverse;
            default:
                return Orient.LandscapeReverse;
        }
    }

    public MediaFile getMediaFile() {
        return mMediaFile;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        mMediaFile = getArguments().getParcelable(EXTRA_MEDIA_FILE);
        mDir = getArguments().getParcelable(EXTRA_DIR);

        btnSend.setOnClickListener(v -> {
            safeActivity(act -> {
                act.submitResult(Collections.singletonList(getMediaFile()));
            });
        });

        btnAdd.setOnClickListener(v -> {
            safeActivity(act -> {
                act.submitSelection(getMediaFile());
            });
        });

        initMedia(mMediaFile);

        return view;
    }

    protected void onPageActive() {
    }

    protected void onPageInactive() {
    }

    protected void safeActivity(ActConsumer actConsumer) {
        if (mActivity != null && mActivity.get() != null) {
            final PreviewerActivity act = mActivity.get();
            actConsumer.onActivity(act);
        }
    }

    protected Dir getDir() {
        return mDir;
    }

    protected abstract void initMedia(MediaFile mediaFile);

    @LayoutRes
    protected abstract int getLayoutId();

    public interface ActConsumer {
        void onActivity(PreviewerActivity act);
    }

}
