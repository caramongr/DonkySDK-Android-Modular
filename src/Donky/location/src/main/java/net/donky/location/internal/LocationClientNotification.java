package net.donky.location.internal;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.location.LocationPoint;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationClientNotification extends ClientNotification {

	/**
	 * Client Notifications types.
	 */
	public enum LocationNotificationType {
		RequestLocation,
		LocationUpdate
	}

	protected LocationClientNotification(String type, String id) {
		super(type, id);
	}

	/**
	 * Create a notification to request location of a user with a given network profile id
	 *
     * @param externalUserId Requester user id.
	 * @param networkProfileId Network profile id of user to ask for a location.
     * @param deviceId Device id for requested location.
	 * @return 'Location Crossed' Client Notification
	 */
	public static ClientNotification createRequestLocationNotification(String externalUserId, String networkProfileId, String deviceId) {

		LocationClientNotification n = new LocationClientNotification(LocationNotificationType.RequestLocation.toString(), IdHelper.generateId());

		Gson gson = new Gson();

		try {

            if (deviceId != null) {
                n.data = new JSONObject(gson.toJson(createLocationRequest(externalUserId, networkProfileId, deviceId)));
            } else {
                n.data = new JSONObject(gson.toJson(createLocationRequest(externalUserId, networkProfileId)));
            }

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return n;
	}

	public static ClientNotification createLocationUpdateNotification(double latitude, double longitude) {

		LocationClientNotification n = new LocationClientNotification(LocationNotificationType.LocationUpdate.toString(), IdHelper.generateId());

		Gson gson = new Gson();

		try {

			n.data = new JSONObject(gson.toJson(createLocationUpdate(latitude, longitude)));

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return n;
	}

    public static ClientNotification createLocationUpdateNotification(double latitude, double longitude, String externalUserId, String networkProfileId) {

        LocationClientNotification n = new LocationClientNotification(LocationNotificationType.LocationUpdate.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createLocationUpdate(latitude, longitude, externalUserId, networkProfileId)));

        } catch (JSONException e) {

            e.printStackTrace();

        }

        return n;
    }

	private static RequestLocation createLocationRequest(String externalUserId, String networkProfileId) {
        RequestLocation requestLocation = new RequestLocation();
        requestLocation.targetUser = new TargetUser(externalUserId, networkProfileId);
        requestLocation.type = LocationNotificationType.RequestLocation.toString();
		return requestLocation;
	}

    private static RequestLocation createLocationRequest(String externalUserId, String networkProfileId, String targetDeviceId) {
        RequestLocation requestLocation = new RequestLocation();
        requestLocation.type = LocationNotificationType.RequestLocation.toString();
        requestLocation.targetUser = new TargetUser(externalUserId, networkProfileId);
        requestLocation.targetDeviceId = targetDeviceId;
        return requestLocation;
    }

	private static LocationUpdate createLocationUpdate(double latitude, double longitude, String externalId, String networkProfileId) {
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.type = LocationNotificationType.LocationUpdate.toString();
        locationUpdate.location = new LocationPoint(latitude, longitude);
        locationUpdate.notifyUser = new TargetUser(externalId, networkProfileId);
        locationUpdate.timestamp = DateAndTimeHelper.getCurrentUTCTime();
		return locationUpdate;
	}

    private static LocationUpdate createLocationUpdate(double latitude, double longitude) {
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.type = LocationNotificationType.LocationUpdate.toString();
        locationUpdate.location = new LocationPoint(latitude, longitude);
        locationUpdate.timestamp = DateAndTimeHelper.getCurrentUTCTime();
        return locationUpdate;
    }

	private static class LocationUpdate {
        @SerializedName("type")
        private String type;
		@SerializedName("notifyUser")
		private TargetUser notifyUser;
		@SerializedName("location")
		private LocationPoint location;
		@SerializedName("timestamp")
		private String timestamp;
	}

    private static class TargetUser {
        @SerializedName("userId")
        private String userId;
        @SerializedName("networkProfileId")
        private String networkProfileId;

        public TargetUser(String userId, String networkProfileId) {
            this.userId = userId;
            this.networkProfileId = networkProfileId;
        }
    }

	private static class RequestLocation {
        @SerializedName("type")
        private String type;
		@SerializedName("targetUser")
		private TargetUser targetUser;
        @SerializedName("targetDeviceId")
        private String targetDeviceId;
	}

}