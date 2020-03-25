package com.edwardstock.multipicker.internal.helpers;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.annotation.Px;
import androidx.annotation.RestrictTo;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class DisplayHelper {

    @Px
    public static int dpToPx(Context context, int dps) {
        float pixels = getMetrics(context).density * dps;
        return (int) (pixels + 0.5f);
    }

    public static int getWidth(Context context) {
        return getMetrics(context).widthPixels;
    }

    public static int getHeight(Context context) {
        return getMetrics(context).heightPixels;
    }

    public static DisplayMetrics getMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }
}
