package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Validity implements Serializable {

    @SerializedName("validFrom")
    private String validFrom;

    @SerializedName("actionData")
    private String validTo;

    public Validity(String validFrom, String validTo) {
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    @Override
    public String toString() {
        return "Validity{" +
                "validFrom='" + validFrom + '\'' +
                ", validTo='" + validTo + '\'' +
                '}';
    }
}
