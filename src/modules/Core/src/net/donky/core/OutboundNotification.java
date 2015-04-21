package net.donky.core;

import net.donky.core.network.AcknowledgementDetail;
import net.donky.core.network.ClientNotification;

import org.json.JSONObject;

/**
 * Represents information about outbound notification.
 *
 * Created by Marcin Swierczek
 * 03/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class OutboundNotification extends Notification {

    /**
     * Acknowledgement Detail of Client Notification  notification.
     */
    private final AcknowledgementDetail acknowledgementDetail;

    /**
     * Json data for Notification.
     */
    private final JSONObject jsonData;

    /**
     * Constructor for outbound notification from {@link net.donky.core.network.ClientNotification}
     *
     * @param clientNotification Client notification as data source.
     */
    public OutboundNotification(ClientNotification clientNotification) {
        super(clientNotification.getBaseNotificationType(), clientNotification.getId());
        this.acknowledgementDetail = clientNotification.getAcknowledgementDetail();
        this.jsonData = clientNotification.getJson();
    }

    /**
     * Get Acknowledgement Detail of Client Notification.
     *
     * @return Acknowledgement Detail of Client Notification.
     */
    public AcknowledgementDetail getAcknowledgementDetail() {
        return acknowledgementDetail;
    }

    /**
     * Get Json data of notification.
     *
     * @return Json data of notification.
     */
    public JSONObject getJsonData() {
        return jsonData;
    }
}
