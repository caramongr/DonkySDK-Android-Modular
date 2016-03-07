package net.donky.core.messaging.rich.inbox.ui.components;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import net.donky.core.messaging.logic.MessagingInternalController;
import net.donky.core.messaging.rich.logic.helpers.RichMessageHelper;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.messaging.ui.components.DonkyFragment;
import net.donky.core.messaging.ui.components.generic.DeletionListener;
import net.donky.core.messaging.ui.components.generic.DetailView;
import net.donky.core.messaging.ui.components.generic.DetailViewPresentedListener;
import net.donky.core.messaging.ui.components.generic.DualPaneModeListener;
import net.donky.core.messaging.ui.components.generic.GenericBuilder;
import net.donky.core.messaging.ui.components.generic.SelectionListener;
import net.donky.core.messaging.rich.inbox.ui.R;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Fragment class to display RichMessage in web view.
 *
 * Created by Marcin Swierczek
 * 15/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageFragment extends DonkyFragment implements GenericBuilder<RichMessageFragment>, SelectionListener<RichMessage>, DetailView, DualPaneModeListener {

    private WebView webView;

    DLog log;

    RichMessage richMessage;

    boolean isSplitViewMode;

    private DeletionListener deletionListener;

    private DetailViewPresentedListener detailViewPresentedListener;

    public RichMessageFragment() {

        log = new DLog("RichMessageFragment");
        this.isSplitViewMode = false;

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.dk_rich_message_fragment, container, false);

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
     * Sets the rich message into the Web View.f
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

            getActivity().setTitle(richMessage.getSenderDisplayName());

            String body = getRichMessageBody();

            webView.loadData(body, "text/html; charset=utf-8", "utf-8");
            webView.setBackgroundColor(Color.WHITE);

            if (detailViewPresentedListener != null) {
                detailViewPresentedListener.onDetailViewPresented(null);
            }
        }

        else if (webView != null) {
            webView.loadData("", "text/html", "utf-8");
            webView.setBackgroundColor(Color.TRANSPARENT);
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

            RichMessageDataController.getInstance().getRichMessagesDAO().markAsRead(richMessage.getInternalId());

            MessagingInternalController.getInstance().sendMessageReadNotification(richMessage, new DonkyListener() {
                @Override
                public void success() {
                    new DLog("RichMessageHandler").debug("Message read notification sent successfully. Message id = "+richMessage.getMessageId());
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    new DLog("RichMessageHandler").error("Error sending message read notification.");
                }
            });

        }

        try {

            Date expireDate = DateAndTimeHelper.parseUtcDate(richMessage.getExpiryTimeStamp());

            if ((expireDate != null && new Date().before(expireDate)) || expireDate == null) {

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

    @Override
    public RichMessageFragment build() {
        return new RichMessageFragment();
    }

    @Override
    public void onSelected(RichMessage selected, boolean isSplitViewMode) {

        setRichMessage(selected);
        this.isSplitViewMode = isSplitViewMode;

        Activity activity = getActivity();

        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onSelectedNew(RichMessage selected) {

    }

    @Override
    public void setDeletionListener(DeletionListener deletionListener) {
        this.deletionListener = deletionListener;
    }

    @Override
    public void setDetailViewPresentedListener(DetailViewPresentedListener detailViewPresentedListener) {
        this.detailViewPresentedListener = detailViewPresentedListener;
    }

    @Override
    public void setIsInDualPaneDisplayMode(boolean dualPane) {

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

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        webView = null;
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dk_rich_inbox_detail_action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.dk_share) {
            Intent shareIntent = RichMessageHelper.getShareRichMessageIntent(richMessage, true);
            // Verify it resolves
            PackageManager packageManager = getActivity().getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(shareIntent, 0);
            if (activities.size() > 0) {
                //sendRichMessageSharedNotification
                startActivity(Intent.createChooser(shareIntent, getString(R.string.dk_rich_message_share_title)));
            }
            webView.onPause();
        } else if (menuItem.getItemId() == R.id.dk_delete) {
            RichMessageDataController.getInstance().getRichMessagesDAO().removeRichMessage(richMessage);
            if (!isSplitViewMode) {
                getActivity().finish();
            } else if (deletionListener != null) {
                deletionListener.onContentDeleted();
            }

        }

        return false;
    }

    private void updateOptionMenu(Menu menu) {

        if (richMessage != null) {

            boolean isMessageExpired = RichMessageHelper.isRichMessageExpired(richMessage);

            /*
            if (!richMessage.isCanForward() || isMessageExpired) {
                setForwardMenuItemVisible(menu, false);
            } else {
                setForwardMenuItemVisible(menu, true);
            }*/
            //TODO
            setForwardMenuItemVisible(menu, false);

            if (!richMessage.isCanShare() || isMessageExpired) {
                setShareMenuItemVisible(menu, false);
            } else {
                setShareMenuItemVisible(menu, true);
            }
        } else {
            setForwardMenuItemVisible(menu, false);
            setShareMenuItemVisible(menu, false);
            setDeleteMenuItemVisible(menu, false);
        }
    }

    private void setForwardMenuItemVisible(Menu menu, boolean isVisible) {
        MenuItem forwardMenuItem = menu.findItem(R.id.dk_forward);
        if (forwardMenuItem != null) {
            forwardMenuItem.setVisible(isVisible);
        }
    }

    private void setShareMenuItemVisible(Menu menu, boolean isVisible) {
        MenuItem shareMenuItem = menu.findItem(R.id.dk_share);
        if (shareMenuItem != null) {
            shareMenuItem.setVisible(isVisible);
        }
    }

    private void setDeleteMenuItemVisible(Menu menu, boolean isVisible) {
        MenuItem deleteMenuItem = menu.findItem(R.id.dk_delete);
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(isVisible);
        }
    }
}
