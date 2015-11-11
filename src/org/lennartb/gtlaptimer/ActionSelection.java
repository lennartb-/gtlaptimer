package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class ActionSelection extends OptionMenuActivity implements OnClickListener {

    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_selection);
        getActionBar().setDisplayShowTitleEnabled(false);

        preferences = getSharedPreferences(MainActivity.PACKAGE_NAME, MODE_PRIVATE);
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
        switch (id) {
            case R.id.browseActivityButton:
                preferences.edit().putString("Action", "Browse").commit();
                break;
            case R.id.timeActivityButton:
                preferences.edit().putString("Action", "Time").commit();
                preferences.edit().putBoolean("fromExisting", false).commit();
                break;
        }
        startActivity(trackSelector);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
