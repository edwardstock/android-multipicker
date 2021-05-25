package com.edwardstock.multipicker.picker.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import com.edwardstock.multipicker.internal.CameraHandler
import com.edwardstock.multipicker.internal.mvp.MvpPresenter

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class PickerPresenter : MvpPresenter<PickerView>() {
    private val cameraHandler: CameraHandler = CameraHandler()

    fun handleCapturePhoto() {
        callOnView { v: PickerView -> v.capturePhotoWithPermissions() }
    }

    fun handleCaptureVideo() {
        callOnView { v: PickerView -> v.captureVideoWithPermissions() }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        cameraHandler.onSaveInstanceState(outState!!)
    }

    override fun onRestoreSavedState(savedState: Bundle?) {
        super.onRestoreSavedState(savedState)
        cameraHandler.onRestoreInstanceState(savedState)
    }

    @Suppress("UNUSED_PARAMETER")
    @CallSuper
    fun handleExtras(requestCode: Int, resultCode: Int, intent: Intent?) {
//        val file = MediaFile(path = cameraHandler.currentMediaPath)
//        if (requestCode == REQUEST_CAPTURE_PHOTO) {
//            if (resultCode == Activity.RESULT_OK) {
//                callOnView { v: PickerView -> v.scanMedia(file) { path: String, uri: Uri -> onScanned(path, uri) } }
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                abortCapture()
//            }
//        } else if (requestCode == REQUEST_CAPTURE_VIDEO) {
//            if (resultCode == Activity.RESULT_OK) {
//                callOnView { v: PickerView -> v.scanMedia(file) { path: String, uri: Uri -> onScanned(path, uri) } }
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                abortCapture()
//            }
//        }
    }

    fun abortCapture() {
        cameraHandler.removeCaptured()
    }


    fun onTakenMediaReady() {
        Handler(Looper.getMainLooper()).postDelayed({
            callOnView { obj: PickerView -> obj.startUpdateFiles() }
        }, 300)
    }

    companion object {
        private const val REQUEST_CAPTURE_PHOTO = 1010
        private const val REQUEST_CAPTURE_VIDEO = 1011
    }

}