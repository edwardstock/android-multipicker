package com.edwardstock.multipicker

import android.os.Parcelable
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.PickerSavePath
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@Parcelize
class PickerConfig(
        var isShowPhotos: Boolean = true,
        var isShowVideos: Boolean = false,
        var isEnableCamera: Boolean = true,
        var videoSavePath: PickerSavePath = PickerSavePath.DEFAULT,
        var photoSavePath: PickerSavePath = PickerSavePath.DEFAULT,
        var dirColumns: Int = 2,
        var dirColumnsTablet: Int = 3,
        var fileColumns: Int = 3,
        var fileColumnsTablet: Int = 5,
        var isEnableSelectionAnimation: Boolean = true,
        /**
         * Toolbar title
         */
        var title: String? = null,
        /**
         * Selection limit. By default 0=infinite selection
         */
        var limit: Int = 0,
        var excludedFiles: MutableList<String> = ArrayList(0),
        /**
         * Copy unreadable files to external cache directory to have ability to read them outside
         */
        @Deprecated("Use Uri and MediaStore API instead of File. See `MediaFile.uri`")
        var copyUnreadableFilesToCache: Boolean = true
) : Parcelable {

    fun excludeFile(file: MediaFile?): PickerConfig {
        if (file?.uri == null) return this

        excludedFiles.add(file.uri)
        return this
    }

    fun excludeFile(absPath: String?): PickerConfig {
        if (absPath == null) return this
        if (absPath.startsWith("file://")) {
            excludedFiles.add(absPath.replace("file://", ""))
        }
        excludedFiles.add(absPath)
        return this
    }

    fun excludeFile(file: File): PickerConfig {
        return excludeFile(file.absolutePath)
    }

    fun excludeFilesStrings(filePaths: Collection<String>?): PickerConfig {
        if (filePaths == null) {
            return this
        }
        excludedFiles.addAll(filePaths)
        return this
    }

    fun excludeFiles(files: List<MediaFile?>): PickerConfig {
        excludedFiles.addAll(
                files.filterNotNull().filter { it.uri != null }.map { it.uri }
        )
        return this
    }

    fun limit(limit: Int): PickerConfig {
        this.limit = limit
        return this
    }

    fun title(title: String?): PickerConfig {
        this.title = title
        return this
    }

    fun enableSelectionAnimation(enableSelectionAnimation: Boolean): PickerConfig {
        isEnableSelectionAnimation = enableSelectionAnimation
        return this
    }

    fun dirColumns(dirColumns: Int): PickerConfig {
        this.dirColumns = dirColumns
        return this
    }

    fun dirColumnsTablet(dirColumnsTablet: Int): PickerConfig {
        this.dirColumnsTablet = dirColumnsTablet
        return this
    }

    fun fileColumns(fileColumns: Int): PickerConfig {
        this.fileColumns = fileColumns
        return this
    }

    fun fileColumnsTablet(fileColumnsTablet: Int): PickerConfig {
        this.fileColumnsTablet = fileColumnsTablet
        return this
    }

    fun setVideoSavePath(pathName: String): PickerConfig {
        videoSavePath = PickerSavePath(pathName, false)
        return this
    }

    fun setPhotoSavePath(pathName: String): PickerConfig {
        photoSavePath = PickerSavePath(pathName, false)
        return this
    }

    fun enableCamera(capturePhoto: Boolean): PickerConfig {
        isEnableCamera = capturePhoto
        return this
    }

    fun showPhotos(showPhotos: Boolean): PickerConfig {
        isShowPhotos = showPhotos
        return this
    }

    fun showVideos(showVideos: Boolean): PickerConfig {
        isShowVideos = showVideos
        return this
    }
}