package br.unicamp.busfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class ServerOperations {

	public static JSONArray getJSON(String site) {

		Log.d("Executing REquest", site);

		StringBuilder builder = new StringBuilder();

		HttpGet get = new HttpGet(site);

		HttpClient client = new DefaultHttpClient();

		HttpResponse response;
		try {
			response = client.execute(get);

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

			Log.d("RESP:", builder.toString());

			return new JSONArray(builder.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;

	}

	public static GeoPoint geoFromJSON(JSONObject j, String lat_, String lon_,
			String name_) {

		try {
			if (j == null)
				return null;

			int lat = (int) (Double.parseDouble(j.getString(lat_)) * 1e6);
			int lon = (int) (Double.parseDouble(j.getString(lon_)) * 1e6);
			String name = j.getString(name_);

			return new GeoPoint(lat, lon);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void Point2Point(GeoPoint touchedpoint, MapView map,
			final Context c, Calendar now) {

		// now.setTime(new Time(12, 40, 00)); // remove this
		String time = pad(now.getTime().getHours()) + ":"
				+ pad(now.getTime().getMinutes()) + ":"
				+ pad(now.getTime().getSeconds());
		// time = "";

		TouchOverlay.pathlist.clearPath(map);

		String req = String
				.format(BusFinderActivity.SERVER
						+ "Point2Point?s_lat=%f;s_lon=%f;d_lat=%f;d_lon=%f;time=%s;limit=%d",
						(double) BusFinderActivity.myPoint.getLatitudeE6() / 1E6,
						(double) BusFinderActivity.myPoint.getLongitudeE6() / 1E6,
						(double) touchedpoint.getLatitudeE6() / 1e6,
						(double) touchedpoint.getLongitudeE6() / 1e6, time, 5);
		JSONArray path = getJSON(req);
		if (path == null)
			Log.d("No response", "null");
		JSONObject obj = null;
		try {
			obj = path.getJSONObject(0);

			int source = Integer.parseInt(obj.getString("source"));
			int dest = Integer.parseInt(obj.getString("dest"));
			String departure = obj.getString("departure");
			String arrival = obj.getString("arrival");
			String circular = obj.getString("circular");
			int timeleft = obj.getInt("time");
			int distSource = obj.getInt("dist_source");
			int distDest = obj.getInt("dist_dest");
			String finalTime = obj.getString("final_time");
			String action = obj.getString("action").replaceAll("_", "");

			req = BusFinderActivity.SERVER + "getStopPosition?stopid=";

			JSONArray jar = getJSON(req + source);

			GeoPoint sourcePoint = geoFromJSON(jar.getJSONObject(0), "lat",
					"lon", "name");

			TouchOverlay.DrawPath(BusFinderActivity.myPoint, sourcePoint,
					Color.GREEN, map, true);

			String source_ = jar.getJSONObject(0).getString("name");
			if (source_ == null)
				source_ = "Point" + source;

			jar = getJSON(req + dest);

			GeoPoint destPoint = geoFromJSON(jar.getJSONObject(0), "lat",
					"lon", "name");

			TouchOverlay.DrawPath(destPoint, touchedpoint, Color.BLUE, map,
					false);
			
			/* EDIT HERE */
			String site = String
					.format(BusFinderActivity.SERVER
							+ "getBusPath?line=%d&via=%d&start=%d&end=%d",
							(int) Integer.parseInt(obj.getString("line")),
							(int) Integer.parseInt(obj.getString("via")),
							(int) Integer.parseInt(obj.getString("source")),
							(int) Integer.parseInt(obj.getString("dest")));
			JSONArray sitePoints = getJSON(site);
			GeoPoint[] points = new GeoPoint[sitePoints.length()];
			for (int i = 0; i < sitePoints.length(); i++) {
				points[i] = ServerOperations.geoFromJSON(sitePoints.getJSONObject(i), "lat", "lon", "name");
			}
			TouchOverlay.DrawPathList(points, Color.RED, map, false);
			Log.d("xxx", "URL=" + site.toString());
			// get the kml (XML) doc. And parse it to get the coordinates(direction
			// route).
			/* END EDIT
			PathOverlay pO = new PathOverlay(sourcePoint, destPoint, 2,
					Color.RED);
			TouchOverlay.pathlist.addItem(pO, map);*/

			String dest_ = jar.getJSONObject(0).getString("name");
			if (dest_ == null)
				dest_ = "Point" + dest;

			Log.d("TOAST", "Take " + circular + " from " + source_ + " at "
					+ departure + " and arrive at " + dest_ + " at " + arrival
					+ "----YOU HAVE " + timeleft + " seconds");

			BusFinderActivity.toast = Toast.makeText(c, "teste",
					Toast.LENGTH_SHORT);
			BusFinderActivity.toast.setGravity(Gravity.BOTTOM, 0, 0);
			//BusFinderActivity.toast.show();
			

			BusFinderActivity.timer = new CountDownTimer(timeleft * 1000, 1000) {

				public void onTick(long millisUntilFinished) {
					//BusFinderActivity.toast.setText("Bus Leaves\n in: "
						//	+ millisUntilFinished / 1000 + " s");
					//BusFinderActivity.toast.show();
					BusFinderActivity.countdown.setText("Bus leaves in:"+ millisUntilFinished / 1000 + " s");
				}

				public void onFinish() {
					BusFinderActivity.toast.setText("Time´s up!");
					BusFinderActivity.toast.show();
					BusFinderActivity.countdown.setText("0:00");
				}
			};

			BusFinderActivity.timer.start();

			BusFinderActivity.dialog = new AlertDialog.Builder(c).create();
			BusFinderActivity.dialog.setMessage(String
					.format("%s to %s (%d m)" + "\n\n Take %s at %s"
							+ "\n\n Arrive at %s at %s"
							+ "\n\n Go to your final destination (%d m) ~%s"
							+ "", action, source + "_" + source_, distSource,
							circular, departure, dest + "_" + dest_, arrival,
							distDest, finalTime));

			BusFinderActivity.dialog.setCanceledOnTouchOutside(true);
			BusFinderActivity.dialog.show();
			BusFinderActivity.dialog.setTitle("Instructions");

			return;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Toast.makeText(c, "Sorry No Path Found or Connection down...",
				Toast.LENGTH_LONG).show();

	}

	public static void nextBuses(final String title, final Context c) {

		int stopid = Integer.parseInt(title.split("_")[0]);

		String req = BusFinderActivity.SERVER + "getNextBus?stopid=";
		JSONArray jar = getJSON(req + stopid);
		String display = "";
		try {

			for (int i = 0; i < jar.length(); i++) {
				JSONObject jos = jar.getJSONObject(i);
				String circular = jos.getString("circular");
				String time = jos.getString("time");

				display += "--" + circular + "------" + time + "\n";

			}

			final String display_ = display;

			AlertDialog dialog = new AlertDialog.Builder(c).create();

			dialog.setTitle(title);
			dialog.setMessage(display_);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();

			return;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Toast.makeText(c, "Error or no Connection ..", Toast.LENGTH_SHORT)
				.show();

	}

	public static ListPoints updateBusPositions(Context c, MapView map, ListPoints realBus) {

		Log.d("Updating Bus Posisionts ...","");
		
		realBus.clear();
		
		
		String req = BusFinderActivity.SERVER + "getBusesPositions";
		JSONArray jar = getJSON(req);
		try {

			for(int i=0; i<jar.length();i++){
			
			JSONObject bus = jar.getJSONObject(i);

			GeoPoint gP = new GeoPoint(
					(int) (bus.getDouble("latitude") * 1e6),
					(int) (bus.getDouble("longitude") * 1e6));
			//GeoPoint gP2 = new GeoPoint(
				//	(int) (bus2.getDouble("latitude") * 1e6),
					//(int) (bus2.getDouble("longitude") * 1e6));
			String placa = bus.getString("licensePlate");
			//String placa2 = bus2.getString("licensePlate");

			Log.d(placa, gP.toString());

			//BusFinderActivity.
			//BusFinderActivity.
			realBus.insertPinpoint(new PItem(gP, "bus1",
					placa));
			//BusFinderActivity.
			//realBus.insertPinpoint(new PItem(gP2, "bus2",
				//	placa2));

			//BusFinderActivity.map.invalidate();
			// map.getController().animateTo(gP2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return realBus;
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

}
