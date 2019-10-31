package jp.co.miosys.aitec.models;

import android.location.Location;
import java.io.Serializable;

/**
 * Created by DatNT on 10/5/2018.
 */

public class LocationGPS implements Serializable {
    private double longitude;
    private double latitude;
    private double altitude;

    public LocationGPS(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public LocationGPS(double longitude, double latitude, double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public LocationGPS(Location currentLocation) {
        this.longitude = currentLocation.getLongitude();
        this.latitude = currentLocation.getLatitude();
        this.altitude = currentLocation.getAltitude();
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

}
