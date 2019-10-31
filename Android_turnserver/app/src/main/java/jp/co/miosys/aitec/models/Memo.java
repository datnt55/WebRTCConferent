package jp.co.miosys.aitec.models;

import java.io.Serializable;

public class Memo implements Serializable {
    private String categoryId;
    private String categoryName;
    private int userId;
    private int kmlId;
    private String memoAt;
    private String content;
    private double lat;
    private double lon;
    private double distance;

    public Memo(String categoryId, String categoryName, int userId, int kmlId, String memoAt, String content, double lat, double lon) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.userId = userId;
        this.kmlId = kmlId;
        this.memoAt = memoAt;
        this.content = content;
        this.lat = lat;
        this.lon = lon;
        distance = -1;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getKmlId() {
        return kmlId;
    }

    public void setKmlId(int kmlId) {
        this.kmlId = kmlId;
    }

    public String getMemoAt() {
        return memoAt;
    }

    public void setMemoAt(String memoAt) {
        this.memoAt = memoAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
