package net.donky.core;

/**
 * Encapsulates the authentication challenge details that may be used to obtain the token from auth provider.
 *
 * Created by Marcin Swierczek
 * 25/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ChallengeOptions {

    private String forUserId;

    private String nonce;

    public ChallengeOptions() {
    }

    public ChallengeOptions(String forUserId, String nonce) {
        this.forUserId = forUserId;
        this.nonce = nonce;
    }

    public String getForUserId() {
        return forUserId;
    }

    public void setForUserId(String forUserId) {
        this.forUserId = forUserId;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
