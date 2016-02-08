package net.donky.core.network.restapi;

import com.google.gson.JsonObject;

import net.donky.core.account.RegistrationDetails;
import net.donky.core.network.DiscoveredContact;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.TagDescription;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;
import net.donky.core.network.restapi.secured.GetPlatformUsersRequest;
import net.donky.core.network.restapi.secured.IsValidPlatformUserResponse;
import net.donky.core.network.restapi.secured.SynchroniseResponse;
import net.donky.core.network.restapi.secured.UpdateClient;
import net.donky.core.network.restapi.secured.UpdateDevice;
import net.donky.core.network.restapi.secured.UpdatePushConfiguration;
import net.donky.core.network.restapi.secured.UpdateRegistration;
import net.donky.core.network.restapi.secured.UpdateUser;
import net.donky.core.network.restapi.secured.UploadAssetResponse;
import net.donky.core.network.restapi.secured.UploadLog;
import net.donky.core.network.restapi.secured.UploadLogResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;

/**
 * REST api for secured network service.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface SecuredAPI {

    /*
     * Synchronous REST calls
     */

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/notification/synchronise")
    SynchroniseResponse synchronise(@Header("Authorization") String authorization, @Body TypedInput body);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/notification/{id}")
    ServerNotification getNotification(@Header("Authorization") String authorization, @Path("id") String id);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("api/notification")
    List<ServerNotification> getNotification(@Header("Authorization") String authorization);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/user")
    Void updateUser(@Header("Authorization") String authorization, @Body UpdateUser updateUser);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/device")
    Void updateDevice(@Header("Authorization") String authorization, @Body UpdateDevice updateDevice);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/client")
    Void updateClient(@Header("Authorization") String authorization, @Body UpdateClient updateClient);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration")
    Void updateRegistration(@Header("Authorization") String authorization, @Body UpdateRegistration register);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/push")
    Void updatePush(@Header("Authorization") String authorization, @Body UpdatePushConfiguration updatePushConfiguration);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @DELETE("/api/registration/push")
    Void deletePush(@Header("Authorization") String authorization);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/content/send")
    Void sendContent(@Header("Authorization") String authorization, @Body TypedInput body);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/debuglog")
    UploadLogResponse uploadLog(@Header("Authorization") String authorization, @Body UploadLog body);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/registration/user/tags")
    List<TagDescription> getTags(@Header("Authorization") String authorization);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/user/tags")
    Void updateTags(@Header("Authorization") String authorization,  @Body List<TagDescription> updateTags);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/locationservices/geofence/active")
    List<GeoFence> getAllGeoFences(@Header("Authorization") String authorization);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/trigger/clientconfiguration")
    List<Trigger> getAllTriggers(@Header("Authorization") String authorizationr);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/contact/{externaluserid}")
    IsValidPlatformUserResponse isValidPlatformUser(@Header("Authorization") String authorization, @Path("externaluserid") String externaluserid);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/contact/search")
    List<DiscoveredContact> getPlatformUsers(@Header("Authorization") String authorization, @Body GetPlatformUsersRequest getPlatformUsersRequest);

    @Headers({"DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/asset")
    UploadAssetResponse uploadAsset(@Header("Authorization") String authorization, @Header("AssetMetadata") String assetMetadata, @Body TypedFile file);

    @Headers({"DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/asset")
    UploadAssetResponse uploadAsset(@Header("Authorization") String authorization, @Header("AssetMetadata") String assetMetadata, @Body TypedInput bytes);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/messagehistory/conversation/{id}/{query}")
    List<JsonObject> getMessagesHistory(@Header("Authorization") String authorization, @Path("id") String id, @Path(value="query", encode=false) String query);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/messagehistory/{query}")
    List<JsonObject> getConversationsHistory(@Header("Authorization") String authorization, @Path(value="query", encode=false) String query);

    //contact?profileId=foo&profileId=bar
    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/{query}")
    List<JsonObject> getContacts(@Header("Authorization") String authorization, @Path(value="query", encode=false) String query);

    /*
     * Asynchronous REST calls
     */

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/notification/synchronise")
    void synchronise(@Header("Authorization") String authorization, @Body TypedInput body, Callback<SynchroniseResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/notification/{id}")
    void getNotification(@Header("Authorization") String authorization, @Path("id") String id, Callback<ServerNotification> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/notification")
    void getNotification(@Header("Authorization") String authorization, Callback<List<ServerNotification>> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/user")
    void updateUser(@Header("Authorization") String authorization, @Body UpdateUser updateUser, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/device")
    void updateDevice(@Header("Authorization") String authorization, @Body UpdateDevice updateDevice, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/client")
    void updateClient(@Header("Authorization") String authorization, @Body UpdateClient updateClient, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration")
    void updateRegistration(@Header("Authorization") String authorization, @Body UpdateRegistration register, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/push")
    void updatePush(@Header("Authorization") String authorization, @Body UpdatePushConfiguration updatePushConfiguration, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @DELETE("/api/registration/push")
    void deletePush(@Header("Authorization") String authorization, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/registration")
    void getRegistration(@Header("Authorization") String authorization, @Body RegistrationDetails register, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/content/send")
    void sendContent(@Header("Authorization") String authorization, @Body TypedInput body, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/debuglog")
    void uploadLog(@Header("Authorization") String authorization, @Body UploadLog body, Callback<UploadLogResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/registration/user/tags")
    void getTags(@Header("Authorization") String authorization, Callback<List<TagDescription>> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @PUT("/api/registration/user/tags")
    void updateTags(@Header("Authorization") String authorization, @Body List<TagDescription> updateTags, Callback<Void> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/locationservices/geofence/active")
    void getAllGeoFences(@Header("Authorization") String authorization, Callback<List<GeoFence>> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/trigger/clientconfiguration")
    void getAllTriggers(@Header("Authorization") String authorization, Callback<List<Trigger>> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/contact/{externaluserid}")
    void isValidPlatformUser(@Header("Authorization") String authorization, @Path("externaluserid") String externaluserid, Callback<IsValidPlatformUserResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/contact/search")
    void getPlatformUsers(@Header("Authorization") String authorization, @Body GetPlatformUsersRequest getPlatformUsersRequest, Callback<List<DiscoveredContact>> cb);

    @Headers({"DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/asset")
    void uploadAsset(@Header("Authorization") String authorization, @Header("AssetMetadata") String assetMetadata, @Body TypedFile file, Callback<UploadAssetResponse> cb);

    @Headers({"DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/asset")
    void uploadAsset(@Header("Authorization") String authorization, @Header("AssetMetadata") String assetMetadata, @Body TypedInput bytes, Callback<UploadAssetResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/messagehistory/conversation/{id}/{query}")
    void getMessagesHistory(@Header("Authorization") String authorization, @Path("id") String id, @Path(value="query", encode=false) String query, Callback<List<JsonObject>> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/messagehistory/{query}")
    void getConversationsHistory(@Header("Authorization") String authorization, @Path(value="query", encode=false) String query, Callback<List<JsonObject>> cb);

    //contact?profileId=foo&profileId=bar
    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/{query}")
    void getContacts(@Header("Authorization") String authorization, @Path(value="query", encode=false) String query, Callback<List<JsonObject>> cb);

}
