package net.donky.core.network.content;

import net.donky.core.Notification;
import net.donky.core.helpers.IdHelper;
import net.donky.core.logging.DLog;
import net.donky.core.network.content.audience.AllUsersAudience;
import net.donky.core.network.content.audience.AudienceMember;
import net.donky.core.network.content.audience.SpecifiedUsersAudience;
import net.donky.core.network.content.content.NotificationContent;
import net.donky.core.network.content.filters.ExclusionDeviceFilter;
import net.donky.core.network.content.filters.OperatingSystemFilter;
import net.donky.core.network.content.filters.SpecificDeviceFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Builder class for Content Notification json. Content of this notification will be send to specified users, devices and operating systems.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ContentNotification extends Notification {

    /**
     * The audience for the content.
     */
    private JSONObject audience;

    /**
     * The filters for the content.
     */
    private final JSONArray filters;

    /**
     * The content of the notification.
     */
    private JSONObject content;

    public ContentNotification() {
        super("SendContent", IdHelper.generateId());
        filters = new JSONArray();
    }

    public ContentNotification(List<String> users, String notificationType, JSONObject data) {

        super("SendContent", IdHelper.generateId());
        filters = new JSONArray();

        List<AudienceMember> audienceMembers = new LinkedList<>();

        for (String userId : users) {

            audienceMembers.add(new AudienceMember(userId, null));

        }

        try {

            this.putAudience(new SpecifiedUsersAudience(audienceMembers));
            this.putContent(new NotificationContent(notificationType, data));

        } catch (JSONException e) {

            new DLog("ContentNotification").error("Error parsing Json in Content Notification.", e);

        }

    }

    public ContentNotification(String userId, String notificationType, JSONObject data) {

        super("SendContent", IdHelper.generateId());
        filters = new JSONArray();

        List<AudienceMember> audienceMembers = new LinkedList<>();

        audienceMembers.add(new AudienceMember(userId, null));

        try {

            this.putAudience(new SpecifiedUsersAudience(audienceMembers));
            this.putContent(new NotificationContent(notificationType, data));

        } catch (JSONException e) {

            new DLog("ContentNotification").error("Error parsing Json in Content Notification.", e);

        }

    }

    /**
     * Add filter to Content Notification to exclude specific devices.
     *
     * @param filter Filter to add.
     * @return Content Notification with added requested filter.
     * @throws JSONException
     */
    public ContentNotification addFilter(ExclusionDeviceFilter filter) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", filter.getType());
        jsonObject.put("deviceIds", filter.getDeviceIds());
        filters.put(jsonObject);

        return this;
    }

    /**
     * Add filter to Content Notification to specify devices that will receive the notification.
     *
     * @param filter Filter to add.
     * @return Content Notification with added requested filter.
     * @throws JSONException
     */
    public ContentNotification addFilter(SpecificDeviceFilter filter) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", filter.getType());

        List<String> list = filter.getDeviceIds();
        JSONArray jsonArray = new JSONArray();
        for (String id : list) {
            jsonArray.put(id);
        }
        jsonObject.put("deviceIds", jsonArray);

        filters.put(jsonObject);

        return this;
    }

    /**
     * Add filter to Content Notification to specify operating system of devices to which notification should be delivered.
     *
     * @param filter Filter to add
     * @return Content Notification with added requested filter.
     * @throws JSONException
     */
    public ContentNotification addFilter(OperatingSystemFilter filter) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", filter.getType());

        List<String> list = filter.getOperatingSystems();
        JSONArray jsonArray = new JSONArray();
        for (String os : list) {
            jsonArray.put(os);
        }
        jsonObject.put("operatingSystems", jsonArray);

        filters.put(jsonObject);

        return this;
    }

    /**
     * Add Audience to Content Notification to request this notification to be send to all users.
     * @param audience Audience to add.
     * @return Content Notification with added requested filter.
     * @throws JSONException
     */
    public ContentNotification putAudience(AllUsersAudience audience) throws JSONException {

        this.audience = new JSONObject();
        this.audience.put("type", audience.getType());

        return this;
    }

    /**
     * Add Audience to Content Notification to request this notification to be send specified users.
     *
     * @param audience Audience to add
     * @return Content Notification with added requested filter.
     * @throws JSONException
     */
    public ContentNotification putAudience(SpecifiedUsersAudience audience) throws JSONException {

        this.audience = new JSONObject();
        this.audience.put("type", audience.getType());

        List<AudienceMember> list = audience.getUsers();
        JSONArray jsonArray = new JSONArray();
        for (AudienceMember member : list) {
            JSONObject obj = new JSONObject();
            obj.put("userId", member.getUserId());
            obj.put("templateData", member.getTemplateData());
            jsonArray.put(obj);
        }
        this.audience.put("users", jsonArray);


        return this;
    }

    /**
     * Describe content of notification that will be send by the network to specified users/devices.
     *
     * @param content Content of notification that will be send by the network.
     * @return Content Notification with added requested filter.
     * @throws JSONException
     */
    public ContentNotification putContent(NotificationContent content) throws JSONException {

        this.content = new JSONObject();
        this.content.put("type", content.getType());
        this.content.put("customType", content.getCustomType());
        this.content.put("data", content.getData());

        setBaseNotificationType(content.getCustomType());

        return this;
    }

    /**
     * Get json string constructed from provided content notification elements.
     *
     * @return Json string constructed from provided content notification elements.
     */
    public String getJsonString() {
        return getJson().toString();
    }

    /**
     * Get Json object representing this content notification.
     *
     * @return Json object representing this content notification.
     */
    public JSONObject getJson() {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();

            JSONObject jsonObjectDefinition = new JSONObject();

            jsonObjectDefinition.put("audience", audience);
            jsonObjectDefinition.put("filters", filters);
            jsonObjectDefinition.put("content", content);

            jsonObject.put("type","SendContent");
            jsonObject.put("definition",jsonObjectDefinition);

        } catch (JSONException e) {
            DLog log = new DLog("ContentNotification");
            log.error("Error parsing ContentNotification", e);
        }


        return jsonObject;
    }
}
