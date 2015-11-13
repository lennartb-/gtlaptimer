package org.lennartb.gtlaptimer.Services;

import org.lennartb.gtlaptimer.CurrentEntryState;
import org.lennartb.gtlaptimer.Enums.Action;
import org.lennartb.gtlaptimer.Enums.Game;
import org.lennartb.gtlaptimer.Helpers.L;
import org.lennartb.gtlaptimer.Interfaces.EntryState;

public class EntryStateProvider
{

    private static EntryState instance;

    public static EntryState getInstance()
    {

        if (instance == null)
        {
            L.w("Instance not initialized, must call init() first");
            instance = new CurrentEntryState();
        }
        return instance;
    }


    public static void init(boolean fromExistingEntry, Action action, Game game)
    {
        instance = new CurrentEntryState(fromExistingEntry, action, game);
    }
}
