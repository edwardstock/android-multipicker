package com.edwardstock.multipicker.internal

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RestrictTo
import androidx.core.content.FileProvider
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.data.MediaFile
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class CameraHandler {
    private var mCurrentMediaPath: String? = null
    fun getCameraPhotoIntent(context: Context, config: PickerConfig, savePath: PickerSavePath?): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = PickerUtils.createImageFile(savePath ?: config.photoSavePath)
        if (imageFile != null) {
            val appContext = context.applicationContext
            val uri: Uri = FileProvider.getUriForFile(appContext, getProviderName(context), imageFile)
            mCurrentMediaPath = "file:" + imageFile.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            PickerUtils.grantAppPermission(context, intent, uri)
            return intent
        }
        return null
    }

    fun getCameraVideoIntent(context: Context, config: PickerConfig, savePath: PickerSavePath?): Intent? {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val videoFile = PickerUtils.createVideoFile(context, savePath ?: config.videoSavePath)
        if (videoFile != null) {
            val appContext = context.applicationContext
            val uri: Uri = FileProvider.getUriForFile(appContext, getProviderName(context), videoFile)
            mCurrentMediaPath = "file:" + videoFile.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            PickerUtils.grantAppPermission(context, intent, uri)
            return intent
        }
        return null
    }

    fun getVideo(context: Context, imageReadyListener: OnImageReadyListener?) {
        checkNotNull(imageReadyListener) { "OnImageReadyListener must not be null" }
        if (mCurrentMediaPath == null) {
            Timber.w("mCurrentMediaPath null. This happen if you haven't call #getCameraVideoIntent() or the activity is being recreated")
            imageReadyListener.onImageReady(emptyList())
            return
        }
        val videoUri = Uri.parse(mCurrentMediaPath)
        if (videoUri != null) {
            MediaScannerConnection.scanFile(context.applicationContext, arrayOf(videoUri.path), null) { path: String?, uri: Uri ->
                var lpath = path
                Timber.d("File $lpath was scanned successfully: $uri")
                if (lpath == null) {
                    Timber.d("This should not happen, go back to Immediate implemenation")
                    lpath = mCurrentMediaPath
                }
                imageReadyListener.onImageReady(singleListFromPath(lpath))
                PickerUtils.revokeAppPermission(context, videoUri)
            }
        }
    }

    fun getImage(context: Context, imageReadyListener: OnImageReadyListener?) {
        checkNotNull(imageReadyListener) { "OnImageReadyListener must not be null" }
        if (mCurrentMediaPath == null) {
            Timber.w("mCurrentMediaPath null. This happen if you haven't call #getCameraPhotoIntent() or the activity is being recreated")
            imageReadyListener.onImageReady(emptyList())
            return
        }
        val imageUri = Uri.parse(mCurrentMediaPath)
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.applicationContext, arrayOf(imageUri.path), null) { path: String?, uri: Uri ->
                var lpath = path
                Timber.d("File $lpath was scanned successfully: $uri")
                if (lpath == null) {
                    Timber.d("This should not happen, go back to Immediate implemenation")
                    lpath = mCurrentMediaPath
                }
                imageReadyListener.onImageReady(singleListFromPath(lpath))
                PickerUtils.revokeAppPermission(context, imageUri)
            }
        }
    }

    fun removeCaptured() {
        if (mCurrentMediaPath != null) {
            val file = File(mCurrentMediaPath!!)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    val currentMediaPath: String
        get() = mCurrentMediaPath!!.replace("file:", "")

    fun onSaveInstanceState(outState: Bundle) {
        if (mCurrentMediaPath != null) {
            Timber.d("Save current capturing path: %s", mCurrentMediaPath)
            outState.putString(CURRENT_CAPTURE_PATH, mCurrentMediaPath)
        }
    }

    fun onRestoreInstanceState(savedState: Bundle?) {
        if (savedState != null && savedState.containsKey(CURRENT_CAPTURE_PATH)) {
            mCurrentMediaPath = savedState.getString(CURRENT_CAPTURE_PATH)
            Timber.d("Restore current capturing path: %s", mCurrentMediaPath)
        }
    }

    private fun getProviderName(context: Context): String {
        return String.format(Locale.getDefault(), "%s%s", context.packageName, ".multipicker.provider")
    }

    interface OnImageReadyListener {
        fun onImageReady(files: List<MediaFile>)
    }

    companion object {
        private const val CURRENT_CAPTURE_PATH = "camera_handler_media_path"
        fun singleListFromPath(path: String?): List<MediaFile> {
            val images: MutableList<MediaFile> = ArrayList<MediaFile>()
            images.add(MediaFile(0, PickerUtils.getNameFromFilePath(path!!), path))
            return images
        }
    }
}