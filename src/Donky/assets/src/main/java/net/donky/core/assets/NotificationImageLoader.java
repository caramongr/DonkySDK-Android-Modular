package net.donky.core.assets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import net.donky.core.assets.utils.ImageUtils;
import net.donky.core.network.assets.ImageHelper;

/**
 * Listener for image download tasks.
 *
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class NotificationImageLoader {

    int width;
    int height;

    public NotificationImageLoader(Context context) {

        Resources resources = context.getResources();

        int size = ImageUtils.getPixelsFromDP(resources, 128);

        this.width = size;
        this.height = size;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            this.width = (int)resources.getDimension(android.R.dimen.notification_large_icon_width);
            this.height = (int)resources.getDimension(android.R.dimen.notification_large_icon_height);
        }

    }

    public NotificationImageLoader(Context context, int dps) {

        Resources resources = context.getResources();

        int size = net.donky.core.network.assets.ImageHelper.getPixelsFromDP(resources, dps);

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
