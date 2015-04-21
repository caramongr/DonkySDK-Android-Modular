package net.donky.core.network.restapi.secured;

import net.donky.core.DonkyException;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

/**
 * Network request to synchronise the notifications.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Synchronise extends GenericSecuredServiceRequest<SynchroniseResponse> {

    private TypedInput typedInput;

    private final List<ClientNotification> clientNotifications;

    private DLog log = new DLog("SendContentRequest");

    public Synchronise() {
        super();

        clientNotifications = new LinkedList<>();

        clientNotifications.addAll(DonkyDataController.getInstance().getNotificationDAO().getNotifications());

        try {

            JSONObject jsonObjectSynchronise = new JSONObject();

            JSONArray jsonArray = new JSONArray();

            for (ClientNotification notification : clientNotifications) {

                jsonArray.put(notification.getJson());

            }

            jsonObjectSynchronise.put("clientNotifications",jsonArray);

            jsonObjectSynchronise.put("isBackground",true);

            typedInput = new TypedByteArray("application/json", jsonObjectSynchronise.toString().getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {

            DonkyException exception = new DonkyException("Error converting json string.");
            exception.initCause(e);

            log.error("Error converting json string.", e);

        } catch (JSONException e) {

            DonkyException exception = new DonkyException("Error converting json string.");
            exception.initCause(e);

            log.error("Error converting json string.", e);

        }
    }

    public List<ClientNotification> getClientNotifications() {
        return clientNotifications;
    }

    @Override
    public String toString() {

        String divider = " | ";

        StringBuilder sb = new StringBuilder();
        sb.append("SYNCHRONISE: ");
        sb.append(divider);
        if (clientNotifications != null) {
            for (ClientNotification notification : clientNotifications) {
                sb.append(notification);
                sb.append(divider);
            }
        }
        return sb.toString();
    }

    @Override
    protected SynchroniseResponse doSynchronousCall(String apiKey) {
        return RestClient.getAPI().synchronise(apiKey, typedInput);
    }

    @Override
    protected void doAsynchronousCall(String authorization, final NetworkResultListener<SynchroniseResponse> listener) {

        RestClient.getAPI().synchronise(authorization, typedInput, new Callback<SynchroniseResponse>() {

            @Override
            public void success(SynchroniseResponse synchroniseResponse, retrofit.client.Response response) {
                listener.success(synchroniseResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                listener.onFailure(error);
            }
        });
    }

    @Override
    protected void doStartListenForConnectionRestored() {

        if (DonkyAccountController.getInstance().isRegistered()) {
            startUniqueListener();
        }

    }

    @Override
    public void onConnected() {

        synchronized (sharedLock) {
            stopUniqueListener();
            sharedLock.notifyAll();
        }

        DonkyNetworkController.getInstance().synchronise();

    }
}
