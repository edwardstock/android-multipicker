package com.edwardstock.multipicker.internal

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.util.Date

data class PickerUri(
        val fileName: String,
        val uri: Uri,
        val contentValues: ContentValues,
        val id: Long
) {

    /**
     * Works only for API 29+, otherwise it does nothing
     */
    fun markNonPending(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
    }

    fun delete(context: Context): Boolean {
        val resolver = context.contentResolver
        return resolver.delete(uri, null, null) > 0
    }

    companion object {
        fun createImage(context: Context, prefix: String = "camera_"): PickerUri {
            val resolver = context.contentResolver

            val imagesRoot =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Images.Media.getContentUri(
                                MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }

            val ts = Date().time
            val fileName = "${prefix}${ts}.jpg"

            val imageValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(imagesRoot, imageValues) ?: throw RuntimeException("Unable to take new picture")

            return PickerUri(fileName, uri, imageValues, uri.lastPathSegment!!.toLong())
        }

        fun createVideo(context: Context, prefix: String = "camera_"): PickerUri {
            val resolver = context.contentResolver

            val videosRoot =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Video.Media.getContentUri(
                                MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }

            val ts = Date().time
            val fileName = "${prefix}${ts}.mp4"

            val videoValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(videosRoot, videoValues) ?: throw RuntimeException("Unable to take new picture")
            return PickerUri(fileName, uri, videoValues, uri.lastPathSegment!!.toLong())
        }
    }
}