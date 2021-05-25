package com.edwardstock.multipicker.internal.helpers

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.Px
import androidx.annotation.RestrictTo

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object DisplayHelper {
    @JvmStatic
    @Px
    fun dpToPx(context: Context, dps: Int): Int {
        val pixels = getMetrics(context).density * dps
        return (pixels + 0.5f).toInt()
    }

    @JvmStatic
    fun getWidth(context: Context): Int {
        return getMetrics(context).widthPixels
    }

    @JvmStatic
    fun getHeight(context: Context): Int {
        return getMetrics(context).heightPixels
    }

    fun getMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }

    fun getRotation(context: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display!!.rotation
        } else {
            @Suppress("DEPRECATION")
            context.windowManager.defaultDisplay.rotation
        }
    }
}