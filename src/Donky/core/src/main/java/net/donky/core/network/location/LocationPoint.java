package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocationPoint implements Serializable {

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;


    public LocationPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "LocationPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
