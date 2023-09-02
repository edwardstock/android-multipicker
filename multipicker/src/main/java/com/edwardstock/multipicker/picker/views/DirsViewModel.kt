package com.edwardstock.multipicker.picker.views

import android.os.Bundle
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.MediaFileLoader.OnLoadListener
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.getParcelableCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class DirsViewModel : BaseFsViewModel(), OnLoadListener {
    var config: PickerConfig? = null
        private set


    private val _showProgress = MutableStateFlow(true)
    val showProgress = _showProgress.asStateFlow()

    private val _showError = MutableStateFlow<Throwable?>(null)
    val showError = _showError.asStateFlow()

    private val _showEmpty = MutableStateFlow(false)
    val showEmpty = _showEmpty.asStateFlow()

    private val _dirs = MutableStateFlow(emptyList<Dir>())
    val dirs = _dirs.asStateFlow()

    override fun updateFiles(loader: MediaFileLoader) {
        _showProgress.tryEmit(true)
        loader.loadDeviceImages(config!!, this)
    }

    fun handleExtras(intent: Bundle) {
        config = intent.getParcelableCompat(PickerConst.EXTRA_CONFIG)
    }

    override fun onFilesLoadFailed(t: Throwable?) {
        _showProgress.tryEmit(false)
        _showError.tryEmit(t)
    }

    override fun onFilesLoadedSuccess(images: List<MediaFile>, dirList: List<Dir>) {
        _showEmpty.tryEmit(dirList.isEmpty())
        _showProgress.tryEmit(false)
        _showError.tryEmit(null)
        _dirs.tryEmit(dirList)
    }

    fun onPermissionDenied() {
        _showProgress.tryEmit(false)
        _showEmpty.tryEmit(true)

    }


}