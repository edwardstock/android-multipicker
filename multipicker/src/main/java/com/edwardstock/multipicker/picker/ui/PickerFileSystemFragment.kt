package com.edwardstock.multipicker.picker.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.PickerSavePath
import com.edwardstock.multipicker.internal.helpers.DisplayHelper.dpToPx
import com.edwardstock.multipicker.internal.widgets.GridSpacingItemDecoration
import com.edwardstock.multipicker.picker.views.BaseFsPresenter
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
abstract class PickerFileSystemFragment : Fragment() {
    private var mGridSpacingItemDecoration: GridSpacingItemDecoration? = null
    private var mActivity: WeakReference<PickerActivity?>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = WeakReference(activity as PickerActivity?)
    }

    override fun onStart() {
        super.onStart()
        mActivity = WeakReference(activity as PickerActivity?)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("Destroy fragment view")
        if (mActivity != null) {
            mActivity!!.clear()
            mActivity = null
        }
    }

    fun updateFiles() {
        getPresenter().updateFiles(MediaFileLoader(requireActivity()))
    }

    open fun onBackPressed() {
        Timber.d("On back pressed fragment")
    }

    protected fun hasActivity(): Boolean {
        if (mActivity == null || mActivity!!.get() == null) {
            return false
        }
        val activity = mActivity!!.get()
        return activity != null
    }

    protected fun safeActivity(callback: (PickerActivity) -> Unit) {
        if (mActivity == null || mActivity!!.get() == null) {
            Timber.d("Activity already unavailable")
            return
        }
        val activity = mActivity!!.get()
        if (activity != null) {
            callback(activity)
        }
    }

    protected fun setGridSpacingItemDecoration(list: RecyclerView, spanCount: Int) {
        Timber.d("Set grid spacing")
        if (mGridSpacingItemDecoration != null) {
            list.removeItemDecoration(mGridSpacingItemDecoration!!)
        }
        mGridSpacingItemDecoration = GridSpacingItemDecoration(spanCount, dpToPx(requireContext(), 1), false)
        list.addItemDecoration(mGridSpacingItemDecoration!!)
    }

    open val capturedSavePath: PickerSavePath?
        get() = null

    protected abstract fun getPresenter(): BaseFsPresenter<*>
}