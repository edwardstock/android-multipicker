package com.edwardstock.multipicker.picker.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.annimon.stream.Stream;
import com.arellomobile.mvp.InjectViewState;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.adapters.FilesAdapter;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@InjectViewState
public class FilesPresenter extends BaseFsPresenter<FilesView> implements MediaFileLoader.OnLoadListener {
    private PickerConfig mConfig;
    private FilesAdapter mAdapter;
    private Dir mDir;
    private int mSelectionCnt = 0;

    public FilesPresenter() {
    }

    @Override
    public void updateFiles(MediaFileLoader loader) {
        getViewState().showProgress();
        Timber.d("Updating files");
        loader.loadDeviceImages(mConfig, this);
    }

    public void handleExtras(Bundle bundle) {
        mDir = bundle.getParcelable(PickerConst.EXTRA_DIR);
        mConfig = bundle.getParcelable(PickerConst.EXTRA_CONFIG);
        mAdapter = new FilesAdapter(mConfig, null, this::onFileClick);
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
            List<MediaFile> dirFiles = null;
            for (Dir d : dirList) {
                if (d.equals(mDir)) {
                    dirFiles = d.getFiles();
                    break;
                }
            }
            if (dirFiles == null || dirFiles.isEmpty()) {
                dirFiles = new ArrayList<>(0);
            }
            mAdapter.setData(dirFiles);
            if (!dirFiles.isEmpty()) {
                mAdapter.notifyDataSetChanged();
            } else {
                getViewState().showEmpty();
            }
        });
    }

    @Override
    public void attachView(FilesView view) {
        super.attachView(view);
        if (mSelectionCnt == 0) {
            getViewState().setSelectionTitle("Выберите файл");
            getViewState().setSelectionSubmitEnabled(false);
        }
        Timber.d("Set adapter");
        getViewState().setAdapter(mAdapter);
        getViewState().setOnSelectionClearListener(v -> {
            getViewState().clearSelection();
        });
        getViewState().setOnSelectionDoneListener(v -> {
            getViewState().finishWithResult();
        });
    }

    @Override
    public void detachView(FilesView view) {
        super.detachView(view);
    }

    public void setSelection(ImmutableList<MediaFile> selection) {
        if (mSelectionCnt == selection.size()) {
            return;
        }
        mSelectionCnt = selection.size();

        if (selection.isEmpty()) {
            getViewState().setSelectionTitle("Выберите файл");
            getViewState().setSelectionSubmitEnabled(false);
            return;
        }

        long numPhotos = Stream.of(selection).filter(item -> !item.isVideo()).count();
        long numVideos = selection.size() - numPhotos;

        if (numPhotos == 0 && numVideos == 0) {
            getViewState().setSelectionTitle("Выберите файл");
            getViewState().setSelectionSubmitEnabled(false);
            return;
        }

        if (numPhotos > 0 && numVideos == 0) {
            getViewState().setSelectionTitle(String.format(Locale.getDefault(), "Выбрано %d фото", numPhotos));
        } else if (numPhotos == 0 && numVideos > 0) {
            getViewState().setSelectionTitle(String.format(Locale.getDefault(), "Выбрано %d видео", numVideos));
        } else {
            getViewState().setSelectionTitle(String.format(Locale.getDefault(), "Выбрано %d фото и %d видео", numPhotos, numVideos));
        }
        getViewState().setSelectionSubmitEnabled(true);
    }

    public PickerConfig getConfig() {
        return mConfig;
    }

    public void setOnFileMeasuredListener(MediaFile file, FilesAdapter.OnMeasuredListener listener) {
        mAdapter.setOnFileMeasuredListener(file, listener);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().setSelectionTitle("Выберите файл");
        getViewState().setSelectionSubmitEnabled(false);

    }

    private void onFileClick(MediaFile file, boolean isSelected, View sharedView) {
        getViewState().startPreview(file, sharedView);
    }
}
