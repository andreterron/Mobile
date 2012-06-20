package br.unicamp.busfinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.OverlayManager;

//teste
public class BusFinderActivity extends MapActivity implements
		OnSharedPreferenceChangeListener, OnClickListener {

	public static final String SERVER = "http://mc933.lab.ic.unicamp.br:8010/";
	private static final int CENTER_LATITUDE = (int) (-22.8177 * 1E6);
	private static final int CENTER_LONGITUDE = (int) (-47.0683 * 1E6);
	public static GeoPoint myPoint = new GeoPoint(CENTER_LATITUDE,
			CENTER_LONGITUDE);
	private static final String TAG = "busFinder";

	public static MapView map;
	private MapController controller;
	public static ListPoints myPosition;

	private static LocationManager lm;
	UnicampLocationListener locationListener;
	String towers;
	Drawable d;

	static BusPoints busPoints;
	static FavoritePoints favorites;
	public static AlertDialog dialog;
	public static CountDownTimer timer;

	OverlayManager overlayManager;

	private SharedPreferences prefs;
	ImageButton button;
	AutoCompleteTextView acTextView;
	boolean isFavorite = true;

	//teste
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// bundle = savedInstanceState;
		// if(bundle==null)bundle = new Bundle();

		try {
			Log.d(TAG, "OnCreate:" + savedInstanceState.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		prefs.registerOnSharedPreferenceChangeListener(this);

		map = (MapView) findViewById(R.id.map);
		map.setBuiltInZoomControls(true);
		map.setSatellite(false);
		map.setTraffic(false);
		map.setStreetView(false);
		map.setStreetView(false);

		busPoints = new BusPoints(getResources()
				.getDrawable(R.drawable.busstop), this);
		map.getOverlays().add(busPoints);

		loadbusStops(busPoints);

		favorites = new FavoritePoints(getResources().getDrawable(
				R.drawable.favorites), this);

		map.getOverlays().add(favorites);

		myPosition = new ListPoints(getResources().getDrawable(
				R.drawable.ic_launcher), this);

		restorePointsList(favorites);

		loadPrefs();
		map.invalidate();

		controller = map.getController();

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationListener = new UnicampLocationListener(this, map);

		locationListener.setMove(false);

		lm.requestLocationUpdates(getBestProvider(), 0, 0, locationListener);

		myPoint = getCurrentPosition(this);
		myPosition = new MyLoc(getResources().getDrawable(R.drawable.user),
				this);
		// myPosition.setItem(new OverlayItem(myPoint, "MyPoin", "snippet"));
		map.getOverlays().add(myPosition);
		myPosition.insertPinpoint(new PItem(myPoint, "mypoint", "snippet"));

		controller.setCenter(myPoint);
		controller.animateTo(myPoint);
		controller.setZoom(16);

		TouchOverlay t = new TouchOverlay(this);
		map.getOverlays().add(t);

		acTextView = (AutoCompleteTextView) findViewById(R.id.fav_search);

		acTextView.setAdapter(favorites.getAdapter());
		
		acTextView.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			public void afterTextChanged(Editable s) {
				Log.d("LISTENER", "TextWatcher");
				favorites.updateShownPoints(s.toString());
				busPoints.updateShownPoints(s.toString());
				map.invalidate();
				
			}
		});
		this.getCurrentFocus();
		acTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("LISTENER", "OnItemClickListener Activated with args: " + arg2 + " and " + arg3);
				BusFinderActivity a = (BusFinderActivity) arg1.getContext();
				a.hideKeyboard();
				arg1.clearFocus();
			}
		});
		// textView.setAdapter(busPoints.getAdapter());

		button = (ImageButton) findViewById(R.id.imageButton1);
		button.setOnClickListener(this);

		overlayManager = new OverlayManager(this, map);
		Drawable defaultmarker = getResources().getDrawable(
				R.drawable.ic_launcher);

		ManagedOverlay managedOverlay = overlayManager
				.createOverlay(defaultmarker);

		// creating some marker:
		// managedOverlay.createItem(new
		// GeoPoint(CENTER_LATITUDE,CENTER_LONGITUDE));
		// //managedOverlay.createItem(new GeoPoint(...));

		// managedOverlay.enableLazyLoadAnimation((ImageView)
		// findViewById(R.id.imageButton1));
		// registers the ManagedOverlayer to the MapView
		overlayManager.populate();

		managedOverlay.setOnOverlayGestureListener(new GestureListener(this));

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");

		savePointsList(favorites);

		super.onDestroy();

	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		lm.removeUpdates(locationListener);

	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				locationListener);
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	public void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(
			    INPUT_METHOD_SERVICE);
		View v = getCurrentFocus();
		if (v != null)
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	private void restorePointsList(FavoritePoints fav) {

		BufferedReader reader = null;
		String line, title = "", snippet = "";

		try {

			reader = new BufferedReader(new FileReader(getFilesDir()
					+ File.separator + "favorites.bus"));
			int lines = 0;
			while ((line = reader.readLine()) != null) {
				Log.d("Reading...", line);
				String[] strs = line.split("\\#");
				if (strs.length > 2)
					title = strs[2];
				if (strs.length > 3)
					snippet = strs[3];

				fav.insertPinpoint(new PItem(new GeoPoint(Integer
						.parseInt(strs[0]), Integer.parseInt(strs[1])), title,
						snippet));

				lines++;

			}
			Log.d("TOTALFAVS", "" + lines);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void savePointsList(FavoritePoints fav) {
		BufferedWriter writer = null;

		String FILENAME = "favorites.bus";

		try {
			deleteFile(FILENAME);

			writer = new BufferedWriter(new FileWriter(getFilesDir()
					+ File.separator + FILENAME));

			for (PItem it : fav.getPinpoints()) {

				Log.d("Writing..", it.toString());
				writer.write(it.getPoint().getLatitudeE6() + "#"
						+ it.getPoint().getLongitudeE6() + "#" + it.getTitle()
						+ "#" + it.getSnippet() + "\n");

			}

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreBundle:" + savedInstanceState.toString());
		super.onRestoreInstanceState(savedInstanceState);

		ArrayList<PItem> favs = savedInstanceState
				.getParcelableArrayList("favs");

		// if (favorites == null) {
		// favorites = new FavoritePoints(getResources().getDrawable(
		// R.drawable.favorites), this);
		// map.getOverlays().add(favorites);
		// }

		// favorites.setPinpoints(favs);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveBundle:" + outState.toString());
		super.onSaveInstanceState(outState);
		// outState.putParcelableArrayList("favs", favorites.getPinpoints());
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
		case R.id.itemKML:

			if(timer!=null)
				timer.cancel();				
			TouchOverlay.pathlist.clearPath(map);
			map.invalidate();
			dialog=null;

			break;

		case R.id.itemInfo:
			if(dialog!=null)
				dialog.show();
			else Toast.makeText(this, "No route created", Toast.LENGTH_SHORT);
			
			break;

		case R.id.itemLocation:

			myPoint = getCurrentPosition(this);
			controller.animateTo(myPoint);
			controller.setZoom(18);
			break;
		}
		Log.d(TAG, "Menu Selected: " + item.getItemId());
		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs,
			String key) {

		Log.d(TAG, "Prefs Changed: " + key);
		this.loadPrefs();

	}

	private void loadPrefs() {
		map.setStreetView((prefs.getBoolean("streetView", false)));
		map.setSatellite(prefs.getBoolean("satelliteView", false));
	}

	public static String getBestProvider() {
		Criteria criteria = new Criteria();
		String ret = lm.getBestProvider(criteria, false);
		Log.d("BestProvideR:", ret);
		return ret;

	}

	public static GeoPoint getCurrentPosition(Context c) {
		GeoPoint src;
		// LocationManager locationMgr = (LocationManager) c
		// .getSystemService(Context.LOCATION_SERVICE);

		Location loc = lm.getLastKnownLocation(getBestProvider());

		if (loc != null) {
			src = new GeoPoint((int) (loc.getLatitude() * 1E6),
					(int) (loc.getLongitude() * 1E6));
			Log.d(TAG, "gotLocation");

		} else {
			Toast.makeText(c, "no provider found", Toast.LENGTH_SHORT).show();
			src = myPoint;
			Log.d(TAG, "location = null");
		}

		return src;
	}

	public static float GeoDistance(GeoPoint p1, GeoPoint p2) {

		Location l1, l2;
		l1 = new Location("");
		l2 = new Location("");
		l1.setLatitude(p1.getLatitudeE6() / 1E6);
		l1.setLongitude(p1.getLongitudeE6() / 1E6);
		l2.setLatitude(p2.getLatitudeE6() / 1E6);
		l2.setLongitude(p2.getLongitudeE6() / 1E6);

		return l1.distanceTo(l2);

	}

	private void loadBusStopsServer(BusPoints stops) {

		String req = null;

		try {
			for (int i = 0; i < 49; i++) {

				req = String.format(BusFinderActivity.SERVER
						+ "getStopPosition?stopid=%d", i);

				JSONArray jar = ServerOperations.getJSON(req);

				if (jar == null)
					return;

				JSONObject job = jar.getJSONObject(0);

				int lat = (int) (Double.parseDouble(job.getString("lat")) * 1e6);
				int lon = (int) (Double.parseDouble(job.getString("lat")) * 1e6);
				String name = job.getString("name");

				stops.insertPinpoint(new PItem(new GeoPoint(lat, lon), name, ""));

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private String[] loadbusStops(BusPoints stops) {

		Log.d(TAG, "GETDIRECTIONDATA");
		Document doc = null;

		InputStream fis = null;

		try {
			fis = getAssets().open("bus_stops.kml");
		} catch (IOException e) {
			Log.d(TAG, "FILENOTFOUND");
			e.printStackTrace();
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(fis);
			if (doc == null)
				Log.d(TAG, "DOC NULL");
			//Log.d("TEXTCONTEXT:", doc.getElementsByTagName("Coordinates").toString());

			NodeList nplace = doc.getElementsByTagName("Placemark");
			int j;

			for (int i = 0; i < nplace.getLength(); i++) {
				NodeList kids = nplace.item(i).getChildNodes();
				GeoPoint gpoint = null;
				String stopName = null;
				String stopid;

				for (j = 0; j < kids.getLength(); j++) {
					if (kids.item(j).getNodeName().contains("Point")) {
						String[] pairs = kids.item(j).getTextContent().trim()
								.split(",");
						//Log.d("Point", "LAT:" + pairs[0] + "LON:" + pairs[1]);
						gpoint = new GeoPoint(
								(int) (Double.parseDouble(pairs[1]) * 1E6),
								(int) (Double.parseDouble(pairs[0]) * 1E6));
					} else if (kids.item(j).getNodeName().contains("name")) {
						stopName = kids.item(j).getTextContent().trim();
						//Log.d("Point Name", "BUSSTOP NAME : " + stopName);
					} else if (kids.item(j).getNodeName().contains("stopid")) {
						stopid = kids.item(j).getTextContent().trim();
						//Log.d(stopName, stopid);
					}

				}
				if (gpoint == null || stopName == null)
					continue;

				stops.insertPinpoint(new PItem(gpoint, stopName, ""));

			}

			/*
			 * NodeList ncoords = doc.getElementsByTagName("coordinates");
			 * 
			 * for (int i = 0; i < ncoords.getLength(); i++) {
			 * 
			 * String[] pairs = ncoords.item(i).getTextContent().split(",");
			 * Log.d("Point", "LAT:" + pairs[0] + "LON:" + pairs[1]);
			 * 
			 * GeoPoint gpoint = new GeoPoint( (int)
			 * (Double.parseDouble(pairs[1]) * 1E6), (int)
			 * (Double.parseDouble(pairs[0]) * 1E6));
			 * 
			 * busStops.insertPinpoint(new OverlayItem(gpoint, "point:" + i,
			 * "snippet"));
			 * 
			 * }
			 */

		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "ERROROR:" + e);
		}

		if (doc == null)
			Log.d(TAG, "DOC NULLL");

		return null;

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.imageButton1:

			if (!isFavorite) {
				button.setImageResource(R.drawable.favorites);
				acTextView.setAdapter(favorites.getAdapter());
				acTextView.setHint(R.string.searchFavoritesHint);
				acTextView.setText("");
				isFavorite = true;
			} else if (isFavorite) {
				button.setImageResource(R.drawable.busstop);
				acTextView.setAdapter(busPoints.getAdapter());
				acTextView.setHint(R.string.searchBusStopsHint);
				acTextView.setText("");
				isFavorite = false;
			}
			Log.d(TAG, "onClickImageButton!");

			break;
		default:
			break;
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

}
