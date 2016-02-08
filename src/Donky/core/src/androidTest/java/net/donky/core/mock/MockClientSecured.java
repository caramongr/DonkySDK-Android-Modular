package net.donky.core.mock;

import android.net.Uri;

import net.donky.core.logging.DLog;

import java.io.IOException;
import java.util.Collections;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by Marcin Swierczek
 * 01/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockClientSecured implements Client {

    @Override
    public Response execute(Request request) throws IOException {
        Uri uri = Uri.parse(request.getUrl());

        new DLog("MockClient").debug("fetching uri: " + uri.toString());

        String responseString = "";

        if(uri.getPath().equals("/api/registration")) {

            responseString = "JSON STRING HERE";

        } else if (uri.getPath().equals("/api/authentication/gettoken")) {

            responseString = "OTHER JSON RESPONSE STRING";
        }

        return new Response(request.getUrl(), 200, "nothing", Collections.EMPTY_LIST, new TypedByteArray("application/json", responseString.getBytes()));
    }
}