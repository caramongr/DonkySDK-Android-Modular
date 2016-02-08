package net.donky.core.network.assets;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Helper class for image processing.
 * @deprecated Please use net.donky.core.assets.utils.ImageUtils from Assets Module instead.
 *
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Deprecated
public class ImageHelper {

    /**
     * Get the number of pixels scaled for the current density.
     *
     * @param resources The {@link android.content.res.Resources}.
     * @param dps Number of pixels in mdpi.
     * @return The number of pixels in current density.
     */
    public static int getPixelsFromDP(Resources resources, float dps) {
        // Get the screen's density scale
        final float scale = resources.getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dps * scale + 0.5f);
    }

    /**
     * Try to resize the given bitmap in memory, keeping it's aspect ratio to the given max dimensions.
     *
     * @param b The bitmap to scale.
     * @param maxWidth The max width.
     * @param maxHeight The max height.
     * @return The resized (if required) bitmap.
     */
    public static Bitmap resizeBitmap(Bitmap b, int maxWidth, int maxHeight, boolean allowLossOfPrecision) {

        Bitmap resized = b;
        if (resized != null) {
            Point p = scaleKeepingAspectRatio(b, maxWidth, maxHeight, allowLossOfPrecision);
            if (p != null) {
                try {
                    resized = Bitmap.createScaledBitmap(b, p.x, p.y, true);
                    //b.recycle();
                } catch (OutOfMemoryError e) {
                    resized = b;
                }
            }
        }
        return resized;
    }

    /**
     * Calculate scale of an image keeping aspect ratio.
     *
     * @param bitmap Image to scale.
     * @param maxWidth The max width of the image.
     * @param maxHeight The max height of the image.
     * @param allowLossOfDetails True if image can be made larger.
     * @return Size of scaled image.
     */
    private static Point scaleKeepingAspectRatio(Bitmap bitmap, int maxWidth, int maxHeight, boolean allowLossOfDetails) {
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();

        if ((oldWidth > maxWidth || oldHeight > maxHeight) || allowLossOfDetails) {
            Point size = new Point();
            float scaleWidth = 1f;
            float scaleHeight = 1f;
            if (oldWidth > maxWidth || allowLossOfDetails) {
                scaleWidth = ((float) maxWidth) / oldWidth;
            }
            if (oldHeight > maxHeight || allowLossOfDetails) {
                scaleHeight = ((float) maxHeight) / oldHeight;
            }

            float scale = Math.min(scaleWidth, scaleHeight);
            size.x = (int) (oldWidth * scale);
            size.y = (int) (oldHeight * scale);

            return size;
        }
        return null;
    }
}
