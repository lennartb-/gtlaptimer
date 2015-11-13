package org.lennartb.gtlaptimer.Interfaces;

import org.lennartb.gtlaptimer.Enums.Action;
import org.lennartb.gtlaptimer.Enums.Game;

public interface EntryState
{
    Game getGame();

    int getCarId();

    void setCarId(int carId);

    int getTrackId();

    boolean isTrackReverse();

    Action getAction();

    boolean isFromExistingEntry();

    void reset();
}
