package net.donky.core.network;

import com.google.gson.Gson;

import net.donky.core.DonkyException;
import net.donky.core.Notification;
import net.donky.core.helpers.IdHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents client notification send by Core SDK to the Donky Network when synchronising.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ClientNotification  extends Notification {

    /**
     * Client Notifications result.
     */
    public enum Type {

        Acknowledgement;

        public boolean equals(String type) {
            return this.toString().equals(type);
        }
    }

    /**
     * The Json representation of client notification.
     */
    protected JSONObject data;

    /**
     * Details of a notification being acknowledged
     */
    private AcknowledgementDetail acknowledgementDetail;

    protected ClientNotification(String type, String id){
        super(type, id);
    }

    /**
     * @return The detail for a notification being acknowledged
     */
    public AcknowledgementDetail getAcknowledgementDetail() {
        return acknowledgementDetail;
    }

    /**
     * Create Acknowledgement Client Notification
     *
     * @param serverNotification {@link ServerNotification} to be acknowledged
     * @param customType The type of the custom notification.
     * @param isDeliveredToSubscriber Is delivered to any subscribed module.
     * @return Instance of Client Notification that is an acknowledgement of provided server notification.
     */
    public static ClientNotification createAcknowledgment(ServerNotification serverNotification, String customType, boolean isDeliveredToSubscriber) {

        // Create acknowledgement

        AcknowledgementDetail acknowledgementDetail = new AcknowledgementDetail();

        acknowledgementDetail.setServerNotificationId(serverNotification.getId());

        acknowledgementDetail.setType(serverNotification.getType());

        if (isDeliveredToSubscriber) {
            acknowledgementDetail.setResult(AcknowledgementDetail.Result.Delivered.toString());
        } else {
            acknowledgementDetail.setResult(AcknowledgementDetail.Result.DeliveredNoSubscription.toString());
        }

        acknowledgementDetail.setCustomNotificationType(customType);

        acknowledgementDetail.setSentTime(serverNotification.getCreatedOn());

        ClientNotification clientNotification = new ClientNotification(Type.Acknowledgement.toString(), IdHelper.generateId());

        // Create json data for acknowledgement

        JSONObject jsonObject = new JSONObject();

        Gson gson = new Gson();

        try {
            jsonObject.put("type", Type.Acknowledgement.toString());
            jsonObject.put("acknowledgementDetail", new JSONObject(gson.toJson(acknowledgementDetail)));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        clientNotification.data = jsonObject;

        clientNotification.acknowledgementDetail = acknowledgementDetail;

        return clientNotification;

    }

    /**
     * Create {@link ClientNotification}
     *
     * @param acknowledgementDetail Acknowledgement detail to be send with client notification.
     * @return Client notification of type Acknowledgement
     */
    public static ClientNotification createClientNotification(String type, String id, AcknowledgementDetail acknowledgementDetail, String json) {


        ClientNotification clientNotification = new ClientNotification(type, id);

        try {

            clientNotification.data = new JSONObject(json);

        } catch (JSONException e) {

            DonkyException exception = new DonkyException("Error converting json string.");
            exception.initCause(e);

        }

        clientNotification.acknowledgementDetail = acknowledgementDetail;

        return clientNotification;

    }

    /**
     * Get json string for this Client Notification
     *
     * @return Json string
     */
    public String getJsonString() {
        return data.toString();
    }

    public JSONObject getJson() {
        return data;
    }

}
