package net.donky.core.messaging.ui.components.rich;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.logging.DLog;
import net.donky.core.messages.RichMessage;
import net.donky.core.messaging.logic.MessagingInternalController;
import net.donky.core.messaging.ui.R;
import net.donky.core.model.DonkyDataController;

import java.util.Date;
import java.util.Map;


/**
 * Fragment class to display RichMessage in web view.
 *
 * Created by Marcin Swierczek
 * 15/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageFragment extends Fragment {

    private WebView webView;

    DLog log;

    RichMessage richMessage;

    public RichMessageFragment() {

        log = new DLog("RichMessageFragment");

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.rich_message_fragment, container, false);

        webView = (WebView) view.findViewById(R.id.rich_message_web_view);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        webView.setWebChromeClient(new DonkyWebChromeClient());

        webView.setWebViewClient(new DonkyWebViewClient());

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDefaultTextEncodingName("utf-8");

        if (savedInstanceState == null)
        {
            refresh();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    /**
     * Sets the rich message into the Web View.
     *
     * @param richMessage The rich message to set into the Web View.
     */
    public void setRichMessage(RichMessage richMessage) {

        this.richMessage = richMessage;

        refresh();

    }

    /**
     * Refresh the web view.
     */
    private void refresh() {

        if (richMessage != null && webView != null) {

            getActivity().setTitle(richMessage.getDescription());

            String body = getRichMessageBody();

            webView.loadData(body, "text/html", "utf-8");

        }

        else if (webView != null) {

            webView.loadData("", "text/html", "utf-8");

        }
    }

    /**
     * Gets the Rich Message body. Marks the message as read. Sends the Message Read notification to Donky Network.
     *
     * @return The Rich Message body.
     */
    private String getRichMessageBody() {

        if (richMessage == null) {

            return null;

        } else if (!richMessage.isMessageRead()) {

            richMessage.setMessageRead(true);

            DonkyDataController.getInstance().getRichMessagesDAO().markAsRead(richMessage.getInternalId());

            MessagingInternalController.getInstance().sendMessageReadNotification(richMessage, new DonkyListener() {
                @Override
                public void success() {
                    new DLog("RichMessageHandler").debug("Message read notification sent successfully.");
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    new DLog("RichMessageHandler").error("Error sending message read notification.");
                }
            });

        }

        try {

            Date expireDate = DateAndTimeHelper.parseUtcDate(richMessage.getExpiryTimeStamp());

            if (expireDate != null && new Date(System.currentTimeMillis()).before(expireDate)) {

                return richMessage.getURLEncodedBody();

            } else {

                return richMessage.getURLEncodedExpiredBody();

            }

        } catch (Exception e) {

            log.error("Error checking message expired.", e);

            return richMessage.getURLEncodedBody();

        }

    }

    /**
     * Gets the description of RichMessage.
     *
     * @return The description of RichMessage.
     */
    public RichMessage getRichMessage() {

        return richMessage;

    }


    private class DonkyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

            if (ConsoleMessage.MessageLevel.ERROR.equals(consoleMessage.messageLevel())) {
                log.warning("WebView console message: " + consoleMessage.lineNumber() + ": " + consoleMessage.message());
            }

            else {

                log.debug("WebView console message: " + consoleMessage.lineNumber() + ": " + consoleMessage.message());

            }

            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView newWebView = new WebView(view.getContext());
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }
    }

    private class DonkyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            log.warning("WebView error: " + errorCode + ": " + description + " @ " + failingUrl);
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            log.warning("WebView SSL error: " + error.toString());
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return override(url);
        }
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            @SuppressWarnings("deprecation")
            WebResourceResponse r = super.shouldInterceptRequest(view, url);
            return r;
        }

        private boolean override(String url) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
            catch (Exception t) {
                log.error("Error overriding url to show in browser: " + url, t);
            }
            return true;
        }
    }
}
