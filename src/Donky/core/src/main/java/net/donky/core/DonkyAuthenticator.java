package net.donky.core;

import net.donky.core.account.DonkyAuthClient;

/**
 * Implementation of this interface will be used to obtain authentication token. Can be set when initialising.
 *
 * Created by Marcin Swierczek
 * 10/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyAuthenticator {

    /**
     * This method will be called whenever SDK will try to authenticate user on the network. During authenticated user registration and re-authentication calls.
     * @param authClient Pass the authentication token to {@link DonkyAuthClient#authenticateWithToken(String)} method
     * @param challengeOptions Encapsulates challenge details - nonce, and expected user id for the token.
     */
    void onAuthenticationChallenge(final DonkyAuthClient authClient, final ChallengeOptions challengeOptions);

}
