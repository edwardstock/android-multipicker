package com.edwardstock.multipicker.picker.ui

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.databinding.MpFragmentImageViewerBinding
import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.helpers.DisplayHelper.getHeight
import com.edwardstock.multipicker.internal.helpers.DisplayHelper.getWidth
import com.edwardstock.multipicker.internal.helpers.ExceptionHelper.doubleTryOOM
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.getParcelableCompat
import com.edwardstock.multipicker.picker.views.BaseFsViewModel
import timber.log.Timber

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class ImageViewerFragment : PickerFileSystemFragment() {
    var mHiddenControls = false
    lateinit var b: MpFragmentImageViewerBinding
    var dir: Dir? = null
        private set

    private val mSendMenu: MenuItem? = null
    private var mFile: MediaFile? = null
    private val mFirstAnim = true
    val addedFile: MediaFile? = null

    @SuppressLint("ResourceType")
    override fun onStop() {
        super.onStop()
        safeActivity { act: PickerActivity ->
            Timber.d("Revert activity colors")
            val typedValue = TypedValue()
            val a = act.obtainStyledAttributes(typedValue.data, intArrayOf(
                    R.attr.colorPrimary,
                    R.attr.colorPrimaryDark,
                    R.attr.titleTextColor
            ))

            @ColorInt val primColor = a.getColor(0, ResourcesCompat.getColor(resources, R.color.mp_colorPrimary, requireActivity().theme))
            @ColorInt val darkColor = a.getColor(1, ResourcesCompat.getColor(resources, R.color.mp_colorPrimaryDark, requireActivity().theme))
            @ColorInt val textColor = a.getColor(2, ResourcesCompat.getColor(resources, R.color.mp_textColorSecondary, requireActivity().theme))
            a.recycle()
            act.b.toolbar.setTitleTextColor(textColor)
            act.b.toolbar.setSubtitleTextColor(textColor)
            act.b.toolbar.setBackgroundColor(primColor)
            act.window.statusBarColor = darkColor
            val menu = act.b.toolbar.menu
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                item.isVisible = true
            }

            act.window.setBackgroundDrawable(ColorDrawable(
                    ResourcesCompat.getColor(resources, R.color.mp_colorBackground, requireActivity().theme)
            ))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        b = MpFragmentImageViewerBinding.inflate(inflater, container, false)
        postponeEnterTransition()
        b.mpPhotoProgress.visibility = View.VISIBLE
        mFile = requireArguments().getParcelableCompat(PickerConst.EXTRA_MEDIA_FILE)
        dir = requireArguments().getParcelableCompat(PickerConst.EXTRA_DIR)
        safeActivity { act: PickerActivity ->
            Timber.d("Set toolbar color")
            act.b.toolbar.setTitleTextColor(0x00FFFFFF)
            act.b.toolbar.setSubtitleTextColor(0x00FFFFFF)
            act.b.toolbar.setBackgroundColor(0x00FFFFFF)
            act.window.setBackgroundDrawable(ColorDrawable(-0x1000000))
            act.window.statusBarColor = -0x1000000
            act.window.decorView.setBackgroundColor(-0x1000000)
            act.window.setBackgroundDrawable(ColorDrawable(-0x1000000))
            val menu = act.b.toolbar.menu
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                item.isVisible = false
            }
            b.layoutActions.mpSelectionActionSend.setOnClickListener {
                safeActivity { act1: PickerActivity ->
                    act1.submitResult(if (mFile == null) emptyList() else listOf(mFile!!))
                }
            }
        }
        val width = getWidth(requireContext())
        val height = getHeight(requireContext())
        doubleTryOOM({
            Glide.with(this)
                    .load(mFile!!.uri)
                    .apply(RequestOptions()
                            .override(width, height)
                            .centerInside()
                            .encodeQuality(80)
                    )
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                            Timber.e(e)
                            b.mpPhotoProgress.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            b.mpPhotoProgress.visibility = View.GONE
                            return false
                        }
                    })
                    .into(b.mpPhotoView)
        }, { t: OutOfMemoryError ->
            b.mpPhotoView.setImageResource(R.drawable.mp_bg_black_error)
            b.mpPhotoProgress.visibility = View.GONE
            Timber.d(t, "Unable to preview image (OOM)")
        }, "mp_preview_load")
        b.mpPhotoView.transitionName = getString(R.string.mp_transition_image) + mFile!!.id.toString()
        startPostponedEnterTransition()
        return b.root
    }


    @SuppressLint("StaticFieldLeak")
    override fun getViewModel(): BaseFsViewModel {
        // dummy, arch needs to be re-worked
        return object : BaseFsViewModel() {
            override fun updateFiles(loader: MediaFileLoader) {
            }
        }
    }

    companion object {
        fun newInstance(dir: Dir?, file: MediaFile?): ImageViewerFragment {
            val args = Bundle()
            args.putParcelable(PickerConst.EXTRA_MEDIA_FILE, file)
            args.putParcelable(PickerConst.EXTRA_DIR, dir)
            val fragment = ImageViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}