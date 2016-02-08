package net.donky.location;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.logging.DLog;
import net.donky.core.model.AbstractLastLocation;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.DonkyNetworkController;
import net.donky.location.internal.LocationClientNotification;
import net.donky.location.internal.LocationUpdateTimer;
import net.donky.location.internal.LocationUpdatesCallback;
import net.donky.location.internal.PollingProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Marcin Swierczek
 * 19/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyLocationController extends AbstractLastLocation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private DLog log;

    private Context context;

    private GoogleApiClient googleApiClient;

    private PollingProfile pollingProfile;

    private ConcurrentLinkedQueue<LocationUpdatesCallback> locationUpdateListeners;

    private ConcurrentLinkedQueue<Pair<LocationRequest, LocationUpdatesCallback>> customLocationUpdateListeners;

    private LocationUpdateTimer timer;

    private boolean shouldAutomaticallySendLocation = false;

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyLocationController INSTANCE = new DonkyLocationController();
    }

    /**
     * @return Instance of Network Controller singleton.
     */
    public static DonkyLocationController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context context){
        this.context = context;
        this.googleApiClient = getLocationAPI();
        this.log = new DLog("LocationController");
        this.pollingProfile = new PollingProfile();
        this.locationUpdateListeners = new ConcurrentLinkedQueue <>();
        this.customLocationUpdateListeners = new ConcurrentLinkedQueue <>();
        this.timer = new LocationUpdateTimer(context);
    }

    private DonkyLocationController() {
    }

    public void setPollingProfile(int interval, int fastestInterval, int priority, int distanceTo) {
        pollingProfile = new PollingProfile(interval, fastestInterval, priority, distanceTo);
    }

    private GoogleApiClient getLocationAPI() {
        if (googleApiClient == null) {
            Log.d(DonkyLocation.TAG, "Initialising Location API.");
            return buildGoogleApiClient();
        }
        return googleApiClient;
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(pollingProfile.getInterval());
        locationRequest.setFastestInterval(pollingProfile.getFastestInterval());
        locationRequest.setPriority(pollingProfile.getPriority());
        locationRequest.setSmallestDisplacement(pollingProfile.getSmallestDisplacement());
        return locationRequest;
    }

    /**
     * Get last user location
     *
     * @return Location object what contain all location data
     */
    public Location getLastLocation() {
        if (!checkLocationServicePermission())
            return null;

        if (!getLocationAPI().isConnected() && !getLocationAPI().isConnecting()) {
            Log.d(DonkyLocation.TAG, "connect API.");
            getLocationAPI().blockingConnect();
        }

        Log.d(DonkyLocation.TAG, "FusedLocationApi.");
        return LocationServices.FusedLocationApi.getLastLocation(getLocationAPI());
    }

    /**
     * Register location with default Location request
     *
     * @param callback to receive onLocationChange data
     */
    public void registerLocationListener(LocationUpdatesCallback callback) {
        locationUpdateListeners.add(callback);
        if (!getLocationAPI().isConnected() && !getLocationAPI().isConnecting()) {
            getLocationAPI().connect();
            return;
        }
        Location location = getLastLocation();
        if (location != null) {
            onLocationChanged(location);
        }
    }

    /**
     * Register with specific Location request
     *
     * @param locationRequest Location request to start tracking position
     * @param callback        to receive onLocationChange data
     */
    public void registerLocationListener(LocationUpdatesCallback callback, LocationRequest locationRequest) {
        Pair<LocationRequest, LocationUpdatesCallback> pair = new Pair<>(locationRequest, callback);
        customLocationUpdateListeners.add(pair);
        if (!getLocationAPI().isConnected() && !getLocationAPI().isConnecting()) {
            getLocationAPI().connect();
            return;
        }
        onLocationChanged(getLastLocation());
    }

    /**
     * Unregister from tracking location
     *
     * @param callback to remove from observers
     */
    public void unregisterLocationListener(LocationUpdatesCallback callback) {
        if (!googleApiClient.isConnected()) {
            return;
        }

        if (locationUpdateListeners.contains(callback)) {
            locationUpdateListeners.remove(callback);
        } else if (customLocationUpdateListeners.contains(callback)) {
            customLocationUpdateListeners.remove(callback);
            LocationServices.FusedLocationApi.removeLocationUpdates(getLocationAPI(), callback);
        }
        if (locationUpdateListeners.isEmpty() && customLocationUpdateListeners.isEmpty()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(getLocationAPI(), this);
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DonkyLocation.TAG, "Location services connected.");
        if (!checkLocationServicePermission()) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(getLocationAPI(), createLocationRequest(), this);
        for (LocationUpdatesCallback callback : locationUpdateListeners) {
            callback.onConnected(bundle);
        }
        for (Pair<LocationRequest, LocationUpdatesCallback> pair : customLocationUpdateListeners) {
            LocationServices.FusedLocationApi.requestLocationUpdates(getLocationAPI(), pair.first, pair.second);
            pair.second.onConnected(bundle);
        }
        onLocationChanged(getLastLocation());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(DonkyLocation.TAG, "onConnectionSuspended");
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.d(DonkyLocation.TAG, "Location services suspended: connection lost");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.d(DonkyLocation.TAG, "Location services suspended: disconnected");
        } else {
            Log.d(DonkyLocation.TAG, "Location services suspended.");
        }
        for (LocationUpdatesCallback callback : locationUpdateListeners) {
            callback.onConnectionSuspended(i);
        }
        for (Pair<LocationRequest, LocationUpdatesCallback> pair : customLocationUpdateListeners) {
            pair.second.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(DonkyLocation.TAG, "Location services connection failed. " + connectionResult);
        for (LocationUpdatesCallback callback : locationUpdateListeners) {
            callback.onConnectionFailed(connectionResult);
        }
        for (Pair<LocationRequest, LocationUpdatesCallback> pair : customLocationUpdateListeners) {
            pair.second.onConnectionFailed(connectionResult);
        }
    }

    /**
     * Update observer about new data of location
     *
     * @param location updated user's location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null && locationUpdateListeners != null && !locationUpdateListeners.isEmpty()) {
            for (LocationUpdatesCallback callback : locationUpdateListeners)
                callback.onLocationChanged(location);
        }
    }

    /**
     * Request location details from another user device.
     * To construct valid Target user object use one of below methods:
     * {@link TargetUser#getTargetUserByExternalId(String)}
     * {@link TargetUser#getTargetUserByExternalId(String, String)}
     * {@link TargetUser#getTargetUserByProfileId(String, String)}
     * {@link TargetUser#getTargetUserByProfileId(String)}
     *
     * @param targetUser Which user on the App Space should be asked to send you his location
     * @param donkyListener Callback for completing synchronisation
     */
    public void requestUserLocation(TargetUser targetUser, DonkyListener donkyListener) {
        DonkyNetworkController.getInstance().sendClientNotification(LocationClientNotification.createRequestLocationNotification(targetUser.getExternalUserId(), targetUser.getNetworkProfileId(), targetUser.getDeviceId()), donkyListener);
    }

    /**
     * Update device location on the Donky Network
     *
     * @param donkyListener Callback for completing synchronisation
     */
    public void sendLocationUpdate(final DonkyListener donkyListener) {
        sendLocationUpdateToUser(null, donkyListener);
    }

    /**
     * Send your current location to another user on the Donky Network
     *
     * @param targetUser Which user on the App Space should be notified about your current location
     * @param donkyListener Callback for completing synchronisation
     */
    public void sendLocationUpdateToUser(final TargetUser targetUser, final DonkyListener donkyListener) {

        if (isLocationEnabled(context)) {

            registerLocationListener(new LocationUpdatesCallback() {

                @Override
                public void onConnected(Bundle bundle) {

                }

                @Override
                public void onLocationChanged(Location location) {

                    if (location != null) {

                        unregisterLocationListener(this);

                        ClientNotification notification;

                        if (targetUser != null) {
                            notification = LocationClientNotification.createLocationUpdateNotification(location.getLatitude(), location.getLongitude(), targetUser.getExternalUserId(), targetUser.getNetworkProfileId());
                        } else {
                            notification = LocationClientNotification.createLocationUpdateNotification(location.getLatitude(), location.getLongitude());
                        }

                        DonkyNetworkController.getInstance().sendClientNotification(notification, new DonkyListener() {

                                    @Override
                                    public void success() {
                                        if (donkyListener != null) {
                                            donkyListener.success();
                                        }
                                    }

                                    @Override
                                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                        if (donkyListener != null) {
                                            donkyListener.error(donkyException, validationErrors);
                                        }
                                    }

                                }
                        );

                    } else {
                        log.debug("onLocationChanged with low accuracy. Waiting for better one.");
                    }

                }

                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    super.onConnectionFailed(connectionResult);
                    unregisterLocationListener(this);
                    if (donkyListener != null) {
                        donkyListener.error(new DonkyException("Location Manager Connection failed with result " + connectionResult.getErrorCode()), null);
                    }
                }

                @Override
                public void onConnectionSuspended(int cause) {
                    super.onConnectionSuspended(cause);
                    unregisterLocationListener(this);
                    if (donkyListener != null) {
                        donkyListener.error(new DonkyException("Location Manager Connection suspended with cause " + cause), null);
                    }
                }

            });

        } else if (donkyListener != null) {
            donkyListener.error(new DonkyException("Location has not been enabled on the phone."), null);
        }
    }



    public static boolean checkGooglePlayServices(final Activity activity) {

        final int googlePlayServicesCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);

        switch (googlePlayServicesCheck) {

            case ConnectionResult.SUCCESS:
                return true;

            default:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, activity, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        activity.finish();
                    }
                });
                dialog.show();

        }

        return false;
    }

    private boolean checkLocationServicePermission(){
        if (!isLocationEnabled(context)) {
            Log.e(DonkyLocation.TAG, "Location Service is not available");
            return false;
        }
        return isPermissionGranted();
    }

    private boolean isPermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add("ACCESS_FINE_LOCATION");
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
                permissionsNeeded.add("ACCESS_COARSE_LOCATION");
            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    for (String permission : permissionsNeeded) {
                        Log.w(DonkyLocation.TAG, "You need to grant access to " + permission);
                    }
                }
                return false;
            }
            return true;
        }
        return true;
    }

    @TargetApi(23)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            return false;
        }
        return true;
    }

    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }

    /**
     * Handle if developer forgot to unregister locationUpdateListeners of location
     */
    public void appStopped() {
        if (locationUpdateListeners != null && locationUpdateListeners.size() > 0) {
            locationUpdateListeners.clear();
        }

        if (customLocationUpdateListeners != null && customLocationUpdateListeners.size() > 0) {
            customLocationUpdateListeners.clear();
        }

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public boolean isShouldAutomaticallySendLocation() {
        return shouldAutomaticallySendLocation;
    }

    public void setShouldAutomaticallySendLocation(boolean shouldAutomaticallySendLocation) {
        this.shouldAutomaticallySendLocation = shouldAutomaticallySendLocation;
    }

    void startAutomaticLocationUpdatesTimer() {
        if (timer != null) {
            timer.startTimer();
        }
    }

    void stopAutomaticLocationUpdatesTimer() {
        if (timer != null) {
            timer.stopTimer();
        }
    }
}