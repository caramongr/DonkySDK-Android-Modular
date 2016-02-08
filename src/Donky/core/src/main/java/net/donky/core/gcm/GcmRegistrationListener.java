package net.donky.core.gcm;

import net.donky.core.DonkyException;

/**
 * Listener used for GCM registration.
 *
 * Created by Marcin Swierczek
 * 23/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
interface GcmRegistrationListener {

    /**
     * Command has executed successfully.
     * @param gcmRegistrationId GCM registration id.
     */
    public void success(String gcmRegistrationId);

    /**
     * Command has failed.
     * @param exception Thrown exception.
     */
    public void failed(DonkyException exception);

    /**
     * GCM was not available so command couldn't be executed.
     * @param googlePlayServicesUtilResultCode Result code defined as in {@link com.google.android.gms.common.ConnectionResult}
     */
    public void googlePlayServicesNotAvailable(int googlePlayServicesUtilResultCode);

}