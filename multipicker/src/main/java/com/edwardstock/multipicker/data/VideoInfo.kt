package com.edwardstock.multipicker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@Parcelize
class VideoInfo(
        var size: MediaSize? = null,
        var durationMs: Long = 0,
        var previewPath: String? = null
) : Parcelable {

    constructor(size: MediaSize?, durationMs: String?) : this(size) {
        if (durationMs == null || durationMs.isEmpty()) {
            this.durationMs = 0
        } else {
            this.durationMs = durationMs.toLong()
        }
    }

    val duration: String
        get() {
            val seconds = durationMs / 1000L
            val second = seconds % 60L
            val minute = seconds / 60L % 60L
            val hour = seconds / (60L * 60L) % 24L
            return if (hour > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second)
            } else {
                String.format(Locale.getDefault(), "00:%02d:%02d", minute, second)
            }
        }
}