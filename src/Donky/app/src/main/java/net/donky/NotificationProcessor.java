package net.donky;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.donky.core.network.ServerNotification;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by marcin.swierczek on 13/03/2015.
 */
public class NotificationProcessor {

    private static Model model;

    private static List<ColorListener> listeners;

    // Private constructor. Prevents instantiation from other classes.
    private NotificationProcessor() {

    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final NotificationProcessor INSTANCE = new NotificationProcessor();
    }

    public static NotificationProcessor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context context) {
        listeners = new LinkedList<>();
        model = new Model(context);
    }

    public void processServerNotification(ServerNotification serverNotification) {

        JsonObject data = serverNotification.getData();

        Gson gson = new GsonBuilder().create();
        ChangeColor color = gson.fromJson(data, ChangeColor.class);

        for (ColorListener colorListener : listeners) {
            colorListener.onColorChanged(color.getCustomData().getNewColour(), color.getCustomData().getIntervalSeconds());
        }

        model.saveColor(color.getCustomData().getNewColour());

    }

    public void addListener(ColorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ColorListener listener) {
        listeners.remove(listener);
    }
}
