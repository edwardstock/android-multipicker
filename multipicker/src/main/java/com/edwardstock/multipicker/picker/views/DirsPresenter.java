package com.edwardstock.multipicker.picker.views;

import android.os.Handler;
import android.os.Looper;

import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.adapters.DirsAdapter;

import java.util.List;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class DirsPresenter extends PickerPresenter<DirsView> implements MediaFileLoader.OnLoadListener {
    private DirsAdapter mAdapter;

    public DirsPresenter() {
        mAdapter = new DirsAdapter(this::onDirClick);
    }


    @Override
    public void updateFiles(MediaFileLoader loader) {
        callOnView(DirsView::showProgress);
        loader.loadDeviceImages(getConfig(), this);
    }

    @Override
    public void onFilesLoadFailed(Throwable t) {
        callOnView(v -> {
            v.hideProgress();
            v.onError(t);
            v.hideRefreshProgress();
        });
    }

    @Override
    public void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList) {
        new Handler(Looper.getMainLooper()).post(() -> {
            callOnView(v -> {
                v.hideRefreshProgress();
                v.hideProgress();
                v.showEmpty(dirList.isEmpty());
            });

            mAdapter.setData(dirList);
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onViewAttach() {
        super.onViewAttach();
        callOnView(v -> {
            v.setAdapter(mAdapter);
            v.setOnRefreshListener(() -> {
                v.rescanFiles(() -> {
                    v.startUpdateFiles();
                    v.showRefreshProgress();
                });
            });
        });
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
    }

    private void onDirClick(Dir dir) {
        MediaFile firstFile = dir.getFiles().get(0);
        dir.getFiles().clear();
        // dont't pass all files to parcel, TransactionTooLarge - guarantee, but don't use Dir.addFile, because files counter will breaks
        // @TODO make more beautiful solution
        dir.getFiles().add(firstFile);
        callOnView(v -> v.startFiles(dir));
    }
}
