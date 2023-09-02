package com.edwardstock.multipicker.picker.views

import android.os.Bundle
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.getParcelableCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */

data class SelectionTitle(
        val title: Int,
        val plural: Int = -1,
        val title2: Int = -1,
        val plural2: Int = -1
)

typealias SelectionTitleBoth = Pair<Pair<Int, Int>, Pair<Int, Int>>

class FilesViewModel : BaseFsViewModel(), MediaFileLoader.OnLoadListener {
    //    private var mConfig: PickerConfig? = null
//    private var mAdapter: FilesAdapter? = null
    private var mDir: Dir? = null
    private var mSelectionCnt = 0

    private var config: PickerConfig? = null


    private val _showProgress = MutableStateFlow(true)
    val showProgress = _showProgress.asStateFlow()

    private val _showError = MutableStateFlow<Throwable?>(null)
    val showError = _showError.asStateFlow()

    private val _showEmpty = MutableStateFlow(false)
    val showEmpty = _showEmpty.asStateFlow()

    private val _filesToDelete = MutableSharedFlow<List<File>>(replay = 0)
    val filesToDelete = _filesToDelete.asSharedFlow()

    private val _files = MutableStateFlow<List<MediaFile>>(emptyList())
    val files = _files.asStateFlow()

    override fun updateFiles(loader: MediaFileLoader) {
        _showProgress.tryEmit(true)
        Timber.d("Updating files")
        loader.loadDeviceImages(config!!, this)

    }

    fun handleExtras(bundle: Bundle) {
        mDir = bundle.getParcelableCompat(PickerConst.EXTRA_DIR)
        config = bundle.getParcelableCompat(PickerConst.EXTRA_CONFIG)
    }

    override fun onFilesLoadFailed(t: Throwable?) {
        _showProgress.tryEmit(false)
        _showError.tryEmit(t)
    }

    override fun onFilesLoadedSuccess(images: List<MediaFile>, dirList: List<Dir>) {
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
                .filter { it.uri != null }
                .map { File(it.uri) }
                .filter { !it.exists() || it.length() == 0L }


        _filesToDelete.tryEmit(toDelete)
        _showProgress.tryEmit(false)
        if (dirFiles.isEmpty()) {
            _showEmpty.tryEmit(true)
            _files.tryEmit(emptyList())
        } else {
            _showEmpty.tryEmit(false)
            _files.tryEmit(dirFiles)
        }
    }

    private var mScrollPosition = 0
    fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(FILE_SCROLL_POSITION, mScrollPosition)
    }

    fun onRestoreSavedState(savedState: Bundle?) {
        if (savedState != null && savedState.containsKey(FILE_SCROLL_POSITION)) {
            mScrollPosition = savedState.getInt(FILE_SCROLL_POSITION)
        }
    }

    private val _selectionTitle = MutableSharedFlow<SelectionTitle>(replay = 1)
    val selectionTitle = _selectionTitle.asSharedFlow()
//    private val _selectionTitleN = MutableSharedFlow<Pair<Int, Int>>(replay = 1)
//    val selectionTitleN = _selectionTitleN.asSharedFlow()
//    private val _selectionTitleBoth = MutableSharedFlow<SelectionTitleBoth>(replay = 1)
//    val selectionTitleBoth = _selectionTitleBoth.asSharedFlow()

    private val _selectionSubmitEnabled = MutableStateFlow(false)
    val selectionSubmitEnabled = _selectionSubmitEnabled.asStateFlow()
    private val _scrollTo = MutableStateFlow(0)
    val scrollTo = _scrollTo.asStateFlow()


    init {
        if (mSelectionCnt == 0) {
            _selectionTitle.tryEmit(SelectionTitle(R.string.mp_choose_file))
            _selectionSubmitEnabled.tryEmit(false)
        }

        _scrollTo.tryEmit(mScrollPosition)
        Timber.d("Restore scroll: %d", mScrollPosition)
    }

    fun setSelection(selection: List<MediaFile>) {
        if (mSelectionCnt == selection.size) {
            return
        }
        mSelectionCnt = selection.size
        if (selection.isEmpty()) {
            _selectionTitle.tryEmit(SelectionTitle(R.string.mp_choose_file))
            _selectionSubmitEnabled.tryEmit(false)
            return
        }

        val numPhotos: Long = selection.count { !it.isVideo }.toLong()
        val numVideos: Long = selection.size - numPhotos
        if (numPhotos == 0L && numVideos == 0L) {
            _selectionTitle.tryEmit(SelectionTitle(R.string.mp_choose_file))
            _selectionSubmitEnabled.tryEmit(false)
            return
        }
        if (numPhotos > 0 && numVideos == 0L) {
            _selectionTitle.tryEmit(SelectionTitle(R.plurals.mp_choosed_n_photos, numPhotos.toInt()))
        } else if (numPhotos == 0L && numVideos > 0) {
            _selectionTitle.tryEmit(SelectionTitle(R.plurals.mp_choosed_n_videos, numVideos.toInt()))
        } else {
            _selectionTitle.tryEmit(SelectionTitle(
                    R.plurals.mp_choosed_n_photos, numPhotos.toInt(),
                    R.plurals.mp_choosed_n_photos_and_videos_suffix, numVideos.toInt()
            ))
        }
        _selectionSubmitEnabled.tryEmit(true)
    }

//    fun setOnFileMeasuredListener(file: MediaFile?, listener: (file: MediaFile) -> Unit) {
//        mAdapter!!.setOnFileMeasuredListener(file, listener)
//    }

    fun setScroll(position: Int) {
        mScrollPosition = position
    }


    companion object {
        private const val FILE_SCROLL_POSITION = "files_scroll_position"
    }
}