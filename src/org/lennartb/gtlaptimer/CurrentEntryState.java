package org.lennartb.gtlaptimer;

import org.lennartb.gtlaptimer.Enums.Action;
import org.lennartb.gtlaptimer.Enums.Game;
import org.lennartb.gtlaptimer.Interfaces.EntryState;

public class CurrentEntryState implements EntryState
{
    Game game;
    int carId;
    int trackId;
    boolean isTrackReverse;
    Action action;
    boolean fromExistingEntry;


    public CurrentEntryState(boolean fromExistingEntry, Action action, Game game)
    {
        this.fromExistingEntry = fromExistingEntry;
        this.action = action;
        this.game = game;
    }

    public CurrentEntryState()
    {
    }

    @Override
    public Game getGame()
    {
        return game;
    }

    @Override
    public int getCarId()
    {
        return carId;
    }

    @Override
    public void setCarId(int carId)
    {
        this.carId = carId;
    }

    @Override
    public int getTrackId()
    {
        return trackId;
    }

    @Override
    public void setTrackId(int trackId)
    {
        this.trackId = trackId;
    }

    @Override
    public boolean isTrackReverse()
    {
        return isTrackReverse;
    }

    @Override
    public void setTrackReverse(boolean trackReverse)
    {
        isTrackReverse = trackReverse;
    }

    @Override
    public Action getAction()
    {
        return action;
    }

    @Override
    public boolean isFromExistingEntry()
    {
        return fromExistingEntry;
    }

    public void reset()
    {
        game = null;
        carId = -1;
        trackId = -1;
        isTrackReverse = false;
        action = null;
        fromExistingEntry = false;
    }
}
