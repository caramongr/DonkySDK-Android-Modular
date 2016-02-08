package net.donky.location;

/**
 * Created by Marcin Swierczek
 * 26/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class TargetUser {

    private String externalUserId;

    private String networkProfileId;

    private String deviceId;

    private TargetUser() {
    }

    public static TargetUser getTargetUserByExternalId(String externalUserId) {
        TargetUser targetUser = new TargetUser();
        targetUser.networkProfileId = null;
        targetUser.externalUserId = externalUserId;
        return targetUser;
    }

    public static TargetUser getTargetUserByProfileId(String networkProfileId) {
        TargetUser targetUser = new TargetUser();
        targetUser.externalUserId = null;
        targetUser.networkProfileId = networkProfileId;
        return targetUser;
    }

    public static TargetUser getTargetUserByExternalId(String externalUserId, String deviceId) {
        TargetUser targetUser = new TargetUser();
        targetUser.networkProfileId = null;
        targetUser.externalUserId = externalUserId;
        targetUser.deviceId = deviceId;
        return targetUser;
    }

    public static TargetUser getTargetUserByProfileId(String networkProfileId, String deviceId) {
        TargetUser targetUser = new TargetUser();
        targetUser.externalUserId = null;
        targetUser.networkProfileId = networkProfileId;
        targetUser.deviceId = deviceId;
        return targetUser;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public String getNetworkProfileId() {
        return networkProfileId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
