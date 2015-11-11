package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import org.lennartb.gtlaptimer.Helpers.Utilities;

public class MainActivity extends OptionMenuActivity implements OnClickListener {

    public static final String PACKAGE_NAME = "org.lennartb.gtlaptimer";
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowTitleEnabled(false);

        // Wipe preferences at each start, we don't want to have weird artifacts stored.
        preferences = getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.ab_newentry);
        item.setVisible(false);
        return true;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        Intent actionSelector = new Intent(this, ActionSelection.class);
        Utilities.resetPreferencesForNewEntry(this);
        switch (id) {
            case R.id.gt3button:
                preferences.edit().putInt("Game", 3).commit();
                break;
            case R.id.gt4button:
                preferences.edit().putInt("Game", 4).commit();
                break;
            case R.id.gt5button:
                preferences.edit().putInt("Game", 5).commit();
                break;
            case R.id.gt0button:
                preferences.edit().putInt("Game", 0).commit();
                break;
        }
        startActivity(actionSelector);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
