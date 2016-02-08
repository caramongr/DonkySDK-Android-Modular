package net.donky.core.signalr.internal.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class to parse json to Java objects
 *
 * Created by Marcin Swierczek
 * 11/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class JsonParsingHelper {

    /**
     * Convert JSONObject to Map of Objects
     * @throws JSONException
     */
    public static Map<String, Object> convertJSONObjectToMap(JSONObject json) throws JSONException {

        Map<String, Object> retMap = new HashMap<>();

        if(json != JSONObject.NULL) {
            retMap = jsonObjectToMap(json);
        }
        return retMap;
    }

    /**
     * Convert JSONObject to Map of Objects
     * @throws JSONException
     */
    private static Map<String, Object> jsonObjectToMap(JSONObject object) throws JSONException {

        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();

        while(keysItr.hasNext()) {

            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            }

            map.put(key, value);

        }

        return map;
    }

    /**
     * Convert JSONArray to List of Objects
     * @throws JSONException
     */
    private static List<Object> jsonArrayToList(JSONArray array) throws JSONException {

        List<Object> list = new ArrayList<>();

        for(int i = 0; i < array.length(); i++) {

            Object value = array.get(i);

            if(value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            }

            list.add(value);

        }
        return list;
    }


}
