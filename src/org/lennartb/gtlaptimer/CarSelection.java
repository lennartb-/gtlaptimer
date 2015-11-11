package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.*;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;

/**
 * User: Lennart
 * Date: 05.04.13
 * Time: 13:54
 */
public class CarSelection extends OptionMenuActivity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private ListView carlist;
    private SQLiteDatabase database;
    private SharedPreferences preferences;
    private int game;
    private int track;
    private Spinner spinner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_selection);

        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
        game = preferences.getInt("Game", -1);
        track = preferences.getInt("Track", -1);
        database = DBHelper.getInstance(this).getDatabase();

        // Display selected game and track.
        //TODO display full game and track name
        TextView car = (TextView) findViewById(R.id.carSelectorText);
        car.setText("Game: GT" + game + " Track: " + track);

        // Find cars and manufacturers for this game
        Cursor carCursor = DBQueries.getCarsForGame(game, "mm", database);
        Cursor manufacturerCursor = DBQueries.getManufacturersForGame(game, database);

        spinner = (Spinner) findViewById(R.id.carManufacturerSpinner);
        carlist = (ListView) findViewById(R.id.carListView);

        // Add an "All" selection to the spinner, to be able to specify no filter
        MatrixCursor extras = new MatrixCursor(new String[]{"manufacturer", "_id"});
        extras.addRow(new String[]{"All", "-1"});
        Cursor[] cursors = {extras, manufacturerCursor};
        Cursor extendedCursor = new MergeCursor(cursors);

        // Spinner with manufacturers
        SimpleCursorAdapter manufacturers = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                extendedCursor,
                new String[]{"manufacturer"},
                new int[]{android.R.id.text1},
                0);

        spinner.setAdapter(manufacturers);
        spinner.setOnItemSelectedListener(this);

        // List with car models
        ListAdapter cars = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                carCursor,
                new String[]{"mm"},
                new int[]{android.R.id.text1},
                0);
        carlist.setAdapter(cars);
        carlist.setClickable(true);
        carlist.setOnItemClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent partSelector = new Intent(CarSelection.this, PartSelection.class);

        // Get and save selected Car ID and proceed
        Cursor c = (Cursor) carlist.getAdapter().getItem(position);
        preferences.edit().putInt("Car", c.getInt(c.getColumnIndex("_id"))).commit();

        startActivity(partSelector);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Cursor cursor = (Cursor) spinner.getSelectedItem();
        String manufacturer = cursor.getString(cursor.getColumnIndex("manufacturer"));

        // If the user wants to view all cars, remove the filter ("%" matches all in the SQL query)
        if (manufacturer.equals("All")) {
            manufacturer = "%";
        }

        // Set the result Cursor as source for the ListView
        Cursor selectedItemCursor = DBQueries.getCarsFromManufacturer(game, manufacturer, "mm", database);
        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                selectedItemCursor,
                new String[]{"mm"},
                new int[]{android.R.id.text1},
                0);
        carlist.setAdapter(adapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}