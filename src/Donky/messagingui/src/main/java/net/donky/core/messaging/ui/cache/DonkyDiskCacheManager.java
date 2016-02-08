package net.donky.core.messaging.ui.cache;

import android.content.Context;
import android.graphics.Bitmap;

import net.donky.core.messaging.ui.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Main access point for different cache folders.
 *
 * Created by Marcin Swierczek
 * 17/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyDiskCacheManager {

    private Context context;

    private Map<String, DiskBitmapCache> cache;

    private Object lock;

    DonkyDiskCacheManager() {
        cache = new HashMap<>();
        lock = new Object();
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyDiskCacheManager INSTANCE = new DonkyDiskCacheManager();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyDiskCacheManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Should be initialised when application is being created.
     * @param context Application context.
     */
    public void init(Context context) {
        this.context = context;
    }

    /**
     * Get disk cashe object for given name.
     *
     * @param name Name of cache folder.
     * @return
     */
    public DiskBitmapCache getDiskCache(String name) {

        DiskBitmapCache diskBitmapCache = null;

        synchronized (lock) {

            if (cache.containsKey(name)) {
                diskBitmapCache = cache.get(name);
            }

            if (diskBitmapCache == null || diskBitmapCache.isCacheClosed()) {
                int maxDiskCacheInBits = Math.min((int) Math.pow(2, R.integer.dk_disk_cache_limit_as_power_of_2_in_bits), Integer.MAX_VALUE);
                diskBitmapCache = new DiskBitmapCache(context, name, maxDiskCacheInBits, Bitmap.CompressFormat.PNG, 70);
                cache.put(name, diskBitmapCache);
            }

            lock.notifyAll();
        }

        return diskBitmapCache;
    }

    /**
     * Close cache with given name.
     *
     * @param name Name of cache folder.
     */
    public void closeDiskCache(String name) {

        synchronized (lock) {
            if (cache.containsKey(name)) {
                DiskBitmapCache diskBitmapCache = cache.get(name);
                diskBitmapCache.close();
                cache.remove(diskBitmapCache);
            }
            lock.notifyAll();
        }
    }

    /**
     * cleare and close cache with given name.
     *
     * @param name Name of cache folder.
     */
    public void clearAndCloseCache(String name) {

        synchronized (lock) {
            if (cache.containsKey(name)) {
                DiskBitmapCache diskBitmapCache = cache.get(name);
                diskBitmapCache.clearCache();
                diskBitmapCache.close();
                cache.remove(diskBitmapCache);
            }
            lock.notifyAll();
        }
    }
}
