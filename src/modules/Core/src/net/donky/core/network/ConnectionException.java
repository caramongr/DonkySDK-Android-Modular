package net.donky.core.network;

import net.donky.core.DonkyException;

/**
 * Created by Marcin Swierczek
 * 18/04/15
 * Dynmark International Ltd.
 */
public class ConnectionException extends DonkyException {

    /**
     * @param description Exception description.
     */
    public ConnectionException(String description) {
        super(description);
    }
}
