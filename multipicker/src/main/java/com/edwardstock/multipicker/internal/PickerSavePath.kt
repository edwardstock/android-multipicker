package com.edwardstock.multipicker.internal

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.edwardstock.multipicker.picker.PickerConst
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.*

@Parcelize
class PickerSavePath(
        var path: String,
        var isFullPath: Boolean = false
) : Parcelable {
    fun toUriFile(context: Context): Uri {
        if (isFullPath) {
            return File(path).toUri()
        }
        return context.getExternalFilesDir(path)!!.toUri()
    }

    fun toUriProvider(context: Context): Uri {
        return FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + PickerConst.FILE_PROVIDER_AUTHORITY,
                toFile(context)!!
        )
    }

    fun toFile(context: Context): File? {
        return if (isFullPath) {
            return File(path)
        } else {
            context.getExternalFilesDir(path)
        }
    }

    companion object {

        @JvmField
        val DEFAULT = PickerSavePath("Camera", false)

        @JvmField
        val THUMBNAILS = PickerSavePath(".thumbnails", false)

        @JvmStatic
        fun newTimestampTmpFile(context: Context, extension: String, prefix: String = "camera_"): PickerSavePath {
            val ts = Date().time
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    throw IllegalStateException("Unable to create directory ${storageDir.absolutePath}")
                }
            }

            return PickerSavePath(
                    File.createTempFile("${prefix}${ts}", ".${extension}", storageDir).absolutePath, true
            )
        }
    }
}