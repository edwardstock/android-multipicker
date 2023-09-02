package com.edwardstock.multipicker.picker.views

import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.recyclerview.selection.SelectionTracker
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.views.ErrorView
import java.io.File

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
interface FilesView : ErrorView, FileSystemView {
    fun setSelectionTitle(title: CharSequence)
    fun setSelectionTitle(@StringRes titleRes: Int)
    fun setSelectionTitleN(@PluralsRes titleRes: Int, n: Int)
    fun setSelectionTitlePhotosAndVideos(@PluralsRes prefix: Int, np: Int, @PluralsRes suffix: Int, ns: Int)
    fun setOnSelectionClearListener(listener: View.OnClickListener)
    fun setOnSelectionDoneListener(listener: View.OnClickListener)
    fun setSelectionObserver(observer: SelectionTracker.SelectionObserver<MediaFile>)
    fun startPreview(file: MediaFile, sharedView: View)
    fun clearSelection()
    fun finishWithResult()
    fun showProgress()
    fun hideProgress()
    fun setSelectionSubmitEnabled(enabled: Boolean)
    fun removeFileFromMediaDB(file: File)
}