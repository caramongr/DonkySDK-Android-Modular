package net.donky.core;

/**
 * Created by Marcin Swierczek
 * 24/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AuthDetails {

    public static final String id1= "marcinswierczek";
    public static final String password1 = "Passw0rd!";

    public static final String id2= "marcinswierczek2";
    public static final String password2 = "Passw0rd!";

    public static final String id3 = "chriswunsch";
    public static final String password3 = "Passw0rd!";

    public static final String id4 = "marcinswierczek2";
    public static final String password4 = "wrong";


    String id;

    String password;

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final AuthDetails INSTANCE = new AuthDetails();
    }

    /**
     * @return Static instance of Donky Core singleton.
     */
    public static AuthDetails getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void setDetails(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}
