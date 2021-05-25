package com.edwardstock.multipicker.internal

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.data.MediaSize
import com.edwardstock.multipicker.data.VideoInfo
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MediaFileLoader(private val ctx: Context) {
    private val photoProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT)
    private val photoAndVideoProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT)

    private val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION)


    private var mExecutorService: ExecutorService? = null
    private val executorLock = Any()

    fun loadDeviceImages(config: PickerConfig, listener: OnLoadListener) {
        executorService.execute(ImageLoadRunnable(config, listener))
    }

    fun abortLoadImages() {
        synchronized(executorLock) {
            if (mExecutorService != null) {
                mExecutorService!!.shutdown()
                mExecutorService = null
            }
        }
    }

    private val executorService: ExecutorService
        get() {
            synchronized(executorLock) {
                if (mExecutorService == null) {
                    mExecutorService = Executors.newSingleThreadExecutor()
                }
            }
            return mExecutorService!!
        }

    private fun createVideoThumbnail(filePath: String): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(filePath)
        //api time unit is microseconds
        return try {
            mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
            mmr.release()
        }
    }

    interface OnLoadListener {
        fun onFilesLoadFailed(t: Throwable?)
        fun onFilesLoadedSuccess(images: List<MediaFile>, dirList: List<Dir>)
    }

    private inner class ImageLoadRunnable(val config: PickerConfig, val imageLoadListener: OnLoadListener) : Runnable {
        private val root = "external"

        override fun run() {
            val cursor: Cursor?
            val projection: Array<String>
            val selection: String

            if (config.isShowVideos && config.isShowPhotos) {
                projection = photoAndVideoProjection
                // select media_type=1 or media_type=3
                selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                        " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

                cursor = ctx.contentResolver.query(
                        MediaStore.Files.getContentUri(root),
                        projection,
                        selection,
                        null,
                        MediaStore.Files.FileColumns.DATE_ADDED
                )
            } else if (!config.isShowPhotos && config.isShowVideos) {
                projection = videoProjection
                cursor = ctx.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        MediaStore.Files.FileColumns.DATE_ADDED
                )
            } else {
                projection = photoProjection
                cursor = ctx.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        MediaStore.Files.FileColumns.DATE_ADDED
                )
            }
            if (cursor == null) {
                imageLoadListener.onFilesLoadFailed(NullPointerException())
                return
            }
            val files: MutableList<MediaFile> = ArrayList<MediaFile>()
            val dirMap: MutableMap<String, Dir> = HashMap<String, Dir>()
            var cntVideos = 0
            if (cursor.moveToLast()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                    val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                    val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                    val bucket = cursor.getString(cursor.getColumnIndex(projection[3]))
                    var videoWidth: String? = null
                    var videoHeight: String? = null
                    var videoDuration: String? = null
                    var imageWidth: String? = null
                    var imageHeight: String? = null
                    if (config.isShowPhotos && config.isShowVideos) {
                        videoWidth = cursor.getString(cursor.getColumnIndex(projection[4]))
                        videoHeight = cursor.getString(cursor.getColumnIndex(projection[5]))
                        videoDuration = cursor.getString(cursor.getColumnIndex(projection[6]))
                        imageWidth = cursor.getString(cursor.getColumnIndex(projection[7]))
                        imageHeight = cursor.getString(cursor.getColumnIndex(projection[8]))
                    } else if (!config.isShowPhotos && config.isShowVideos) {
                        videoWidth = cursor.getString(cursor.getColumnIndex(projection[4]))
                        videoHeight = cursor.getString(cursor.getColumnIndex(projection[5]))
                        videoDuration = cursor.getString(cursor.getColumnIndex(projection[6]))
                    } else {
                        imageWidth = cursor.getString(cursor.getColumnIndex(projection[4]))
                        imageHeight = cursor.getString(cursor.getColumnIndex(projection[5]))
                    }
                    val file = makeSafeFile(path) ?: continue
                    if (config.excludedFiles.contains(file.absolutePath)) {
                        continue
                    }
                    if (file.isFile) {

                        val mediaFile: MediaFile = if (PickerUtils.isVideoFormat(path)) {
                            cntVideos++
                            val contentUri: Uri = ContentUris.withAppendedId(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    id
                            )
                            MediaFile(id, name, path, videoInfo = VideoInfo(MediaSize(videoWidth, videoHeight), videoDuration), uri = contentUri)
                        } else {
                            val contentUri: Uri = ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                            )
                            MediaFile(id, name, path, mediaSize = MediaSize(imageWidth, imageHeight), uri = contentUri)
                        }
                        files += mediaFile
                        var folder: Dir? = dirMap[bucket]
                        if (folder == null) {
                            folder = Dir(bucket)
                            dirMap[bucket] = folder
                        }
                        folder.files += mediaFile
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
            if (cntVideos > 0) {
                executorService.execute(VideoThumbResolver(files))
            }

            /* Convert HashMap to ArrayList if not null */
            val dirs: List<Dir> = ArrayList<Dir>(dirMap.values)
            imageLoadListener.onFilesLoadedSuccess(files, dirs)
        }
    }

    private inner class VideoThumbResolver(files: List<MediaFile>) : Runnable {
        var files = files.filter { it.isVideo }

        override fun run() {
            for (item in files) {
                if (item.path == null || item.uri == null) {
                    continue
                }
                val thumbFile = PickerUtils.createVideoThumbFile(ctx, item) ?: return

                var bmp: Bitmap? = null
                try {
                    bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ctx.contentResolver.loadThumbnail(item.uri!!, android.util.Size(640, 640), null)
                    } else {
                        createVideoThumbnail(item.path!!) ?: continue
                    }

                    FileOutputStream(thumbFile).use { o ->
                        Timber.d("Write thumbnail for video %s: %s", item.path, thumbFile.absolutePath)
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, o)
                    }
                } catch (e: FileNotFoundException) {
                    Timber.e(e, "Unable to create or save thumbnail")
                } catch (e: IOException) {
                    Timber.e(e, "Unable to create or save thumbnail")
                } finally {
                    bmp?.recycle()
                }
            }
        }
    }

    companion object {
        private fun makeSafeFile(path: String?): File? {
            return if (path == null || path.isEmpty()) {
                null
            } else try {
                File(path)
            } catch (ignored: Exception) {
                null
            }
        }
    }
}