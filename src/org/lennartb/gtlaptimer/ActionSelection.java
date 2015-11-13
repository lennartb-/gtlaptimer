package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import org.lennartb.gtlaptimer.Enums.Action;
import org.lennartb.gtlaptimer.Enums.Game;
import org.lennartb.gtlaptimer.Services.EntryStateProvider;

public class ActionSelection extends OptionMenuActivity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_selection);
        getActionBar().setDisplayShowTitleEnabled(false);
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
    public void onClick(View v) {
        final int id = v.getId();
        Intent trackSelector = new Intent(this, TrackSelection.class);
        Bundle extras = getIntent().getExtras();
        int gameId;
        if (extras != null)
        {
            gameId = extras.getInt("Game");
        } else
        {
            return;
        }

        Action action;
        boolean fromExisting = false;
        switch (id) {
            case R.id.browseActivityButton:
                action = Action.Browse;
                fromExisting = true;
                break;
            case R.id.timeActivityButton:
                action = Action.NewEntry;
                break;
            default:
                throw new IllegalArgumentException("Unknown action button");
        }

        EntryStateProvider.init(fromExisting, action, Game.fromId(gameId));
        startActivity(trackSelector);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
