package com.edwardstock.multipicker.picker.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.PluralsRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.databinding.MpFragmentFilesystemBinding
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.PickerSavePath
import com.edwardstock.multipicker.internal.helpers.DisplayHelper
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.adapters.FilesAdapter
import com.edwardstock.multipicker.picker.adapters.MediaFileDetailsLookup
import com.edwardstock.multipicker.picker.adapters.MediaFileKeyProvider
import com.edwardstock.multipicker.picker.getParcelableCompat
import com.edwardstock.multipicker.picker.views.BaseFsViewModel
import com.edwardstock.multipicker.picker.views.FilesView
import com.edwardstock.multipicker.picker.views.FilesViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class FilesFragment : PickerFileSystemFragment(), FilesView {
    private val viewModel: FilesViewModel by viewModels()

    private lateinit var b: MpFragmentFilesystemBinding
    private var selectionTracker: SelectionTracker<MediaFile>? = null
    private var lastSelectionState = false
    private var choosenDir: Dir? = null
    private lateinit var config: PickerConfig
    private val selectedFiles: MutableList<MediaFile> = ArrayList<MediaFile>(10)
    private var lastPreview: MediaFile? = null
    private var listState: Parcelable? = null


    private lateinit var adapter: FilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onRestoreSavedState(savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        if (b.list.layoutManager != null) {
            listState = b.list.layoutManager!!.onSaveInstanceState()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (selectionTracker != null) {
            selectionTracker!!.onSaveInstanceState(outState)
        }
        viewModel.onSaveInstanceState(outState)
        if (b.list.layoutManager != null) {
            listState = b.list.layoutManager!!.onSaveInstanceState()
            outState.putParcelable(LIST_STATE, listState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        if (config.limit > 0) {
            safeActivity { act: PickerActivity -> act.b.toolbar.subtitle = null }
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        b.list.layoutManager!!.onRestoreInstanceState(listState)
        if (config.limit > 0) {
            setSubtitle()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        b = MpFragmentFilesystemBinding.inflate(inflater, container, false)

        choosenDir = requireArguments().getParcelableCompat(PickerConst.EXTRA_DIR)
        config = requireArguments().getParcelableCompat(PickerConst.EXTRA_CONFIG)!!

        viewModel.handleExtras(requireArguments())
        if (selectionTracker != null) {
            selectionTracker!!.onRestoreInstanceState(savedInstanceState)
        }
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FilesAdapter(config, null) { file: MediaFile, _: Boolean, sharedView: View ->
//            onFileClick(file, isSelected, sharedView)
            startPreview(file, sharedView)
        }
        initAdapter(adapter)


        viewModel.filesToDelete
                .onEach {
                    it.forEach { file -> removeFileFromMediaDB(file) }
                }
                .launchIn(lifecycleScope)

        viewModel.files
                .onEach {
                    adapter.setData(it)
                    adapter.notifyItemRangeChanged(0, it.size)
                }
                .launchWhileVisible()

        viewModel.showProgress
                .onEach { if (it) showProgress() else hideProgress() }
                .launchWhileVisible()

//        viewModel.showEmpty
//                .onEach { if(it) showEmpty() else hideEmpty() }
//                .launchWhileVisible()

        viewModel.showError
                .onEach {
                    Timber.e(it)
                }
                .launchWhileVisible()

        viewModel.scrollTo
                .onEach {
                    b.list.layoutManager?.scrollToPosition(it)
                }
                .launchWhileVisible()

        viewModel.selectionSubmitEnabled
                .onEach { setSelectionSubmitEnabled(it) }
                .launchWhileVisible()

        viewModel.selectionTitle
                .onEach {
                    when {
                        it.plural == -1 -> setSelectionTitle(it.title)
                        it.plural > -1 -> setSelectionTitleN(it.title, it.plural)
                        it.title2 != -1 && it.plural2 != -1 -> this.setSelectionTitlePhotosAndVideos(it.title, it.plural, it.title2, it.plural2)
                    }
                }
                .launchWhileVisible()

        viewModel.updateFiles(MediaFileLoader(requireContext()))

        setOnSelectionClearListener { clearSelection() }
        setOnSelectionDoneListener { finishWithResult() }
    }

    override fun getViewModel(): BaseFsViewModel {
        return viewModel
    }

    fun initAdapter(adapter: FilesAdapter) {
        Timber.d("Set adapter from fragment")
        val isTablet = resources.getBoolean(R.bool.mp_isTablet)
        var spanCount: Int = if (isTablet) config.fileColumnsTablet else config.fileColumns
        if (activity != null) {
            val rot = DisplayHelper.getRotation(requireActivity())
            spanCount = if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
                (if (isTablet) config.fileColumnsTablet else config.fileColumns) + 1
            } else {
                if (isTablet) config.fileColumnsTablet else config.fileColumns
            }
        }
        val layoutManager = GridLayoutManager(context, spanCount)
        setGridSpacingItemDecoration(b.list, spanCount)
        b.list.layoutManager = layoutManager
        b.list.adapter = adapter
        b.list.itemAnimator = null
        layoutManager.onRestoreInstanceState(listState)
        b.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                adapter.setIsScrolling(newState != RecyclerView.SCROLL_STATE_IDLE)
            }
        })
        val filesAdapter: FilesAdapter = adapter
        if (selectionTracker != null) {
            Timber.d("Reusing tracker")
            filesAdapter.setSelectionTracker(selectionTracker)
            return
        }
        Timber.d("Create tracker")
        selectionTracker = SelectionTracker.Builder(
                "picker",
                b.list,
                MediaFileKeyProvider({
                    if (b.list.adapter == null) {
                        emptyList()
                    } else {
                        (b.list.adapter as FilesAdapter).items
                    }
                }, ItemKeyProvider.SCOPE_MAPPED),
                MediaFileDetailsLookup(b.list),
                StorageStrategy.createParcelableStorage(MediaFile::class.java)
        )
                .withSelectionPredicate(object : SelectionTracker.SelectionPredicate<MediaFile>() {
                    override fun canSetStateForKey(mediaFile: MediaFile, nextState: Boolean): Boolean {
                        return config.limit <= 0 || selectedFiles.size != config.limit || selectedFiles.contains(mediaFile)
                    }

                    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
                        return true
                    }

                    override fun canSelectMultiple(): Boolean {
                        return true
                    }
                })
                .build()

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<MediaFile?>() {
            override fun onItemStateChanged(key: MediaFile, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                viewModel.setSelection(selectionTracker!!.selection.toList())
                selectedFiles.clear()
                selectedFiles.addAll(selectionTracker!!.selection.toList())
                setSubtitle()
                val hasSelection: Boolean = selectionTracker!!.hasSelection()
                if (lastSelectionState != hasSelection) {
                    lastSelectionState = hasSelection
                    filesAdapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
            }

            override fun onSelectionRestored() {
                super.onSelectionRestored()
                val hasSelection: Boolean = selectionTracker!!.hasSelection()
                viewModel.setSelection(selectionTracker!!.selection.toList())
                if (lastSelectionState != hasSelection) {
                    lastSelectionState = hasSelection
                    filesAdapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
            }
        })
        filesAdapter.setSelectionTracker(selectionTracker)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("Delete selection tracker")
        selectionTracker = null
    }

    override fun setSelectionTitle(title: CharSequence) {
        b.mpSelectionTitle.text = title
    }

    override fun setSelectionTitle(titleRes: Int) {
        b.mpSelectionTitle.setText(titleRes)
    }

    override fun setSelectionTitleN(@PluralsRes titleRes: Int, n: Int) {
        b.mpSelectionTitle.text = requireContext().resources.getQuantityString(titleRes, n, n)
    }

    @SuppressLint("SetTextI18n")
    override fun setSelectionTitlePhotosAndVideos(@PluralsRes prefix: Int, np: Int, @PluralsRes suffix: Int, ns: Int) {
        val res = requireContext().resources
        b.mpSelectionTitle.text = "${res.getQuantityString(prefix, np, np)}${res.getQuantityString(suffix, ns, ns)}"
    }

    override fun setOnSelectionClearListener(listener: View.OnClickListener) {
        b.mpSelectionActionClear.setOnClickListener(listener)
    }

    override fun setOnSelectionDoneListener(listener: View.OnClickListener) {
        b.mpSelectionActionDone.setOnClickListener(listener)
    }

    override fun setSelectionObserver(observer: SelectionTracker.SelectionObserver<MediaFile>) {
        selectionTracker!!.addObserver(observer)
    }

    override fun startPreview(file: MediaFile, sharedView: View) {
        lastPreview = file
        safeActivity { act: PickerActivity -> act.startPreview(choosenDir, file, sharedView) }
    }

    override fun clearSelection() {
        if (selectionTracker != null) {
            selectionTracker!!.clearSelection()
            lastSelectionState = false
        }
    }

    override fun finishWithResult() {
        safeActivity { act: PickerActivity ->
            act.submitResult(selectionTracker?.selection?.toList() ?: emptyList())
        }
    }

    override fun showProgress() {
        b.progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        b.progress.visibility = View.GONE
    }

    override fun setSelectionSubmitEnabled(enabled: Boolean) {
        safeActivity { act: PickerActivity ->
            act.runOnUiThread {
                Timber.d("Set selection submit: %b", enabled)
                val set = ConstraintSet()
                set.clone(b.selectionRootSub)
                set.setAlpha(R.id.mp_selection_action_clear, if (enabled) 1f else 0f)
                set.setAlpha(R.id.mp_selection_action_done, if (enabled) 1f else 0f)
                set.setMargin(R.id.mp_selection_title, ConstraintSet.START, if (enabled) DisplayHelper.dpToPx(requireContext(), 48) else DisplayHelper.dpToPx(requireContext(), 16))
                val transitionSet = TransitionSet()
                val shortTransition: Transition = AutoTransition()
                shortTransition.interpolator = DecelerateInterpolator()
                //                shortTransition.setDuration(300);
                val longTransition: Transition = AutoTransition()
                longTransition.interpolator = AccelerateDecelerateInterpolator()
                //                longTransition.setDuration(600);
                transitionSet.addTransition(shortTransition)
                transitionSet.addTransition(longTransition)
                transitionSet.ordering = TransitionSet.ORDERING_TOGETHER
                if (enabled) {
                    longTransition.addTarget(R.id.mp_selection_action_clear)
                    longTransition.addTarget(R.id.mp_selection_action_done)
                    shortTransition.addTarget(R.id.mp_selection_title)
                } else {
                    longTransition.addTarget(R.id.mp_selection_title)
                    shortTransition.addTarget(R.id.mp_selection_action_clear)
                    shortTransition.addTarget(R.id.mp_selection_action_done)
                }
                TransitionManager.beginDelayedTransition(b.selectionRootSub, transitionSet)
                set.applyTo(b.selectionRootSub)
            }
        }
    }

    override fun removeFileFromMediaDB(file: File) {
        safeActivity { act: PickerActivity ->
            Timber.d("Remove file: %s as it doesn't exists", file.absoluteFile)
            try {
                act.contentResolver.delete(Uri.fromFile(file), null, null)
                file.delete()
            } catch (ignore: Throwable) {
            }
        }
    }

    fun addSelection(selection: MediaFile?) {
        if (selection != null && selectionTracker != null) {
            selectionTracker!!.select(selection)
        }
    }

    override fun onError(error: CharSequence?) {
        safeActivity { act: PickerActivity -> act.onError(error) }
    }

    override fun onError(t: Throwable?) {
        safeActivity { act: PickerActivity -> act.onError(t) }
    }

    override val capturedSavePath: PickerSavePath?
        get() = if (choosenDir != null) {
            PickerSavePath(choosenDir!!.name!!)
        } else {
            super.capturedSavePath
        }

    private fun setSubtitle() {
        if (config.limit > 0) {
            safeActivity { act: PickerActivity -> act.b.toolbar.subtitle = String.format(Locale.getDefault(), "%d / %d", selectionTracker!!.selection.size(), config.limit) }
        }
    }

    companion object {
        private const val LIST_STATE = "LIST_STATE"
        fun newInstance(config: PickerConfig?, dir: Dir?): FilesFragment {
            val args = Bundle()
            args.putParcelable(PickerConst.EXTRA_DIR, dir)
            args.putParcelable(PickerConst.EXTRA_CONFIG, config)
            val fragment = FilesFragment()
            fragment.arguments = args
            return fragment
        }
    }
}