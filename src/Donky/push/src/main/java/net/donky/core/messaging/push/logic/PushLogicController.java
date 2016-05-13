package net.donky.core.messaging.push.logic;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.messaging.push.DonkyPush;
import net.donky.core.network.DonkyNetworkController;

import java.util.Random;

/**
 * Controller responsible for all actions related to push notifications.
 *
 * Created by Marcin Swierczek
 * 11/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushLogicController {

    private final static String ACTION_TYPE_OPEN = "Open";

    private final static String ACTION_TYPE_DEEP_LINK = "DeepLink";

    private final static String ACTION_TYPE_DISMISS = "Dismiss";

    final static String EXTRAS_KEY_NOTIFICATION_ID = "AndroidSystemNotificationId";

    private Context context;

    // Private constructor. Prevents instantiation from other classes.
    private PushLogicController() {

    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final PushLogicController INSTANCE = new PushLogicController();
    }

    /**
     * Get instance of Account Controller singleton.
     *
     * @return Instance of Account Controller singleton.
     */
    public static PushLogicController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application) {
        this.context = application.getApplicationContext();
    }

    /**
     * Create Pending intent for notification button. This will describe the action when notification button will be clicked. Single button is interpreted as notification itself.
     *
     * @param buttonSetAction {@link SimplePushData.ButtonSetAction} describing notification button.
     * @param simplePushData Description of Simple/Interactive Push notification.
     * @param notificationId Notification identifier for the OS.
     * @return Pending Intent for notification button.
     */
    public PendingIntent createPendingIntent(SimplePushData.ButtonSetAction buttonSetAction, SimplePushData simplePushData, int notificationId) {

        if (DonkyPush.isInitialised()) {

            Intent intent = new Intent(context, PushLogicIntentService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            }

            intent.putExtra(EXTRAS_KEY_NOTIFICATION_ID, notificationId);

            intent.putExtras(getReportingDataBundle(buttonSetAction, simplePushData));

            Integer pendingIntentId = Math.abs(new Random().nextInt(Integer.MAX_VALUE));

            if (buttonSetAction != null) {

                if (ACTION_TYPE_OPEN.equals(buttonSetAction.getActionType())) {

                    intent.setAction(PushLogicIntentService.ACTION_OPEN_APPLICATION);

                } else if (ACTION_TYPE_DEEP_LINK.equals(buttonSetAction.getActionType())) {

                    intent.setAction(PushLogicIntentService.ACTION_OPEN_DEEP_LINK);

                } else if (ACTION_TYPE_DISMISS.equals(buttonSetAction.getActionType())) {

                    intent.setAction(PushLogicIntentService.ACTION_CANCEL_NOTIFICATION);

                }

                return PendingIntent.getService(context, pendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            } else {

                String packageName = context.getApplicationContext().getPackageName();

                PackageManager pm = context.getPackageManager();

                Intent newIntent = pm.getLaunchIntentForPackage(packageName);

                if (newIntent != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
                        newIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    }
                    newIntent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, true);
                    return PendingIntent.getActivity(context, notificationId, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
                return null;
            }

        } else {

            return null;
        }

    }

    /**
     * Create data bundle necessary to create Interaction Result client notification.
     *
     * @param buttonSetAction {@link SimplePushData.ButtonSetAction} for clicked notification button.
     * @param simplePushData Description of Simple Push message.
     * @return
     */
    protected Bundle getReportingDataBundle(SimplePushData.ButtonSetAction buttonSetAction, SimplePushData simplePushData) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("simplePushData", simplePushData);
        bundle.putSerializable("buttonSetAction", buttonSetAction);

        return bundle;
    }

    /**
     * Queue Interaction Result client notification.
     *
      * @param intent Intent holding interaction data.
     */
    public void reportPushNotificationClicked(Intent intent) {

        Bundle bundle = intent.getExtras();

        SimplePushData simplePushData =
                (SimplePushData) bundle.getSerializable("simplePushData");

        SimplePushData.ButtonSetAction buttonSetAction =
                (SimplePushData.ButtonSetAction) bundle.getSerializable("buttonSetAction");

        DonkyNetworkController.getInstance().sendClientNotification(ClientNotification.createInteractionResultNotification(buttonSetAction, simplePushData), null);

    }
}
