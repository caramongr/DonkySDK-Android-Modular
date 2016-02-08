package net.donky.core.assets.utils;

import android.annotation.SuppressLint;

/**
 * Helper for the file system operations.
 *
 * Created by Marcin Swierczek
 * 13/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class FileUtils {

    /**
     * Human readable file size description.
     *
     * @param bytes Number of Bytes to convert.
     * @param si True if converting to SI units
     * @return String describing the number of Bytes in more convenient units.
     */
    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
