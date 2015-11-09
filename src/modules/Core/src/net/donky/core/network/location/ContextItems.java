package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

public class ContextItems {

    @SerializedName("ExternalRef")
    private String externalRef;

    @SerializedName("TriggerId")
    private String triggerId;

    @SerializedName("TriggerActionType")
    private String triggerActionType;

    public ContextItems(String externalRef, String triggerId, String triggerActionType) {
        this.externalRef = externalRef;
        this.triggerId = triggerId;
        this.triggerActionType = triggerActionType;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getTriggerActionType() {
        return triggerActionType;
    }

    public void setTriggerActionType(String triggerActionType) {
        this.triggerActionType = triggerActionType;
    }
}
