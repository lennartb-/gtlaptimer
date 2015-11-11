package org.lennartb.gtlaptimer.Objects;

import java.util.HashMap;

public class PartProfile {

    private int gameId;
    private int timesetId;
    private int carId;
    private int trackId;
    private HashMap<Integer, Part> installedParts = new HashMap<Integer, Part>();

    public PartProfile(int gameId, int timesetId, int carId, int trackId, Part... parts) {
        this.gameId = gameId;
        this.timesetId = timesetId;
        this.carId = carId;
        this.trackId = trackId;
        addParts(parts);
    }

    public PartProfile(int gameId, int timesetId, int carId, int trackId) {
        this.gameId = gameId;
        this.timesetId = timesetId;
        this.carId = carId;
        this.trackId = trackId;
    }

    public PartProfile(int gameId, int carId, int trackId) {
        this.gameId = gameId;
        this.carId = carId;
        this.trackId = trackId;
    }

    public HashMap<Integer, Part> getInstalledParts() {
        return installedParts;
    }

    public void addParts(Part part) {
        this.installedParts.put(part.getGroupId(), part);
    }

    public void addParts(Part... parts) {
        for (Part part : parts) {
            this.installedParts.put(part.getGroupId(), part);
        }
    }

    public Part getPart(int groupid) {
        return installedParts.get(groupid);
    }

    public void removePart(Part part) {
        this.installedParts.remove(part.getGroupId());
    }
}
