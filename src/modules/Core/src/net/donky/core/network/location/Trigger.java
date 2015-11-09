package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Trigger implements Serializable {

    @SerializedName("actionData")
    private Action[] actionData;

    @SerializedName("activationId")
    private String activationId;

    @SerializedName("restrictions")
    private Restrictions restrictions;

    @SerializedName("triggerData")
    private TriggerData triggerData;

    @SerializedName("triggerId")
    private String triggerId;

    @SerializedName("triggerType")
    private String triggerType;

    @SerializedName("validity")
    private Validity validity;

    private long lastExecutionTime;

    public Trigger(Action[] actionData, String activationId, Restrictions restrictions, TriggerData triggerData, String triggerId, String triggerType, Validity validity, long lastExecutionTime) {
        this.actionData = actionData;
        this.activationId = activationId;
        this.restrictions = restrictions;
        this.triggerData = triggerData;
        this.triggerId = triggerId;
        this.triggerType = triggerType;
        this.validity = validity;
        this.lastExecutionTime = lastExecutionTime;
    }

    public long getLastExecutionTime() {
        return lastExecutionTime;
    }

    public void setLastExecutionTime(long lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }

    public Action[] getActionData() {
        return actionData;
    }

    public void setActionData(Action[] actionData) {
        this.actionData = actionData;
    }

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    public TriggerData getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(TriggerData triggerData) {
        this.triggerData = triggerData;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Validity getValidity() {
        return validity;
    }

    public void setValidity(Validity validity) {
        this.validity = validity;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Trigger{");
        sb.append("actionData=").append(actionData == null ? "null" : Arrays.asList(actionData).toString());
        sb.append(", activationId='").append(activationId).append('\'');
        sb.append(", restrictions=").append(restrictions);
        sb.append(", triggerData=").append(triggerData);
        sb.append(", triggerId='").append(triggerId).append('\'');
        sb.append(", triggerType='").append(triggerType).append('\'');
        sb.append(", validity=").append(validity);
        sb.append(", lastExecutionTime=").append(new Date(lastExecutionTime).toString());
        sb.append('}');
        return sb.toString();
    }
}
