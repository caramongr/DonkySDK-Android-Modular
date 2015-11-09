package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("messageType")
    private String messageType;

    @SerializedName("description")
    private String description;

    @SerializedName("canReply")
    private boolean canReply;

    @SerializedName("canForward")
    private boolean canForward;

    @SerializedName("canShare")
    private boolean canShare;

    @SerializedName("silentNotification")
    private boolean silentNotification;

    @SerializedName("assets")
    private String[] assets;

    @SerializedName("senderAccountType")
    private String senderAccountType;

    @SerializedName("senderDisplayName")
    private String senderDisplayName;

    @SerializedName("messageScope")
    private String messageScope;

    @SerializedName("senderInternalUserId")
    private String senderInternalUserId;

    @SerializedName("messageId")
    private String messageId;

    @SerializedName("contextItems")
    private ContextItems contextItems;

    @SerializedName("avatarAssetId")
    private String avatarAssetId;

    @SerializedName("sentTimestamp")
    private String sentTimestamp;

    public Message(String messageType, String description, boolean canReply, boolean canForward, boolean canShare, boolean silentNotification, String[] assets, String senderAccountType, String senderDisplayName, String messageScope, String senderInternalUserId, String messageId, ContextItems contextItems, String avatarAssetId, String sentTimestamp) {
        this.messageType = messageType;
        this.description = description;
        this.canReply = canReply;
        this.canForward = canForward;
        this.canShare = canShare;
        this.silentNotification = silentNotification;
        this.assets = assets;
        this.senderAccountType = senderAccountType;
        this.senderDisplayName = senderDisplayName;
        this.messageScope = messageScope;
        this.senderInternalUserId = senderInternalUserId;
        this.messageId = messageId;
        this.contextItems = contextItems;
        this.avatarAssetId = avatarAssetId;
        this.sentTimestamp = sentTimestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCanReply() {
        return canReply;
    }

    public void setCanReply(boolean canReply) {
        this.canReply = canReply;
    }

    public boolean isCanForward() {
        return canForward;
    }

    public void setCanForward(boolean canForward) {
        this.canForward = canForward;
    }

    public boolean isCanShare() {
        return canShare;
    }

    public void setCanShare(boolean canShare) {
        this.canShare = canShare;
    }

    public boolean isSilentNotification() {
        return silentNotification;
    }

    public void setSilentNotification(boolean silentNotification) {
        this.silentNotification = silentNotification;
    }

    public String[] getAssets() {
        return assets;
    }

    public void setAssets(String[] assets) {
        this.assets = assets;
    }

    public String getSenderAccountType() {
        return senderAccountType;
    }

    public void setSenderAccountType(String senderAccountType) {
        this.senderAccountType = senderAccountType;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public String getMessageScope() {
        return messageScope;
    }

    public void setMessageScope(String messageScope) {
        this.messageScope = messageScope;
    }

    public String getSenderInternalUserId() {
        return senderInternalUserId;
    }

    public void setSenderInternalUserId(String senderInternalUserId) {
        this.senderInternalUserId = senderInternalUserId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public ContextItems getContextItems() {
        return contextItems;
    }

    public void setContextItems(ContextItems contextItems) {
        this.contextItems = contextItems;
    }

    public String getAvatarAssetId() {
        return avatarAssetId;
    }

    public void setAvatarAssetId(String avatarAssetId) {
        this.avatarAssetId = avatarAssetId;
    }

    public String getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }
}
