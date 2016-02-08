package net.donky;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.donky.assets.AssetActivity;
import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.automation.events.TriggerExecutedEvent;
import net.donky.core.messaging.rich.inbox.ui.components.RichInboxAndMessageActivityWithToolbar;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.content.ContentNotification;
import net.donky.location.DonkyLocationController;
import net.donky.location.TargetUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main demo app activity to showcase some of basic functionality.
 */
public class MainActivity extends AppCompatActivity implements ColorListener {

    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_EXTERNAL_STORAGE = 1;
    private String TAG = "Donky Test App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserDetails user = DonkyAccountController.getInstance().getCurrentDeviceUser();
        if (user != null && !TextUtils.isEmpty(user.getUserId())) {
            String idTitle = getResources().getString(R.string.id_title)+" "+user.getUserId();
            ((TextView) findViewById(R.id.user_id)).setText(idTitle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listener for custom content notifications with new colour
        NotificationProcessor.getInstance().addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Listener for custom content notifications with new colour
        NotificationProcessor.getInstance().removeListener(this);
    }

    /**
     * This method will send custom content notification with new colour for the background of Activity to itself.
     * @param v View that registered this method on onClick
     */
    public void sendContent(View v) {

        JSONObject jsonObject = new JSONObject();

        // Define json content of notification
        try {

            jsonObject.put("newColour", "#FF0FFF");
            jsonObject.put("intervalSeconds", 5);

        } catch (JSONException e) {

            Log.i(TAG, "failed to create json");

        }

        List<ContentNotification> contentNotifications = new LinkedList<>();
        // Create new Content notification to be send to yourself
        contentNotifications.add(new ContentNotification(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId(), "changeColour", jsonObject));

        // Send notification
        DonkyNetworkController.getInstance().sendContentNotifications(contentNotifications, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "success sending content notification");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.i(TAG, "failed to send content notification");
            }
        });
    }

    /**
     * Execute third party trigger with name 'automation_test'. You can define this trigger on Campaign builder.
     * @param v View that registered this method on onClick
     */
    public void executeTrigger(View v) {
        Map<String, String> additionalData = new TreeMap<>();
        // To execute this trigger you will need to setup trigger on Donky Control portal with the same key 'automation_test'
        DonkyCore.publishLocalEvent(new TriggerExecutedEvent("automation_test", additionalData));
    }

    /**
     * Open Rich Messaging Inbox prebuilt UI
     * @param v View that registered this method on onClick
     */
    public void openInbox(View v) {
        Intent intent = new Intent(this, RichInboxAndMessageActivityWithToolbar.class);
        startActivity(intent);
    }

    /**
     * You can choose a file on the device. This file will be uploaded to the Donky Network and then downloaded again to External storage file. You will see the path to that file displayed in the Activity
     * @param v View that registered this method on onClick
     */
    public void openAssetTest(View v) {
        openAssetTest(false);
    }

    /**
     * Send yourself a request for current location. Check logs to see the result.
     * @param v View that registered this method on onClick
     */
    public void requestLocation(View v){
        DonkyLocationController.getInstance().requestUserLocation(TargetUser.getTargetUserByExternalId(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId()), new DonkyListener() {
            @Override
            public void success() {
                Toast.makeText(MainActivity.this, "Location requested. Check logs for details.", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Send notification to yourself with the current location. Check logs to see the result.
     * @param v View that registered this method on onClick
     */
    public void updateLocation(View v){
        DonkyLocationController.getInstance().sendLocationUpdateToUser(TargetUser.getTargetUserByExternalId(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId()), new DonkyListener() {
            @Override
            public void success() {
                Toast.makeText(MainActivity.this, "Location sent. Check logs for details.", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Setup temporary files to store avatar bitmaps
     */
    private void openAssetTest(boolean isPermGranted) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsList = permissions();
            if (!permissionsList.isEmpty() && !isPermGranted) {
                requestExternalStoragePermissions(permissionsList);
            } else {
                Intent intent = new Intent(this, AssetActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(this, AssetActivity.class);
            startActivity(intent);
        }
    }

    @TargetApi(23)
    private void requestExternalStoragePermissions(List<String> permissionsList) {
        if (!permissionsList.isEmpty()) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_EXTERNAL_STORAGE);
        }
    }

    private List<String> permissions(){
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("WRITE_EXTERNAL_STORAGE");
        if (!addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("READ_EXTERNAL_STORAGE");
        return permissionsList;

    }

    @TargetApi(23)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_EXTERNAL_STORAGE:
            {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_EXTERNAL_STORAGE) {
                        openAssetTest(true);
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * As a response to received custom notification with colour (see #sendContent method) change the colour of the background.
     * @param color Color to which the background should be changed.
     * @param interval
     */
    @Override
    public void onColorChanged(final String color, int interval) {
        final View view = findViewById(R.id.main_layout);
        view.setBackgroundColor(Color.parseColor(color));
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setBackgroundColor(Color.WHITE);
            }
        }, interval*1000);
    }
}
