package com.edwardstock.multipicker.picker.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.internal.PickerSavePath;
import com.edwardstock.multipicker.internal.helpers.Acceptor;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.internal.widgets.GridSpacingItemDecoration;
import com.edwardstock.multipicker.picker.views.BaseFsPresenter;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public abstract class PickerFileSystemFragment extends Fragment {
    private GridSpacingItemDecoration mGridSpacingItemDecoration;
    private WeakReference<PickerActivity> mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = new WeakReference<>(((PickerActivity) getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity = new WeakReference<>(((PickerActivity) getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("Destroy fragment view");
        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }
    }

    public void updateFiles() {
        if (getPresenter() != null) {
            getPresenter().updateFiles(new MediaFileLoader(getActivity()));
        }
    }

    public void onBackPressed() {
        Timber.d("On back pressed fragment");
    }

    protected boolean hasActivity() {
        if (mActivity == null || mActivity.get() == null) {
            return false;
        }
        final PickerActivity activity = mActivity.get();
        return activity != null;
    }

    protected final void safeActivity(Acceptor<PickerActivity> callback) {
        if (mActivity == null || mActivity.get() == null) {
            Timber.d("Activity already unavailable");
            return;
        }
        final PickerActivity activity = mActivity.get();
        if (activity != null) {
            callback.call(activity);
        }
    }

    protected void setGridSpacingItemDecoration(RecyclerView list, int spanCount) {
        Timber.d("Set grid spacing");
        if (mGridSpacingItemDecoration != null) {
            list.removeItemDecoration(mGridSpacingItemDecoration);
        }
        mGridSpacingItemDecoration = new GridSpacingItemDecoration(spanCount, DisplayHelper.dpToPx(getContext(), 1), false);
        list.addItemDecoration(mGridSpacingItemDecoration);
    }

    protected PickerSavePath getCapturedSavePath() {
        return null;
    }

    protected abstract BaseFsPresenter getPresenter();
}
