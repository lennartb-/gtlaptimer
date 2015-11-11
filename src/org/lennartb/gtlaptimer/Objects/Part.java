package org.lennartb.gtlaptimer.Objects;

public class Part {

    private String partName;
    private String levelName;
    private int groupId;
    private int gameId;
    private int partId;
    private int groupLevel;
    private String category;

    public Part(String partName, String levelName, String category, int groupId, int gameId, int partId, int groupLevel) {
        this.partName = partName;
        this.levelName = levelName;
        this.category = category;
        this.groupId = groupId;
        this.gameId = gameId;
        this.partId = partId;
        this.groupLevel = groupLevel;
    }

    public int getGroupLevel() {
        return groupLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPartName() {
        return partName;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getPartId() {
        return partId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Part part = (Part) o;

        return groupId == part.groupId;
    }

    @Override
    public int hashCode() {
        return groupId;
    }
}
