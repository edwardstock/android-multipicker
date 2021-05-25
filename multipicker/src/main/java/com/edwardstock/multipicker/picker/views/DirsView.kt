package com.edwardstock.multipicker.picker.views

import androidx.recyclerview.widget.RecyclerView
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.internal.views.ErrorView

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
interface DirsView : ErrorView, FileSystemView {
    fun setAdapter(adapter: RecyclerView.Adapter<*>)
    fun startFiles(dir: Dir)
    fun showProgress()
    fun hideProgress()
    fun showEmpty(show: Boolean)
}