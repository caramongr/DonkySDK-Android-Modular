package net.donky.core.messaging.logic.database;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by MobileTeam on 19/08/15.
 */
public class BaseDAO {

    /**
     * Translate int to boolean.
     */
    public static boolean toBoolean(int i) {
        return i > 0;
    }

    /**
     * Translate Map of Strings to json string.
     */
    public static String toJson(Map<String, String> map) {

        if (map != null) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toString();
        } else {
            return null;
        }

    }

    /**
     * Translate List of Strings to json string.
     */
    public static String toJson(List<String> list) {

        if (list != null) {
            String json = new Gson().toJson(list);
            return json;
        } else {
            return null;
        }

    }

    /**
     * Translate json string to Map of Strings.
     */
    public static Map<String, String> toMap(String json) throws JSONException {

        if (!TextUtils.isEmpty(json)) {
            HashMap<String, String> map = new HashMap<String, String>();

            JSONObject jObject = new JSONObject(json);

            Iterator<?> keys = jObject.keys();

            while (keys.hasNext()) {

                String key = (String) keys.next();
                String value = jObject.getString(key);
                map.put(key, value);

            }

            return map;
        }

        return null;
    }

    /**
     * Translate json string to List of Strings.
     */
    public static List<String> toList(String json) {

        if (!TextUtils.isEmpty(json)) {

            Type token = new TypeToken<LinkedList<String>>() {}.getType();

            LinkedList<String> result = new Gson().fromJson(json, token);

            return result;

        }

        return null;
    }

    protected String makePlaceholders(int len) {

        if (len > 0) {

            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }

        return null;
    }

    protected void reportError(Exception exception, DonkyListener listener) {
        if (listener != null) {
            DonkyException donkyException = new DonkyException(exception.getLocalizedMessage());
            donkyException.initCause(exception);
            listener.error(donkyException, null);
        }
    }

    protected void reportError(Exception exception, DonkyResultListener listener) {
        reportError(exception,  null, listener);
    }

    protected void reportError(Exception exception,  Map<String, String> validationErrors, DonkyResultListener listener) {
        if (listener != null) {
            DonkyException donkyException = new DonkyException(exception.getLocalizedMessage());
            donkyException.initCause(exception);
            listener.error(donkyException, validationErrors);
        }
    }

    protected void reportError(Exception exception) {
    }
}