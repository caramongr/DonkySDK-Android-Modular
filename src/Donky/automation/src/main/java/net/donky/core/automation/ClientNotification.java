package net.donky.core.automation;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Class responsible for constructing any Client notification that Automation Module may want to send to the donky Network.
 *
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ClientNotification extends net.donky.core.network.ClientNotification {

    /**
     * Client Notifications result.
     */
    enum Type {

        ExecuteThirdPartyTriggers

    }

    enum ActionType {

        NotDefined,
        SendMessage,
        ActivatePreDeployedMessage,
        NotifyExternalSystem,
        SendCampaign

    }

    protected ClientNotification(String type, String id) {
        super(type, id);
    }

    static net.donky.core.network.ClientNotification createExecuteThirdPartyTriggersNotification(String triggerKey, Map<String,String> customData) {

        ClientNotification n = new ClientNotification(Type.ExecuteThirdPartyTriggers.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createExecuteThirdPartyTriggers(n, triggerKey, null, null, customData)));

        } catch (JSONException e) {

            e.printStackTrace();

        };

        return n;
    }

    /**
     * Create serialized object for application open event.
     */
    private static ExecuteThirdPartyTriggers createExecuteThirdPartyTriggers(ClientNotification n, String triggerKey, Location location, List<TriggerActionExecuted> triggerActionsExecuted, Map<String,String> customData) {

        ExecuteThirdPartyTriggers u = n.new ExecuteThirdPartyTriggers();

        u.type = Type.ExecuteThirdPartyTriggers.toString();
        u.triggerKey = triggerKey;
        u.timestamp = DateAndTimeHelper.getUTCTimeFormated(System.currentTimeMillis());
        u.location = location;
        u.triggerActionsExecuted = triggerActionsExecuted;
        u.customData = customData;

        return u;
    }

    private class ExecuteThirdPartyTriggers {

        @SerializedName("type")
        private String type;

        @SerializedName("triggerKey")
        private String triggerKey;

        @SerializedName("timestamp")
        private String timestamp;

        @SerializedName("location")
        private Location location;

        @SerializedName("triggerActionsExecuted")
        private List<TriggerActionExecuted> triggerActionsExecuted;

        @SerializedName("customData")
        private Map<String,String> customData;

    }

    class TriggerActionExecuted {

        @SerializedName("triggerId")
        private String triggerId;

        @SerializedName("actionsExecuted")
        private List<TriggerActionExecutedData> actionsExecuted;

    }

    class TriggerActionExecutedData {

        @SerializedName("actionType")
        private String actionType;

        @SerializedName("timeStamp")
        private String timeStamp;
    }

    private class Location {

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
