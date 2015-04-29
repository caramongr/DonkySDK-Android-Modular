package net.donky.core;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.EventObservable;
import net.donky.core.events.LocalEvent;
import net.donky.core.gcm.DonkyGcmController;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.logging.DLog;
import net.donky.core.logging.DonkyLoggingController;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.assets.DonkyAssetController;
import net.donky.core.network.restapi.RestClient;
import net.donky.core.network.restapi.secured.UploadLog;
import net.donky.core.observables.SubscriptionController;
import net.donky.core.settings.AppSettings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is main entry point class for Donky Core library integration.
 * To initialise Donky Core add initialiseDonkySDK method call to Application class onCreate call-back.
 *
 * Created by Marcin Swierczek
 * 17/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyCore {

    /**
     * Logging helper instance.
     */
    private final DLog log = new DLog("DonkyCore");

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Object to lock the thread on
     */
    private static final Object sharedLock = new Object();

    /**
     * Object holding service subscriptions.
     */
    private final ConcurrentHashMap<String, ServiceWrapper> services = new ConcurrentHashMap<>();

    /**
     * Application context;
     */
    private Context context;

    /**
     * Module definitions to save after SDK will be initialised.
     */
    private static List<ModuleDefinition> moduleDefinitionsToRegister = new LinkedList<>();

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyCore() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyCore INSTANCE = new DonkyCore();
    }

    /**
     * @return Static instance of Donky Core singleton.
     */
    public static DonkyCore getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * This operation will ensure the SDK is active, and that the device is registered on the network with the correct API key and able to send/receive data.
     * This will also ensure that the registered module details are passed to the network if changed.
     * This method should be called in {@link android.app.Application} class {@link android.app.Application#onCreate()}
     *
     * @param application   The {@link android.app.Application} instance.
     * @param apiKey        The Client API key for the app space
     * @param userDetails   User details to use for the registration
     * @param deviceDetails Device details to use for the registration
     * @param appVersion    The app version as specified by the integrator
     * @param donkyListener The callback to invoke when the SDK is initialised.
     */
    public static void initialiseDonkySDK(final Application application, final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, final DonkyListener donkyListener) {
        SingletonHolder.INSTANCE.init(application, apiKey, userDetails, deviceDetails, appVersion, donkyListener);
    }

    /**
     * This operation will ensure the SDK is active, and that the device is registered on the network with the correct API key and able to send/receive data.
     * This will also ensure that the registered module details are passed to the network if changed.
     * This method should be called in {@link android.app.Application} class {@link android.app.Application#onCreate()}
     *
     * @param application   The {@link android.app.Application} instance.
     * @param apiKey        The Client API key for the app space
     * @param donkyListener The callback to invoke when the SDK is initialised.
     */
    public static void initialiseDonkySDK(final Application application, final String apiKey, final DonkyListener donkyListener) {
        SingletonHolder.INSTANCE.init(application, apiKey, null, null, null, donkyListener);
    }

    /**
     * Initialises all SDK components, and performs registration to Donky Network and GCM
     *
     * @param application   The {@link android.app.Application} instance.
     * @param apiKey        The Client API key for the app space
     * @param userDetails   User details to use for the registration
     * @param deviceDetails Device details to use for the registration
     * @param appVersion    The app version as specified by the integrator
     * @param donkyListener The callback to invoke when the SDK is initialised.
     */
    private void init(final Application application, final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, final DonkyListener donkyListener) {

        final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        if (!TextUtils.isEmpty(apiKey)) {

            this.context = application.getApplicationContext();

            synchronized (sharedLock) {

                new Thread("Initialise Donky SDK") {

                    public void run() {

                        if (!initialised.get()) {

                            synchronized (sharedLock) {

                                try {

                                    // Static app settings [no dependency]
                                    AppSettings.getInstance().init(application);

                                    // Logging [depends on AppSettings]
                                    DonkyLoggingController.getInstance().init(application);

                                    // Database controller [depends on Logging Controller]
                                    DonkyDataController.getInstance().init(application);

                                    // User, Device and App details [depends on Logging Controller and Database Controller]
                                    DonkyAccountController.getInstance().init(application);

                                    // Network communication [depends on App Settings, Logging Controller and Database Controller]
                                    DonkyNetworkController.getInstance().init(application, apiKey);

                                    // GCM [depends on Network, Logging Controller and Database Controller]
                                    DonkyGcmController.getInstance().init(application);

                                    //Subscribe Donky Logging Controller for TRANSMITDEBUGLOG notifications
                                    List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();

                                    LifeCycleObserver.getInstance().init(context);

                                    DonkyAssetController.getInstance().init(context, RestClient.getInstance().getOkHttpClient());

                                    serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TYPE_TransmitDebugLog,
                                            new NotificationListener<ServerNotification>() {
                                                @Override
                                                public void onNotification(ServerNotification notification) {
                                                    DonkyLoggingController.getInstance().submitLog(UploadLog.SubmissionReason.NotDefined, null);
                                                }
                                            }));

                                    subscribeToDonkyNotifications(
                                            new ModuleDefinition(DonkyCore.class.getSimpleName(), AppSettings.getVersion()),
                                            serverNotificationSubscriptions,
                                            true);

                                    log.info("Initialised Donky SDK.");

                                    initialised.set(true);

                                    sharedLock.notifyAll();

                                } catch (final Exception e) {

                                    log.error("Error initialising Donky SDK.");

                                    final DonkyException donkyException = new DonkyException("Error initialising Donky SDK controllers.");
                                    donkyException.initCause(e);

                                    sharedLock.notifyAll();

                                    postError(mainThreadHandler, donkyListener, donkyException);
                                }
                            }
                        }

                        if (initialised.get()) {

                            try {

                                for (ModuleDefinition moduleDefinition : moduleDefinitionsToRegister) {
                                    getInstance().registerModule(moduleDefinition);
                                }
                                moduleDefinitionsToRegister.clear();

                                // register user or update registration
                                DonkyAccountController.getInstance().register(apiKey, userDetails, deviceDetails, appVersion);

                                // Register for Push messages in GCM
                                DonkyGcmController.getInstance().registerPush(new DonkyListener() {

                                    @Override
                                    public void success() {
                                        postSuccess(mainThreadHandler, donkyListener);
                                    }

                                    @Override
                                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                        postError(mainThreadHandler, donkyListener, donkyException);
                                    }
                                });

                            } catch (final DonkyException donkyException) {

                                postError(mainThreadHandler, donkyListener, donkyException);

                            } catch (final Exception exception) {

                                final DonkyException donkyException = new DonkyException("Error initialising Donky SDK controllers.");
                                donkyException.initCause(exception);

                                postError(mainThreadHandler, donkyListener, donkyException);
                            }
                        }
                    }
                }.start();

            }
        } else {

            log.error("Wrong argument when initialising Donky Core SDK. Check initialiseDonkySDK method call.");

            final DonkyException donkyException = new DonkyException("Error initialising Donky SDK controllers.");
            postError(mainThreadHandler, donkyListener, donkyException);
        }
    }

    /**
     * @return If Donky SDK is initialised.
     */
    public static boolean isInitialised() {
        return initialised.get();
    }

    /**
     * Subscribes the caller to a local event type.
     *
     * @param donkyEventListener The callback to invoke when the event of the type provided as template argument is raised.
     */
    public static void subscribeToLocalEvent(DonkyEventListener donkyEventListener) {
        EventObservable.registerObserver(donkyEventListener);
    }

    /**
     * Removes a subscription for a local event type.
     *
     * @param donkyEventListener The callback to invoke when the event of the type provided as template argument is raised.
     */
    public static void unsubscribeFromLocalEvent(DonkyEventListener donkyEventListener) {
        EventObservable.removeObserver(donkyEventListener);
    }

    /**
     * Publishes a LocalEvent.
     * §
     *
     * @param event The event to publish.
     */
    public static void publishLocalEvent(LocalEvent event) {
        EventObservable.notifyObserver(event);
    }

    /**
     * Adds a subscription for specific types of Content notification. Should be called before Initialise to avoid a race condition resulting in missed notifications.
     *
     * @param moduleDefinition                          The module details.
     * @param serverNotificationSubscriptions The subscriptions to register for this module.
     */
    public static void subscribeToContentNotifications(ModuleDefinition moduleDefinition, List<Subscription<ServerNotification>> serverNotificationSubscriptions) {

        SubscriptionController.subscribeToContentNotifications(moduleDefinition, serverNotificationSubscriptions);

        registerModule(moduleDefinition);

    }

    /**
     * Adds a subscription for specific types of Content notification. Should be called before Initialise to avoid a race condition resulting in missed notifications.
     *
     * @param moduleDefinition                          The module details.
     * @param serverNotificationSubscription The subscription to register for this module.
     */
    public static void subscribeToContentNotifications(ModuleDefinition moduleDefinition, Subscription<ServerNotification> serverNotificationSubscription) {

        List<Subscription<ServerNotification>> list = new LinkedList<>();
        list.add(serverNotificationSubscription);

        SubscriptionController.subscribeToContentNotifications(moduleDefinition, list);

        registerModule(moduleDefinition);

    }

    /**
     * API for Donky module usage only. Adds a subscription for specific types of Donky notification. Should be called before Initialise to avoid a race condition resulting in missed notifications.
     *
     * @param moduleDefinition                          The module details.
     * @param serverNotificationSubscriptions The subscriptions to register for this module.
     * @param autoAcknowledge True if Core SDK should acknowledge the notification.
     */
    public static void subscribeToDonkyNotifications(ModuleDefinition moduleDefinition, List<Subscription<ServerNotification>> serverNotificationSubscriptions, boolean autoAcknowledge) {

        SubscriptionController.subscribeToDonkyNotifications(moduleDefinition, serverNotificationSubscriptions, autoAcknowledge);

        registerModule(moduleDefinition);

    }

    /**
     * API for Donky module usage only. Adds a subscription for specific types of Donky notification. Should be called before Initialise to avoid a race condition resulting in missed notifications.
     *
     * @param moduleDefinition                          The module details.
     * @param serverNotificationSubscription The subscription to register for this module.
     * @param autoAcknowledge True if Core SDK should acknowledge the notification.
     */
    public static void subscribeToDonkyNotifications(ModuleDefinition moduleDefinition, Subscription<ServerNotification> serverNotificationSubscription, boolean autoAcknowledge) {

        List<Subscription<ServerNotification>> list = new LinkedList<>();
        list.add(serverNotificationSubscription);

        SubscriptionController.subscribeToDonkyNotifications(moduleDefinition, list, autoAcknowledge);

        registerModule(moduleDefinition);

    }

    /**
     * Subscribes to outbound notifications. Callbacks are made during the Synchronise flow.
     *
     * @param moduleDefinition                            The module details.
     * @param outboundNotificationSubscriptions The subscriptions to register for this module.
     */
    public static void subscribeToOutboundNotifications(ModuleDefinition moduleDefinition, List<Subscription<OutboundNotification>> outboundNotificationSubscriptions) {

        SubscriptionController.subscribeToOutboundNotifications(moduleDefinition, outboundNotificationSubscriptions);

        registerModule(moduleDefinition);

    }

    /**
     * Subscribes to outbound notifications. Callbacks are made during the Synchronise flow.
     *
     * @param moduleDefinition                            The module details.
     * @param outboundNotificationSubscription The subscription to register for this module.
     */
    public static void subscribeToOutboundNotifications(ModuleDefinition moduleDefinition, Subscription<OutboundNotification> outboundNotificationSubscription) {

        List<Subscription<OutboundNotification>> list = new LinkedList<>();
        list.add(outboundNotificationSubscription);

        SubscriptionController.subscribeToOutboundNotifications(moduleDefinition, list);

        registerModule(moduleDefinition);

    }

    /**
     * Called to register a module with the core. Enables a module that does not use any notifications to be discoverable. Not required if notifications are being used.
     * Will keep the module definition for additional processing when SDK will be initialised.
     *
     * @param moduleDefinition Definition of the Module to register.
     */
    public static void registerModule(ModuleDefinition moduleDefinition) {

        if (!isInitialised()) {
            moduleDefinitionsToRegister.add(moduleDefinition);
        } else {
            DonkyDataController.getInstance().getConfigurationDAO().addModule(moduleDefinition);
        }

    }

    /**
     * The Core SDK will act as a Service Provider, allowing modules to register ‘services’ that other modules can consume.  This is largely to enable other Donky modules to interoperate.
     * Only a single instance of any given type can be tracked.  This will replace any previously registered instances of the given type.
     *
     * @param type     The type of the service being registered.
     * @param instance Instance of the specified type.
     */
    public void registerService(String type, Object instance) {

        if (type != null && instance != null) {
            ServiceWrapper service = new ServiceWrapper(type, instance);
            services.put(type, service);
        }
    }

    /**
     * Gets a reference to a registered service.
     *
     * @param type The type of the required service.
     * @return Instance of the specified type if registered, else null.
     */
    public Object getService(String type) {

        if (type != null) {

            ServiceWrapper wrapper = services.get(type);

            if (wrapper != null) {
                return wrapper.getServiceInstance();
            }

        }
        return null;
    }

    /**
     * Unregisters a service.
     *
     * @param type The type of the service to unregister.
     */
    public void unregisterService(String type) {

        if (type != null) {
            services.remove(type);
        }

    }

    /**
     * Returns details of all registered modules.
     *
     * @return All the registered module definitions.
     */
    public List<ModuleDefinition> getRegisteredModules() {

        Map<String, String> modulesMap = DonkyDataController.getInstance().getConfigurationDAO().getModules();

        List<ModuleDefinition> moduleList = new LinkedList<>();

        for (String key : modulesMap.keySet()) {
            moduleList.add(new ModuleDefinition(key, modulesMap.get(key)));
        }

        return moduleList;
    }

    /**
     * Checks is a specific module is registered.
     *
     * @param name              The name of the module to check for.
     * @param minimumVersionStr The minimum version required.
     * @return True is a matching module is registered, otherwise false.
     */
    public boolean isModuleRegistered(String name, String minimumVersionStr) {

        Map<String, String> modulesMap = DonkyDataController.getInstance().getConfigurationDAO().getModules();

        if (modulesMap.containsKey(name)) {

            if (!TextUtils.isEmpty(minimumVersionStr)) {

                ModuleVersion minModuleVersion = new ModuleVersion(minimumVersionStr);

                ModuleVersion moduleVersion = new ModuleVersion(modulesMap.get(name));

                if (moduleVersion.isGreaterThanOrEqual(minModuleVersion)) {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper class to compare module versions.
     */
    private class ModuleVersion {

        String versionStr;

        List<Integer> versionList;

        public ModuleVersion(String version) {

            this.versionStr = version;
            versionList = new ArrayList<>();

            parseToList(versionStr);
        }

        /**
         * Compares two module version strings, assuming format x.x.x.x
         *
         * @param verToCompare Version to compare with
         * @return Returns true if module version is greater then the one provided as a parameter
         */
        public boolean isGreaterThanOrEqual(ModuleVersion verToCompare) {

            if (versionList != null && verToCompare != null && verToCompare.getVersionList() != null) {
                for (int i = 0; i < versionList.size(); i++) {

                    if (versionList.get(i) != null && verToCompare.getVersionList().get(i) != null) {

                        if (versionList.get(i) > verToCompare.getVersionList().get(i)) {
                            return true;
                        } else if (versionList.get(i) < verToCompare.getVersionList().get(i)) {
                            return false;
                        } else if (versionList.size() - 1 == i) {
                            return true;
                        }

                    } else if (versionList.get(i) != null) {
                        if (versionList.get(i).intValue() > 0) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (verToCompare.getVersionList().get(i) != null) {
                        return verToCompare.getVersionList().get(i).intValue() < 0;
                    } else {
                        return false;
                    }
                }
            }

            return false;
        }

        /**
         * Parse Module version String to List of subsequent version numbers, assuming String format x.x.x.x
         *
         * @param version Version String to parse.
         */
        private void parseToList(String version) {

            try {

                String[] versionArrayTemp = version.split("\\.");

                for (String aVersionArrayTemp : versionArrayTemp) {
                    versionList.add(Integer.valueOf(aVersionArrayTemp));
                }

            } catch (Exception e) {
                log.error("Error parsing module version", e);
            }
        }

        public List<Integer> getVersionList() {
            return versionList;
        }
    }

    /**
     * Class to hold service subscription.
     */
    private class ServiceWrapper {

        String type;
        Object instance;

        /**
         * Class constructor to hold service subscription.
         *
         * @param type     Class type of the service instance.
         * @param instance Service instance.
         */
        ServiceWrapper(String type, Object instance) {
            this.type = type;
            this.instance = instance;
        }

        /**
         * @return Type of subscribed service.
         */
        public String getType() {
            return type;
        }

        /**
         * @return Get Service instance
         */
        public Object getServiceInstance() {
            return instance;
        }
    }

    /**
     * Post error message to result listener the on main thread.
     *
     * @param mainThreadHandler Main thread handler.
     * @param donkyListener     Result listener.
     * @param donkyException    Exception to pass to listener callback.
     */
    public void postError(final Handler mainThreadHandler, final DonkyListener donkyListener, final DonkyException donkyException) {
        if (donkyListener != null) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (donkyException != null) {
                        donkyListener.error(donkyException, donkyException.getValidationErrors());
                    } else {
                        donkyListener.error(null, null);
                    }
                }
            });
        }
    }

    /**
     * Post error message to result listener the on main thread.
     *
     * @param mainThreadHandler Main thread handler.
     * @param donkyListener     Result listener.
     */
    public void postSuccess(final Handler mainThreadHandler, final DonkyListener donkyListener) {
        if (donkyListener != null) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.success();
                }
            });
        }
    }
}