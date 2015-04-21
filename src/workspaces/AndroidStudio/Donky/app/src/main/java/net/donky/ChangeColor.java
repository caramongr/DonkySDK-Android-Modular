package net.donky;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marcin.swierczek on 13/03/2015.
 */
public class ChangeColor {

    @SerializedName("customType")
    private String customType;

    @SerializedName("customData")
    private CustomData customData;

    public class CustomData {

        @SerializedName("newColour")
        private String newColour;

        @SerializedName("intervalSeconds")
        private int intervalSeconds;

        public String getNewColour() {
            return newColour;
        }

        public int getIntervalSeconds() {
            return intervalSeconds;
        }

        public CustomData setIntervalSeconds(int intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
            return this;
        }

        public CustomData setNewColour(String newColour) {
            this.newColour = newColour;
            return this;
        }

    }

    public CustomData getCustomData() {
        return customData;
    }

}

