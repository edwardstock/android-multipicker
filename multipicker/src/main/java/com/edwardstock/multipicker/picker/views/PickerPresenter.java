package com.edwardstock.multipicker.picker.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.edwardstock.multipicker.internal.CameraHandler;

import androidx.annotation.CallSuper;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@InjectViewState
public class PickerPresenter extends MvpPresenter<PickerView> {
    private final static int REQUEST_CAPTURE_PHOTO = 1010;
    private final static int REQUEST_CAPTURE_VIDEO = 1011;
    private CameraHandler mCameraHandler;

    public PickerPresenter() {
        mCameraHandler = new CameraHandler();
    }

    public void handleCapturePhoto() {
        getViewState().capturePhotoWithPermissions(mCameraHandler, REQUEST_CAPTURE_PHOTO);
    }

    public void handleCaptureVideo() {
        getViewState().captureVideoWithPermissions(mCameraHandler, REQUEST_CAPTURE_VIDEO);
    }

    @CallSuper
    public void handleExtras(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CAPTURE_PHOTO) {
            if (resultCode == RESULT_OK) {
                getViewState().scanMedia(mCameraHandler.getCurrentMediaPath(), this::onScanned);
            } else if (resultCode == RESULT_CANCELED) {
                abortCapture();
            }
        } else if (requestCode == REQUEST_CAPTURE_VIDEO) {
            if (resultCode == RESULT_OK) {
                getViewState().scanMedia(mCameraHandler.getCurrentMediaPath(), this::onScanned);
            } else if (resultCode == RESULT_CANCELED) {
                abortCapture();
            }
        }
    }

    private void onScanned(String path, Uri uri) {
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            Timber.d("OnScanned: %s", path);
            getViewState().startUpdateFiles();
        }, 300);
    }


    void abortCapture() {
        mCameraHandler.removeCaptured();
    }
}
