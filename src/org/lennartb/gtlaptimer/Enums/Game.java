package org.lennartb.gtlaptimer.Enums;

import org.lennartb.gtlaptimer.Helpers.L;

public enum Game
{
    GT1(1), GT2(2), GT3(3), GT4(4), GT5(5), GT6(6), GT7(7), GTPSP(100);

    private final int value;

    private Game(int value)
    {
        this.value = value;
    }

    public static Game fromId(int id)
    {

        switch (id)
        {

            case 1:
                return GT1;
            case 2:
                return GT2;
            case 3:
                return GT3;
            case 4:
                return GT4;
            case 5:
                return GT5;
            case 6:
                return GT6;
            case 7:
                return GT7;
            case 0:
            case 100:
                return GTPSP;

            default:
                L.w("No game found for id " + id);
                return null;
        }
    }

    public int getValue()
    {
        return value;
    }
}
