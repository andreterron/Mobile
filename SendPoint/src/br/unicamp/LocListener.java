package br.unicamp;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocListener implements LocationListener {

	public void onLocationChanged(Location location) {
		Log.d("LL", "onlocationChanged");
		SendPointActivity.latitude = location.getLatitude();
		SendPointActivity.longitude = location.getLongitude();

	}

	public void onProviderDisabled(String provider) {
		Log.d("LL", "onProviderDisabled");
	}

	public void onProviderEnabled(String provider) {
		Log.d("LL", "onProviderEnabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("LL", "onStatusChanged");
	}

}
