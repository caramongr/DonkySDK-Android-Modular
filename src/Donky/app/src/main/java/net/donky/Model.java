package net.donky;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by marcin.swierczek on 12/03/2015.
 */
public class Model {

    protected static final String SHARED_PREFERENCES_FILENAME = "PreferencesTestApp";

    public static final String KEY_COLOR = "color";

    Context context;

    public Model(Context context) {
        this.context = context;
    }

    public boolean saveColor(String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COLOR, value);
        return editor.commit();
    }

    public String loadColor() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_COLOR, null);
    }
}
