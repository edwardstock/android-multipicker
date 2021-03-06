package com.edwardstock.multipicker.picker.views;

import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.internal.mvp.MvpPresenter;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public abstract class BaseFsPresenter<View extends FileSystemView> extends MvpPresenter<View> {

    public abstract void updateFiles(MediaFileLoader loader);
}
