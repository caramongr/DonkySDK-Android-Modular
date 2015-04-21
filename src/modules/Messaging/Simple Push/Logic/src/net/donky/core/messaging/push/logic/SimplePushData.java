package net.donky.core.messaging.push.logic;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Description of Simple Push Message contract with Donky Network.
 *
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SimplePushData implements Serializable {

    @SerializedName("messageType")
    private String messageType;

    @SerializedName("msgSentTimeStamp")
    private String msgSentTimeStamp;

    @SerializedName("senderDisplayName")
    private String senderDisplayName;

    @SerializedName("buttonSets")
    private List<ButtonSet> buttonSets;

    @SerializedName("body")
    private String body;

    @SerializedName("senderInternalUserId")
    private String senderInternalUserId;

    @SerializedName("senderMessageId")
    private String senderMessageId;

    @SerializedName("messageId")
    private String messageId;

    @SerializedName("contextItems")
    private Map<String, String> contextItems;

    @SerializedName("avatarAssetId")
    private String avatarAssetId;

    @SerializedName("sentTimestamp")
    private String sentTimestamp;

    @SerializedName("expiryTimeStamp")
    private String expiryTimeStamp;

    /**
     * Get type of message.
     *
     * @return Type of message.
     */
    public String getMessageType() {
        return messageType;
    }

    public String getMsgSentTimeStamp() {
        return msgSentTimeStamp;
    }

    /**
     * Get display name of sender.
     *
     * @return Display name of sender.
     */
    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    /**
     * Get message body.
     *
     * @return Message body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Get sender internal user id.
     *
     * @return Sender internal user id.
     */
    public String getSenderInternalUserId() {
        return senderInternalUserId;
    }

    /**
     * Get sender message id.
     *
     * @return Sender message id.
     */
    public String getSenderMessageId() {
        return senderMessageId;
    }

    /**
     * Get message id.
     *
     * @return Message id.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Get message context.
     *
     * @return Message context.
     */
    public Map<String, String> getContextItems() {
        return contextItems;
    }

    /**
     * Get avatar asset id.
     *
     * @return Avatar asset id.
     */
    public String getAvatarAssetId() {
        return avatarAssetId;
    }

    /**
     * Get message sent timestamp.
     *
     * @return Message sent timestamp.
     */
    public String getSentTimestamp() {
        return sentTimestamp;
    }

    /**
     * Get message expiry timestamp.
     *
     * @return
     */
    public String getExpiryTimeStamp() {
        return expiryTimeStamp;
    }

    /**
     * Get buttons descriptions.
     *
     * @return Buttons descriptions.
     */
    public List<ButtonSet> getButtonSets() {
        return buttonSets;
    }

    public class ButtonSet implements Serializable {

        @SerializedName("buttonSetId")
        private String buttonSetId;

        @SerializedName("platform")
        private String platform;

        @SerializedName("interactionType")
        private String interactionType;

        @SerializedName("buttonSetActions")
        private ButtonSetAction[] buttonSetActions;

        public String getButtonSetId() {
            return buttonSetId;
        }

        /**
         * Get target platform.
         *
         * @return Target platform.
         */
        public String getPlatform() {
            return platform;
        }

        /**
         * Get interaction type.
         *
         * @return Interaction type.
         */
        public String getInteractionType() {
            return interactionType;
        }

        /**
         * Get description for buttons.
         *
         * @return Description for buttons.
         */
        public ButtonSetAction[] getButtonSetActions() {
            return buttonSetActions;
        }
    }


    public class ButtonSetAction implements Serializable {

        @SerializedName("actionType")
        private String actionType;

        @SerializedName("data")
        private String data;

        @SerializedName("label")
        private String label;

        /**
         * Get action type.
         *
         * @return Action type.
         */
        public String getActionType() {
            return actionType;
        }

        /**
         * Get data for deep link.
         *
         * @return Data for deep link.
         */
        public String getData() {
            return data;
        }

        /**
         * Get button label.
         *
         * @return Button label.
         */
        public String getLabel() {
            return label;
        }
    }
}