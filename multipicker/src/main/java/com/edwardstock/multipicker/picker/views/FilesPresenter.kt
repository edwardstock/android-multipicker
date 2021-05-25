package com.edwardstock.multipicker.picker.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.adapters.FilesAdapter
import timber.log.Timber
import java.io.File

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class FilesPresenter : BaseFsPresenter<FilesView>(), MediaFileLoader.OnLoadListener {
    private var mConfig: PickerConfig? = null
    private var mAdapter: FilesAdapter? = null
    private var mDir: Dir? = null
    private var mSelectionCnt = 0

    override fun updateFiles(loader: MediaFileLoader) {
        callOnView { obj: FilesView ->
            obj.showProgress()
            Timber.d("Updating files")
            loader.loadDeviceImages(mConfig!!, this)
        }

    }

    fun handleExtras(bundle: Bundle) {
        mDir = bundle.getParcelable(PickerConst.EXTRA_DIR)
        mConfig = bundle.getParcelable(PickerConst.EXTRA_CONFIG)
        mAdapter = FilesAdapter(mConfig!!, null) { file: MediaFile, isSelected: Boolean, sharedView: View ->
            onFileClick(file, isSelected, sharedView)
        }
    }

    override fun onFilesLoadFailed(t: Throwable?) {
        callOnView { v: FilesView ->
            v.hideProgress()
            v.onError(t)
        }
    }

    override fun onFilesLoadedSuccess(images: List<MediaFile>, dirList: List<Dir>) {
        Handler(Looper.getMainLooper()).post {
            callOnView { obj: FilesView -> obj.hideProgress() }
            var dirFiles: List<MediaFile>? = null
            for (d in dirList) {
                if (d.name == mDir?.name) {
                    dirFiles = d.files
                    break
                }
            }
            if (dirFiles == null || dirFiles.isEmpty()) {
                dirFiles = emptyList()
            }

            val toDelete = dirFiles
                    .filter { it.path != null }
                    .map { File(it.path!!) }
                    .filter { !it.exists() || it.length() == 0L }


            Thread {
                for (item in toDelete) {
                    callOnView { v: FilesView -> v.removeFileFromMediaDB(item) }
                }
            }.start()
            mAdapter!!.setData(dirFiles)
            if (dirFiles.isNotEmpty()) {
                mAdapter!!.notifyDataSetChanged()
            } else {
                callOnView { v: FilesView -> v.showEmpty() }
            }
        }
    }

    private var mScrollPosition = 0
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(FILE_SCROLL_POSITION, mScrollPosition)
    }

    override fun onRestoreSavedState(savedState: Bundle?) {
        super.onRestoreSavedState(savedState)
        if (savedState != null && savedState.containsKey(FILE_SCROLL_POSITION)) {
            mScrollPosition = savedState.getInt(FILE_SCROLL_POSITION)
        }
    }

    public override fun onViewAttach() {
        super.onViewAttach()
        if (mSelectionCnt == 0) {
            callOnView { v: FilesView ->
                v.setSelectionTitle(R.string.mp_choose_file)
                v.setSelectionSubmitEnabled(false)
            }
        }
        Timber.d("Set adapter")
        callOnView { v: FilesView ->
            v.setAdapter(mAdapter!!)
            v.scrollTo(mScrollPosition)
            v.setOnSelectionClearListener { v1: View? -> v.clearSelection() }
            v.setOnSelectionDoneListener { v1: View? -> v.finishWithResult() }
            Timber.d("Restore scroll: %d", mScrollPosition)
        }
    }

    fun setSelection(selection: List<MediaFile>) {
        if (mSelectionCnt == selection.size) {
            return
        }
        mSelectionCnt = selection.size
        if (selection.isEmpty()) {
            callOnView { v: FilesView ->
                v.setSelectionTitle(R.string.mp_choose_file)
                v.setSelectionSubmitEnabled(false)
            }
            return
        }

        val numPhotos: Long = selection.filter { !it.isVideo }.count().toLong()
        val numVideos: Long = selection.size - numPhotos
        if (numPhotos == 0L && numVideos == 0L) {
            callOnView { v: FilesView ->
                v.setSelectionTitle(R.string.mp_choose_file)
                v.setSelectionSubmitEnabled(false)
            }
            return
        }
        if (numPhotos > 0 && numVideos == 0L) {
            callOnView {
                it.setSelectionTitleN(R.plurals.mp_choosed_n_photos, numPhotos.toInt())
            }
        } else if (numPhotos == 0L && numVideos > 0) {
            callOnView {
                it.setSelectionTitleN(R.plurals.mp_choosed_n_videos, numVideos.toInt())
            }
        } else {
            callOnView {
                it.setSelectionTitlePhotosAndVideos(
                        R.plurals.mp_choosed_n_photos_and_videos_prefix, numPhotos.toInt(),
                        R.plurals.mp_choosed_n_photos_and_videos_suffix, numVideos.toInt()
                )
            }
        }
        callOnView { v: FilesView -> v.setSelectionSubmitEnabled(true) }
    }

    val config: PickerConfig?
        get() = mConfig

    fun setOnFileMeasuredListener(file: MediaFile?, listener: (file: MediaFile) -> Unit) {
        mAdapter!!.setOnFileMeasuredListener(file, listener)
    }

    fun setScroll(position: Int) {
        mScrollPosition = position
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        callOnView { v: FilesView ->
            v.setSelectionTitle(R.string.mp_choose_file)
            v.setSelectionSubmitEnabled(false)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onFileClick(file: MediaFile, isSelected: Boolean, sharedView: View) {
        callOnView { v: FilesView -> v.startPreview(file, sharedView) }
    }

    companion object {
        private const val FILE_SCROLL_POSITION = "files_scroll_position"
    }
}