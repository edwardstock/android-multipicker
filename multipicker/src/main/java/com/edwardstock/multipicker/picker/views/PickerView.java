package com.edwardstock.multipicker.picker.views;

import android.content.Intent;
import android.media.MediaScannerConnection;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.CameraHandler;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@StateStrategyType(AddToEndSingleStrategy.class)
public interface PickerView extends MvpView {
    void capturePhotoWithPermissions(CameraHandler cameraHandler, int requestCode);
    void captureVideoWithPermissions(CameraHandler cameraHandler, int requestCode);
    void finishCapturePhoto(CameraHandler cameraHandler, Intent intent);
    void finishCaptureVideo(CameraHandler cameraHandler, Intent intent);
    void scanMedia(MediaFile path, MediaScannerConnection.OnScanCompletedListener listener);
    void startUpdateFiles();
}
