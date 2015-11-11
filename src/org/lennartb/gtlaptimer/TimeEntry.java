package org.lennartb.gtlaptimer;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;
import org.lennartb.gtlaptimer.Helpers.L;
import org.lennartb.gtlaptimer.Helpers.Utilities;
import org.lennartb.gtlaptimer.Objects.Part;
import org.lennartb.gtlaptimer.Objects.PartProfile;
import org.lennartb.gtlaptimer.Objects.TuneProfile;
import org.lennartb.gtlaptimer.Objects.Tuneable;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The Activity in which the user enters a new lap time. All previously entered data is here written into the SQLite database.
 */
public class TimeEntry extends OptionMenuActivity implements View.OnClickListener {
    private SharedPreferences preferences;
    private SQLiteDatabase database;
    private EditText commentBox;
    private int selectedTimeSetId;
    private boolean fromExisting;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_entry);

        initializeUI();
    }

    @Override
    public void onBackPressed() {
        if (preferences.getBoolean("fromExisting", false)) {
            Intent tuningSelection = new Intent(this, TuningSelection.class);
            startActivity(tuningSelection);
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void initializeUI() {
        database = DBHelper.getInstance(this).getDatabase();
        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
        commentBox = (EditText) findViewById(R.id.commentBox);

        initialiseNumberPickers();

        // Get current track and car name and put them in a header
        TextView informationHeader = (TextView) findViewById(R.id.overview);
        String trackname = DBQueries.getTracknameFromId(preferences.getInt("Track", -1), database);
        String car = DBQueries.getManufacturerAndModelFromId(preferences.getInt("Car", -1), database);

        if (preferences.getInt("Reverse", -1) == 0) {
            informationHeader.setText(trackname + " - " + car);
        }
        else {
            informationHeader.setText(trackname + " (Reverse) - " + car);
        }

        restoreSavedValues();
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        commentBox.setText(inState.getString("comment"));
        restoreNumberPicker(inState.getStringArray("numbers"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Get time, remove colons, split on anything (one char into one array element).
        outState.putStringArray("numbers", getTime().replace(":", "").split("(?!^)"));
        outState.putString("comment", commentBox.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Restores saved NumberPickers values. For use on orientation change etc.
     *
     * @param numbers Array including the value for each picker, 0 being the leftmost.
     */

    private void restoreNumberPicker(String[] numbers) {
        ((NumberPicker) findViewById(R.id.tenMinutePicker)).setValue(Integer.parseInt(numbers[0]));
        ((NumberPicker) findViewById(R.id.oneMinutePicker)).setValue(Integer.parseInt(numbers[1]));
        ((NumberPicker) findViewById(R.id.tenSecondPicker)).setValue(Integer.parseInt(numbers[2]));
        ((NumberPicker) findViewById(R.id.oneSecondPicker)).setValue(Integer.parseInt(numbers[3]));
        ((NumberPicker) findViewById(R.id.tenthMillisecondPicker)).setValue(Integer.parseInt(numbers[4]));
        ((NumberPicker) findViewById(R.id.hundredthMillisecondPicker)).setValue(Integer.parseInt(numbers[5]));
        ((NumberPicker) findViewById(R.id.thousandsMillisecondPicker)).setValue(Integer.parseInt(numbers[6]));
    }

    /**
     * Gets saved values from preferences and database to re-populate the layout.
     */

    private void restoreSavedValues() {
        // Find previous times and put them into the layout
        populateTimes();
        fromExisting = preferences.getBoolean("fromExisting", false);
        // If there is a saved comment, put it into the EditText box
        selectedTimeSetId = preferences.getInt("SelectedTimeSet", -1);
        if (selectedTimeSetId != -1) {
            commentBox.setText(DBQueries.getCommentFromTimeset(selectedTimeSetId, database));
        }
    }

    /**
     * Manually set the min/max values for the NumberPickers. No idea why Google didn't provide XML properties for that...
     */

    private void initialiseNumberPickers() {

        ViewGroup pickerElements = (ViewGroup) findViewById(R.id.pickerLayout);
        for (int j = 0; j < pickerElements.getChildCount(); j++) {
            View grandchild = pickerElements.getChildAt(j);
            if (grandchild instanceof NumberPicker) {

                ((NumberPicker) grandchild).setMaxValue(9);
                ((NumberPicker) grandchild).setMinValue(0);
                if (grandchild.getId() == R.id.tenSecondPicker) {
                    ((NumberPicker) grandchild).setMaxValue(5);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {

        database.beginTransaction();
        try {
            insertValues(getTime());
            database.setTransactionSuccessful();
        } catch (Exception e) {
            L.e("Error inserting values into database!");
            L.e(e.getMessage());
        } finally {
            database.endTransaction();
        }
        populateTimes();
    }

    /**
     * Populates the two ListViews with both the top and latest times.
     */

    private void populateTimes() {

        int trackID = preferences.getInt("Track", -1);
        int reverse = preferences.getInt("Reverse", -1);
        Cursor topTimes = DBQueries.getTopTimes(trackID, reverse, "liststring", 3, database);
        Cursor latestTimes = DBQueries.getLatestTimes(trackID, reverse, "liststring", 3, database);

        ListView latestTimeList = (ListView) findViewById(R.id.latestTimes);
        ListView topTimesList = (ListView) findViewById(R.id.topTimes);

        ListAdapter topTimesAdapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                topTimes,
                new String[]{topTimes.getColumnName(topTimes.getColumnIndex("liststring"))},
                new int[]{android.R.id.text1},
                0);
        ListAdapter latestTimesAdapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                latestTimes,
                new String[]{latestTimes.getColumnName(latestTimes.getColumnIndex("liststring"))},
                new int[]{android.R.id.text1},
                0);

        topTimesList.setAdapter(topTimesAdapter);
        latestTimeList.setAdapter(latestTimesAdapter);

        Utilities.setListViewHeightFromContent(topTimesList);
        Utilities.setListViewHeightFromContent(latestTimeList);
    }

    /**
     * Combines the numbers from all NumberPickers into a readable time string.
     *
     * @return A string in the format mm:ss:msmsms.
     */

    private String getTime() {
        String time = "";
        NumberPicker np = (NumberPicker) findViewById(R.id.tenMinutePicker);
        time = time.concat(String.valueOf(np.getValue()));
        np = (NumberPicker) findViewById(R.id.oneMinutePicker);
        time = time.concat(String.valueOf(np.getValue()));
        time = time.concat(":");
        np = (NumberPicker) findViewById(R.id.tenSecondPicker);
        time = time.concat(String.valueOf(np.getValue()));
        np = (NumberPicker) findViewById(R.id.oneSecondPicker);
        time = time.concat(String.valueOf(np.getValue()));
        time = time.concat(":");
        np = (NumberPicker) findViewById(R.id.tenthMillisecondPicker);
        time = time.concat(String.valueOf(np.getValue()));
        np = (NumberPicker) findViewById(R.id.hundredthMillisecondPicker);
        time = time.concat(String.valueOf(np.getValue()));
        np = (NumberPicker) findViewById(R.id.thousandsMillisecondPicker);
        time = time.concat(String.valueOf(np.getValue()));

        return time;
    }

    /**
     * Inserts a TimeSet into the database.
     * <p>All other parameters (except the time) are fetched from the SharedPreferences.</p>
     *
     * @param time The time to insert.
     */

    private void insertValues(String time) {

        // Get previously saved values.
        String gameid = String.valueOf(preferences.getInt("Game", -1));
        String carid = String.valueOf(preferences.getInt("Car", -1));
        String trackid = String.valueOf(preferences.getInt("Track", -1));
        String reverse = String.valueOf(preferences.getInt("Reverse", -1));

        ContentValues cv = new ContentValues();

        // JSON objects for deserialization of Parts and Tuning objects.
        Gson gson = new Gson();
        Type genericType = new TypeToken<PartProfile>() {
        }.getType();

        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yy HH:mm:ss");
        PartProfile parts;

        // Check if the time is derived from an existing entry.
        if (fromExisting) {
            parts = DBQueries.getPartsForTimeSet(selectedTimeSetId, database);
        }
        else {
            String prefs2 = preferences.getString("Parts", "PARTS_ERROR");
            parts = gson.fromJson(prefs2, genericType);
        }

        // Put values together to commit them to the database.
        cv.put("gameid", gameid);
        cv.put("carid", carid);
        cv.put("trackid", trackid);
        cv.put("time", time);
        cv.put("reverse", reverse);
        cv.put("date", dateFormat.format(new Date()));
        cv.put("comment", commentBox.getText().toString());

        // Commit values and keep PK of inserted row.
        long primaryKeyvalue = database.insert("TimeSet", null, cv);
        cv.clear();

        // Commit installed parts into the database.
        HashMap<Integer, Part> installedParts = parts.getInstalledParts();

        for (Map.Entry<Integer, Part> entry : installedParts.entrySet()) {
            cv.put("timesetid", primaryKeyvalue);
            cv.put("partid", entry.getValue().getPartId());
            database.insert("TimeSet_Parts", null, cv);
            cv.clear();
        }

        // Fetch information of applied tuning values.
        genericType = new TypeToken<TuneProfile>() {
        }.getType();

        TuneProfile tunes;
        if (fromExisting) {
            tunes = DBQueries.getTuningForTimeSet(selectedTimeSetId, database);
        }
        else {
            String prefs2 = preferences.getString("Tuning", "TUNING_ERROR");
            tunes = gson.fromJson(prefs2, genericType);
        }

        // Commit tuning values into database.
        Map<Tuneable, Double> tuning = tunes.getAppliedTuning();
        for (HashMap.Entry<Tuneable, Double> entry : tuning.entrySet()) {
            cv.put("timesetid", primaryKeyvalue);
            cv.put("tuneid", entry.getKey().getTuneId());
            cv.put("progress", entry.getValue());
            database.insert("TimeSet_Tuning", null, cv);
            cv.clear();
        }
    }
}