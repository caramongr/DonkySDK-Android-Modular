package net.donky.core.messaging.ui.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import net.donky.core.logging.DLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wrapper around {@link AndroidDiskLruCache} class with bitmap loading/saving methods.
 *
 * Created by Marcin Swierczek
 * 16/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DiskBitmapCache {

    /**
     * bitmaps shouldn't exceed this size in bites.
     */
    public static final int IO_BUFFER_SIZE = 1024;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int compressQuality = 70;
    private static final int DISK_CACHE_VERSION = 1;

    private AndroidDiskLruCache diskCache;

    private DLog log;

    /**
     * Recommended constructor.
     *
     * @param context Application context.
     * @param uniqueName Unique string used for cache folder name.
     * @param diskCacheSize Maximum disk cache size in bits.
     * @param compressFormat Image compression format.
     * @param quality compression quality 1-100
     */
    public DiskBitmapCache(Context context, String uniqueName, int diskCacheSize,
                           Bitmap.CompressFormat compressFormat, int quality) {
        log = new DLog("DiskBitmapCache");
        try {
            final File diskCacheDir = getDiskCacheDir(context, uniqueName);
            diskCache = AndroidDiskLruCache.open(diskCacheDir, DISK_CACHE_VERSION, 1, diskCacheSize);
            this.compressFormat = compressFormat;
            compressQuality = quality;
        } catch (Exception e) {
            log.error("Error initialising disk cache.",e);
        }
    }

    /**
     * Write bitmap to file.
     *
     * @param bitmap Bitmap to save.
     * @param editor {@link AndroidDiskLruCache} {@link net.donky.core.messaging.ui.cache.AndroidDiskLruCache.Editor}
     * @return True if success.
     * @throws IOException
     */
    private boolean writeBitmapToFile( Bitmap bitmap, AndroidDiskLruCache.Editor editor )
            throws IOException {

        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(compressFormat, compressQuality, out);
        } catch (Exception e) {
            log.error("Error saving bitmap to file",e);
        } finally {
            if ( out != null ) {
                out.close();
            }
        }

        return false;
    }

    /**
     * Get folder for disk cache.
     *
     * @param context Application context.
     * @param uniqueName Unique string used for cache folder name.
     * @return Folder for disk cache.
     */
    private File getDiskCacheDir(Context context, String uniqueName) {

        if (context != null) {

            final String cachePath;

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                    Environment.isExternalStorageRemovable()) {
                File file = context.getExternalCacheDir();
                if (file != null) {
                    cachePath = context.getExternalCacheDir().getPath();
                } else {
                    cachePath = context.getCacheDir().getPath();
                }
            } else {
                cachePath = context.getCacheDir().getPath();
            }

            return new File(cachePath + File.separator + uniqueName);
        }

        return null;
    }

    /**
     * Save bitmap to disk cache.
     *
     * @param key Id of the cache entry.
     * @param data Bitmap to save.
     */
    public void put( String key, Bitmap data ) {

        AndroidDiskLruCache.Editor editor = null;
        try {

            editor = diskCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {
                diskCache.flush();
                editor.commit();
            } else {
                editor.abort();
            }

        } catch (Exception e) {

            log.error("Error saving image to disk cache",e);

            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (Exception ignored) {
                log.warning("Error aborting disk cache editor");
            }
        }

    }

    /**
     * Load bitmap from disk cache.
     *
     * @param key Id of disk cache entry.
     * @return Cached Bitmap image.
     */
    public Bitmap getBitmap( String key ) {

        Bitmap bitmap = null;
        AndroidDiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = diskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (Exception e ) {
            log.error("Error getting bitmap from disk cache.", e);
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return bitmap;

    }

    /**
     * Clear disk cache.
     */
    public void clearCache() {

        try {
            diskCache.delete();
        } catch (Exception e ) {
            log.error("Error clearing disk cache.", e);
        }
    }

    /**
     * Close disk cache. Perform this operation before exiting application.
     */
    public void close() {
        try {
            diskCache.close();
        } catch (Exception e) {
            log.error("Error closing disk cache.",e);
        }
    }

    /**
     * Check if cache has been closed.
     * @return True if cache has been closed.
     */
    public boolean isCacheClosed() {
        return diskCache.isClosed();
    }

}
