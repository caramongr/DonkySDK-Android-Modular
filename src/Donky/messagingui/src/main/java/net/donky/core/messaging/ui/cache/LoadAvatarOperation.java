package net.donky.core.messaging.ui.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import net.donky.core.assets.DonkyAssetController;
import net.donky.core.assets.NotificationImageLoader;
import net.donky.core.messaging.ui.helpers.MetricsHelper;

import java.lang.ref.WeakReference;

/**
 * Async Task for downloading and storing image view resources from the network.
 *
 * Created by Marcin Swierczek
 * 17/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LoadAvatarOperation extends AsyncTask<String, Void, Bitmap> {

    Context context;
    private CursorAdapterWithImageCache richInboxAdapter;
    String avatarAssetId;
    String diskCacheId;
    WeakReference<ImageView> imageViewWeakReference;
    int sizeInDensityPixels;

    /**
     * Recommended constructor.
     *
     * @param context Application context.
     * @param richInboxAdapter Database adapter extending {@link CursorAdapterWithImageCache}
     * @param avatarAssetId ID of the image asset on Donky Network.
     * @param imageViewWeakReference Weak reference to image view to which the avatar should be loaded.
     * @param sizeInDensityPixels Size of avatar image view in density pixels
     */
    public LoadAvatarOperation(Context context, CursorAdapterWithImageCache richInboxAdapter, final String avatarAssetId, final WeakReference<ImageView> imageViewWeakReference, int sizeInDensityPixels) {
        this.context = context;
        this.richInboxAdapter = richInboxAdapter;
        this.avatarAssetId = avatarAssetId;
        this.diskCacheId = CacheHelper.getDiskCacheKey(avatarAssetId);
        this.imageViewWeakReference = imageViewWeakReference;
        this.sizeInDensityPixels = sizeInDensityPixels;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return richInboxAdapter.getBitmapFromDiskCache(diskCacheId);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            richInboxAdapter.addBitmapToMemoryCache(avatarAssetId, bitmap);

            setImage(bitmap, imageViewWeakReference);

        } else {

            DonkyAssetController.getInstance().downloadImageAsset(avatarAssetId, new NotificationImageLoader(context, MetricsHelper.dpToPx(context, sizeInDensityPixels)) {

                @Override
                public void success(final Bitmap bitmap) {

                    if (bitmap != null) {

                        richInboxAdapter.addBitmapToMemoryCache(avatarAssetId, bitmap);

                        richInboxAdapter.addBitmapToDiskCache(diskCacheId, bitmap);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                setImage(bitmap, imageViewWeakReference);
                            }
                        });

                    }

                }

                @Override
                public void failure(Exception e) {
                }

            });

        }
    }

    /**
     * Sets the downloaded image to the associated image view.
     *
     * @param bitmap Downloaded avatar image.
     * @param imageViewWeakReference Weak reference to image view to which the avatar should be loaded.
     */
    private void setImage(Bitmap bitmap, final WeakReference<ImageView> imageViewWeakReference) {

        if (imageViewWeakReference != null) {
            ImageView imageView = imageViewWeakReference.get();

            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

    }
}
