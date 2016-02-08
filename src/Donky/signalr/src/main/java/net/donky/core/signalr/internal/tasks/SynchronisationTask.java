package net.donky.core.signalr.internal.tasks;

import net.donky.core.DonkyException;
import net.donky.core.DonkyResultListener;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.restapi.secured.SynchroniseResponse;
import net.donky.core.signalr.internal.HubConnectionFactory;
import net.donky.core.signalr.internal.helpers.JsonParsingHelper;

import java.util.List;

import donky.microsoft.aspnet.signalr.client.Action;
import donky.microsoft.aspnet.signalr.client.ErrorCallback;
import donky.microsoft.aspnet.signalr.client.SignalRFuture;

/**
 * Current synchronisation task performed by SignalR Module.
 *
 * Created by Marcin Swierczek
 * 23/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SynchronisationTask extends SignalRTask<SynchroniseResponse> {

    List<ClientNotification> clientNotificationsToSend;

    public SynchronisationTask(final HubConnectionFactory hubConnectionFactory, final List<ClientNotification> clientNotificationsToSend, final DonkyResultListener<SynchroniseResponse> resultListener) {
        super(hubConnectionFactory, resultListener);
        this.clientNotificationsToSend = clientNotificationsToSend;
    }

    @Override
    public void performTask() {

        try {

            Object[] arrayCombined = new Object[1];

            Object[] arrayClient = new Object[clientNotificationsToSend.size()];

            for (int i = 0; i < arrayClient.length; i++) {

                arrayClient[i] = JsonParsingHelper.convertJSONObjectToMap(clientNotificationsToSend.get(i).getJson());

            }

            arrayCombined[0] = arrayClient;

            SignalRFuture<SynchroniseResponse> signalRFuture =
                    hubConnectionFactory.
                            getHubProxy().
                            invoke(SynchroniseResponse.class, "synchronise", arrayCombined);

            signalRFuture.done(new Action<SynchroniseResponse>() {

                @Override
                public void run(final SynchroniseResponse response) throws Exception {
                    notifyTaskSuccessful(response);
                }

            });

            signalRFuture.onError(new ErrorCallback() {

                @Override
                public void onError(Throwable error) {
                    final DonkyException donkyException = new DonkyException("SignalR synchronisation error");
                    donkyException.initCause(error);
                    notifyTaskFailed(donkyException, null);
                }

            });

        } catch (final Exception exception) {

            final DonkyException donkyException = new DonkyException("Error invoking synchronise with signalR");
            donkyException.initCause(exception);
            notifyTaskFailed(donkyException, null);

        }

    }

}
