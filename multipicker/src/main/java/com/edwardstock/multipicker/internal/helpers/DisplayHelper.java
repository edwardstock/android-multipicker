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

    private static DisplayMetrics getMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }
}
