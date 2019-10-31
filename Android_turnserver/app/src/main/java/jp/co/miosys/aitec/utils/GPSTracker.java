package jp.co.miosys.aitec.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Random;

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// location permission
	boolean isHavePermission = false;
	// flag for GPS status
	boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute

	private static final float LONGITUDE_TO_KILOMETER_AT_ZERO_LATITUDE = 111.320f;
	private static final float KILOMETER_TO_METER = 1000.0f;
	private static final float LATITUDE_TO_KILOMETER = 111.133f;
	// Declaring BookingLongTripAroundAdapter Position Manager
	protected LocationManager locationManager;
	private LocateListener listener;
	private static final double SQUARE_ROOT_TWO = Math.sqrt(2);
	private static final Random mRandom = new Random();
	private int mBlurRadius;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

//	public boolean handlePermissionsAndGetLocation() {
//		if (Build.VERSION.SDK_INT < 23)
//			return true;
//		int hasWriteContactsPermission = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
//		if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
//			ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Globals.REQUEST_CODE_LOCATION_PERMISSIONS);
//			return false;
//		}
//		return true;
//	}

	public Location getLocation() {
		try {
			int hasWriteContactsPermission = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
			if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Globals.REQUEST_CODE_LOCATION_PERMISSIONS);
				return null;
			}
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;

				if (isNetworkEnabled) {
					//Looper.prepare()
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					//Looper.loop();
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							location = blurWithRadius(location);
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						ALog.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								location = blurWithRadius(location);
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude(){
		if(location != null){
			latitude = location.getLatitude();
		}
		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude(){
		if(location != null){
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}


	public static double calculateDistance(double startLongitude,double startLatitude,double endLongitude,double endLatitude) {
		float[] results = new float[3];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
		return results[0];
	}
	private Location blurWithRadius(final Location originalLocation) {
		if (mBlurRadius <= 0) {
			return originalLocation;
		} else {
			Location newLocation = new Location(originalLocation);

			double blurMeterLong = calculateRandomOffset(mBlurRadius) / SQUARE_ROOT_TWO;
			double blurMeterLat = calculateRandomOffset(mBlurRadius) / SQUARE_ROOT_TWO;

			newLocation.setLongitude(newLocation.getLongitude() + meterToLongitude(blurMeterLong, newLocation.getLatitude()));
			newLocation.setLatitude(newLocation.getLatitude() + meterToLatitude(blurMeterLat));

			return newLocation;
		}
	}
	private static int calculateRandomOffset(final int radius) {
		return mRandom.nextInt((radius + 1) * 2) - radius;
	}

	public void setBlurRadius(final int blurRadius) {
		mBlurRadius = blurRadius;
	}
	public static double longitudeToMeter(double longitude, double latitude) {
		return longitudeToKilometer(longitude, latitude) * KILOMETER_TO_METER;
	}
	public static double longitudeToKilometer(double longitude, double latitude) {
		return longitude * LONGITUDE_TO_KILOMETER_AT_ZERO_LATITUDE * Math.cos(Math.toRadians(latitude));
	}
	public static double meterToLatitude(double meter) {
		return meter / latitudeToMeter(1.0f);
	}
	public static double latitudeToMeter(double latitude) {
		return latitudeToKilometer(latitude) * KILOMETER_TO_METER;
	}
	public static double latitudeToKilometer(double latitude) {
		return latitude * LATITUDE_TO_KILOMETER;
	}
	public static double meterToLongitude(double meter, double latitude) {
		return meter / longitudeToMeter(1.0f, latitude);
	}

	public void onLocationChanged(Location location) {
		if (this.listener != null)
			this.listener.onLocate(location.getLongitude(),location.getLatitude());
		stopUsingGPS();
	}
	public void getLocationCoodinate(LocateListener listener){
		this.listener = listener;
	}
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	public interface LocateListener {
		public void onLocate(double longitude, double latitude);
	}
}
