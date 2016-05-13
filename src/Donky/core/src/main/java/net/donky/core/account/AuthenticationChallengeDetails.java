package net.donky.core.account;

/**
 * Encapsulates the authentication details for the auth network call.
 *
 * Created by Marcin Swierczek
 * 10/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AuthenticationChallengeDetails {

    private String correlationId;

    private String nonce;

    private String token;

    public AuthenticationChallengeDetails(String correlationId, String nonce, String token) {
        this.correlationId = correlationId;
        this.nonce = nonce;
        this.token = token;
    }

    /**
     * Used as single use id for Donky network authentication process.
     * @return
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Single use id for external server auth process.
     * @return
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * ID token obtained from integrator through {@link net.donky.core.DonkyAuthenticator} and {@link DonkyAuthClient}
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets ID token obtained from integrator through {@link net.donky.core.DonkyAuthenticator} and {@link DonkyAuthClient}.
     */
    public void setToken(String token) {
        this.token = token;
    }
}
