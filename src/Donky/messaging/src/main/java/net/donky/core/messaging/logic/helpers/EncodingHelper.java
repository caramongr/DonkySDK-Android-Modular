package net.donky.core.messaging.logic.helpers;

import java.util.List;

/**
 * Created by Marcin Swierczek
 * 27/08/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class EncodingHelper {

    /**
     * @return If this is an MML message then this will return the body of the MML.
     */
    public static String extractMMLMessageBody(String mmlText) {
        // Return the body of the MML.
        int bStart = mmlText.indexOf("<body>");
        if (bStart == -1) {
            return mmlText;
        }
        bStart += 6;
        int bEnd = mmlText.lastIndexOf("</body>");
        if (bEnd == -1) {
            return mmlText;
        }
        String htmlBody = mmlText.substring(bStart, bEnd).trim();
        htmlBody = htmlBody.replaceAll("\n", "<br/>");
        return htmlBody;
    }

}
