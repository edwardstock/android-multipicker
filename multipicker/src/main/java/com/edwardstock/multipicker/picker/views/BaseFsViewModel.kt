package com.edwardstock.multipicker.picker.views

import androidx.lifecycle.ViewModel
import com.edwardstock.multipicker.internal.MediaFileLoader

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
abstract class BaseFsViewModel : ViewModel() {
    abstract fun updateFiles(loader: MediaFileLoader)
}