package br.unicamp.busfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	public static GeoPoint geoFromJSON(JSONObject j) {

		try {
			if (j == null)
				return null;

			int lat = (int) (Double.parseDouble(j.getString("lat")) * 1e6);
			int lon = (int) (Double.parseDouble(j.getString("lon")) * 1e6);
			String name = j.getString("name");

			return new GeoPoint(lat, lon);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void Point2Point(GeoPoint touchedpoint, MapView map,
			Context c, Calendar now) {

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

			GeoPoint sourcePoint = geoFromJSON(jar.getJSONObject(0));

			TouchOverlay.DrawPath(BusFinderActivity.myPoint, sourcePoint,
					Color.GREEN, map, true);

			String source_ = jar.getJSONObject(0).getString("name");
			if (source_ == null)
				source_ = "Point" + source;

			jar = getJSON(req + dest);

			GeoPoint destPoint = geoFromJSON(jar.getJSONObject(0));

			TouchOverlay.DrawPath(destPoint, touchedpoint, Color.BLUE, map,
					false);

			PathOverlay pO = new PathOverlay(sourcePoint, destPoint, 2,
					Color.RED);
			TouchOverlay.pathlist.addItem(pO, map);

			String dest_ = jar.getJSONObject(0).getString("name");
			if (dest_ == null)
				dest_ = "Point" + dest;

			Log.d("TOAST", "Take " + circular + " from " + source_ + " at "
					+ departure + " and arrive at " + dest_ + " at " + arrival
					+ "----YOU HAVE " + timeleft + " seconds");

			BusFinderActivity.toast = Toast.makeText(c, "teste",
					Toast.LENGTH_SHORT);
			// toast.setGravity(Gravity.TOP, 0, 70);
			BusFinderActivity.toast.setGravity(Gravity.BOTTOM, 0, 0);
			BusFinderActivity.toast.show();

			BusFinderActivity.timer = new CountDownTimer(timeleft * 1000, 1000) {

				public void onTick(long millisUntilFinished) {
					BusFinderActivity.toast.setText("Bus Leaves in: \n"
							+ millisUntilFinished / 1000 + " seconds");
					// mTextField.setText("seconds remaining: " +
					// millisUntilFinished / 1000);
					BusFinderActivity.toast.show();
				}

				public void onFinish() {
					BusFinderActivity.toast.setText("Time´s up!");
					BusFinderActivity.toast.show();
				}
			};

			BusFinderActivity.timer.start();

			BusFinderActivity.dialog = new AlertDialog.Builder(c).create();
			BusFinderActivity.dialog.setMessage(String.format("%s to %s (%d m)"
					+ "\n\n Take %s at %s" + "\n\n Arrive at %s at %s"
					+ "\n\n Go to your final destination (%d m) ~%s" + "",
					action,source+"_"+source_, distSource, circular, departure,dest+"_"+dest_,
					arrival, distDest, finalTime));

			/*
			 * "Take " + circular + " from " + source_ + " at " + departure +
			 * "\n\nArrive at " + dest_ + " at " + arrival + "\n\nYOU HAVE " +
			 * timeleft + " seconds untill the bus leaves(you are )"++action);
			 */

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

	public static void nextBuses(String title, Context c) {

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

			AlertDialog dialog = new AlertDialog.Builder(c).create();

			dialog.setTitle(title);
			dialog.setMessage(display);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			
			return;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		Toast.makeText(c, "Error or no Connection ..", Toast.LENGTH_SHORT).show();


	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

}
