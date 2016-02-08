package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

import net.donky.core.network.location.GeoFence;

import java.io.Serializable;
import java.util.List;

public class TriggerData implements Serializable{

    @SerializedName("regions")
    private List<GeoFence> regions;

    @SerializedName("condition")
    private String condition;

    @SerializedName("direction")
    private String direction;

    @SerializedName("timeInRegionSeconds")
    private int timeInRegionSeconds;

    public TriggerData(List<GeoFence> regions, String condition, String direction, int timeInRegionSeconds) {
        this.regions = regions;
        this.condition = condition;
        this.direction = direction;
        this.timeInRegionSeconds = timeInRegionSeconds;
    }

    public  List<GeoFence> getRegions() {
        return regions;
    }

    public void setRegions( List<GeoFence> regions) {
        this.regions = regions;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getTimeInRegionSeconds() {
        return timeInRegionSeconds;
    }

    public void setTimeInRegionSeconds(int timeInRegionSeconds) {
        this.timeInRegionSeconds = timeInRegionSeconds;
    }



    @Override
    public String toString() {
        return "TriggerData{" +
                "regions=" + regions +
                ", condition='" + condition + '\'' +
                ", direction='" + direction + '\'' +
                ", timeInRegionSeconds=" + timeInRegionSeconds +
                '}';
    }
}
