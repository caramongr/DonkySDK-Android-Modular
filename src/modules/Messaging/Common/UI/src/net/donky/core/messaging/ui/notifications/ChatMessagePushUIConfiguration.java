package net.donky.core.messaging.ui.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import net.donky.core.logging.DLog;
import net.donky.core.messaging.ui.R;

/**
 * UI configuration for rich message remote notification.
 *
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ChatMessagePushUIConfiguration {

    private int ledColour;

    private int ledOnMilliseconds;

    private int ledOffMilliseconds;

    private PendingIntent contentIntent;

    private Uri soundUri;

    private long[] vibratePattern;

    private String category;

    private int priority;

    private int visibility;

    private int iconBackgroundColor;

    private int smallIconId;

    @SuppressLint("NewApi")
    public ChatMessagePushUIConfiguration(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.category = Notification.CATEGORY_MESSAGE;
            this.priority = Notification.PRIORITY_HIGH;
            this.visibility = Notification.VISIBILITY_PUBLIC;
        }
        this.vibratePattern = new long[] { 0l, 100l };
        this.soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        this.iconBackgroundColor = R.color.donky_notification_icon_background_rich;
        this.ledOnMilliseconds = 200;
        this.ledOffMilliseconds = 2000;
        this.ledColour = R.color.donky_notification_led;
        this.contentIntent = null;

        int attributeResourceId = -1;
        try {
            String packageName = context.getApplicationContext().getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            int theme = packageInfo.applicationInfo.theme;
            int[] attribute = new int[] {R.attr.dk_notification_small_icon_rich};
            TypedArray array = context.getTheme().obtainStyledAttributes(theme, attribute);
            attributeResourceId = array.getResourceId(0, -1);
            array.recycle();
        } catch (PackageManager.NameNotFoundException e) {
            new DLog("RichMessagePushUIConfiguration").error("Error getting styled notification icon",e);
        }

        if (attributeResourceId != -1) {
            this.smallIconId = attributeResourceId;
        } else {
            this.smallIconId = R.drawable.donky_notification_small_icon_rich;
        }

    }

    /**
     * Get small icon for system notification.
     *
     * @return Small icon for system notification.
     */
    public int getSmallIconId() {
        return smallIconId;
    }

    /**
     * Set small icon for system notification.
     *
     * @param smallIconId Small icon for system notification.
     */
    public void setSmallIconId(int smallIconId) {
        this.smallIconId = smallIconId;
    }

    /**
     * Get sound for notification.
     *
     * @return Sound for notification.
     */
    public Uri getSoundUri() {
        return soundUri;
    }

    /**
     * Set sound for notification.
     *
     * @param soundUri Sound for notification.
     */
    public void setSoundUri(Uri soundUri) {
        this.soundUri = soundUri;
    }

    /**
     * Get vibration pattern for notification.
     *
     * @return Vibration pattern for notification.
     */
    public long[] getVibratePattern() {
        return vibratePattern;
    }

    /**
     * Set vibration pattern for notification.
     *
     * @param vibratePattern Vibration pattern for notification.
     */
    public void setVibratePattern(long[] vibratePattern) {
        this.vibratePattern = vibratePattern;
    }

    /**
     * Get notification category. Category is interpreted by OS.
     *
     * @return Notification category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set notification category. Category is interpreted by OS.
     *
     * @param category Notification category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get notification priority. Priority is interpreted by OS.
     *
     * @return Notification priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set notification priority. Priority is interpreted by OS.
     *
     * @param priority Notification priority.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Get notification visibility. Visibility is interpreted by OS.
     *
     * @return Notification visibility.
     */
    public int getVisibility() {
        return visibility;
    }

    /**
     * Set notification visibility. Visibility is interpreted by OS.
     *
     * @param visibility Notification visibility.
     */
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    /**
     * Get background color for small icon if large icon is not available.
     *
     * @return Background color for small icon if large icon is not available.
     */
    public int getIconBackgroundColor() {
        return iconBackgroundColor;
    }

    /**
     * Set background color for small icon if large icon is not available.
     *
     * @param iconBackgroundColor Background color for small icon if large icon is not available.
     */
    public void setIconBackgroundColor(int iconBackgroundColor) {
        this.iconBackgroundColor = iconBackgroundColor;
    }

    /**
     * Get led color.
     *
     * @return Led color.
     */
    public int getLedColour() {
        return ledColour;
    }

    /**
     * Get time period when led light will be switched on after notification was received.
     *
     * @return Time period when led light will be switched on after notification was received.
     */
    public int getLedOnMilliseconds() {
        return ledOnMilliseconds;
    }

    /**
     * Get time period when led light will be switched off after notification was received.
     *
     * @return Time period when led light will be switched off after notification was received.
     */
    public int getLedOffMilliseconds() {
        return ledOffMilliseconds;
    }

    /**
     * Set colour of led light.
     *
     * @param ledColour Colour of led light.
     */
    public void setLedColour(int ledColour) {
        this.ledColour = ledColour;
    }

    /**
     * Set time period when led light will be switched on after notification was received.
     *
     * @param ledOnMilliseconds Time period when led light will be switched on after notification was received.
     */
    public void setLedOnMilliseconds(int ledOnMilliseconds) {
        this.ledOnMilliseconds = ledOnMilliseconds;
    }

    /**
     * Set time period when led light will be switched off after notification was received.
     *
     * @param ledOffMilliseconds Time period when led light will be switched off after notification was received.
     */
    public void setLedOffMilliseconds(int ledOffMilliseconds) {
        this.ledOffMilliseconds = ledOffMilliseconds;
    }

    /**
     * Get PendingIntent for notification click.
     *
     * @return PendingIntent for notification click.
     */
    public PendingIntent getContentIntent() {
        return contentIntent;
    }

    /**
     * Set PendingIntent for notification click.
     *
     * @param contentIntent PendingIntent for notification click.
     */
    public void setContentIntent(PendingIntent contentIntent) {
        this.contentIntent = contentIntent;
    }
}
