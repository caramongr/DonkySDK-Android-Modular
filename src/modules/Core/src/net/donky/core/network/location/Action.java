package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

public class Action {

    @SerializedName("actionType")
    private String actionType;

    @SerializedName("message")
    private Message message;

    public Action(String actionType, Message message) {
        this.actionType = actionType;
        this.message = message;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
