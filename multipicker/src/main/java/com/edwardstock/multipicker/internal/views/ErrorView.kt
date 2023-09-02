package com.edwardstock.multipicker.internal.views

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
interface ErrorView {
    fun onError(error: CharSequence?)
    fun onError(t: Throwable?)
}