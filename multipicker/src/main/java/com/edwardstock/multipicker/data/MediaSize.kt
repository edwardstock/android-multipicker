package com.edwardstock.multipicker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@Parcelize
class MediaSize(
        var width: Int = 0,
        var height: Int = 0
) : Parcelable {

    constructor(width: String?, height: String?) : this() {
        if (width == null || width.isEmpty() || height == null || height.isEmpty()) {
            this.width = 0
            this.height = 0
            return
        }
        this.width = width.toInt()
        this.height = height.toInt()
    }
}