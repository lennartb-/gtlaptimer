package org.lennartb.gtlaptimer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import org.lennartb.gtlaptimer.Services.EntryStateProvider;

import java.security.InvalidParameterException;

public class MainActivity extends OptionMenuActivity implements OnClickListener
{

    public static final String PACKAGE_NAME = "org.lennartb.gtlaptimer";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.ab_newentry);
        item.setVisible(false);
        return true;
    }

    @Override
    public void onClick(View v)
    {
        final int id = v.getId();
        Intent actionSelector = new Intent(this, ActionSelection.class);
        int selectedGame;
        EntryStateProvider.getInstance().reset();
        switch (id)
        {
            case R.id.gt3button:
                selectedGame = 3;
                break;
            case R.id.gt4button:
                selectedGame = 4;
                break;
            case R.id.gt5button:
                selectedGame = 5;
                break;
            case R.id.gt0button:
                selectedGame = 100;
                break;

            default:
                throw new InvalidParameterException("Unrecognized game button");
        }
        actionSelector.putExtra("Game", selectedGame);
        startActivity(actionSelector);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
