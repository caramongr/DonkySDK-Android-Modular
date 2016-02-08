package net.donky.core.messaging.ui.helpers;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Helper for display metrics.
 *
 * Created by Marcin Swierczek
 * 23/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MetricsHelper {

    /**
     * Converts density pixels to pixsels.
     * @param context Application context
     * @param dp Density pixels to convert
     * @return Number of pixels
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * Converts pixels to density pixels.
     * @param context Application context
     * @param px Pixels to convert
     * @return Number of density pixels
     */
    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

}
