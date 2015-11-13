package org.lennartb.gtlaptimer;

import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.lennartb.gtlaptimer.Enums.Action;
import org.lennartb.gtlaptimer.Helpers.DBHelper;
import org.lennartb.gtlaptimer.Helpers.DBQueries;
import org.lennartb.gtlaptimer.Helpers.DialogCreator;
import org.lennartb.gtlaptimer.Services.EntryStateProvider;

public class TrackSelection extends OptionMenuActivity implements DialogCreator.DialogCreatorListener, AdapterView.OnItemClickListener {


    private ListView tracklist;
    private RelativeLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_selection);
        layout = (RelativeLayout) findViewById(R.id.trackSelector);

        // Get tracks for selected game.

        int game = EntryStateProvider.getInstance().getGame().getValue();
        SQLiteDatabase database = DBHelper.getInstance(this).getDatabase();
        Cursor tracks = DBQueries.getTracksForGame(game, database);

        // Display tracks in ListView.
        tracklist = (ListView) findViewById(R.id.listView1);

        ListAdapter trackAdapter = new SimpleCursorAdapter(
                this,
                R.layout.custom_listview_item,
                tracks,
                new String[]{tracks.getColumnName(tracks.getColumnIndex("trackname"))},
                new int[]{android.R.id.text1},
                0);

        tracklist.setAdapter(trackAdapter);
        tracklist.setClickable(true);
        tracklist.setOnItemClickListener(this);
    }

    @Override
    public void onFinishDialog(boolean result) {
        EntryStateProvider.getInstance().setTrackReverse(result);
        runActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // Hide the "New Entry" button in this Activity, the users hasn't chosen anything to renew yet.
        MenuItem item = menu.findItem(R.id.ab_newentry);
        item.setVisible(false);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        // Get TrackID of selected track.
        Cursor c = (Cursor) tracklist.getAdapter().getItem(position);
        EntryStateProvider.getInstance().setTrackId(c.getInt(c.getColumnIndex("_id")));

        // Determine if a track can be driven in reverse direction, show a dialog to choose which if it's possible.
        if (c.getString(c.getColumnIndex("reversepossible")).equals("true")) {
//             layout.setBackground(getResources().getDrawable(R.drawable.track));
            FragmentManager fm = getFragmentManager();
            DialogCreator reverseDialog = new DialogCreator();
            Bundle dialogType = new Bundle();
            dialogType.putSerializable("DialogType", DialogCreator.DIALOG_TYPE.REVERSE_DIALOG);
            reverseDialog.setArguments(dialogType);
            reverseDialog.show(fm, "reverse_dialog_fragment");
        }
        else {
            EntryStateProvider.getInstance().setTrackReverse(false);
            runActivity();
        }
    }

    /**
     * Proceeds to the next Activity depending on which action the user has chosen (browser or new time).
     */

    private void runActivity() {

        Intent activitySelector;
        if (EntryStateProvider.getInstance().getAction() == Action.Browse)
        {
            activitySelector = new Intent(TrackSelection.this, TimeBrowser.class);
            startActivity(activitySelector);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        if (EntryStateProvider.getInstance().getAction() == Action.NewEntry)
        {
            activitySelector = new Intent(TrackSelection.this, CarSelection.class);
            startActivity(activitySelector);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
