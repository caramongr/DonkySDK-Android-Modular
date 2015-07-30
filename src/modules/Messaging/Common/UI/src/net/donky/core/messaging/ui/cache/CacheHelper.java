package net.donky.core.messaging.ui.cache;

import android.text.TextUtils;

/**
 * Created by Marcin Swierczek
 * 17/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class CacheHelper {

    /**
     * Translates avatar id to string that can be used by disc cache as file name.
     * @param avatarId Avatar ID.
     * @return Key/file name for disk cache.
     */
    public static String getDiskCacheKey(final String avatarId) {

        if (!TextUtils.isEmpty(avatarId)) {
            return avatarId.replace("|","").toLowerCase();
        }

        return null;
    }

}
