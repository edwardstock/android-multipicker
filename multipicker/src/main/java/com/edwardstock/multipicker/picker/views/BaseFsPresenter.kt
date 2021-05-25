package com.edwardstock.multipicker.picker.views

import com.edwardstock.multipicker.internal.MediaFileLoader
import com.edwardstock.multipicker.internal.mvp.MvpPresenter

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
abstract class BaseFsPresenter<View : FileSystemView> : MvpPresenter<View>() {
    abstract fun updateFiles(loader: MediaFileLoader)
}