package net.donky.core.messaging.ui.cache;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import net.donky.core.messaging.ui.R;

import java.lang.ref.WeakReference;

/**
 * Cursor Adapter that loads images and stores them in disk and memory cache.
 *
 * Created by Marcin Swierczek
 * 16/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class CursorAdapterWithImageCache extends CursorAdapter {

    public static final String DISK_CACHE_UNIQUE_NAME_USER_AVATARS = "DNUsersAvatars";

    private LruCache<String, Bitmap> mMemoryCache;

    private String diskCacheUniqueName;

    private Context context;

    int avatarSizeInDP;

    public CursorAdapterWithImageCache(Context context, Cursor c, int flags, String diskCacheUniqueName, int avatarSizeInDP) {

        super(context, c, flags);

        this.context = context;

        this.avatarSizeInDP = avatarSizeInDP;

        // Get max available memory
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / Math.max(4, context.getResources().getInteger(R.integer.dk_part_of_memory_to_use_as_cache));

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        this.diskCacheUniqueName = diskCacheUniqueName;

    }

    /**
     * Load avatar image from the network.
     *
     * @param avatarAssetId Avatar ID on the network.
     * @param imageViewWeakReference Weak reference to image to which the avatar should be loaded.
     */
    public void loadAvatar(final String avatarAssetId, final WeakReference<ImageView> imageViewWeakReference) {

        Bitmap bitmap = getBitmapFromMemCache(avatarAssetId);

        if (bitmap == null) {

            new LoadAvatarOperation(context, this, avatarAssetId, imageViewWeakReference, avatarSizeInDP).execute();

        } else {

            if (imageViewWeakReference != null) {
                ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }

        }

    }

    /**
     * Save bitmap to memory cache.
     *
     * @param key The unique id for the bitmap. Avatar id.
     * @param bitmap Bitmap to save.
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Retrieve the bitmap from memory cache.
     *
     * @param key The unique id for the bitmap. Avatar id.
     * @return Bitmap from memory cache.
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * Save bitmap to disk cache.
     *
     * @param key The unique id for the bitmap. Avatar id.
     * @param bitmap Bitmap to save.
     */
    public void addBitmapToDiskCache(String key, Bitmap bitmap) {
        DonkyDiskCacheManager.getInstance().getDiskCache(diskCacheUniqueName).put(key, bitmap);
    }

    /**
     * Retrieve the bitmap from disk cache.
     *
     * @param key The unique id for the bitmap. Avatar id.
     * @return Bitmap from memory cache.
     */
    public Bitmap getBitmapFromDiskCache(String key) {
        return DonkyDiskCacheManager.getInstance().getDiskCache(diskCacheUniqueName).getBitmap(key);
    }

    /**
     * Close disk cache. This method should be called before application will be closed.
     */
    public void closeDiskCache() {
        DonkyDiskCacheManager.getInstance().closeDiskCache(diskCacheUniqueName);
    }
}
