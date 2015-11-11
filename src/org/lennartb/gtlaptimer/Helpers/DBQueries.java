package org.lennartb.gtlaptimer.Helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import org.lennartb.gtlaptimer.Objects.Part;
import org.lennartb.gtlaptimer.Objects.PartProfile;
import org.lennartb.gtlaptimer.Objects.TuneProfile;
import org.lennartb.gtlaptimer.Objects.Tuneable;

public class DBQueries {

    /**
     * Fetches the tuning parameters for a TimeSet.
     *
     * @param timesetid ID of the TimeSet.
     * @param database  Database object.
     * @return A HashmMap that contains the parameters.
     */

    public static TuneProfile getTuningForTimeSet(int timesetid, SQLiteDatabase database) {
        if (timesetid == -1) {
            return null;
        }

        Cursor tuningElements = database.rawQuery("SELECT tuneid, progress, tstid AS _id " +
                                                          "FROM TimeSet_Tuning " +
                                                          "WHERE timesetid = " + timesetid, null);
        Cursor data = database.rawQuery("SELECT timesetid AS _id, gameid, carid, trackid " +
                                                "FROM TimeSet " +
                                                "WHERE timesetid=" + timesetid, null);
        data.moveToFirst();
        TuneProfile profile = new TuneProfile(data.getInt(data.getColumnIndex("gameid")),
                                              timesetid,
                                              data.getInt(data.getColumnIndex("carid")),
                                              data.getInt(data.getColumnIndex("trackid")));
        Cursor converter;
        int key;
        while (tuningElements.moveToNext()) {
            key = tuningElements.getInt(tuningElements.getColumnIndex("tuneid"));
            converter = database.rawQuery("SELECT tunepart, increment, seqID, category, fixedRange, minValue, maxValue, tuneid AS _id " +
                                                  "FROM Tuning " +
                                                  "WHERE tuneid =" + key, null);
            converter.moveToFirst();
            profile.addTuneable(new Tuneable(converter), tuningElements.getDouble(tuningElements.getColumnIndex("progress")));
        }
        return profile;
    }

    /**
     * Fetches the installed parts for a TimeSet
     *
     * @param timesetid ID of the TimeSet.
     * @param database  Database object.
     * @return A HashMap containing the installed parts.
     */

    public static PartProfile getPartsForTimeSet(int timesetid, SQLiteDatabase database) {
        if (timesetid == -1) {
            return null;
        }

        Cursor parts = database.rawQuery("SELECT partid, tspid AS _id " +
                                                 "FROM TimeSet_Parts " +
                                                 "WHERE timesetid = " + timesetid, null);

        Cursor data = database.rawQuery("SELECT timesetid AS _id, gameid, carid, trackid " +
                                                "FROM TimeSet " +
                                                "WHERE timesetid=" + timesetid, null);
        data.moveToFirst();
        PartProfile profile = new PartProfile(
                data.getInt(data.getColumnIndex("gameid")),
                timesetid,
                data.getInt(data.getColumnIndex("carid")),
                data.getInt(data.getColumnIndex("trackid")));

        int partid;
        Cursor groupIdcursor;
        while (parts.moveToNext()) {
            partid = parts.getInt(parts.getColumnIndex("partid"));
            groupIdcursor = database.rawQuery("SELECT name, levelname, category, groupid, partid AS _id " +
                                                      "FROM Parts " +
                                                      "WHERE _id = " + partid, null);
            groupIdcursor.moveToFirst();
            profile.addParts(new Part(
                    groupIdcursor.getString(groupIdcursor.getColumnIndex("name")),
                    groupIdcursor.getString(groupIdcursor.getColumnIndex("levelname")),
                    groupIdcursor.getString(groupIdcursor.getColumnIndex("category")),
                    groupIdcursor.getInt(groupIdcursor.getColumnIndex("groupid")),
                    data.getInt(data.getColumnIndex("gameid")),
                    partid,
                    DBQueries.getPositionInGroup(partid, groupIdcursor.getInt(groupIdcursor.getColumnIndex("groupid")), database)));
        }
        return profile;
    }

    /**
     * Fetches the Manufacturer and Model for a car ID in a single String
     *
     * @param carid    The ID of the car.
     * @param database Database object.
     * @return A String that contains both the manufacturer and model, separated by a whitespace.
     */

    public static String getManufacturerAndModelFromId(int carid, SQLiteDatabase database) {
        L.i("CarID: " + carid);
        Cursor car = database.rawQuery("SELECT carid AS _id, manufacturer || ' ' || model AS mm " +
                                               "FROM cars " +
                                               "WHERE carid=" + carid, null);
        car.moveToFirst();
        return car.getString(car.getColumnIndex("mm"));
    }

    /**
     * Fetches the top times for a specific track.
     *
     * @param trackid    ID of the Track.
     * @param reverse    Whether to get the reverse version of the track, 1 is true, 0 is false.
     * @param columnName Name of the column that contains the times.
     * @param limit      Number of items that will be fetched.
     * @param database   Database object.
     * @return A String that contains the time, manufacturer, model and date of the specific laptime.
     */

    public static Cursor getTopTimes(int trackid, int reverse, String columnName, int limit, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT time || ' - ' || Cars.manufacturer || ' ' || Cars.model || ' ('||date||')' AS " + columnName + " , timesetid AS _id " +
                        "FROM timeset INNER JOIN Cars ON Cars.carid=Timeset.carid " +
                        "WHERE trackid=" + trackid + " AND Timeset.reverse=" + reverse + " " +
                        "ORDER BY time ASC LIMIT " + limit,
                null);
    }

    /**
     * Fetches the latest times for a specific track.
     *
     * @param trackID    ID of the Track.
     * @param reverse    Whether to get the reverse version of the track, 1 is true, 0 is false.
     * @param columnName Name of the column that contains the times.
     * @param limit      Number of items that will be fetched.
     * @param database   Database object.
     * @return A String that contains the time, manufacturer, model and date of the specific laptime.
     */

    public static Cursor getLatestTimes(int trackID, int reverse, String columnName, int limit, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT time || ' - ' || Cars.manufacturer || ' ' || Cars.model || ' ('||date||')' AS " + columnName + " , timesetid AS _id " +
                        "FROM timeset INNER JOIN Cars ON Cars.carid=Timeset.carid " +
                        "WHERE trackid=" + trackID + " AND Timeset.reverse=" + reverse + " " +
                        "ORDER BY timesetid DESC LIMIT " + limit,
                null);
    }

    /**
     * Fetches the available tuning options for a specific game.
     *
     * @param gameid   ID of the game.
     * @param database Database object.
     * @return A Cursor containing all available tuning options, ordered by their SequenceID.
     */

    public static Cursor getTuningOptionsForGame(int gameid, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT tunepart, increment, seqID, fixedRange, category, minValue, maxValue, tuneid AS _id " +
                        "FROM Tuning " +
                        "WHERE gameid =" + gameid + " " +
                        "ORDER BY seqID", null);
    }

    /**
     * Fetches all cars for a specific game.
     *
     * @param gameid   ID of the game.
     * @param database Database Object.
     * @return A Cursor containing all cars in the game, readily combined as "Manufacturer Model".
     */

    public static Cursor getCarsForGame(int gameid, String columnName, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT carid AS _id, manufacturer || ' ' || model AS " + columnName + " " +
                        "FROM cars " +
                        "WHERE gameid=" + gameid, null);
    }

    /**
     * Fetches all manufacturers for a game.
     *
     * @param gameid   ID of the game.
     * @param database Database object.
     * @return A Cursor containing all manufacturers that have cars in the game.
     */

    public static Cursor getManufacturersForGame(int gameid, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT DISTINCT manufacturer, carid AS _id " +
                        "FROM cars " +
                        "WHERE gameid=" + gameid + " " +
                        "GROUP BY manufacturer", null);
    }

    /**
     * Fetches the track name of a specfied track ID
     *
     * @param trackid  ID of the track.
     * @param database Database object.
     * @return A String with the track name.
     */

    public static String getTracknameFromId(int trackid, SQLiteDatabase database) {
        Cursor track = database.rawQuery("SELECT trackid as _id, trackname " +
                                                 "FROM Track " +
                                                 "WHERE trackid=" + trackid, null);
        track.moveToFirst();
        return track.getString(track.getColumnIndex("trackname"));
    }

    /**
     * Fetches all available tuning parts for a game.
     *
     * @param gameid   ID of the game.
     * @param database Database object.
     * @return A Cursor containing all available tuning parts for the game, ordered by GroupID ascending.
     */

    public static Cursor getPartsForGame(int gameid, SQLiteDatabase database) {
        return database.rawQuery("SELECT name, levelname, category, groupid, partid AS _id " +
                                         "FROM parts " +
                                         "WHERE gameid =" + gameid +
                                         " ORDER BY groupid ASC", null);
    }

    /**
     * Fetches tuning parts for a specific group and additionally counts their number.
     *
     * @param gameid   ID of the game.
     * @param groupid  ID of the group.
     * @param database Database object.
     * @return A Cursor containing the parts from a specific group as well as the overall number of parts in the group.
     */

    public static int getPartsPerGroup(int gameid, int groupid, SQLiteDatabase database) {
        Cursor partsPerGroup = database.rawQuery(
                "SELECT name, levelname, partid AS _id, groupid, count(groupid) AS num " +
                        "FROM parts " +
                        "WHERE gameid =" + gameid + " " +
                        "AND groupid=" + groupid + " " +
                        "GROUP BY groupid", null);
        partsPerGroup.moveToFirst();

        return partsPerGroup.getInt(partsPerGroup.getColumnIndex("num"));
    }

    /**
     * Fetches the plain names of installed parts for a specific TimeSet.
     *
     * @param timesetid ID of the TimeSet.
     * @param database  Database object.
     * @return A cursor containing the names of the installed parts of the TimeSet.
     */

    public static Cursor getPartNamesForTimeset(int timesetid, String columnName, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT " +
                        "CASE WHEN Parts.levelname IS NULL THEN Parts.name ELSE Parts.name || ': ' || Parts.levelname END " +
                        "AS " + columnName + ", TimeSet.timesetid AS _id " +
                        "FROM TimeSet_Parts " +
                        "INNER JOIN Parts ON Parts.partid=TimeSet_Parts.partid " +
                        "INNER JOIN TimeSet ON TimeSet.timesetid=TimeSet_Parts.timesetid " +
                        "WHERE TimeSet.timesetid=" + timesetid, null);
    }

    /**
     * Fetches the plain names of applied tuning options for a specific TimeSet.
     *
     * @param timesetid ID of the TimeSet.
     * @param database  Database object.
     * @return A Cursor containing the names of the applied tuning of the TimeSet.
     */

    public static Cursor getTuningNamesForTimeSet(int timesetid, String columnName, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT Tuning.tunepart || ': ' || TimeSet_Tuning.progress AS " + columnName + ", TimeSet_Tuning.timesetid AS _id " +
                        "FROM TimeSet_Tuning " +
                        "INNER JOIN Tuning ON Tuning.tuneid=TimeSet_Tuning.tuneid " +
                        "WHERE TimeSet_Tuning.timesetid=" + timesetid + " " +
                        "ORDER BY (" +
                        "SELECT seqid " +
                        "FROM Tuning " +
                        "WHERE Tuning.tuneid=TimeSet_Tuning.tuneid)", null);
    }

    /**
     * Fetches the ID of the car from a specific Timeset.
     *
     * @param timesetid ID of the TimeSet.
     * @param database  Database Object.
     * @return The ID of the car.
     */

    public static int getCarIdFromTimeSet(int timesetid, SQLiteDatabase database) {
        Cursor car = database.rawQuery("SELECT carid AS _id FROM TimeSet WHERE timesetid=" + timesetid, null);
        car.moveToFirst();
        return car.getInt(car.getColumnIndex("_id"));
    }

    /**
     * Fetches all tracks for a game.
     *
     * @param gameid   ID of the game.
     * @param database Database Object.
     * @return A Cursor containing the Trackname, TrackID and whether reverse is possible on the track.
     */

    public static Cursor getTracksForGame(int gameid, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT DISTINCT t.trackname, t.reversepossible, t.trackid AS _id " +
                        "FROM track AS t, game AS g, trackgame AS tg " +
                        "WHERE tg.gameid =" + gameid + " AND t.trackid=tg.trackid " +
                        "ORDER BY t.trackname", null);
    }

    public static String getCommentFromTimeset(int timesetid, SQLiteDatabase database) {
        Cursor curs = database.rawQuery("SELECT comment FROM TimeSet WHERE timesetid=" + timesetid, null);
        curs.moveToFirst();
        return curs.getString(curs.getColumnIndex("comment"));
    }

    /**
     * Fetches all cars from a specific manufacturer.
     *
     * @param gameid       ID of the game.
     * @param manufacturer Name of the manufacturer.
     * @param columnName   Name of the column that contains the name of the manufacturer plus the name of the car.
     * @param database     Database object.
     * @return A Cursor containing the manufacturer and manufacturer + model.
     */

    public static Cursor getCarsFromManufacturer(int gameid, String manufacturer, String columnName, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT carid AS _id, manufacturer, manufacturer || ' ' || model AS " + columnName + " " +
                        "FROM cars " +
                        "WHERE gameid=" + gameid + " AND manufacturer LIKE '" + manufacturer + "'", null);
    }

    /**
     * Fetches the position of a part in a group.
     *
     * @param partid   ID of the part.
     * @param groupid  ID of the group.
     * @param database Database object.
     * @return An int representing the position of a part in a group, starting from 0 for the "worst" part in a group.
     */

    public static int getPositionInGroup(int partid, int groupid, SQLiteDatabase database) {
        long startTime = System.nanoTime();
        Cursor position = database.rawQuery("SELECT COUNT(*) AS _id " +
                                                    "FROM Parts " +
                                                    "WHERE groupid=" + groupid + " AND partid < " +
                                                    "(SELECT partid FROM Parts WHERE partid=" + partid + ")", null);
        position.moveToFirst();

        long endTime = System.nanoTime();

        long duration = endTime - startTime;
        int val = position.getInt(position.getColumnIndex("_id"));
        L.i("Time taken without prepared statement: " + duration);
        return val;
    }

    /**
     * Fetches the position of a part in a group. This version uses prepared statements to cut the query time by about 40%;
     *
     * @param partid   ID of the part.
     * @param groupid  ID of the group.
     * @param database Database object.
     * @return An int representing the position of a part in a group, starting from 0 for the "worst" part in a group.
     */

    public static int getPositionInGroupPrepared(int partid, int groupid, SQLiteDatabase database) {

        SQLiteStatement stmt = database.compileStatement("SELECT COUNT(*) AS _id " +
                                                                 "FROM Parts " +
                                                                 "WHERE groupid=? AND partid < " +
                                                                 "(SELECT partid FROM Parts WHERE partid=?)");
        stmt.bindLong(1, groupid);
        stmt.bindLong(2, partid);
        return (int) stmt.simpleQueryForLong();
    }

    /**
     * Fetches existing time, car and date entries for a specific track.
     *
     * @param gameid     ID of the game.
     * @param trackid    ID of the track.
     * @param reverse    Whether the track is in reverse mode or not.
     * @param columnName Name of the column for display in the Listview.
     * @param database   Database object.
     * @return A Cursor containing the time, car, date and timesetID for the track and game.
     */

    public static Cursor getTimesForTrack(int gameid, int trackid, int reverse, String columnName, SQLiteDatabase database) {
        return database.rawQuery(
                "SELECT time || ' - ' || Cars.manufacturer || ' ' || Cars.model || ' ('||date||')' AS " + columnName + " , timesetid AS _id " +
                        "FROM timeset " +
                        "INNER JOIN Cars ON Cars.carid=Timeset.carid " +
                        "WHERE trackid=" + trackid + " AND Timeset.gameid = " + gameid + " AND Timeset.reverse=" + reverse +
                        " ORDER BY time", null);
    }
}
