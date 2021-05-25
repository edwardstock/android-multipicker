package com.edwardstock.multipicker.picker.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.MediaFileLoader.OnLoadListener
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.adapters.DirsAdapter

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class DirsPresenter : BaseFsPresenter<DirsView>(), OnLoadListener {
    var config: PickerConfig? = null
        private set

    private val mAdapter: DirsAdapter = DirsAdapter { dir: Dir -> onDirClick(dir) }
    override fun updateFiles(loader: MediaFileLoader) {
        callOnView { obj: DirsView -> obj.showProgress() }
        loader.loadDeviceImages(config!!, this)
    }

    fun handleExtras(intent: Bundle) {
        config = intent.getParcelable(PickerConst.EXTRA_CONFIG)
    }

    override fun onFilesLoadFailed(t: Throwable?) {
        callOnView { v: DirsView ->
            v.hideProgress()
            v.onError(t)
        }
    }

    override fun onFilesLoadedSuccess(images: List<MediaFile>, dirList: List<Dir>) {
        Handler(Looper.getMainLooper()).post {
            callOnView { v: DirsView ->
                v.hideProgress()
                v.showEmpty(dirList.isEmpty())
            }
            mAdapter.setData(dirList)
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onViewAttach() {
        super.onViewAttach()
        callOnView { v: DirsView -> v.setAdapter(mAdapter) }
    }

    private fun onDirClick(dir: Dir) {
        callOnView { v: DirsView -> v.startFiles(dir) }
    }
}