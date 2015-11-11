package org.lennartb.gtlaptimer.Helpers;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Source: http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/

public class DBHelper extends SQLiteOpenHelper {

    // The Android's default system path of your application database.
    private static final String DB_PATH = "/data/data/org.lennartb.gtlaptimer/databases/";
    private static final String DB_NAME = "gtlaptimer.db";
    private static DBHelper instance;
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to
     * the application assets and resources.
     *
     * @param context Application context.
     */
    private DBHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates and/or returns (if it already exists) the Singleton instance of the DBHelper class.
     *
     * @param context Application context.
     * @return The instance of th DBHelper class.
     */

    public static DBHelper getInstance(Context context) {

        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    /**
     * Creates and/or returns (if it already exists) the database object.
     *
     * @return The database.
     */

    public SQLiteDatabase getDatabase() {
        L.v("Hardcoded: " + DB_PATH);
        L.v("Softcoded: " + myContext.getFilesDir().getPath());
        try {
            createDataBase();
        } catch (IOException ioe) {
            L.e("Couldn't copy the database into onto the system.");
            throw new Error("Couldn't copy the database into onto the system.");
        }

        try {
            openDataBase();
        } catch (SQLException sqle) {
            L.e("Could not open database");
        }

        return myDataBase;
    }

    /**
     * Creates an empty database on the system and rewrites it with your own
     * database.
     */
    private void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            L.i("Database already exists, skipping new database creation");
        }
        else {

            // By calling this method an empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            this.getWritableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            L.i("Database doesn't exist, creating new one.");
        }

        if (checkDB != null) {

            checkDB.close();
        }

        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        // Open your local database as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty database
        String outFileName = DB_PATH + DB_NAME;

        // Open the empty database as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private void openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}