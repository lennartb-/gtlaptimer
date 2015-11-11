package org.lennartb.gtlaptimer.Helpers;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DBExport {
    public static final String EXPORT_FILENAME = "GTLapTimer_Export.csv";

    private DBExport() {
    }

    /**
     * Exports all saved times including tuning, parts and times to a CSV file.
     *
     * @param outFileName Name of the CSV file.
     * @param context     Application context.
     * @return True if succeeded, false if not.
     */

    public static Boolean backupDatabaseCSV(String outFileName, Activity context) {

        Boolean returnCode;
        String csvValues;
        SQLiteDatabase database = DBHelper.getInstance(context).getDatabase();
        File outFile;

        // Notification to display failure or success to the user.
        Toast notification = new Toast(context);
        notification.setView(context.findViewById(org.lennartb.gtlaptimer.R.id.centered_toast_text));

        try {
            outFile = new File(Environment.getExternalStorageDirectory(), outFileName);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);

            // Super complicated SQL query. Basically gathers all information from all timesets across all tables.
            Cursor cursor = database.rawQuery("SELECT Cars.manufacturer || ' ' || Cars.model AS car," +
                                                      "Game.gamename, " +
                                                      "Group_Concat(DISTINCT Tuning.tunepart || ': ' || TimeSet_Tuning.progress) AS tuning, " +
                                                      "Group_Concat(DISTINCT CASE WHEN Parts.levelname IS NULL THEN Parts.name ELSE Parts.name || ': ' || Parts.levelname END) AS fullpartname, " +
                                                      "TimeSet.time, " +
                                                      "TimeSet.date, " +
                                                      "Track.trackname || CASE (TimeSet.reverse) WHEN \"0\" THEN '' WHEN \"1\" THEN ' (Reverse)' END, " +
                                                      "TimeSet.timesetid " +
                                                      "FROM Cars " +
                                                      "INNER JOIN Parts ON Game.gameid = Parts.gameid " +
                                                      "INNER JOIN Game ON Game.gameid = TimeSet.gameid " +
                                                      "INNER JOIN TimeSet ON Cars.carid = TimeSet.carid " +
                                                      "INNER JOIN TimeSet_Parts ON Parts.partid = TimeSet_Parts.partid AND TimeSet.timesetid = TimeSet_Parts.timesetid " +
                                                      "LEFT OUTER JOIN TimeSet_Tuning ON TimeSet.timesetid = TimeSet_Tuning.timesetid " +
                                                      "INNER JOIN Track ON Track.trackid = TimeSet.trackid " +
                                                      "LEFT OUTER JOIN Tuning ON Tuning.tuneid = TimeSet_Tuning.tuneid " +
                                                      "GROUP BY TimeSet.date", null);

            // Add commas to make a CSV file.
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    csvValues = cursor.getString(0) + ",";
                    csvValues += cursor.getString(1) + ",";
                    csvValues += cursor.getString(2) + ",";
                    csvValues += cursor.getString(3) + ",";
                    csvValues += cursor.getString(4) + ",";
                    csvValues += cursor.getString(5) + ",";
                    csvValues += cursor.getString(6);
                    csvValues += "\n";
                    out.write(csvValues);
                }
                cursor.close();
            }
            out.close();

            // Scan file, so it is immediately visible to other Android applications.
            MediaScannerConnection.scanFile(context, new String[]{outFile.getAbsolutePath()}, null, null);
            returnCode = true;
            notification.makeText(context, "Exported Database successfully to " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            returnCode = false;
            L.e(e.getMessage());
            notification.makeText(context, "Exporting Database failed. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return returnCode;
    }
}