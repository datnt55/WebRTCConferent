package jp.co.miosys.aitec.models;

import org.joda.time.DateTime;

import java.io.Serializable;

public class VoiceMemo implements Serializable {
    private String userName;
    private String category;
    private DateTime collectionTime;
    private DateTime startTime;
    private DateTime endTime;
    private String linkVideo;
    private double lat;
    private double lon;

    public VoiceMemo(String userName,String category, DateTime collectionTime, DateTime startTime, DateTime endTime, String linkVideo, double lat, double lon) {
        this.userName = userName;
        this.category = category;
        this.collectionTime = collectionTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.linkVideo = linkVideo;
        this.lat = lat;
        this.lon = lon;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public DateTime getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(DateTime collectionTime) {
        this.collectionTime = collectionTime;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public String getLinkVideo() {
        return linkVideo;
    }

    public void setLinkVideo(String linkVideo) {
        this.linkVideo = linkVideo;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
