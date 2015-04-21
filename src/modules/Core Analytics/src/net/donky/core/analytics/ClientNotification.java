package net.donky.core.analytics;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class responsible for constructing any Client notification that Analytics Module may want to send to the donky Network.
 *
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class ClientNotification extends net.donky.core.network.ClientNotification {

    /**
     * Client Notifications result.
     */
    private enum Type {

        AppLaunch,
        AppSession;

    }

    protected ClientNotification(String type, String id) {
        super(type, id);
    }

    /**
     * Create client notification describing application start.
     *
     * @param time Time of application start.
     * @param trigger Trigger for starting application.
     * @return
     */
    static net.donky.core.network.ClientNotification createAppStartNotification(long time, AnalyticsInternalController.Trigger trigger) {

        ClientNotification n = new ClientNotification(Type.AppLaunch.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createAppStart(n, time, trigger)));

        } catch (JSONException e) {

            e.printStackTrace();

        };

        return n;

    }

    /**
     * Create client notification describing application stop.
     *
     * @param timeStart Time of last application start.
     * @param timeStop Time of last application stop.
     * @param trigger
     * @return
     */
    static net.donky.core.network.ClientNotification createAppStopNotification(long timeStart, long timeStop, AnalyticsInternalController.Trigger trigger) {

        ClientNotification n = new ClientNotification(Type.AppSession.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createAppStop(n, timeStart, timeStop, trigger)));

        } catch (JSONException e) {

            e.printStackTrace();

        };

        return n;
    }

    /**
     * Create serialized object for application open event.
     */
    private static AppStart createAppStart(ClientNotification n, long time, AnalyticsInternalController.Trigger trigger) {

        AppStart u = n.new AppStart();

        u.type = Type.AppLaunch.toString();
        u.launchTimeUtc = DateAndTimeHelper.getUTCTimeFormated(time);
        u.sessionTrigger = trigger.getValue();
        u.operatingSystem = "Android";

        return u;
    }

    /**
     * Create serialized object for application open event.
     */
    private static AppStop createAppStop(ClientNotification n, long timeStart, long timeStop, AnalyticsInternalController.Trigger trigger) {

        AppStop u = n.new AppStop();

        u.type = Type.AppSession.toString();
        u.startTimeUtc = DateAndTimeHelper.getUTCTimeFormated(timeStart);
        u.endTimeUtc = DateAndTimeHelper.getUTCTimeFormated(timeStop);
        u.sessionTrigger = trigger.getValue();
        u.operatingSystem = "Android";

        return u;
    }

    /**
     * Description of json content of 'Application Start' notification.
     */
    private class AppStart {

        @SerializedName("type")
        private String type;

        @SerializedName("launchTimeUtc")
        private String launchTimeUtc;

        @SerializedName("sessionTrigger")
        private int sessionTrigger;

        @SerializedName("operatingSystem")
        private String operatingSystem;

    }

    /**
     * Description of json content of 'Application Stop' notification.
     */
    private class AppStop {

        @SerializedName("type")
        private String type;

        @SerializedName("startTimeUtc")
        private String startTimeUtc;

        @SerializedName("endTimeUtc")
        private String endTimeUtc;

        @SerializedName("sessionTrigger")
        private int sessionTrigger;

        @SerializedName("operatingSystem")
        private String operatingSystem;

    }
}
