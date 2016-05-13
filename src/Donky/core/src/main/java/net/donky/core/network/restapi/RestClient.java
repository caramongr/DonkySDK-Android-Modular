package net.donky.core.network.restapi;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.OkHttpClient;

import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.settings.AppSettings;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Perform initialisation of network clients and provides access to REST API.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RestClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 60 * 1000;
    private static final int READ_TIMEOUT_MILLIS = 90 * 1000;

    private AuthenticationAPI authenticationAPI;

    private SecuredAPI securedAPI;

    private OkClient okClient;

    private GsonConverter gsonConverter;

    private RestAdapter.LogLevel logLevel;

    private OkHttpClient okHttpClient;

    // Private constructor. Prevents instantiation from other classes.
    private RestClient() {}

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final RestClient INSTANCE = new RestClient();
    }

    /**
     * @return Instance of RestClient singleton.
     */
    public static RestClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * @return Authentication part of REST API interface.
     */
    public static AuthenticationAPI getAuthAPI() {
        return SingletonHolder.INSTANCE.authenticationAPI;
    }

    /**
     * @return Secured part of REST API interface.
     */
    public static SecuredAPI getAPI() {
        return SingletonHolder.INSTANCE.securedAPI;
    }

    /**
     * Initialise RestClient. This method should be called only by Donky Core SDK during initialisation.
     */
    public void init() {

        DLog log = new DLog("RestClient");

        setupLogLevel();
        setupGsonConverter();
        setupOkHttpClient();

        try {

            setupAuthenticationRestAdapter();

            String internalRootUrl = DonkyDataController.getInstance().getConfigurationDAO().getSecureServiceDomain();
            if (internalRootUrl != null) {
                setupSecuredRestAdapter(internalRootUrl);
            }

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * Setup and initialise the secured REST API adapter.
     *
     * @param internalRootUrl URL for secured service REST calls.
     */
    public void setupSecuredRestAdapter(String internalRootUrl) {

        if (getInstance().securedAPI == null) {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(internalRootUrl)
                    .setClient(okClient)
                    .setConverter(gsonConverter)
                    .setLogLevel(logLevel);

            RestAdapter restAdapter = builder.build();
            getInstance().securedAPI = restAdapter.create(SecuredAPI.class);
        }
    }

    /**
     * Retrofit Log level based on App Settings
     */
    private void setupLogLevel() {

        if (AppSettings.getInstance().isSensitiveLogsEnabled()) {
            logLevel = RestAdapter.LogLevel.FULL;
        } else if (AppSettings.getInstance().isDebugLogsEnabled()) {
            logLevel = RestAdapter.LogLevel.FULL;
        } else if (AppSettings.getInstance().isInfoLogsEnabled()) {
            logLevel = RestAdapter.LogLevel.BASIC;
        } else {
            logLevel = RestAdapter.LogLevel.NONE;
        }
    }

    /**
     * Setup the OkHTTP Client
     */
    private void setupOkHttpClient() {

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        okHttpClient = client;

        okClient = new OkClient(client);
    }

    /**
     * Setup the Gson converter used to parse network requests and responses..
     */
    private void setupGsonConverter() {

        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping().

                addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getAnnotation(SerializedName.class) == null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                });
        gsonConverter = new GsonConverter(gsonBuilder.create());
    }

    /**
     * Setup and initialise the authentication REST API adapter.
     */
    private void setupAuthenticationRestAdapter() {

        String authenticationRootUrl = AppSettings.getInstance().getAuthRootUrl();

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(authenticationRootUrl)
                .setClient(okClient)
                .setConverter(gsonConverter)
                .setLogLevel(logLevel);

        RestAdapter restAdapter = builder.build();
        SingletonHolder.INSTANCE.authenticationAPI = restAdapter.create(AuthenticationAPI.class);
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
