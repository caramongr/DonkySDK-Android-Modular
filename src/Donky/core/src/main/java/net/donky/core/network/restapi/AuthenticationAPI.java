package net.donky.core.network.restapi;

import net.donky.core.network.restapi.authentication.Login;
import net.donky.core.network.restapi.authentication.LoginAuth;
import net.donky.core.network.restapi.authentication.LoginResponse;
import net.donky.core.network.restapi.authentication.Register;
import net.donky.core.network.restapi.authentication.RegisterAuth;
import net.donky.core.network.restapi.authentication.RegisterResponse;
import net.donky.core.network.restapi.authentication.StartAuthResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * REST api for account authentication on the network.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface AuthenticationAPI {

    /*
     *  Synchronous REST calls
    */

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/registration")
    RegisterResponse register(@Header("ApiKey") String apiKey, @Body Register register);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/authentication/gettoken")
    LoginResponse login(@Header("ApiKey") String apiKey, @Body Login login);

    /*
     *  Asynchronous REST calls
    */

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/registration")
    void register(@Header("ApiKey") String apiKey, @Body Register register, Callback<RegisterResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/authentication/gettoken")
    void login(@Header("ApiKey") String apiKey, @Body Login login, Callback<LoginResponse> cb);

    /*
     *  Synchronous REST calls. This calls will require a valid Auth token from auth provider to succeed.
    */

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/authenticatedregistration")
    RegisterResponse register(@Header("ApiKey") String apiKey, @Body RegisterAuth register);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/authentication/reauthenticate")
    LoginResponse login(@Header("ApiKey") String apiKey, @Body LoginAuth login);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/authentication/start")
    StartAuthResponse startAuth(@Header("ApiKey") String apiKey);

    /*
     *  Asynchronous REST calls. This calls will require a valid Auth token from auth provider to succeed.
    */

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/authenticatedregistration")
    void register(@Header("ApiKey") String apiKey, @Body RegisterAuth register, Callback<RegisterResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @POST("/api/authentication/reauthenticate")
    void login(@Header("ApiKey") String apiKey, @Body LoginAuth login, Callback<LoginResponse> cb);

    @Headers({"Accept: application/json", "DonkyClientSystemIdentifier : DonkyAndroidModularSdk"})
    @GET("/api/authentication/start")
    void startAuth(@Header("ApiKey") String apiKey, Callback<StartAuthResponse> cb);

}
