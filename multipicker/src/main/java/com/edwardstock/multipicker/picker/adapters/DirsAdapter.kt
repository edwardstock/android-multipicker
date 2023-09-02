package com.edwardstock.multipicker.picker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.databinding.MpItemFolderBinding
import com.edwardstock.multipicker.picker.adapters.DirsAdapter.DirViewHolder

class DirsAdapter(
        private val folderClickListener: ((dir: Dir) -> Unit)?
) : RecyclerView.Adapter<DirViewHolder>() {
    private var mInflater: LayoutInflater? = null
    private val mDirs: MutableList<Dir> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirViewHolder {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.context)
        }
        return DirViewHolder(
                MpItemFolderBinding.inflate(mInflater!!, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DirViewHolder, position: Int) {
        val dir = mDirs[position]
        Glide.with(holder.b.image)
                .load(dir.files[0].uri)
                .apply(RequestOptions()
                        .error(R.drawable.mp_folder_placeholder)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.b.image)
        holder.b.tvName.text = mDirs[position].name
        holder.b.tvNumber.text = mDirs[position].files.size.toString()
        holder.itemView.setOnClickListener { folderClickListener?.invoke(dir) }
    }

    fun setData(folders: List<Dir>?) {
        if (folders != null) {
            mDirs.clear()
            mDirs.addAll(folders)
        }
        //        notifyDataSetChanged();
    }

    override fun getItemCount(): Int {
        return mDirs.size
    }

    class DirViewHolder(var b: MpItemFolderBinding) : RecyclerView.ViewHolder(b.root)
}