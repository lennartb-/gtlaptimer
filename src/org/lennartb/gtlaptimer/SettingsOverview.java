package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;

/**
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 06.05.13
 * Time: 13:35
 */
public class SettingsOverview extends OptionMenuActivity implements View.OnClickListener {

    private SQLiteDatabase database;
    private ListView partsOverview;
    private ListView tuningOverview;
    private SharedPreferences preferences;
    private int selectedTimeSetId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_overview);

        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
        selectedTimeSetId = preferences.getInt("TimeSetID", -1);
        database = DBHelper.getInstance(this).getDatabase();

        tuningOverview = (ListView) findViewById(R.id.overviewTuning);
        partsOverview = (ListView) findViewById(R.id.overviewParts);
        populateTable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Populates the ListView with all installed parts and applied tuning values from a specific timeset.
     */

    private void populateTable() {
        final String PART_COLUMN_NAME = "fullpartname";
        final String TUNEABLE_COLUMN_NAME = "fulltuningname";

        Cursor selectedItemCursor = DBQueries.getPartNamesForTimeset(selectedTimeSetId, PART_COLUMN_NAME, database);

        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                selectedItemCursor,
                new String[]{PART_COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);
        partsOverview.setAdapter(adapter);

        Cursor tuningQuery = DBQueries.getTuningNamesForTimeSet(selectedTimeSetId, TUNEABLE_COLUMN_NAME, database);
        ListAdapter tuningAdapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                tuningQuery,
                new String[]{TUNEABLE_COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);
        tuningOverview.setAdapter(tuningAdapter);
    }

    @Override
    public void onClick(View view) {
        preferences.edit().putInt("SelectedTimeSet", selectedTimeSetId).commit();
        Intent timeEntry = new Intent(this, TimeEntry.class);
        preferences.edit().putInt("Car", DBQueries.getCarIdFromTimeSet(selectedTimeSetId, database)).commit();
        preferences.edit().putBoolean("fromExisting", true).commit();
        startActivity(timeEntry);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}