package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

public class GeoFence implements Serializable {

    @SerializedName("applicationId")
    private String applicationId;

    @SerializedName("name")
    private String name;

    @SerializedName("centrePoint")
    private LocationPoint centrePoint;

    @SerializedName("radiusMetres")
    private int radiusMetres;

    @SerializedName("labels")
    private String[] labels;

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("createdOn")
    private String createdOn;

    @SerializedName("updatedOn")
    private String updatedOn;

    @SerializedName("activationId")
    private String activationId;

    @SerializedName("activatedOn")
    private String activatedOn;

    @SerializedName("relatedTriggers")
    private Trigger[] relatedTriggers;

    private String hash;

    private String internalId;

    private int isActivate;

    private int relatedTriggersCount;
    
    private double distanceToBorder;

    private double realDistanceToBorder;

    private boolean isInside;

    private long lastEnter;

    private long lastExit;

    public GeoFence(){}

    public GeoFence(String applicationId, String name, LocationPoint centrePoint, int radiusMetres, String[] labels, String type, String id, String status, String createdOn, String updatedOn, String activationId, String activatedOn, String hash, String internalId, int isActivate, int relatedTriggersCount, double distanceToBorder, int isInside, double realDistanceToBorder, long lastEnter, long lastExit) {
        this.applicationId = applicationId;
        this.name = name;
        this.centrePoint = centrePoint;
        this.radiusMetres = radiusMetres;
        this.labels = labels;
        this.type = type;
        this.id = id;
        this.status = status;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.activationId = activationId;
        this.activatedOn = activatedOn;
        this.hash = hash;
        this.internalId = internalId;
        this.isActivate = isActivate;
        this.relatedTriggersCount = relatedTriggersCount;
        this.distanceToBorder = distanceToBorder;
        this.realDistanceToBorder = realDistanceToBorder;
        this.isInside = isInside == 1;
        this.lastEnter = lastEnter;
        this.lastExit = lastExit;
    }

    public void setDistanceToBorder(double distanceToBorder) {
        this.distanceToBorder = distanceToBorder;
    }

    public void setRealDistanceToBorder(double realDistanceToBorder) {
        this.realDistanceToBorder = realDistanceToBorder;
    }

    public long getLastEnter() {
        return lastEnter;
    }

    public void setLastEnter(long lastEnter) {
        this.lastEnter = lastEnter;
    }

    public long getLastExit() {
        return lastExit;
    }

    public void setLastExit(long lastExit) {
        this.lastExit = lastExit;
    }

    public double getRealDistanceToBorder() {
        return realDistanceToBorder;
    }

    public void setRealDistanceToBorder(float realDistanceToBorder) {
        this.realDistanceToBorder = realDistanceToBorder;
    }

    public double getDistanceToBorder() {
        return distanceToBorder;
    }

    public void setDistanceToBorder(float distanceToBorder) {
        this.distanceToBorder = distanceToBorder;
    }

    public int getRelatedTriggersCount() {
        return relatedTriggersCount;
    }

    public Trigger[] getRelatedTriggers() {
        return relatedTriggers;
    }

    public void setRelatedTriggers(Trigger[] relatedTriggers) {
        this.relatedTriggers = relatedTriggers;
    }

    public void setRelatedTriggersCount(int relatedTriggersCount) {
        this.relatedTriggersCount = relatedTriggersCount;
    }

    public String getActivatedOn() {
        return activatedOn;
    }

    public void setActivatedOn(String activatedOn) {
        this.activatedOn = activatedOn;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationPoint getCentrePoint() {
        return centrePoint;
    }

    public void setCentrePoint(LocationPoint centrePoint) {
        this.centrePoint = centrePoint;
    }

    public int getRadiusMetres() {
        return radiusMetres;
    }

    public void setRadiusMetres(int radiusMetres) {
        this.radiusMetres = radiusMetres;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public boolean getIsActivate() {
        return isActivate == 1;
    }

    public void setIsActivate(int isActivate) {
        this.isActivate = isActivate;
    }

    public boolean isInside() {
        return isInside;
    }

    public void setIsInside(boolean isInside) {
        this.isInside = isInside;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GeoFence{");
        sb.append("applicationId='").append(applicationId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", centrePoint=").append(centrePoint);
        sb.append(", radiusMetres=").append(radiusMetres);
        sb.append(", labels=").append(labels == null ? "null" : Arrays.asList(labels).toString());
        sb.append(", type='").append(type).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", createdOn='").append(createdOn).append('\'');
        sb.append(", updatedOn='").append(updatedOn).append('\'');
        sb.append(", activationId='").append(activationId).append('\'');
        sb.append(", activatedOn='").append(activatedOn).append('\'');
        sb.append(", relatedTriggers=").append(relatedTriggers == null ? "null" : Arrays.asList(relatedTriggers).toString());
        sb.append(", hash='").append(hash).append('\'');
        sb.append(", internalId='").append(internalId).append('\'');
        sb.append(", isActivate=").append(isActivate);
        sb.append(", relatedTriggersCount=").append(relatedTriggersCount);
        sb.append(", distanceToBorder=").append(distanceToBorder);
        sb.append(", realDistanceToBorder=").append(realDistanceToBorder);
        sb.append(", isInside=").append(isInside);
        sb.append('}');
        return sb.toString();
    }
}
