package com.edwardstock.multipicker.picker.views

import android.media.MediaScannerConnection.OnScanCompletedListener
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.mvp.MvpView

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
interface PickerView : MvpView {
    fun capturePhotoWithPermissions()
    fun captureVideoWithPermissions()
    fun scanMedia(file: MediaFile, listener: OnScanCompletedListener)
    fun startUpdateFiles()
}