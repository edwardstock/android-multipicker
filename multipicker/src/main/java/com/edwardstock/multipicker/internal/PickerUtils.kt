package com.edwardstock.multipicker.internal

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.edwardstock.multipicker.data.MediaFile
import timber.log.Timber
import java.io.*
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

object PickerUtils {

    fun createVideoThumbFile(ctx: Context, file: MediaFile): File? {
        // External sdcard location
        val mediaStorageDir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PickerSavePath.THUMBNAILS.path)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.w("Oops! Failed create %s", mediaStorageDir.toString())
                return null
            }
        }

        if (file.path == null) {
            Timber.e("MediaFile %s does not have a path", file.toString())
        }

        // Create a media file name
        var srcName = File(file.path!!).name
        srcName = srcName.substring(0, srcName.lastIndexOf('.'))
        val imageFileName = "thumb_$srcName"
        var imageFile: File? = null
        try {
            val f = File(mediaStorageDir, "$imageFileName.jpg")
            if (f.exists() && f.length() > 0) {
                file.videoInfo!!.previewPath = f.absolutePath
                return null
            }
            if (!f.createNewFile()) {
                Timber.d("Can't create new file")
                return null
            }
            imageFile = f
            file.videoInfo!!.previewPath = imageFile.absolutePath
        } catch (e: IOException) {
            Timber.d("Oops! Failed create $imageFileName file")
        }
        return imageFile
    }

    fun createVideoFile(ctx: Context, savePath: PickerSavePath): File? {
        // External sdcard location
        val path = savePath.path
        val mediaStorageDir = if (savePath.isFullPath) {
            File(path)
        } else {
            File(ctx.getExternalFilesDir(Environment.DIRECTORY_DCIM), path)
//            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), path)
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("Oops! Failed create %s", path)
                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "VID_$timeStamp"
        var imageFile: File? = null
        try {
            imageFile = File.createTempFile(imageFileName, ".mp4", mediaStorageDir)
        } catch (e: IOException) {
            Timber.d("Oops! Failed create $imageFileName file")
        }
        return imageFile
    }

    fun createImageFile(savePath: PickerSavePath): File? {
        // External sdcard location
        val path = savePath.path
        val mediaStorageDir = if (savePath.isFullPath) File(path) else File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("Oops! Failed create %s", path)
                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp"
        var imageFile: File? = null
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", mediaStorageDir)
        } catch (e: IOException) {
            Timber.d("Oops! Failed create $imageFileName file")
        }
        return imageFile
    }

    fun getNameFromFilePath(path: String): String {
        return if (path.contains(File.separator)) {
            path.substring(path.lastIndexOf(File.separator) + 1)
        } else path
    }

    fun grantAppPermission(context: Context, intent: Intent?, fileUri: Uri?) {
        val resolvedIntentActivities = context.packageManager
                .queryIntentActivities(intent!!, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            context.grantUriPermission(packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun revokeAppPermission(context: Context, fileUri: Uri?) {
        context.revokeUriPermission(fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun isGifFormat(image: MediaFile): Boolean {
        val extension = image.path!!.substring(image.path!!.lastIndexOf(".") + 1, image.path!!.length)
        return extension.equals("gif", ignoreCase = true)
    }

    fun isVideoFormat(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }

    fun isVideoFormat(image: MediaFile): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(image.path)
        return mimeType != null && mimeType.startsWith("video")
    }

    private fun getContentFileFromUri(context: Context, uri: Uri): File? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(uri, proj, null, null, null)
            if (cursor == null) {
                throw IllegalStateException("Unable to get cursor using contentResolver")
            }

            val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()

            File(cursor.getString(columnIndex))
        } finally {
            cursor?.close()
        }
    }

    fun writeCapturedMedia(context: Context, mediaFile: MediaFile) {
        val resolver = context.applicationContext.contentResolver
        val sourceFile = File(mediaFile.path!!)

        val targetLibrary = if (!mediaFile.isVideo) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        }

        val newContent = ContentValues().apply {
            if (mediaFile.isVideo) {
                put(MediaStore.Video.Media.TITLE, sourceFile.name)
                put(MediaStore.Video.Media.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, Environment.DIRECTORY_PICTURES)
                }
            } else {
                put(MediaStore.Images.Media.TITLE, sourceFile.name)
                put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, Environment.DIRECTORY_PICTURES)
                }
            }
        }

        val insertedContentUri = resolver.insert(targetLibrary, newContent)
        @Suppress("FoldInitializerAndIfToElvis")
        if (insertedContentUri == null) {
            throw IllegalStateException("Unable to save media file: contentResolver returned null after insert new media")
        }

        var sourceInputStream: InputStream? = null
        var targetoutputStream: OutputStream? = null
        try {
            sourceInputStream = FileInputStream(sourceFile)
            targetoutputStream = try {
                resolver.openOutputStream(insertedContentUri)
            } catch (e: FileNotFoundException) {
                val insertedContentFile = getContentFileFromUri(context, insertedContentUri)
                        ?: throw IllegalStateException("Unable to get direct path for content uri: $insertedContentUri")

                if (!insertedContentFile.exists()) {
                    // for case, when target dir not exists
                    val parentDirs = File(insertedContentFile.absolutePath.substring(0, insertedContentFile.absolutePath.lastIndexOf(File.separator)))
                    if (!parentDirs.exists()) {
                        parentDirs.mkdirs()
                    }
                    insertedContentFile.createNewFile()
                }
                resolver.openOutputStream(insertedContentUri)
            }
            if (targetoutputStream == null) {
                throw IllegalStateException("Unable to open saved image output stream $insertedContentUri")
            }
            sourceInputStream.copyTo(targetoutputStream)

        } finally {
            sourceInputStream?.close()
            targetoutputStream?.close()
            sourceFile.delete()
        }
    }
}