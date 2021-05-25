package com.edwardstock.multipicker.data

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.edwardstock.multipicker.internal.PickerUtils
import kotlinx.parcelize.Parcelize
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.*

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@Parcelize
class MediaFile(
        var id: Long = 0,
        var name: String? = null,
        // Android API 29+ restricts to direct access to files with absolute path, so use `uri` instead
        var path: String? = null,
        var videoInfo: VideoInfo? = null,
        var mediaSize: MediaSize? = null,
        // in android 29+ use Uri instead of absolute file path as it's not readable without special permission
        var uri: Uri? = null

) : Parcelable {

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%s{id=%d, name=%s, uri=%s, path=%s}", javaClass.simpleName, id, name, uri, path)
    }

    val isVideo: Boolean
        get() = PickerUtils.isVideoFormat(this)

    val size: Long
        get() {
            if (path == null) return 0
            return File(path!!).length()
        }

    @Deprecated(message = "Use size instead", ReplaceWith("size"))
    val length: Long = size

    val file: File?
        get() {
            if (path == null) {
                return null
            }
            return File(path!!)
        }

    val pathFS: Path?
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            if (path == null) return null
            return FileSystems.getDefault().getPath(path)
        }

    val uriFile: Uri?
        get() {
            if (file == null) {
                return null
            }
            return Uri.fromFile(file)
        }

    val exists: Boolean
        get() {
            if (file == null) return false
            return file!!.exists()
        }

    fun delete(): Boolean {
        if (file != null) {
            return file!!.delete()
        }
        return false
    }
}