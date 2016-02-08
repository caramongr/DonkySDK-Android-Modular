package net.donky.sample.rich.inbox;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.model.RichMessage;

/**
 * Created by Marcin Swierczek
 * 28/01/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ActivityMessage extends AppCompatActivity {

    private static final String TAG = "ActivityMessage";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Setup WebView to display Rich Message HTML+JavaScript body.
        webView = (WebView) findViewById(R.id.web_view);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");

        if (savedInstanceState == null)
        {
            // If Activity has been created from scratch load the rich message body to the WebView
            loadMessageToWebView(getRichMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // WebView need to follow the Activity lifecycle to avoid issues with audio/video streams not being closed etc.
        webView.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // WebView need to follow the Activity lifecycle to avoid issues with audio/video streams not being closed etc.
        webView.onResume();
    }

    @Override
    public void onDestroy() {
        // WebView need to follow the Activity lifecycle to avoid issues with audio/video streams not being closed etc.
        webView.destroy();
        webView = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store WebView state on rotations
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore WebView state on rotations
        webView.restoreState(savedInstanceState);
    }

    /**
     * Obtains the Rich Message from intent bundle.
     * @return Rich Message to be displayed.
     */
    private RichMessage getRichMessage() {

        RichMessage richMessage = null;

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE)) {
            finish();
        } else {
            richMessage = (RichMessage) extras.get(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE);
            if (richMessage == null) {
                finish();
            }
        }

        return richMessage;
    }

    /**
     * Load data to WebView
     * @param richMessage Rich Message to be loaded into a WebView
     */
    private void loadMessageToWebView(RichMessage richMessage) {

        if (richMessage != null && webView != null) {

            setTitle(richMessage.getSenderDisplayName());
            String body = richMessage.getURLEncodedBody();
            webView.loadData(body, "text/html; charset=utf-8", "utf-8");
            webView.setBackgroundColor(Color.WHITE);

        } else if (webView != null) {

            webView.loadData("", "text/html", "utf-8");
            webView.setBackgroundColor(Color.TRANSPARENT);

        }
    }
}

