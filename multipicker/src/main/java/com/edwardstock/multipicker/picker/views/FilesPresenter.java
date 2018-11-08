package com.edwardstock.multipicker.picker.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.annimon.stream.Stream;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.adapters.FilesAdapter;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class FilesPresenter extends PickerPresenter<FilesView> implements MediaFileLoader.OnLoadListener {
    private final static String FILE_SCROLL_POSITION = "files_scroll_position";
    private PickerConfig mConfig;
    private FilesAdapter mAdapter;
    private Dir mDir;
    private int mSelectionCnt = 0;
    private int mScrollPosition = 0;

    public FilesPresenter() {
    }

    @Override
    public void updateFiles(MediaFileLoader loader) {
        callOnView(FilesView::showProgress);
        Timber.d("Updating files");
        loader.loadDeviceImages(mConfig, mDir, this);
    }

    public void handleExtras(Bundle bundle) {
        mDir = bundle.getParcelable(PickerConst.EXTRA_DIR);
        mConfig = bundle.getParcelable(PickerConst.EXTRA_CONFIG);
        mAdapter = new FilesAdapter(mConfig, null, this::onFileClick);
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
            callOnView(FilesView::hideProgress);
            callOnView(FilesView::hideRefreshProgress);

            final List<File> toDelete = Stream.of(images)
                    .map(MediaFile::getFile)
                    .filter(item -> !item.exists() || item.length() == 0)
                    .toList();

            new Thread(() -> {
                for (final File item : toDelete) {
                    callOnView(v -> {
                        v.removeFileFromMediaDB(item);
                    });
                }
            }).start();

            mAdapter.setData(Stream.of(images).filter(item -> item.exists() && item.length() > 0).toList());
            if (!images.isEmpty()) {
                mAdapter.notifyDataSetChanged();
            } else {
                callOnView(FilesView::showEmpty);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putInt(FILE_SCROLL_POSITION, mScrollPosition);
        }
    }

    @Override
    public void onRestoreSavedState(Bundle savedState) {
        super.onRestoreSavedState(savedState);
        if (savedState != null && savedState.containsKey(FILE_SCROLL_POSITION)) {
            mScrollPosition = savedState.getInt(FILE_SCROLL_POSITION);
        }
    }

    @Override
    public void onViewAttach() {
        super.onViewAttach();
        if (mSelectionCnt == 0) {
            callOnView(v -> {
                v.setSelectionTitle("Выберите файл");
                v.setSelectionSubmitEnabled(false);
            });
        }
        Timber.d("Set adapter");
        callOnView(v -> {
            v.setOnRefreshListener(() -> {
                v.rescanFiles(() -> {
                    v.startUpdateFiles();
                    v.showRefreshProgress();
                });
            });
            v.setAdapter(mAdapter);
            v.scrollTo(mScrollPosition);
            v.setOnSelectionClearListener(v1 -> {
                v.clearSelection();
            });
            v.setOnSelectionDoneListener(v1 -> {
                v.finishWithResult();
            });
            v.setOnSelectionAllListener(v1 -> {
                v.selectAll();
            });


            Timber.d("Restore scroll: %d", mScrollPosition);
        });

    }

    public void setSelection(ImmutableList<MediaFile> selection) {
        if (mSelectionCnt == selection.size()) {
            return;
        }
        mSelectionCnt = selection.size();

        if (selection.isEmpty()) {
            callOnView(v -> {
                v.setSelectionTitle("Выберите файл");
                v.setSelectionSubmitEnabled(false);
            });
            return;
        }

        long numPhotos = Stream.of(selection).filter(item -> !item.isVideo()).count();
        long numVideos = selection.size() - numPhotos;

        if (numPhotos == 0 && numVideos == 0) {
            callOnView(v -> {
                v.setSelectionTitle("Выберите файл");
                v.setSelectionSubmitEnabled(false);
            });
            return;
        }

        if (numPhotos > 0 && numVideos == 0) {
            callOnView(v -> {
                v.setSelectionTitle(String.format(Locale.getDefault(), "Выбрано %d фото", numPhotos));
            });
        } else if (numPhotos == 0 && numVideos > 0) {
            callOnView(v -> {
                v.setSelectionTitle(String.format(Locale.getDefault(), "Выбрано %d видео", numVideos));
            });
        } else {
            callOnView(v -> {
                v.setSelectionTitle(String.format(Locale.getDefault(), "Выбрано %d фото и %d видео", numPhotos, numVideos));
            });
        }
        callOnView(v -> {
            v.setSelectionSubmitEnabled(true);
        });
    }

    public PickerConfig getConfig() {
        return mConfig;
    }

    public void setOnFileMeasuredListener(MediaFile file, FilesAdapter.OnMeasuredListener listener) {
        mAdapter.setOnFileMeasuredListener(file, listener);
    }

    public void setScroll(int position) {
        mScrollPosition = position;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callOnView(v -> {
            v.selectFile(data.getParcelableExtra(PickerConst.EXTRA_MEDIA_FILE));
        });
    }

    @Override
    protected void onViewDetach() {
        super.onViewDetach();
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        callOnView(v -> {
            v.setSelectionTitle("Выберите файл");
            v.setSelectionSubmitEnabled(false);
        });

    }

    private void onFileClick(MediaFile file, boolean isSelected, View sharedView) {
        callOnView(v -> {
            v.startPreview(file, sharedView);
        });
    }
}
