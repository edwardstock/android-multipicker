package com.edwardstock.multipicker.picker.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.arellomobile.mvp.InjectViewState;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.adapters.DirsAdapter;

import java.util.Collections;
import java.util.List;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@InjectViewState
public class DirsPresenter extends BaseFsPresenter<DirsView> implements MediaFileLoader.OnLoadListener {
    private PickerConfig mConfig;
    private DirsAdapter mAdapter;

    public DirsPresenter() {
        mAdapter = new DirsAdapter(this::onDirClick);
    }


    @Override
    public void updateFiles(MediaFileLoader loader) {
        getViewState().showProgress();
        loader.loadDeviceImages(
                true,
                mConfig.isShowPhotos(),
                mConfig.isShowVideos(),
                Collections.emptyList(),
                this
        );
    }

    public void handleExtras(Bundle intent) {
        mConfig = intent.getParcelable(PickerConst.EXTRA_CONFIG);
    }

    @Override
    public void onFilesLoadFailed(Throwable t) {
        getViewState().hideProgress();
        getViewState().onError(t);
    }

    @Override
    public void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList) {
        new Handler(Looper.getMainLooper()).post(() -> {
            getViewState().hideProgress();
            mAdapter.setData(dirList);
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void attachView(DirsView view) {
        super.attachView(view);
        getViewState().setAdapter(mAdapter);
    }

    public PickerConfig getConfig() {
        return mConfig;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

    }

    private void onDirClick(Dir dir) {
        getViewState().startFiles(dir);
    }
}
