package com.edwardstock.multipicker.picker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.databinding.MpFragmentFilesystemBinding
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.helpers.DisplayHelper
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.adapters.DirsAdapter
import com.edwardstock.multipicker.picker.getParcelableCompat
import com.edwardstock.multipicker.picker.views.BaseFsViewModel
import com.edwardstock.multipicker.picker.views.DirsView
import com.edwardstock.multipicker.picker.views.DirsViewModel
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class DirsFragment : PickerFileSystemFragment(), DirsView {
    private lateinit var config: PickerConfig
    private lateinit var b: MpFragmentFilesystemBinding

    private val viewModel: DirsViewModel by viewModels()
    private val adapter: DirsAdapter = DirsAdapter { dir: Dir -> startFiles(dir) }

    private val launchPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // for API >= 29 we just need at least READ_EXTERNAL_STORAGE
            permissions.entries.any { it.key == Manifest.permission.READ_EXTERNAL_STORAGE && it.value }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // we don't need extra permissions to just take own pictures and read them
            true
        } else {
            // for API < 29 we need all permissions
            permissions.entries.all { it.value }
        }
        if (granted) {
            viewModel.updateFiles(MediaFileLoader(requireContext()))
        } else {
            b.mpEmptyText.text = getString(R.string.mp_error_permission_denied)
            viewModel.onPermissionDenied()
        }
    }

    override fun startFiles(dir: Dir) {
        safeActivity { act: PickerActivity -> act.startFiles(dir) }
    }

    override fun showProgress() {
        b.progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        b.progress.visibility = View.GONE
    }

    override fun showEmpty(show: Boolean) {
        b.mpEmptyText.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onError(error: CharSequence?) {
        safeActivity { act: PickerActivity -> act.onError(error) }
    }

    override fun onError(t: Throwable?) {
        t?.let {
            safeActivity { act: PickerActivity -> act.onError(t) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        b = MpFragmentFilesystemBinding.inflate(inflater, container, false)
        b.selectionRoot.visibility = View.GONE
        config = requireArguments().getParcelableCompat(PickerConst.EXTRA_CONFIG)!!
        viewModel.handleExtras(requireArguments())

        Timber.d("DirsFragment onCreateView: $config")

        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.getBoolean(R.bool.mp_isTablet)
        var spanCount = if (isTablet) config.dirColumnsTablet else config.dirColumns
        if (activity != null) {
            val rot = DisplayHelper.getRotation(requireActivity())

            spanCount = if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
                (if (isTablet) config.dirColumnsTablet else config.dirColumns) + 1
            } else {
                if (isTablet) config.dirColumnsTablet else config.dirColumns
            }
        }
        val layoutManager = GridLayoutManager(activity, spanCount)
        setGridSpacingItemDecoration(b.list, spanCount)
        b.list.layoutManager = layoutManager
//        b.list.setHasFixedSize(true)
        b.list.adapter = adapter

        viewModel.dirs
                .onEach {
                    adapter.setData(it)
                    adapter.notifyItemRangeChanged(0, it.size)
                }
                .launchWhileVisible()

        viewModel.showEmpty
                .onEach { showEmpty(it) }
                .launchWhileVisible()

        viewModel.showProgress
                .onEach { if (it) showProgress() else hideProgress() }
                .launchWhileVisible()

        viewModel.showError
                .onEach { onError(it) }
                .launchWhileVisible()

        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            viewModel.updateFiles(MediaFileLoader(requireContext()))
            val granted = perms.all { checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
            if (!granted) {
                launchPermissionRequest.launch(perms)
            }
        } else {
            perms.all { checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }.also {
                if (it) {
                    viewModel.updateFiles(MediaFileLoader(requireContext()))
                } else {
                    launchPermissionRequest.launch(perms)
                }
            }
        }
    }

    override fun getViewModel(): BaseFsViewModel {
        return viewModel
    }

    companion object {
        fun newInstance(config: PickerConfig?): DirsFragment {
            val args = Bundle()
            args.putParcelable(PickerConst.EXTRA_CONFIG, config)
            val fragment = DirsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}