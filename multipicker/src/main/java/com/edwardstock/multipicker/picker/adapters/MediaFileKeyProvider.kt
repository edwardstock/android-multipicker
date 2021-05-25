package com.edwardstock.multipicker.picker.adapters

import androidx.recyclerview.selection.ItemKeyProvider
import com.edwardstock.multipicker.data.MediaFile

/**
 * android-image-picker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 *
 * Creates a new provider with the given scope.
 * @param scope Scope can't be changed at runtime.
 */
class MediaFileKeyProvider(
        private val mImages: () -> List<MediaFile>, scope: Int
) : ItemKeyProvider<MediaFile>(scope) {
    override fun getKey(position: Int): MediaFile {
        return mImages()[position]
    }

    override fun getPosition(key: MediaFile): Int {
        return mImages().indexOf(key)
    }
}