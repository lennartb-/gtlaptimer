package org.lennartb.gtlaptimer.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.lennartb.gtlaptimer.MainActivity;
import org.lennartb.gtlaptimer.Objects.PartProfile;
import org.lennartb.gtlaptimer.Objects.TuneProfile;

import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 17.04.13
 * Time: 14:42
 */
public class Utilities {

    /**
     * Provides helper methods that don't fit into any particular class, or are interesting for debugging etc.
     */

    private Utilities() {
    }

    /**
     * Serializes an object as JSON string.
     *
     * @param o           The object to serialize.
     * @param genericType The type of the object. Get with <i>Type genericType = new TypeToken< YOUR_OBJECT_TYPE >() {}.getType();</i>
     * @return The object as JSON serialized String.
     */

    public static String serializeWithJSON(Object o, Type genericType) {
        Gson gson = new Gson();
        return gson.toJson(o, genericType);
    }

    public static String serializeComplexObjectWithJSON(Object o, Type genericType) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(o, genericType);
    }

    /**
     * Resets all shared preferences for the creation of a new time entry. The selected game is preserved.
     *
     * @param context Application Context.
     */
    public static void resetPreferencesForNewEntry(Context context) {
        L.i("Called reset prefs");
        SharedPreferences preferences = context.getSharedPreferences(MainActivity.PACKAGE_NAME, Context.MODE_PRIVATE);
        int game = preferences.getInt("Game", -1);
        preferences.edit().clear().commit();
        preferences.edit().putInt("Game", game).commit();
        preferences.edit().putString("Action", "Time").commit();
    }

    /**
     * Prints all currently stored preference values to Logcat.
     *
     * @param context Application Context.
     */

    public static void printPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MainActivity.PACKAGE_NAME, Context.MODE_PRIVATE);
        L.i(preferences.getAll().toString());
    }

    /**
     * Counts the number of decimal places of a double value.
     *
     * @param number The number.
     * @return The number of decimal places.
     */

    public static int getNumberOfDecimalPlaces(double number) {
        String stringifiedNumber = Double.toString(number);
        String[] splitResult = stringifiedNumber.split("\\.");
        return splitResult[1].length();
    }

    /**
     * Creates a horizontal rule with a specified color and thickness.
     *
     * @param color     The color as a String, in the format of {@link android.graphics.Color#parseColor(String) parseColor}.
     * @param thickness Thickness in pixels.
     * @return A rule with the specified parameters
     */

    public static View getRule(String color, int thickness, Context context) {
        View ruler = new View(context);
        ruler.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, thickness));
        ruler.setBackgroundColor(Color.parseColor(color));
        ruler.setPadding(0, 10, 0, 10);

        return ruler;
    }

    /**
     * Adjusts a ListView's height to match its content. Displays all entries without having the ListView itself to scroll.
     * Only for use with ListViews that have very few (< 10) items.<br><br>
     * Source: http://stackoverflow.com/a/3495908/368354
     *
     * @param listView The ListView to adjust.
     */

    public static void setListViewHeightFromContent(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            // No adapter, no dice.
            return;
        }

        int overallHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            overallHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = overallHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static String serializeTuneProfile(TuneProfile appliedTuning) {
        Type genericType = new TypeToken<TuneProfile>() {
        }.getType();

        return serializeComplexObjectWithJSON(appliedTuning, genericType);
    }

    public static TuneProfile deserializeTuneProfile(String serializedString) {
        Gson gson = new Gson();
        Type genericType = new TypeToken<TuneProfile>() {
        }.getType();

        return gson.fromJson(serializedString, genericType);
    }

    public static String serializePartProfile(PartProfile installedParts) {
        Type genericType = new TypeToken<PartProfile>() {
        }.getType();

        return serializeComplexObjectWithJSON(installedParts, genericType);
    }

    public static PartProfile deserializePartProfile(String serializedString) {
        Gson gson = new Gson();
        Type genericType = new TypeToken<PartProfile>() {
        }.getType();

        return gson.fromJson(serializedString, genericType);
    }
}
