package net.donky;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.automation.events.TriggerExecutedEvent;
import net.donky.core.events.OnCreateEvent;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.content.ContentNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends ActionBarActivity implements ColorListener {

    private String TAG = "Donky Test App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnCreateEvent(getIntent()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationProcessor.getInstance().addListener(this);

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnResumeEvent());
    }

    @Override
    protected void onPause() {
        super.onPause();

        NotificationProcessor.getInstance().removeListener(this);

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnPauseEvent());
    }

    public void sendContent(View v) {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("newColour", "#FF0FFF");
            jsonObject.put("intervalSeconds", 5);

        } catch (JSONException e) {

            Log.i(TAG, "failed to create json");

        }

        List<ContentNotification> contentNotifications = new LinkedList<>();
        contentNotifications.add(new ContentNotification("john-smith", "changeColour", jsonObject));

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

    public void executeTrigger(View v) {

        Map<String, String> additionalData = new TreeMap<>();

        // To execute this trigger you will need to setup trigger on Donky Control portal with the same key 'automation_test'
        DonkyCore.publishLocalEvent(new TriggerExecutedEvent("automation_test", additionalData ));

    }

    @Override
    public void onColorChanged(String color, int interval) {

        final View view = findViewById(R.id.wallpaper);
        try {
            view.setBackgroundColor(Color.parseColor(color));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing colour");
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setBackgroundColor(Color.WHITE);
            }
        }, 5000);

    }
}
