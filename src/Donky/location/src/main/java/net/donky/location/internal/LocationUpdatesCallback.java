package net.donky.location.internal;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

/**
 * Created by Marcin Swierczek
 * 19/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class LocationUpdatesCallback implements LocationListener,
                                                         GoogleApiClient.ConnectionCallbacks,
                                                         GoogleApiClient.OnConnectionFailedListener {

    /**
     * Called when the client is temporarily in a disconnected state. If you want to track this callback
     * Override this method
     * @param cause The reason for the disconnection. Defined by constants CAUSE_*
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /**
     * Called when there was an error connecting the client to the service. If you want to track this callback
     * Override this method
     * @param connectionResult A ConnectionResult that can be used for resolving the error, and deciding what sort of error occurred.
     *                         To resolve the error, the resolution must be started from an activity with a non-negative requestCode
     *                         passed to startResolutionForResult(Activity, int).Applications should implement onActivityResult in their
     *                         Activity to call connect() again if the user has resolved the issue (resultCode is RESULT_OK).
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}