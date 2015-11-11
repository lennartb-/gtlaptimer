package org.lennartb.gtlaptimer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import org.lennartb.gtlaptimer.Helpers.DBExport;
import org.lennartb.gtlaptimer.Helpers.DialogCreator;
import org.lennartb.gtlaptimer.Helpers.Utilities;

/**
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 01.08.13
 * Time: 14:35 *
 */
public class OptionMenuActivity extends Activity implements DialogCreator.DialogCreatorListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Home Button must be explicitly enabled from ICS onwards.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Check which item was selected.
        switch (item.getItemId()) {

            // Export database to disk.
            case R.id.ab_export:
                DBExport.backupDatabaseCSV(DBExport.EXPORT_FILENAME, this);
                return true;

            // Home Button.
            case android.R.id.home:
                Intent home = new Intent(this, MainActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(home);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;

            // About dialog.
            case R.id.ab_about:
                FragmentManager fm = getFragmentManager();
                DialogCreator reverseDialog = new DialogCreator();
                Bundle dialogType = new Bundle();
                dialogType.putSerializable("DialogType", DialogCreator.DIALOG_TYPE.ABOUT_DIALOG);
                reverseDialog.setArguments(dialogType);
                reverseDialog.show(fm, "about_dialog_fragment");
                return true;

            // "New Entry" for a specific game.
            case R.id.ab_newentry:
                Intent newEntry = new Intent(this, TrackSelection.class);
                Utilities.resetPreferencesForNewEntry(this);
                startActivity(newEntry);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFinishDialog(boolean result) {
        // Do nothing, the "About" dialog is just closed.
    }
}