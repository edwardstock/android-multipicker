package com.edwardstock.multipicker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@Parcelize
class Dir(
        var name: String? = null,
        var files: MutableList<MediaFile> = ArrayList()
) : Parcelable