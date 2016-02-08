package net.donky.core.network.assets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

/**
 * Listener for image download tasks.
 * @deprecated Please use net.donky.core.assets.NotificationImageLoader from Assets Module instead.
 *
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Deprecated
public abstract class NotificationImageLoader {

    int width;
    int height;

    public NotificationImageLoader(Context context) {

        Resources resources = context.getResources();

        int size = ImageHelper.getPixelsFromDP(resources, 128);

        this.width = size;
        this.height = size;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            this.width = (int)resources.getDimension(android.R.dimen.notification_large_icon_width);
            this.height = (int)resources.getDimension(android.R.dimen.notification_large_icon_height);
        }

    }

    public NotificationImageLoader(Context context, int dps) {

        Resources resources = context.getResources();

        int size = ImageHelper.getPixelsFromDP(resources, dps);

        this.width = size;
        this.height = size;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            this.width = (int)resources.getDimension(android.R.dimen.notification_large_icon_width);
            this.height = (int)resources.getDimension(android.R.dimen.notification_large_icon_height);
        }

    }

    /**
     * Image downloaded successfully.
     * @param bitmap Image downloaded from the network.
     */
    public abstract void success(Bitmap bitmap);

    /**
     * Image download failed.
     *
     * @param e Exception description.
     */
    public abstract void failure(Exception e);

    /**
     * The method that will be called when image was downloaded and before returning to the API.
     * @param bitmap
     */
    void downloadCompleted(Bitmap bitmap) {

        success(ImageHelper.resizeBitmap(bitmap, width, height, true));

    }
}
