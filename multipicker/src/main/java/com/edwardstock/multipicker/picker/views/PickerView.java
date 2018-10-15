package com.edwardstock.multipicker.picker.views;

import android.content.Intent;
import android.media.MediaScannerConnection;

import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.CameraHandler;
import com.edwardstock.multipicker.internal.mvp.MvpView;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public interface PickerView extends MvpView {
    void capturePhotoWithPermissions(CameraHandler cameraHandler, int requestCode);
    void captureVideoWithPermissions(CameraHandler cameraHandler, int requestCode);
    void finishCapturePhoto(CameraHandler cameraHandler, Intent intent);
    void finishCaptureVideo(CameraHandler cameraHandler, Intent intent);
    void scanMedia(MediaFile path, MediaScannerConnection.OnScanCompletedListener listener);
    void startUpdateFiles();
}
