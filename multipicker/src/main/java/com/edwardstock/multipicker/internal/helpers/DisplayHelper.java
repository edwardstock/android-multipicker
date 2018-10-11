package com.edwardstock.multipicker.internal.helpers;

import android.content.Context;
import android.support.annotation.Px;
import android.support.annotation.RestrictTo;
import android.util.DisplayMetrics;

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
