package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.google.gson.reflect.TypeToken;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;
import org.lennartb.gtlaptimer.Helpers.Utilities;
import org.lennartb.gtlaptimer.Objects.Part;
import org.lennartb.gtlaptimer.Objects.PartProfile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Lennart
 * Date: 06.04.13
 * Time: 11:52
 */
public class PartSelection extends OptionMenuActivity implements SeekBar.OnSeekBarChangeListener {

    private SharedPreferences preferences;
    private SQLiteDatabase database;
    private int game;
    private PartProfile installedParts;
    private ArrayList<Part> availableParts;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_selection);

        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
        game = preferences.getInt("Game", -1);
        installedParts = new PartProfile(game, preferences.getInt("Car", -1), preferences.getInt("Track", -1));
        availableParts = new ArrayList<Part>();
        database = DBHelper.getInstance(this).getDatabase();

        initializeLayout();

        // Fill layout with values if we come from an existing TimeSet
        if (preferences.getBoolean("fromExisting", false)) {
            installedParts = DBQueries.getPartsForTimeSet(preferences.getInt("SelectedTimeSet", -1), database);
            fillWithValues(installedParts);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_reset:
                resetValues();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initializes all available parts for the current game.
     */

    private void initializeParts() {
        Cursor gameParts = DBQueries.getPartsForGame(game, database);
        int groupid;
        int partid;

        // Fetch details for each part from the Cursor, and create a new Part object.
        while (gameParts.moveToNext()) {
            groupid = gameParts.getInt(gameParts.getColumnIndex("groupid"));
            partid = gameParts.getInt(gameParts.getColumnIndex("_id"));

            Part newCurrentPart = new Part(
                    gameParts.getString(gameParts.getColumnIndex("name")),
                    gameParts.getString(gameParts.getColumnIndex("levelname")),
                    gameParts.getString(gameParts.getColumnIndex("category")),
                    groupid,
                    game,
                    partid,
                    DBQueries.getPositionInGroupPrepared(partid, groupid, database)
            );
            availableParts.add(newCurrentPart);
        }
    }

    /**
     * Finds the upgraded part that corresponds to the position on the Seekbar of the group.
     *
     * @param progress Progess of the Seekbar/"Upgrade Level"
     * @param groupid  ID of the group.
     * @return The upgraded Part the user wants to install.
     */

    private Part getUpgradePart(int progress, int groupid) {

        for (Part part : availableParts) {
            if (part.getGroupId() == groupid && part.getGroupLevel() == progress) {
                return part;
            }
        }

        return null;
    }

    /**
     * Creates the basic layout for this Activity.
     */

    private void initializeLayout() {
        TableLayout tl = (TableLayout) findViewById(R.id.partsTable);
        int previousGroupId = -1;
        String currentPart;
        int currentNumberOfParts;
        int currentGroupId;
        String currentCategory;
        String previousCategory = "none";
        TableRow.LayoutParams defaultParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        initializeParts();

        // Iterate over all available parts.
        for (Part availablePart : availableParts) {

            currentGroupId = availablePart.getGroupId();
            currentCategory = availablePart.getCategory();

            // Add a category header for each new section of parts.
            if (!currentCategory.equals(previousCategory)) {
                tl.addView(setUpNewCategory(currentCategory, defaultParams));
            }
            previousCategory = currentCategory;

            // Add a new table row for each group of parts.
            if (previousGroupId < currentGroupId) {
                currentNumberOfParts = DBQueries.getPartsPerGroup(game, currentGroupId, database);
                currentPart = availablePart.getPartName();

                // Set up table row.
                TableRow tr = setUpNewTableRow(defaultParams);

                // First column (Partname).
                tr.addView(setUpNewPartname(currentPart, currentGroupId, defaultParams));

                // Second column (Seekbar).
                tr.addView(setUpNewSeekBar(currentNumberOfParts, availablePart, defaultParams));

                // Third column (installed level of part).
                tr.addView(setUpNewPartlevel(currentPart, currentGroupId, defaultParams));

                previousGroupId = currentGroupId;
                tl.addView(tr);
            }
        }
    }

    /**
     * Initialises a new formatted table row.
     *
     * @param defaultParams Layout parameters.
     * @return A new TableRow with the assigned properties.
     */

    private TableRow setUpNewTableRow(TableRow.LayoutParams defaultParams) {
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(defaultParams);
        tr.setGravity(Gravity.CENTER_VERTICAL);
        tr.setPadding(0, 3, 0, 3);
        return tr;
    }

    /**
     * Initialises a new category table row.
     *
     * @param currentCategory The category name.
     * @param defaultParams   Layout parameters.
     * @return A new TableRow containing the category name.
     */

    private TableRow setUpNewCategory(String currentCategory, TableRow.LayoutParams defaultParams) {
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(defaultParams);
        tr.setPadding(0, 10, 0, 10);
        TextView categoryName = new TextView(this);
        categoryName.setText(currentCategory);
        categoryName.setLayoutParams(defaultParams);
        categoryName.setTypeface(categoryName.getTypeface(), Typeface.BOLD);
        categoryName.setGravity(Gravity.CENTER_VERTICAL);
        categoryName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        categoryName.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        tr.addView(categoryName);
        return tr;
    }

    /**
     * Initialises a new TextView to display the part level in the layout.
     *
     * @param currentPart    The partname.
     * @param currentGroupId The group ID of the part.
     * @param defaultParams  Layout parameters.
     * @return A new TextView with the assigned properties.
     */

    private TextView setUpNewPartlevel(String currentPart, int currentGroupId, TableRow.LayoutParams defaultParams) {
        TextView partLevel = new TextView(this);
        // Label is slightly different when using tires ("Not installed" doesn't make sense).
        if (currentPart.contains("Tire")) {
            partLevel.setText("Default");
        }
        else {
            partLevel.setText("Not installed");
        }
        partLevel.setTag("pl" + currentGroupId);
        partLevel.setLayoutParams(defaultParams);
        return partLevel;
    }

    /**
     * Initialises a new TextView to display a partname in the layout.
     *
     * @param currentPart    The partname.
     * @param currentGroupId The group ID of the part.
     * @param defaultParams  Layout parameters.
     * @return A new TextView with the assigned properties.
     */

    private TextView setUpNewPartname(String currentPart, int currentGroupId, TableRow.LayoutParams defaultParams) {
        TextView partName = new TextView(this);
        partName.setText(currentPart);
        partName.setTag("pn" + currentGroupId);
        partName.setLayoutParams(defaultParams);
        return partName;
    }

    /**
     * Initialises a new SeekBar for use in the layout.
     *
     * @param currentNumberOfParts Number of parts for this SeekBar.
     * @param availablePart        The part for the SeekBar.
     * @param defaultParams        Layout parameters.
     * @return A new SeekBar with the assigned properties.
     */

    private SeekBar setUpNewSeekBar(int currentNumberOfParts, Part availablePart, TableRow.LayoutParams defaultParams) {
        SeekBar levelSeeker = new SeekBar(this);
        levelSeeker.incrementProgressBy(1);
        levelSeeker.setMax(currentNumberOfParts);
        levelSeeker.setProgress(0);
        levelSeeker.setOnSeekBarChangeListener(this);
        levelSeeker.setTag(availablePart);
        levelSeeker.setLayoutParams(defaultParams);
        return levelSeeker;
    }

    /**
     * Resets the values of all layout elements to their default (zero).
     */

    private void resetValues() {
        List<TableRow> tableRows = getTableRows();
        View element;
        for (TableRow currentRow : tableRows) {
            for (int i = 0; i < currentRow.getChildCount(); i++) {
                element = currentRow.getChildAt(i);
                if (element instanceof SeekBar) {
                    ((SeekBar) element).setProgress(0);
                }
            }
        }
    }

    /**
     * Fills the existing layout with previously installed parts.
     *
     * @param installedParts Contains the values that will be inserted.
     */

    private void fillWithValues(PartProfile installedParts) {

        List<TableRow> tableRows = getTableRows();

        SeekBar tempBar;
        View element;

        for (TableRow currentRow : tableRows) {
            for (int i = 0; i < currentRow.getChildCount(); i++) {
                element = currentRow.getChildAt(i);

                if (element instanceof SeekBar) {
                    tempBar = (SeekBar) element;
                    if (tempBar.getTag() instanceof Part) {
                        Part elementPart = (Part) tempBar.getTag();
                        Part savedPart = installedParts.getPart(elementPart.getGroupId());
                        if (savedPart != null) {
                            tempBar.setProgress(savedPart.getGroupLevel() + 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds all table rows in the current layout.
     *
     * @return A List with the found table rows.
     */

    private List<TableRow> getTableRows() {
        TableLayout root = (TableLayout) findViewById(R.id.partsTable);
        List<TableRow> tableRows = new ArrayList<TableRow>();
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof TableRow) {
                tableRows.add((TableRow) root.getChildAt(i));
            }
        }
        return tableRows;
    }

    /**
     * Called when the user changes the level of a part.
     *
     * @param bar      The SeekBar used.
     * @param progress The new value.
     * @param fromUser True if the user altered the progress, false if done programatically.
     */

    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {

        Part part = (Part) bar.getTag();
        int groupsize = -1;
        TextView partLevel = (TextView) findViewById(R.id.partsTable).findViewWithTag("pl" + part.getGroupId());

        // Label is slightly different when using tires ("Not installed" doesn't make sense).
        boolean isTire = part.getPartName().contains("Tire");

        if (progress == 0 && isTire) {
            partLevel.setText("Default");
        }

        // If a part gets removed.
        else if (progress == 0) {
            partLevel.setText("Not Installed");
            installedParts.removePart(part);
        }

        // If a part gets installed.
        else {
            Part upgrade = getUpgradePart(progress - 1, part.getGroupId());
            groupsize = DBQueries.getPartsPerGroup(game, upgrade.getGroupId(), database);
            partLevel.setText(upgrade.getLevelName());
            installedParts.addParts(upgrade);
        }

        // For on/off parts that can only be either installed or not.
        if (groupsize == 1 && progress > 0) {
            partLevel.setText("Installed");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seek) {
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        fillWithValues(Utilities.deserializePartProfile(inState.getString("values")));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("values", Utilities.serializePartProfile(installedParts));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reset, menu);
        return true;
    }

    public void onClick(View v) {
        Intent tuningSelector = new Intent(this, TuningSelection.class);
        Type genericType = new TypeToken<PartProfile>() {
        }.getType();
        String serializedPartProfile = Utilities.serializeComplexObjectWithJSON(installedParts, genericType);

        preferences.edit().putString("Parts", serializedPartProfile).commit();
        startActivity(tuningSelector);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}