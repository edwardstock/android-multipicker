package com.edwardstock.multipicker.picker.adapters

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.databinding.MpItemImageBinding
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class FilesAdapter(
        private val config: PickerConfig,
        private val selectedInit: MutableList<MediaFile>?,
        private val itemClickListener: ((file: MediaFile, isSelected: Boolean, sharedView: View) -> Unit)? = null
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private var mInflater: LayoutInflater? = null
    private val mItems: MutableList<MediaFile> = ArrayList()
    private var mSelectionTracker: SelectionTracker<MediaFile>? = null
    private var mWaitingForMeasure: MediaFile? = null
    private var mOnMeasuredListener: ((file: MediaFile) -> Unit)? = null
    private val mItemChanged: Map<Int, Boolean> = HashMap()
    private var mList: WeakReference<RecyclerView?>? = null
    private var mIsScrolling = false

    fun setSelectionTracker(selectionTracker: SelectionTracker<MediaFile>?) {
        mSelectionTracker = selectionTracker
        if (selectedInit != null && selectedInit.isNotEmpty()) {
            mSelectionTracker!!.setItemsSelected(selectedInit, true)
            selectedInit.clear()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.context)
        }
        val binding = MpItemImageBinding.inflate(mInflater!!, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemId(position: Int): Long {
        return mItems[position].id
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val ctx = viewHolder.itemView.context
        val file = mItems[position]
        val isSelected = mSelectionTracker!!.isSelected(file)
        val isEnableSelection = isSelected || mSelectionTracker!!.selection.size() > 0
        ViewCompat.setTransitionName(viewHolder.b.imageView, ctx.resources.getString(R.string.mp_transition_image) + file.id.toString())
        val matrix = ColorMatrix()
        matrix.setSaturation(.8f)
        val filter = ColorMatrixColorFilter(matrix)
        viewHolder.b.imageView.colorFilter = filter
        Glide.with(viewHolder.b.imageView)
                .load(file.uri)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        if (mWaitingForMeasure == null || mOnMeasuredListener == null) {
                            return false
                        }
                        if (file == mWaitingForMeasure) {
                            mOnMeasuredListener!!.invoke(mWaitingForMeasure!!)
                        }
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (mWaitingForMeasure == null || mOnMeasuredListener == null) {
                            return false
                        }
                        if (file == mWaitingForMeasure) {
                            mOnMeasuredListener!!.invoke(mWaitingForMeasure!!)
                        }
                        return false
                    }
                })
                .apply(RequestOptions().error(R.drawable.mp_image_placeholder))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.b.imageView)
        val target = ConstraintSet()
        target.clone(viewHolder.b.root)
        if (isSelected) {
            target.constrainPercentHeight(R.id.mp_image_view_root, .80f)
            target.constrainPercentWidth(R.id.mp_image_view_root, .80f)
        } else {
            target.constrainPercentHeight(R.id.mp_image_view_root, 1.0f)
            target.constrainPercentWidth(R.id.mp_image_view_root, 1.0f)
        }
        if (file.isVideo) {
            if (file.videoInfo != null) {
                viewHolder.b.mpFileTypeDesc.visibility = View.VISIBLE
                viewHolder.b.mpFileTypeDesc.text = file.videoInfo!!.duration
            }
            viewHolder.b.mpFileTypeIcon.setImageResource(R.drawable.mp_ic_play_circle_filled)
            viewHolder.b.mpFileTypeIcon.visibility = View.VISIBLE
        }
        if (isEnableSelection) {
            target.setAlpha(R.id.image_selection, 1f)
        } else {
            target.setAlpha(R.id.image_selection, 0f)
        }
        target.setAlpha(R.id.mp_overlay, if (isSelected) .5f else 0f)
        viewHolder.b.imageSelection.isSelected = isSelected
        viewHolder.itemView.setOnClickListener {
            itemClickListener?.invoke(file, isSelected, viewHolder.b.imageView)
        }
        Timber.d("Update item: %d, selection enable: %b", viewHolder.bindingAdapterPosition, isEnableSelection)
        if (mList != null && mList!!.get() != null) {
            mList!!.get()!!.post {
                val allover = TransitionSet()
                allover.addTransition(AutoTransition())
                allover.duration = 100
                val check = TransitionSet()
                check.addTarget(R.id.image_selection)
                check.duration = 120
                allover.addTransition(check)
                allover.ordering = TransitionSet.ORDERING_TOGETHER
                if (config.isEnableSelectionAnimation && !mIsScrolling) {
                    TransitionManager.beginDelayedTransition(viewHolder.b.root, allover)
                }
                target.applyTo(viewHolder.b.root)
            }
        }
    }

    val items: List<MediaFile>
        get() = mItems

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setData(files: List<MediaFile>?) {
        mItems.clear()
        if (files != null) {
            mItems.addAll(files)
        } else {
            Timber.w("Null list media files")
        }

    }

    fun getItem(position: Int): MediaFile {
        return mItems[position]
    }

    @get:Deprecated("") val selectedImages: Selection<MediaFile>
        get() = mSelectionTracker!!.selection

    fun setOnFileMeasuredListener(file: MediaFile?, listener: (file: MediaFile) -> Unit) {
        mWaitingForMeasure = file
        mOnMeasuredListener = listener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mList = WeakReference(recyclerView)
    }

    fun setIsScrolling(b: Boolean) {
        mIsScrolling = b
    }

    interface OnMeasuredListener {
        fun onMeasured(file: MediaFile?)
    }

    interface OnMediaClickListener {
        fun onClick(file: MediaFile?, isSelected: Boolean, sharedView: View?)
    }

    internal interface ViewHolderWithDetails<T> {
        fun getItemDetails(adapter: FilesAdapter, position: Int): ItemDetails<T>
    }

    class ViewHolder internal constructor(var b: MpItemImageBinding) : RecyclerView.ViewHolder(b.root), ViewHolderWithDetails<MediaFile> {
        override fun getItemDetails(adapter: FilesAdapter, position: Int): ItemDetails<MediaFile> {
            return object : ItemDetails<MediaFile>() {
                override fun getPosition(): Int {
                    return position
                }

                override fun getSelectionKey(): MediaFile {
                    return adapter.getItem(position)
                }
            }
        }
    }
}