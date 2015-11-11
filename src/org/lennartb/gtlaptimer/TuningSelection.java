package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;
import org.lennartb.gtlaptimer.Helpers.L;
import org.lennartb.gtlaptimer.Helpers.Utilities;
import org.lennartb.gtlaptimer.Objects.TuneProfile;
import org.lennartb.gtlaptimer.Objects.Tuneable;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the tuning options that the user can adjust for the selected game.
 * User: Lennart
 * Date: 08.04.13
 * Time: 18:19
 */
public class TuningSelection extends OptionMenuActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, View.OnFocusChangeListener {

    LinearLayout layout;
    private SharedPreferences preferences;
    private int game;
    private TableLayout table;
    private SQLiteDatabase database;
    private int lastSeqId = -1;
    private EditText finalEditText;
    private TuneProfile appliedTuning;
    private String previousCategory = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
        setContentView(R.layout.activity_tuning_selection);
        layout = (LinearLayout) findViewById(R.id.tuninglayout);
        //layout.setBackground(getResources().getDrawable(R.drawable.cockpit));

        game = preferences.getInt("Game", -1);
        database = DBHelper.getInstance(this).getDatabase();
        appliedTuning = new TuneProfile(game, preferences.getInt("Car", -1),
                                        preferences.getInt("Track", -1));

        initializeLayout(false);

        if (preferences.getBoolean("fromExisting", false)) {
            appliedTuning = DBQueries.getTuningForTimeSet(preferences.getInt("SelectedTimeSet", -1), database);
            fillWithValues(appliedTuning);
        }
    }

    @Override
    public void onBackPressed() {
        if (preferences.getBoolean("fromExisting", false)) {
            Intent partSelection = new Intent(this, PartSelection.class);
            startActivity(partSelection);
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_reset:
                initializeLayout(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creates the blank layout for this Activity.
     *
     * @param reset Whether existing rows should be removed. Used when the user wants to reset his input.
     */

    private void initializeLayout(boolean reset) {
        lastSeqId = 0;
        table = (TableLayout) findViewById(R.id.tuningTable);
        Cursor tuningElements = DBQueries.getTuningOptionsForGame(game, database);

        if (reset) {
            removeOldRows();
        }

        while (tuningElements.moveToNext()) {
            createTableRow(new Tuneable(tuningElements));
        }
    }

    /**
     * Removes all table rows from the layout.
     */

    private void removeOldRows() {
        List<TableRow> tableRows = getTableRows();
        for (TableRow tableRow : tableRows) {
            if (tableRow.getTag() instanceof String) {

                if (((String) tableRow.getTag()).startsWith("tr")) {
                    TableLayout root = (TableLayout) findViewById(R.id.tuningTable);
                    root.removeView(tableRow);
                }

                else if (((String) tableRow.getTag()).startsWith("category")) {
                    TableLayout root = (TableLayout) findViewById(R.id.tuningTable);
                    root.removeView(tableRow);
                }
            }
        }
    }

    /**
     * Creates a new table row and initializes its basic properties.
     *
     * @param tuneable Properties used for this row.
     */

    private void createTableRow(Tuneable tuneable) {
        TableRow settingsRow = new TableRow(this);
        String currentCategory;

        TableRow.LayoutParams defaultParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        settingsRow.setLayoutParams(defaultParams);
        settingsRow.setPadding(0, 3, 0, 3);
        settingsRow.setTag("tr" + tuneable.getElementName());
        settingsRow.setGravity(Gravity.CENTER_VERTICAL);

        currentCategory = tuneable.getCategory();

        // Add a category header for each new section of parts.
        if (!currentCategory.equals(previousCategory)) {
            table.addView(setUpNewTableRow(currentCategory, defaultParams));
        }
        previousCategory = currentCategory;

        // First column (Tuning element name)
        TextView partName = new TextView(this);
        partName.setText(tuneable.getElementName());
        partName.setLayoutParams(defaultParams);
        settingsRow.addView(partName);

        // Second column (Seekbar or EditText)
        if (tuneable.hasFixedRange() && (tuneable.getIncrement() % 1 == 0)) {
            // Coarse enough to change value with a SeekBar
            //tuneable.setElementName(tuneable.getElementName()); // WTF?

            settingsRow.addView(setUpNewSeekBar(tuneable, defaultParams));
        }
        else {
            // EditText is needed

            lastSeqId = tuneable.getSeqId();
            settingsRow.addView(setUpNewEditText(tuneable));
        }

        // Third column (Tuning value)
        TextView tuningLevel = new TextView(this);
        tuningLevel.setTag("tv" + tuneable.getElementName());

        // Set default text for SeekBars
        if (settingsRow.getChildAt(1) instanceof SeekBar) {
            tuningLevel.setText("Not set");
        }

        tuningLevel.setLayoutParams(defaultParams);
        settingsRow.addView(tuningLevel);
        table.addView(settingsRow);
    }

    private EditText setUpNewEditText(Tuneable tuneable) {
        EditText customValue = new EditText(this);
        customValue.setOnFocusChangeListener(this);

        // Allow negative values for input if the tuneable requires it.
        if (tuneable.getMinValue() < 0) {
            customValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        else {
            customValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        customValue.setSingleLine(true);
        customValue.setTag(tuneable);

        // Set the "Done" button on the virtual keyboard if it's the last element, otherwise change it to "Next".
        if (lastSeqId < tuneable.getSeqId()) {
            customValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
            // Swap last and current EditText, and set the old one to "Next"
            if (finalEditText != null) finalEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            finalEditText = customValue;
        }
        else {
            customValue.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        return customValue;
    }

    private SeekBar setUpNewSeekBar(Tuneable tuneable, TableRow.LayoutParams defaultParams) {
        SeekBar levelSeeker = new SeekBar(this);
        levelSeeker.incrementProgressBy(1);
        levelSeeker.setMax(tuneable.getMaxValue() - tuneable.getMinValue());
        levelSeeker.setProgress(levelSeeker.getMax() / 2);

        levelSeeker.setOnSeekBarChangeListener(this);

        levelSeeker.setTag(tuneable);
        levelSeeker.setLayoutParams(defaultParams);
        return levelSeeker;
    }

    private TableRow setUpNewTableRow(String currentCategory, TableRow.LayoutParams defaultParams) {
        TableRow categoryRow = new TableRow(this);
        categoryRow.setLayoutParams(defaultParams);
        categoryRow.setPadding(0, 10, 0, 10);
        TextView categoryName = new TextView(this);
        categoryName.setText(currentCategory);
        categoryName.setLayoutParams(defaultParams);
        categoryName.setTypeface(categoryName.getTypeface(), Typeface.BOLD);
        categoryName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        categoryName.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        categoryRow.addView(categoryName);
        categoryRow.setGravity(Gravity.CENTER_VERTICAL);
        categoryRow.setTag("category");
        return categoryRow;
    }

    /**
     * Fills the existing layout with previously saved values.
     *
     * @param appliedTuning Contains the values that will be inserted.
     */

    private void fillWithValues(TuneProfile appliedTuning) {

        List<TableRow> tableRows = getTableRows();
        Tuneable currentTunable;
        EditText currentEditText;
        SeekBar currentSeekBar;
        View currentElement;

        // Iterate over all table rows and the child elements of each row
        for (TableRow currentRow : tableRows) {
            for (int i = 0; i < currentRow.getChildCount(); i++) {
                currentElement = currentRow.getChildAt(i);

                // Set value for EditText
                if (currentElement instanceof EditText) {
                    currentEditText = (EditText) currentElement;
                    if (currentEditText.getTag() instanceof Tuneable) {
                        currentTunable = (Tuneable) currentEditText.getTag();
                        Double tuningValue = appliedTuning.getAppliedTuning().get(currentTunable);
                        // Check if the Tuneable exists in the TuningProfile.
                        if (tuningValue != null) {
                            currentEditText.setText(String.valueOf(tuningValue));
                        }
                    }
                }

                // Set value for Seekbar
                if (currentElement instanceof SeekBar) {
                    currentSeekBar = (SeekBar) currentElement;
                    if (currentSeekBar.getTag() instanceof Tuneable) {
                        currentTunable = (Tuneable) currentSeekBar.getTag();
                        Double tuningValue = appliedTuning.getAppliedTuning().get(currentTunable);
                        // Check if the Tuneable exists in the TuningProfile.
                        if (tuningValue != null) {
                            // Set the correct maximum SeekBar value and set the "fake" progress to set negative values correctly.
                            currentSeekBar.setMax(currentTunable.getMaxValue() - currentTunable.getMinValue());
                            currentSeekBar.setProgress(tuningValue.intValue() - currentTunable.getMinValue());
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
        TableLayout root = (TableLayout) findViewById(R.id.tuningTable);
        List<TableRow> tableRows = new ArrayList<TableRow>();

        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof TableRow) {
                tableRows.add((TableRow) root.getChildAt(i));
            }
        }

        return tableRows;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        Tuneable settings = (Tuneable) seekBar.getTag();
        TextView tv = (TextView) table.findViewWithTag("tv" + settings.getElementName());

        // "Add" the minimum value to get the actual progress (i.e. not the "fake" progress of the SeekBar, which can't display negative values).
        tv.setText(String.valueOf(progress + settings.getMinValue()));
        appliedTuning.addTuneable(settings, progress + settings.getMinValue());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onClick(View v) {
        Intent timeEntry = new Intent(this, TimeEntry.class);

        // Explicitly focus the current view, to un-focus a potentially focused EditText (to save its value from onFocusChange()).
        v.setFocusableInTouchMode(true);
        v.requestFocus();

        preferences.edit().putString("Tuning", Utilities.serializeTuneProfile(appliedTuning)).commit();

        startActivity(timeEntry);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        L.i("Restoring values...");
        appliedTuning = Utilities.deserializeTuneProfile(inState.getString("values"));
        fillWithValues(appliedTuning);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        L.i("Saving values...");
        table.setFocusableInTouchMode(true);
        table.requestFocus();
        outState.putString("values", Utilities.serializeTuneProfile(appliedTuning));
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reset, menu);
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        // If an EditText loses focus, save the entered value.
        if (!hasFocus) {
            EditText text = (EditText) view;
            Tuneable settings = (Tuneable) text.getTag();
            try {
                appliedTuning.addTuneable(settings, Double.valueOf(text.getText().toString()));
            } catch (NumberFormatException nfe) {
                L.w(nfe.getMessage());
            }
        }
    }
}