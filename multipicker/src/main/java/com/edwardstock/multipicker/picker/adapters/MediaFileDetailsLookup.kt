package com.edwardstock.multipicker.picker.adapters

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.picker.adapters.FilesAdapter.ViewHolderWithDetails
import java.lang.ref.WeakReference

/**
 * android-image-picker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class MediaFileDetailsLookup(recyclerView: RecyclerView) : ItemDetailsLookup<MediaFile>() {

    private val mRecyclerView: WeakReference<RecyclerView> = WeakReference(recyclerView)

    override fun getItemDetails(e: MotionEvent): ItemDetails<MediaFile>? {
        val view = mRecyclerView.get()!!.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = mRecyclerView.get()!!.getChildViewHolder(view)
            if (viewHolder is ViewHolderWithDetails<*>) {
                val position = viewHolder.bindingAdapterPosition
                return (viewHolder as ViewHolderWithDetails<MediaFile>).getItemDetails(mRecyclerView.get()!!.adapter!! as FilesAdapter, position)
            }
        }
        return null
    }

}