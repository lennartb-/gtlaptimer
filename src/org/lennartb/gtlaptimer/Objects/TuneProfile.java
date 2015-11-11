package org.lennartb.gtlaptimer.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TuneProfile {
    private int gameId;
    private int timesetId;
    private int carId;
    private int trackId;
    private HashMap<Tuneable, Double> appliedTuning = new HashMap<Tuneable, Double>();

    public TuneProfile(int gameId, int timesetId, int carId, int trackId, Tuneable... appliedTuning) {
        this.gameId = gameId;
        this.timesetId = timesetId;
        this.carId = carId;
        this.trackId = trackId;
        this.appliedTuning = new HashMap<Tuneable, Double>();
    }

    public TuneProfile(int gameId, int carId, int trackId) {
        this.gameId = gameId;
        this.carId = carId;
        this.trackId = trackId;
    }

    public void addTuneable(Tuneable tune, double progress) {
        appliedTuning.put(tune, progress);
    }

    public HashMap<Tuneable, Double> getAppliedTuning() {
        return appliedTuning;
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("gameId", this.gameId);
            jsonObject.put("timesetId", this.timesetId);
            jsonObject.put("carId", this.carId);
            jsonObject.put("trackid", this.trackId);
            jsonObject.put("appliedTuning", this.appliedTuning);

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
