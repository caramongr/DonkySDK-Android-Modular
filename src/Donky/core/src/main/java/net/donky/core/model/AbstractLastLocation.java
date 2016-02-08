package net.donky.core.model;

import android.location.Location;
/**
 * Interface for location module that are registered in DonkyCore as Service and need to be included in analytics
 *
 * Created by Igor Bykov
 * 11/08/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class AbstractLastLocation {

    public static final String SERVICE_CATEGORY_LOCATION = "Location";


    public abstract Location getLastLocation();
}
