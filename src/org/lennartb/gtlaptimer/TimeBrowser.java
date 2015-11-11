package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;

/**
 * Displays all previously inserted times of a specific track in a specific game.
 */
public class TimeBrowser extends OptionMenuActivity implements AdapterView.OnItemClickListener {
    private SQLiteDatabase database;
    private ListView timeBrowser;
    private SharedPreferences preferences;
    private int game;
    private int track;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_browser);

        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
        game = preferences.getInt("Game", -1);
        track = preferences.getInt("Track", -1);
        database = DBHelper.getInstance(this).getDatabase();

        timeBrowser = (ListView) findViewById(R.id.timeListView);
        timeBrowser.setClickable(true);
        timeBrowser.setOnItemClickListener(this);

        populateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Fetches laptime, car and date for the game and track for the database and puts it into the ListView.
     */
    private void populateListView() {
        int reverse = preferences.getInt("Reverse", -1);
        Cursor selectedItemCursor = DBQueries.getTimesForTrack(game, track, reverse, "liststring", database);
        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                selectedItemCursor,
                new String[]{"liststring"},
                new int[]{android.R.id.text1},
                0);
        timeBrowser.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Cursor c = (Cursor) timeBrowser.getAdapter().getItem(position);
        preferences.edit().putInt("TimeSetID", c.getInt(c.getColumnIndex("_id"))).commit();
        Intent settingsOverview = new Intent(TimeBrowser.this, SettingsOverview.class);
        startActivity(settingsOverview);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}