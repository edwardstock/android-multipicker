package com.edwardstock.multipicker.picker.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;

import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.CameraHandler;
import com.edwardstock.multipicker.internal.mvp.MvpPresenter;

import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class PickerPresenter extends MvpPresenter<PickerView> {
    private final static int REQUEST_CAPTURE_PHOTO = 1010;
    private final static int REQUEST_CAPTURE_VIDEO = 1011;
    private CameraHandler mCameraHandler;

    public PickerPresenter() {
        mCameraHandler = new CameraHandler();
    }

    public void handleCapturePhoto() {
        callOnView(v -> v.capturePhotoWithPermissions(mCameraHandler, REQUEST_CAPTURE_PHOTO));
    }

    public void handleCaptureVideo() {
        callOnView(v -> v.captureVideoWithPermissions(mCameraHandler, REQUEST_CAPTURE_VIDEO));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCameraHandler.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreSavedState(Bundle savedState) {
        super.onRestoreSavedState(savedState);
        mCameraHandler.onRestoreInstanceState(savedState);
    }

    @CallSuper
    public void handleExtras(int requestCode, int resultCode, Intent intent) {
        MediaFile file = new MediaFile(mCameraHandler.getCurrentMediaPath());
        if (requestCode == REQUEST_CAPTURE_PHOTO) {
            if (resultCode == RESULT_OK) {
                callOnView(v -> v.scanMedia(file, this::onScanned));
            } else if (resultCode == RESULT_CANCELED) {
                abortCapture();
            }
        } else if (requestCode == REQUEST_CAPTURE_VIDEO) {
            if (resultCode == RESULT_OK) {
                callOnView(v -> v.scanMedia(file, this::onScanned));
            } else if (resultCode == RESULT_CANCELED) {
                abortCapture();
            }
        }
    }

    void abortCapture() {
        mCameraHandler.removeCaptured();
    }

    private void onScanned(String path, Uri uri) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Timber.d("OnScanned: %s", path);
            callOnView(PickerView::startUpdateFiles);
        }, 300);
    }
}
