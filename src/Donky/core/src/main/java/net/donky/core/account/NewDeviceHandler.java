package net.donky.core.account;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyCore;
import net.donky.core.R;
import net.donky.core.events.NewDeviceEvent;
import net.donky.core.logging.DLog;
import net.donky.core.network.ServerNotification;
import net.donky.core.settings.AppSettings;

import java.util.List;
import java.util.Random;

/**
 * Class that process new device registration (against the same user account) notifications and displays a warning message in notification center.
 *
 * Created by Marcin Swierczek
 * 28/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NewDeviceHandler {

    private Context context;

    public static final String newDeviceModelTag = "DeviceModel";

    public static final String newDeviceOperatingSystemTag = "DeviceOperatingSystem";

    public NewDeviceHandler(Context context) {
        this.context = context;
    }

    /**
     * Process the new device registration notifications and displays a warning message in notification center.
     *
     * @param notifications A list of {@link ServerNotification#NOTIFICATION_TYPE_NewDeviceAddedToUser} type notification.
     */
    public void process(List<ServerNotification> notifications) {

        try {

            for (ServerNotification notification : notifications) {

                Gson gson = new GsonBuilder().create();

                DeviceDetails newDevice = gson.fromJson(notification.getData(), DeviceDetails.class);

                if (newDevice != null) {

                    DonkyCore.publishLocalEvent(new NewDeviceEvent(newDevice.getModel(), newDevice.getSystem()));
                    displayNotification(newDevice);

                }

            }

        } catch (Exception exception) {

            new DLog("NewDeviceHandler").error("Error processing new device notification.", exception);

        }
    }

    /**
     * Display notification about new device registration in Android Notification Center.
     *
     * @param newDevice New registration details.
     */
    private void displayNotification(DeviceDetails newDevice) {

        int smallIcon = AppSettings.getInstance().getNewDeviceNotificationSmallIconID();

        String packageName = context.getApplicationContext().getPackageName();

        PackageManager pm = context.getPackageManager();

        Intent newIntent = pm.getLaunchIntentForPackage(packageName);

        if (smallIcon == 0 || newIntent == null) {
            return;
        }

        final String message = AppSettings.getInstance().getNewDeviceMessage().replaceAll(
                "[{]DeviceModel[}]", newDevice.getModel()).replaceAll(
                "[{]DeviceOperatingSystem[}]", newDevice.getSystem());

        final String title = AppSettings.getInstance().getNewDeviceTitle();

        NotificationCompat.Builder builder =

                new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIcon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setCategory(android.app.Notification.CATEGORY_ALARM)
                        .setPriority(android.app.Notification.PRIORITY_HIGH)
                        .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                        .setColor(context.getResources().getColor(R.color.donky_new_device_notification_color))
                        .setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(context, new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE))), newIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE))), builder.build());
    }

    /**
     * Represents a new device details from the data of a server notification.
     */
    private class DeviceDetails {

        @SerializedName("model")
        private String model;

        @SerializedName("operatingSystem")
        private String operatingSystem;

        public String getSystem() {
            return operatingSystem;
        }

        public String getModel() {
            return model;
        }
    }
}
