package org.lennartb.gtlaptimer.Objects;

import android.database.Cursor;

public class Tuneable {

    private int tuneId;
    private String elementName;
    private int minValue;
    private int maxValue;
    private float increment;
    private int seqId;
    private boolean hasFixedRange;
    private String category;

    public Tuneable(int tuneId, String elementName, int minValue, int maxValue, float increment, int seqUd, boolean hasFixedRange, String category) {
        this.tuneId = tuneId;
        this.elementName = elementName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.increment = increment;
        this.seqId = seqUd;
        this.hasFixedRange = hasFixedRange;
        this.category = category;
    }

    public Tuneable(Cursor tuningElements) {
        this.tuneId = tuningElements.getInt(tuningElements.getColumnIndex("_id"));
        this.elementName = tuningElements.getString(tuningElements.getColumnIndex("tunepart"));
        this.minValue = tuningElements.getInt(tuningElements.getColumnIndex("minValue"));
        this.maxValue = tuningElements.getInt(tuningElements.getColumnIndex("maxValue"));
        this.increment = tuningElements.getFloat(tuningElements.getColumnIndex("increment"));
        this.seqId = tuningElements.getInt(tuningElements.getColumnIndex("seqID"));
        this.category = tuningElements.getString(tuningElements.getColumnIndex("category"));
        this.hasFixedRange = (tuningElements.getInt(tuningElements.getColumnIndex("fixedRange")) == 1);
    }

    // Copy Constructor
    public static Tuneable newInstance(Tuneable tv) {
        return new Tuneable(tv.getTuneId(), tv.getElementName(), tv.getMinValue(), tv.getMaxValue(), tv.getIncrement(), tv.getSeqId(),
                            tv.hasFixedRange(), tv.getCategory());
    }

    public int getTuneId() {
        return tuneId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuneable tuneable = (Tuneable) o;

        return seqId == tuneable.seqId;
    }

    @Override
    public int hashCode() {
        return seqId;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {

        return maxValue;
    }

    public float getIncrement() {
        return increment;
    }

    public int getSeqId() {
        return seqId;
    }

    public boolean hasFixedRange() {
        return hasFixedRange;
    }
}
