package net.donky.core.messaging.push.logic;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

/**
 * Class responsible for constructing any Client notification that Push Logic module may want to send to the donky Network
 *
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ClientNotification extends net.donky.core.network.ClientNotification {

    /**
     * Client Notifications type that Push Logic module can send.
     */
    enum Type {

        InteractionResult

    }

    protected ClientNotification(String type, String id) {
        super(type, id);
    }

    /**
     * Creates a {@link net.donky.core.network.ClientNotification} that informs donky Network about notification button being clicked.
     *
     * @param buttonSetAction Description of the clicked button.
     * @param simplePushData Description of Simple Push interactive message.
     * @return InteractionResult client notification.
     */
    static net.donky.core.network.ClientNotification createInteractionResultNotification(SimplePushData.ButtonSetAction buttonSetAction, SimplePushData simplePushData) {

        ClientNotification n = new ClientNotification(Type.InteractionResult.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createInteractionResult(n, buttonSetAction, simplePushData)));

        } catch (JSONException e) {

            e.printStackTrace();

        };

        return n;
    }

    /**
     * Creates a InteractionResult object for Interaction Result notification.
     *
     * @param n Client notification of type 'InteractionResult' to be completed
     * @param buttonSetAction Description of the clicked button.
     * @param simplePushData Description of Simple Push interactive message.
     * @return Description of the json content for InteractionResult Client notification.
     */
    private static InteractionResult createInteractionResult(ClientNotification n, SimplePushData.ButtonSetAction buttonSetAction, SimplePushData simplePushData) {

        InteractionResult u = n.new InteractionResult();
        u.type = Type.InteractionResult.toString();
        u.operatingSystem = "Android";

        if (simplePushData != null) {

            u.senderInternalUserId = simplePushData.getSenderInternalUserId();
            u.messageId = simplePushData.getMessageId();
            u.senderMessageId = simplePushData.getSenderMessageId();
            u.interactionTimeStamp = DateAndTimeHelper.getCurrentUTCTime();
            u.messageSentTimestamp = simplePushData.getSentTimestamp();
            u.contextItems = simplePushData.getContextItems();

            u.timeToInteractionSeconds =
                    (new Date().getTime() - DateAndTimeHelper.parseUtcDate(simplePushData.getSentTimestamp()).getTime()) / 1000;

            if (simplePushData.getButtonSets() != null) {

                SimplePushData.ButtonSet buttonSetToUse = null;

                for (SimplePushData.ButtonSet buttonSet : simplePushData.getButtonSets()) {

                    if (buttonSet != null && DonkyPushLogic.PLATFORM.equals(buttonSet.getPlatform())) {

                        buttonSetToUse = buttonSet;

                    }

                }

                if (buttonSetToUse != null) {

                    u.interactionType = buttonSetToUse.getInteractionType();

                    if (buttonSetToUse.getButtonSetActions() != null) {

                        if (buttonSetToUse.getButtonSetActions().length == 1) {

                            u.buttonDescription = buttonSetToUse.getButtonSetActions()[0].getLabel();

                        } else if (buttonSetToUse.getButtonSetActions().length == 2) {

                            StringBuilder sb = new StringBuilder();
                            sb.append(buttonSetToUse.getButtonSetActions()[0].getLabel());
                            sb.append("|");
                            sb.append(buttonSetToUse.getButtonSetActions()[1].getLabel());

                            u.buttonDescription = sb.toString();

                        }
                    }
                }
            }
        }

        if (buttonSetAction != null) {
            u.userAction = buttonSetAction.getActionType();
        }

        return u;
    }

    /**
     * Class describing the json content of InteractionResult Client notification.
     */
    private class InteractionResult {

        @SerializedName("type")
        private String type;

        @SerializedName("senderInternalUserId")
        private String senderInternalUserId;

        @SerializedName("messageId")
        private String messageId;

        @SerializedName("senderMessageId")
        private String senderMessageId;

        @SerializedName("timeToInteractionSeconds")
        private long timeToInteractionSeconds;

        @SerializedName("interactionTimeStamp")
        private String interactionTimeStamp;

        @SerializedName("interactionType")
        private String interactionType;

        @SerializedName("buttonDescription")
        private String buttonDescription;

        @SerializedName("userAction")
        private String userAction;

        @SerializedName("operatingSystem")
        private String operatingSystem;

        @SerializedName("messageSentTimestamp")
        private String messageSentTimestamp;

        @SerializedName("contextItems")
        private  Map<String,String> contextItems;


    }

}
