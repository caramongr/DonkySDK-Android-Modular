package net.donky.core.analytics.mock;

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

        JsonObject obj = new JsonParser().parse("{\"messageType\":\"Rich\",\"senderExternalUserId\":\"2wvqpp\",\"externalRef\":\"\",\"description\":\"sdf\",\"canReply\":false,\"canForward\":false,\"canShare\":false,\"silentNotification\":false,\"assets\":[],\"senderAccountType\":\"MessagingIdentity\",\"senderDisplayName\":\"Modular SDK Sandbox\",\"body\":\"sd\",\"messageScope\":\"A2P\",\"senderInternalUserId\":\"38b8367f-cc47-4ec2-af54-162050aefabe\",\"senderMessageId\":\"2cc02a29-c6e1-478e-aeea-46cc96930f61\",\"messageId\":\"51c75d24-1ca3-4971-935d-f85c2bd7034b\",\"contextItems\":{},\"avatarAssetId\":\"df4f24c4-db96-49cc-9ca9-0445d7de970f|avatar|NE_PRD_RC1_RG2\",\"sentTimestamp\":\"2015-07-20T14:58:25.162Z\"}").getAsJsonObject();

        setMockData("RichMessage", IdHelper.generateId(), obj, "2015-07-20T14:58:25.995Z");
    }
}
