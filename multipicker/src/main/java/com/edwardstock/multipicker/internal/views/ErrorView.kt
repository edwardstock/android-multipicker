package com.edwardstock.multipicker.internal.views

import com.edwardstock.multipicker.internal.mvp.MvpView

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
interface ErrorView : MvpView {
    fun onError(error: CharSequence?)
    fun onError(t: Throwable?)
}