package net.donky.core.messaging.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import net.donky.core.assets.DonkyAssetController;
import net.donky.core.assets.NotificationImageLoader;
import net.donky.core.messaging.ui.cache.CacheHelper;
import net.donky.core.messaging.ui.cache.DonkyDiskCacheManager;
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
    String avatarAssetId;
    String diskCacheId;
    WeakReference<ImageView> imageViewWeakReference;
    int sizeInDensityPixels;

    /**
     * Recommended constructor.
     *
     * @param context Application context.
     * @param avatarAssetId ID of the image asset on Donky Network.
     * @param imageViewWeakReference Weak reference to image view to which the avatar should be loaded.
     * @param sizeInDensityPixels Size of avatar image view in density pixels
     */
    public LoadAvatarOperation(Context context, final String avatarAssetId, final WeakReference<ImageView> imageViewWeakReference, int sizeInDensityPixels) {
        this.context = context;
        this.avatarAssetId = avatarAssetId;
        this.diskCacheId = CacheHelper.getDiskCacheKey(avatarAssetId);
        this.imageViewWeakReference = imageViewWeakReference;
        this.sizeInDensityPixels = sizeInDensityPixels;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return DonkyDiskCacheManager.getInstance().getDiskCache(net.donky.core.messaging.logic.helpers.GlobalConst.DISK_CACHE_UNIQUE_NAME).getBitmap(diskCacheId);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            setImage(bitmap, imageViewWeakReference);

        } else {

            DonkyAssetController.getInstance().downloadImageAsset(avatarAssetId, new NotificationImageLoader(context, MetricsHelper.dpToPx(context, sizeInDensityPixels)) {

                @Override
                public void success(final Bitmap bitmap) {

                    if (bitmap != null) {
                        DonkyDiskCacheManager.getInstance().getDiskCache(net.donky.core.messaging.logic.helpers.GlobalConst.DISK_CACHE_UNIQUE_NAME).put(diskCacheId, bitmap);
                        setImage(bitmap, imageViewWeakReference);
                    }
                }

                @Override
                public void failure(Exception e) {
                    setImage(null, imageViewWeakReference);
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
    private void setImage(final Bitmap bitmap, final WeakReference<ImageView> imageViewWeakReference) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (imageViewWeakReference != null) {
                    ImageView imageView = imageViewWeakReference.get();

                    if (imageView != null) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.dk_default_avatar_single_192dp);
                        }
                    }
                }
            }
        });
    }
}
