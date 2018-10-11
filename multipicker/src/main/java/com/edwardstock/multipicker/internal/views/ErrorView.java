package com.edwardstock.multipicker.internal.views;

import com.arellomobile.mvp.MvpView;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public interface ErrorView extends MvpView {
    void onError(CharSequence error);
    void onError(Throwable t);
}
