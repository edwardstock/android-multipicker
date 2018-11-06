package com.edwardstock.multipicker.picker.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_DIR;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_MEDIA_FILE;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public abstract class PreviewPagerItem extends Fragment {
    @BindView(R2.id.mp_selection_action_add) ImageView btnAdd;
    @BindView(R2.id.mp_selection_action_send) ImageView btnSend;
    @BindView(R2.id.mp_progress) ProgressBar progress;
    @BindView(R2.id.layout_actions) ViewGroup actionGroup;
    private MediaFile mMediaFile;
    private Dir mDir;
    private WeakReference<PreviewerActivity> mActivity;
    protected PreviewerActivity.SystemUiVisibilityListener mSystemUiVisibilityListener = new PreviewerActivity.SystemUiVisibilityListener() {
        @Override
        public void onChangeVisibleState(boolean visibleNow) {
            if (visibleNow) {
                safeActivity(act -> {
                    actionGroup.animate()
                            .translationY(-act.getNavigationBarHeight())
                            .start();
                });
            } else {
                actionGroup.animate()
                        .translationY(0)
                        .start();
            }
        }
    };

    public static PreviewPagerItem newInstance(MediaFile file, Dir dir) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MEDIA_FILE, file);
        args.putParcelable(EXTRA_DIR, dir);

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

        safeActivity(act -> {
            act.addVisibilityListener(mSystemUiVisibilityListener);
        });

        initMedia(mMediaFile);

        return view;
    }

    @Override
    public void onDestroyView() {
        safeActivity(act -> {
            act.removeVisibilityListener(mSystemUiVisibilityListener);
        });
        super.onDestroyView();
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
