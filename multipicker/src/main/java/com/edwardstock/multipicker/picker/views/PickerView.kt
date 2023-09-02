package com.edwardstock.multipicker.picker.views

import android.media.MediaScannerConnection.OnScanCompletedListener
import com.edwardstock.multipicker.data.MediaFile

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
interface PickerView {
    fun capturePhotoWithPermissions()
    fun captureVideoWithPermissions()
    fun scanMedia(file: MediaFile, listener: OnScanCompletedListener)
    fun startUpdateFiles()
}