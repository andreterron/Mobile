package br.unicamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class SendPointActivity extends Activity implements OnClickListener,
		OnSharedPreferenceChangeListener {
	private static final String CENTER_LATITUDE = "-00.8177";
	private static final String CENTER_LONGITUDE = "-00.0683";
	private static final String TAG = "SendPoint";
	static double latitude;
	static double longitude;
	static LocationManager lm;
	LocationListener ll;
	public static Location loc;
	static String bestProvider;
	SharedPreferences prefs;
	Context context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ImageButton b = (ImageButton) findViewById(R.id.imageButton1);
		b.setOnClickListener(this);
				
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		ll = new LocListener();

		Criteria criteria = new Criteria();
		bestProvider = lm.getBestProvider(criteria, false);
		Log.d("Bestprovider", bestProvider);
		
		bestProvider = LocationManager.GPS_PROVIDER;
		
		lm.requestLocationUpdates(bestProvider, 100, 1, ll);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		latitude = Double.parseDouble(prefs.getString("latitude",
				CENTER_LATITUDE));
		longitude = Double.parseDouble(prefs.getString("longitude",
				CENTER_LONGITUDE));

		Log.d("LAT>", String.valueOf((int) latitude * 1e6));
		// Preference pref = (Preference) findPre

	}

	public static GeoPoint getCurrentPosition() {

		//loc = lm.getLastKnownLocation(bestProvider);
		List<String>providers =lm.getProviders(false);
		
		for (String prov : providers){
			loc = lm.getLastKnownLocation(prov);
			if(loc!=null)break;

		}
		
		if (loc == null) {
			Log.d("Location>", "null");
			Log.d(String.valueOf(latitude), String.valueOf(longitude));
			return new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));

		}
		Log.d("Location>", loc.toString());

		return new GeoPoint((int) (loc.getLatitude() * 1E6),
				(int) (loc.getLongitude() * 1E6));

	}

	public void onClick(View v) {

		Log.d("onClick", "ok");

		GeoPoint gp = getCurrentPosition();

		StringBuilder builder = new StringBuilder();

		HttpGet get = new HttpGet(String.format(
				"http://mc933.lab.ic.unicamp.br:8010/savePoint?lat=%s&lon=%s",
				gp.getLatitudeE6() / 1E6, gp.getLongitudeE6() / 1E6));

		HttpClient client = new DefaultHttpClient();

		try {

			HttpResponse response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e("ERRRO", "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d("RESP:", builder.toString());

		Toast.makeText(v.getContext(), builder.toString(), Toast.LENGTH_LONG)
				.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		Log.d(TAG, "CreateMenu");
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemPrefs:

			Log.d(TAG, "PrefsMENU");

			startActivity(new Intent(this, PrefsActivity.class));
			break;

		}
		return false;

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs,
			String key) {

		Log.d(TAG, "Prefs Changed: " + key);

		if (key.equalsIgnoreCase("latitude")) {
			String lat = sharedPrefs.getString("latitude", CENTER_LATITUDE);
			latitude = (double) Double.parseDouble(lat);
		}
		if (key.equalsIgnoreCase("longitude")) {
			String lon = sharedPrefs.getString("longitude", CENTER_LONGITUDE);
			latitude = (double) Double.parseDouble(lon);
		}

	}
	
	

}