package net.donky.core.assets.utils;

import android.webkit.MimeTypeMap;

/**
 * Helper class for recognising Mime Types.
 *
 * Created by Marcin Swierczek
 * 11/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MimeUtils {

    public static final String MIME_TYPE_MESSAGE_ASSET = "image/jpeg";

    public static final String MIME_TYPE_MESSAGE_ASSET_JPEG = "image/jpeg";
    public static final String MIME_TYPE_MESSAGE_ASSET_PNG = "image/png";

    public static final String MIME_TYPE_MESSAGE_ASSET_PDF = "application/pdf";
    public static final String MIME_TYPE_MESSAGE_ASSET_MSWORD = "application/msword";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_TEMPLATE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORD = "application/vnd.openxmlformats-officedocument.word";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_TEMPLATE = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_SHEET_MACROENABLED_12 = "application/vnd.ms-excel.sheet.macroEnabled.12";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_TEMPLATE_MACROENABLED_12 = "application/vnd.ms-excel.template.macroEnabled.12";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_ADDIN_MACROENABLED_12 = "application/vnd.ms-excel.addin.macroEnabled.12";
    public static final String MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_SHEET_BINARY_MACROENABLED_12 = "application/vnd.ms-excel.sheet.binary.macroEnabled.12";

    public static final String VIDEO_X_FLV = "video/x-flv";
    public static final String VIDEO_MP4 = "video/mp4";
    public static final String VIDEO_MPEG = "video/mpeg";
    public static final String VIDEO_3GPP = "video/3gpp";
    public static final String VIDEO_QUICKTIME = "video/quicktime";
    public static final String VIDEO_X_MSVIDEO = "video/x-msvideo";
    public static final String VIDEO_X_MS_WMV = "video/x-ms-wmv";

    public static final String AUDIO_BASIC = "audio/basic";
    public static final String AUIDO_L24 = "auido/L24";
    public static final String AUDIO_MID = "audio/mid";
    public static final String AUDIO_MPEG = "audio/mpeg";
    public static final String AUDIO_MP4 = "audio/mp4";
    public static final String AUDIO_MP3 = "audio/mp3";
    public static final String AUDIO_X_AIFF = "audio/x-aiff";
    public static final String AUDIO_X_MPEGURL = "audio/x-mpegurl";
    public static final String AUDIO_VND_RN_REALAUDIO = "audio/vnd.rn-realaudio";
    public static final String AUDIO_OGG = "audio/ogg";
    public static final String AUDIO_VORBIS = "audio/vorbis";
    public static final String AUDIO_VND_WAV = "audio/vnd.wav";
    public static final String AUDIO_WAV = "audio/wav";

    public static final String[] APPLICATION_MIME_TYPES = {
            MIME_TYPE_MESSAGE_ASSET_PDF,
            MIME_TYPE_MESSAGE_ASSET_MSWORD,
            MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT,
            MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_TEMPLATE,
            MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORD,
            MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL,
            MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET,
            MIME_TYPE_MESSAGE_ASSET_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_TEMPLATE,
            MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_SHEET_MACROENABLED_12,
            MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_TEMPLATE_MACROENABLED_12,
            MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_ADDIN_MACROENABLED_12,
            MIME_TYPE_MESSAGE_ASSET_VND_MS_EXCEL_SHEET_BINARY_MACROENABLED_12
    };

    public static final String[] IMAGE_MIME_TYPES = {
            MIME_TYPE_MESSAGE_ASSET_JPEG,
            MIME_TYPE_MESSAGE_ASSET_PNG
    };

    public static final String[] VIDEO_MIME_TYPES = {
            VIDEO_X_FLV,
            VIDEO_MP4,
            VIDEO_MPEG,
            VIDEO_3GPP,
            VIDEO_QUICKTIME,
            VIDEO_X_MSVIDEO,
            VIDEO_X_MS_WMV
    };

    public static final String[] AUDIO_MIME_TYPES = {
            AUDIO_BASIC,
            AUIDO_L24,
            AUDIO_MID,
            AUDIO_MPEG,
            AUDIO_MP4,
            AUDIO_MP3,
            AUDIO_X_AIFF,
            AUDIO_X_MPEGURL,
            AUDIO_VND_RN_REALAUDIO,
            AUDIO_OGG,
            AUDIO_VORBIS,
            AUDIO_VND_WAV,
            AUDIO_WAV
    };

    /**
     * Get MIME type from provided file URL.
     *
     * @param url URL to saved file
     * @return Mime type or null if not recognised.
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
