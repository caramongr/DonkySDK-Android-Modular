package net.donky.core.assets.mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.donky.core.helpers.IdHelper;
import net.donky.core.network.ServerNotification;

/**
 * Created by Marcin Swierczek
 * 20/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockServerNotification extends ServerNotification {

    public MockServerNotification() {

        JsonObject obj = new JsonParser().parse("{\"messageType\":\"Normal\",\"recipientExternalUserId\":\"mstest\",\"senderNumber\":\"447555555555\",\"senderExternalUserId\":\"m.s@d.com\",\"externalRef\":\"\",\"externalId\":\"\",\"canReply\":true,\"canForward\":true,\"silentNotification\":false,\"conversationId\":\"5bdf3e66-5226-43ac-b231-9df066009f95\",\"assets\":[],\"senderAccountType\":\"ApplicationUser\",\"senderDisplayName\":\"ms\",\"body\":\"nv\",\"messageScope\":\"P2P\",\"senderInternalUserId\":\"22932bc1-8b72-4a1a-bf6f-61c1ae237e25\",\"senderMessageId\":\"7b21f648-a3ac-4017-9db8-acefb70cb586\",\"messageId\":\"66422cce-8a1f-4d02-a86d-1e57eb7e3bbd\",\"contextItems\":{},\"sentTimestamp\":\"2015-08-20T11:43:50.547Z\"}").getAsJsonObject();

        setMockData(ServerNotification.NOTIFICATION_TYPE_ChatMessage, IdHelper.generateId(), obj, "2015-07-20T14:58:25.995Z");
    }
}
