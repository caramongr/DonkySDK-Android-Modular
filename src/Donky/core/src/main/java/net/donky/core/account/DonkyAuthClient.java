package net.donky.core.account;

/**
 * Interface for a client class to obtain ID token from the integrator to be used to authenticate user on the Donky network.
 *
 * Created by Marcin Swierczek
 * 10/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyAuthClient {

    /**
     * Sets the auth ID token to be used to authenticate user on Donky Network.
     * @param token Auth ID token
     */
    void authenticateWithToken(String token);

}
