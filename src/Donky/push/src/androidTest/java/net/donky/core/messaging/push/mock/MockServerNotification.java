package net.donky.core.messaging.push.mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;
import net.donky.core.network.ServerNotification;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marcin Swierczek
 * 20/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockServerNotification extends ServerNotification {

    public MockServerNotification() {

        JsonObject obj = new JsonParser().parse("{\"messageType\":\"SimplePush\",\"msgSentTimeStamp\":\"2015-07-22T07:52:34.848Z\",\"buttonSets\":[],\"senderDisplayName\":\"Donky Sandbox\",\"body\":\"hi\",\"messageScope\":\"A2P\",\"senderInternalUserId\":\"ca064491-8768-4513-b2d8-43bf93e51bc6\",\"senderMessageId\":\"TO6cJuDp/E6jfPGnz/HB8w:5398203\",\"messageId\":\"ca0280bd-3ce9-4203-bf82-4bda003bd13d\",\"contextItems\":{\"ExternalRef\":\"push no buttons\",\"ExternalId\":\"CampaignId:67223\"},\"avatarAssetId\":\"5bfd8aa3-55ff-488e-b382-0f115c769942|avatar|NE_PRD_RC1_RG3\",\"sentTimestamp\":\"2015-07-22T07:52:34.848Z\"}").getAsJsonObject();

        setMockData("SimplePushMessage", IdHelper.generateId(), obj, "2015-07-20T14:58:25.995Z");
    }

    public MockServerNotification(String messageId, boolean receivedExpired) {

        DateAndTimeHelper.getCurrentUTCTime();

        long currentTime = System.currentTimeMillis();
        long hour = TimeUnit.HOURS.toMillis(1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeZone = ".000Z";

        String expiredTimestampExpired = sdf.format(currentTime - 2*hour)+timeZone;
        String expiredTimestampNotExpired = sdf.format(currentTime + 2*hour)+timeZone;
        String sentTimestampPast = sdf.format(currentTime - hour)+timeZone;
        //String sentTimestampRecent = sdf.format(currentTime);

        if (receivedExpired) {
            JsonObject obj = new JsonParser().parse("{\"messageType\":\"SimplePush\",\"expiryTimeStamp\":\""+expiredTimestampExpired+"\",\"msgSentTimeStamp\":\"2015-07-22T07:52:34.848Z\",\"buttonSets\":[],\"senderDisplayName\":\"Donky Sandbox\",\"body\":\"hi\",\"messageScope\":\"A2P\",\"senderInternalUserId\":\"ca064491-8768-4513-b2d8-43bf93e51bc6\",\"senderMessageId\":\"TO6cJuDp/E6jfPGnz/HB8w:5398203\",\"messageId\":\""+messageId+"\",\"contextItems\":{\"ExternalRef\":\"push no buttons\",\"ExternalId\":\"CampaignId:67223\"},\"avatarAssetId\":\"5bfd8aa3-55ff-488e-b382-0f115c769942|avatar|NE_PRD_RC1_RG3\",\"sentTimestamp\":\""+sentTimestampPast+"\"}").getAsJsonObject();
            setMockData("SimplePushMessage", IdHelper.generateId(), obj, sentTimestampPast);
        } else {
            JsonObject obj = new JsonParser().parse("{\"messageType\":\"SimplePush\",\"expiryTimeStamp\":\""+expiredTimestampNotExpired+"\",\"msgSentTimeStamp\":\"2015-07-22T07:52:34.848Z\",\"buttonSets\":[],\"senderDisplayName\":\"Donky Sandbox\",\"body\":\"hi\",\"messageScope\":\"A2P\",\"senderInternalUserId\":\"ca064491-8768-4513-b2d8-43bf93e51bc6\",\"senderMessageId\":\"TO6cJuDp/E6jfPGnz/HB8w:5398203\",\"messageId\":\""+messageId+"\",\"contextItems\":{\"ExternalRef\":\"push no buttons\",\"ExternalId\":\"CampaignId:67223\"},\"avatarAssetId\":\"5bfd8aa3-55ff-488e-b382-0f115c769942|avatar|NE_PRD_RC1_RG3\",\"sentTimestamp\":\""+sentTimestampPast+"\"}").getAsJsonObject();
            setMockData("SimplePushMessage", IdHelper.generateId(), obj, sentTimestampPast);
        }

    }
}