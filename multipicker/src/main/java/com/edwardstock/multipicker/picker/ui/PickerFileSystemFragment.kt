package com.edwardstock.multipicker.picker.ui

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.PickerSavePath
import com.edwardstock.multipicker.internal.helpers.DisplayHelper.dpToPx
import com.edwardstock.multipicker.internal.widgets.GridSpacingItemDecoration
import com.edwardstock.multipicker.picker.views.BaseFsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import timber.log.Timber

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
abstract class PickerFileSystemFragment : Fragment() {
    private var gridSpacingItemDecoration: GridSpacingItemDecoration? = null
    private val runningJobs = ArrayList<Job>()

    protected fun addOnBackCallback(callback: () -> Unit) {
        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        callback()
                    }
                })
    }

    protected fun <T> Flow<T>.launchWhileVisible(): Job {
        val job = launchIn(lifecycleScope)
        runningJobs.add(job)
        return job
    }

    fun updateFiles() {
        Timber.d("Update files")
        getViewModel().updateFiles(MediaFileLoader(requireActivity()))
    }

    override fun onResume() {
        super.onResume()
        updateFiles()
        Timber.d("${this::class} On resume fragment")
    }

    open fun onBackPressed() {
        Timber.d("On back pressed fragment")
    }

    protected fun safeActivity(callback: (PickerActivity) -> Unit) {
        activity?.let { callback(it as PickerActivity) }
    }

    protected fun setGridSpacingItemDecoration(list: RecyclerView, spanCount: Int) {
        Timber.d("Set grid spacing")
        if (gridSpacingItemDecoration != null) {
            list.removeItemDecoration(gridSpacingItemDecoration!!)
        }
        gridSpacingItemDecoration = GridSpacingItemDecoration(spanCount, dpToPx(requireContext(), 1), false)
        list.addItemDecoration(gridSpacingItemDecoration!!)
    }

    open val capturedSavePath: PickerSavePath?
        get() = null

    protected abstract fun getViewModel(): BaseFsViewModel
}