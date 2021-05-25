package com.edwardstock.multipicker.picker.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.databinding.MpFragmentFilesystemBinding
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.helpers.DisplayHelper
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.views.BaseFsPresenter
import com.edwardstock.multipicker.picker.views.DirsPresenter
import com.edwardstock.multipicker.picker.views.DirsView
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RuntimePermissions
class DirsFragment : PickerFileSystemFragment(), DirsView {
    lateinit var presenter: DirsPresenter
    private lateinit var mConfig: PickerConfig
    private lateinit var b: MpFragmentFilesystemBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        presenter = DirsPresenter()
        presenter.attachToLifecycle(this)
        presenter.attachView(this)
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        val isTablet = resources.getBoolean(R.bool.mp_isTablet)
        var spanCount = if (isTablet) mConfig.dirColumnsTablet else mConfig.dirColumns
        if (activity != null) {
            val rot = DisplayHelper.getRotation(requireActivity())

            spanCount = if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
                (if (isTablet) mConfig.dirColumnsTablet else mConfig.dirColumns) + 1
            } else {
                if (isTablet) mConfig.dirColumnsTablet else mConfig.dirColumns
            }
        }
        val layoutManager = GridLayoutManager(activity, spanCount)
        setGridSpacingItemDecoration(b.list, spanCount)
        b.list.layoutManager = layoutManager
        b.list.setHasFixedSize(true)
        b.list.adapter = adapter
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onError(error: CharSequence?) {
        safeActivity { act: PickerActivity -> act.onError(error) }
    }

    override fun onError(t: Throwable?) {
        safeActivity { act: PickerActivity -> act.onError(t) }
    }

    override fun startUpdateFiles() {
        presenter.updateFiles(MediaFileLoader(requireActivity()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        b = MpFragmentFilesystemBinding.inflate(inflater, container, false)
        b.selectionRoot.visibility = View.GONE
        mConfig = requireArguments().getParcelable(PickerConst.EXTRA_CONFIG)!!
        presenter.handleExtras(requireArguments())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setLoaderWithPermissionCheck()
        } else {
            setLoaderPreQWithPermissionCheck()
        }

        return b.root
    }

    override fun getPresenter(): BaseFsPresenter<*> {
        return presenter
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun setLoaderPreQ() {
        presenter.updateFiles(MediaFileLoader(requireActivity()))
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun setLoader() {
        presenter.updateFiles(MediaFileLoader(requireActivity()))
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