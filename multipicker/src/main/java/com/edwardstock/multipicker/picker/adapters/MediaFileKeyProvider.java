package com.edwardstock.multipicker.picker.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.edwardstock.multipicker.data.MediaFile;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;

/**
 * android-image-picker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class MediaFileKeyProvider extends ItemKeyProvider<MediaFile> {
    private LazyValue<List<MediaFile>> mImages;

    /**
     * Creates a new provider with the given scope.
     * @param scope Scope can't be changed at runtime.
     */
    public MediaFileKeyProvider(LazyValue<List<MediaFile>> files, int scope) {
        super(scope);
        mImages = files;
    }

    @Nullable
    @Override
    public MediaFile getKey(int position) {
        return mImages.get().get(position);
    }

    @Override
    public int getPosition(@NonNull MediaFile key) {
        return mImages.get().indexOf(key);
    }
}
