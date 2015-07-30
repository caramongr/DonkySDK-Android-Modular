package net.donky.core.messaging.push.logic;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.logging.DLog;

import java.util.List;

/**
 * Intent service to process button clicks on system notification.
 *
 * Created by Marcin Swierczek
 * 11/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushLogicIntentService extends IntentService {

    /**
     * Action name of intent. Notification described in intent should be canceled.
     */
    static final String ACTION_CANCEL_NOTIFICATION = "net.donky.core.messaging.push.CANCEL_NOTIFICATION";

    /**
     * Action name of intent. Notification described in intent should be canceled. Main application Activity will be opened.
     */
    static final String ACTION_OPEN_APPLICATION = "net.donky.core.messaging.push.OPEN";

    /**
     * Action name of intent. Notification described in intent should be canceled. System will try to open activity responding to deep link.
     */
    static final String ACTION_OPEN_DEEP_LINK = "net.donky.core.messaging.push.DEEP_LINK";

    /**
     * Logging helper.
     */
    private DLog log = new DLog("PushLogicIntentService");

    public PushLogicIntentService() {
        super("PushLogicIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        if (ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {

            cancelNotification(intent);

            PushLogicController.getInstance().reportPushNotificationClicked(intent);

        } else if (ACTION_OPEN_DEEP_LINK.equals(intent.getAction())) {

            cancelNotification(intent);

            PushLogicController.getInstance().reportPushNotificationClicked(intent);

            Intent newIntent = new Intent();

            setDonkyDataBundle(intent, newIntent);

            newIntent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, true);

            newIntent.setAction(Intent.ACTION_VIEW);

            newIntent.addCategory(Intent.CATEGORY_DEFAULT);

            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (isActivityAvailable(newIntent)) {
                startActivity(newIntent);
            }

        } else if (ACTION_OPEN_APPLICATION.equals(intent.getAction())) {

            cancelNotification(intent);

            PushLogicController.getInstance().reportPushNotificationClicked(intent);

            String packageName = getApplicationContext().getApplicationContext().getPackageName();

            PackageManager pm = getApplicationContext().getPackageManager();

            Intent newIntent = pm.getLaunchIntentForPackage(packageName);

            setDonkyDataBundle(intent, newIntent);

            newIntent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, true);

            newIntent.setAction(Intent.ACTION_VIEW);

            newIntent.addCategory(Intent.CATEGORY_DEFAULT);

            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(newIntent);

        }

    }

    /**
     * Cancel notification described in intent.
     *
     * @param intent Intent from notification button click.
     */
    private void cancelNotification(Intent intent) {

        if (intent.getExtras().containsKey(PushLogicController.EXTRAS_KEY_NOTIFICATION_ID)) {

            int notificationId = intent.getIntExtra(PushLogicController.EXTRAS_KEY_NOTIFICATION_ID, 0);

            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null && notificationId != 0) {
                manager.cancel(notificationId);
            }

        } else {

            log.error("Missing notification id for dismiss action.");

        }
    }

    /**
     * Transfer data between intents. This data will be used to create Interaction Result client notification.
     *
     * @param oldIntent Intent to transfer data from.
     * @param intent Intent to transfer data to.
     */
    private void setDonkyDataBundle(Intent oldIntent, Intent intent) {

        Bundle bundle = new Bundle();

        try {

            SimplePushData simplePushData = (SimplePushData) oldIntent.getExtras().getSerializable("simplePushData");
            SimplePushData.ButtonSetAction buttonSetAction = (SimplePushData.ButtonSetAction) oldIntent.getExtras().getSerializable("buttonSetAction");

            bundle.putSerializable("simplePushData", simplePushData);
            bundle.putSerializable("buttonSetAction", buttonSetAction);

            if (buttonSetAction != null && !TextUtils.isEmpty(buttonSetAction.getData())) {

                intent.setData(Uri.parse(buttonSetAction.getData()));

            }

        } catch (Exception e) {

            log.error("Error transmitting data between intents.", e);

        }

        intent.putExtras(bundle);

    }

    /**
     * Check if there is Activity responding to an Intent.
     *
     * @param intent Intent to check if any Activity responds to.
     * @return True if Activity responds to an Intent.
     */
    public boolean isActivityAvailable(Intent intent) {

        final PackageManager mgr = getApplicationContext().getPackageManager();

        List<ResolveInfo> list =
                mgr.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;

    }

}