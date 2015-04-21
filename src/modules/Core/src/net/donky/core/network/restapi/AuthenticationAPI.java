package net.donky.core.network.restapi;

import net.donky.core.network.TagDescription;
import net.donky.core.network.restapi.authentication.Login;
import net.donky.core.network.restapi.authentication.LoginResponse;
import net.donky.core.network.restapi.authentication.Register;
import net.donky.core.network.restapi.authentication.RegisterResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;

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

    @Headers({"Accept: application/json"})
    @POST("/api/registration")
    RegisterResponse register(@Header("ApiKey") String apiKey, @Body Register register);

    @Headers({"Accept: application/json"})
    @POST("/api/authentication/gettoken")
    LoginResponse login(@Header("ApiKey") String apiKey, @Body Login login);

    /*
     *  Asynchronous REST calls
    */

    @Headers({"Accept: application/json"})
    @POST("/api/registration")
    void register(@Header("ApiKey") String apiKey, @Body Register register, Callback<RegisterResponse> cb);

    @Headers({"Accept: application/json"})
    @POST("/api/authentication/gettoken")
    void login(@Header("ApiKey") String apiKey, @Body Login login, Callback<LoginResponse> cb);

}
