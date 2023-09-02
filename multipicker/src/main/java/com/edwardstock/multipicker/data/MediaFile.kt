package com.edwardstock.multipicker.data

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.edwardstock.multipicker.internal.PickerUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.Locale

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 *
 * @property id MediaStore database ID
 * @property name File name with extension
 * @property path Absoulte file path
 * @property uri Content `Uri`
 * @property videoInfo Detailed info about video
 * @property mediaSize Media file witdth and height
 */
@Parcelize
data class MediaFile(
        var id: Long,
        var name: String,
        @Deprecated("Use Uri and MediaStore API instead")
        var path: String,
        var uri: Uri,
        var videoInfo: VideoInfo? = null,
        var mediaSize: MediaSize? = null,

        ) : Parcelable {

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%s{id=%d, name=%s, uri=%s, path=%s}", javaClass.simpleName, id, name, uri, uri)
    }

    val isVideo: Boolean
        get() = PickerUtils.isVideoFormat(this)

    val size: Long
        get() {
            return File(path).length()
        }

    @Deprecated(message = "Use size instead", ReplaceWith("size"))
    @IgnoredOnParcel
    val length: Long = size

    @Deprecated("Use Uri instead")
    val file: File
        get() {
            return File(path)
        }

    @Deprecated(message = "Use Uri instead", ReplaceWith("uri"))
    val pathFS: Path?
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            return FileSystems.getDefault().getPath(path)
        }

    @Deprecated(message = "Use Uri instead", ReplaceWith("uri"))
    val uriFile: Uri?
        get() {
            return Uri.fromFile(file)
        }

    @Deprecated(message = "Use Uri instead", ReplaceWith("uri"))
    val exists: Boolean
        get() {
            return file.exists()
        }

    @Deprecated(message = "Use Uri instead", ReplaceWith("uri"))
    fun delete(): Boolean {
        return file.delete()
    }
}