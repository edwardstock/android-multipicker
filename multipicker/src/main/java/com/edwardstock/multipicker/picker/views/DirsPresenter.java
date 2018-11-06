package com.edwardstock.multipicker.picker.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.adapters.DirsAdapter;

import java.util.List;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class DirsPresenter extends PickerPresenter<DirsView> implements MediaFileLoader.OnLoadListener {
    private PickerConfig mConfig;
    private DirsAdapter mAdapter;

    public DirsPresenter() {
        mAdapter = new DirsAdapter(this::onDirClick);
    }


    @Override
    public void updateFiles(MediaFileLoader loader) {
        callOnView(DirsView::showProgress);
        loader.loadDeviceImages(mConfig, this);
    }

    public void handleExtras(Bundle intent) {
        mConfig = intent.getParcelable(PickerConst.EXTRA_CONFIG);
    }

    @Override
    public void onFilesLoadFailed(Throwable t) {
        callOnView(v -> {
            v.hideProgress();
            v.onError(t);
        });
    }

    @Override
    public void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList) {
        new Handler(Looper.getMainLooper()).post(() -> {
            callOnView(v -> {
                v.hideProgress();
                v.showEmpty(dirList.isEmpty());
            });

            mAdapter.setData(dirList);
            mAdapter.notifyDataSetChanged();
        });
    }

    public PickerConfig getConfig() {
        return mConfig;
    }

    @Override
    protected void onViewAttach() {
        super.onViewAttach();
        callOnView(v -> v.setAdapter(mAdapter));
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

    }

    private void onDirClick(Dir dir) {
        callOnView(v -> v.startFiles(dir));
    }
}
