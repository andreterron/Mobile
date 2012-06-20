package br.unicamp.busfinder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TouchOverlay extends Overlay {
	long start_time, stop_time;
	private Context context;
	private Drawable d;

	private PointF sp, ep;
	private float distance;
	GeoPoint touchedpoint;
	static PathList pathlist;

	public TouchOverlay(Context context) {
		this.context = context;
		d = context.getResources().getDrawable(R.drawable.ic_launcher);
		sp = new PointF();
		ep = new PointF();
		pathlist = new PathList();

	}

	@Override
	public boolean onTouchEvent(MotionEvent e, final MapView map) {

		// Log.d("TouchOverlay", "onTouch");
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			start_time = e.getEventTime();
			sp.set(e.getX(), e.getY());

			touchedpoint = map.getProjection().fromPixels((int) sp.x,
					(int) sp.y);

		}
		if (e.getAction() == MotionEvent.ACTION_UP) {
			stop_time = e.getEventTime();
			ep.set(e.getX(), e.getY());

			distance = ((ep.x - sp.x) * ((ep.y - sp.y)) + (ep.y - sp.y)
					* (ep.y - sp.y));
			if (distance < 0)
				distance *= -1;

		}

		if (stop_time - start_time > 600 & distance < 50) {
			Log.d("TouchOverlay", "LongTouch");

			Vibrator v = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(200);

			AlertDialog alert = new AlertDialog.Builder(context).create();
			alert.setTitle("Alert Title");
			alert.setMessage("Pick Option");
			alert.setButton("Add to Favorites",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							AlertDialog alert2 = new AlertDialog.Builder(context)
									.create();

							alert2.setTitle("Enter new point name");

							final EditText inputName = new EditText(context);
							inputName.setHint("Point Name");

							final EditText inputDesc = new EditText(context);
							inputDesc.setHint("Point Description");

							LinearLayout v = new LinearLayout(context);
							v.setOrientation(LinearLayout.VERTICAL);
							v.addView(inputName);
							v.addView(inputDesc);

							alert2.setView(v);

							alert2.setButton("ok",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {

											Toast.makeText(context,
													"added new point",
													Toast.LENGTH_SHORT).show();

											PItem item = new PItem(
													touchedpoint, inputName
															.getText()
															.toString(),
													inputDesc.getText()
															.toString());

											BusFinderActivity.favorites
													.insertPinpoint(item);

											map.invalidate();

										}
									});
							alert2.show();

						}
					});

			alert.setButton2("GetPath", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					
					
					AlertDialog alert2 = new AlertDialog.Builder(context)
					.create();
					alert2.setTitle("When do you want to go?");
					
					alert2.setButton("Now", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {

							
							//new Thread(new Runnable() {
								
							//	public void run() {
									ServerOperations.Point2Point(touchedpoint, BusFinderActivity.map, context,Calendar.getInstance());
							//	}
						//	});
							
						}
					});
					
					
					
					alert2.setButton2("Choose Time", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
							final Calendar now = Calendar.getInstance();
							
							
							TimePickerDialog.OnTimeSetListener mTimeSetListener =
								    new TimePickerDialog.OnTimeSetListener() {
								        public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
								          
								        	
								        //	new Thread(new Runnable() {
												
										//		public void run() {
										        	now.setTime(new Time(hourOfDay, minute, 0));
										        	ServerOperations.Point2Point(touchedpoint, BusFinderActivity.map, context,now);													
										//		}
										//	});

								        	
								        	
								        }
								    };
							
							TimePickerDialog tp = new TimePickerDialog(context, mTimeSetListener, now.getTime().getHours(),now.getTime().getMinutes(), true);
							tp.show();
							tp.setCanceledOnTouchOutside(true);
							
						}
					});
					alert2.show();
					
									
					


									}
			});
			alert.setButton3("Cancel", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					

				}
			});

			alert.setCanceledOnTouchOutside(true);

			alert.show();

		}

		map.invalidate();
		return false;
	}

	public static void DrawPath(GeoPoint src, GeoPoint dest, int color,
			MapView mapView, boolean clear) {
		// connect to map web service

		
		if (mapView == null)
			mapView = BusFinderActivity.map;

		if (clear){
			if(BusFinderActivity.timer!=null)
				BusFinderActivity.timer.cancel();				
			BusFinderActivity.dialog=null;
			pathlist.clearPath(mapView);
		}
		PathOverlay pO;

		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString((double) src.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString
				.append(Double.toString((double) src.getLongitudeE6() / 1.0E6));
		urlString.append("&daddr=");// to
		urlString
				.append(Double.toString((double) dest.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString
				.append(Double.toString((double) dest.getLongitudeE6() / 1.0E6));
		urlString.append("&ie=UTF8&0&dirflg=w&cad=tm:d&mra=atm&output=kml&");
		Log.d("xxx", "URL=" + urlString.toString());
		// get the kml (XML) doc. And parse it to get the coordinates(direction
		// route).
		Document doc = null;
		HttpURLConnection urlConnection = null;
		URL url = null;
		try {
			url = new URL(urlString.toString());
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.connect();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(urlConnection.getInputStream());

			if (doc.getElementsByTagName("GeometryCollection").getLength() > 0) {

				String path = doc.getElementsByTagName("GeometryCollection")
						.item(0).getFirstChild().getFirstChild()
						.getFirstChild().getNodeValue();
				Log.d("xxx", "path=" + path);
				String[] pairs = path.split(" ");
				String[] coords = pairs[0].split(","); // coords[0]=longitude
														// coords[1]=latitude
														// coords[2]=height
				// src
				GeoPoint startGP = new GeoPoint(
						(int) (Double.parseDouble(coords[1]) * 1E6),
						(int) (Double.parseDouble(coords[0]) * 1E6));

				pO = new PathOverlay(startGP, startGP, 1);
				// mapView.getOverlays().add(pO);
				pathlist.addItem(pO, mapView);

				GeoPoint gp1;
				GeoPoint gp2 = startGP;
				for (int i = 1; i < pairs.length; i++) // the last one would be
														// crash
				{
					coords = pairs[i].split(",");
					gp1 = gp2;
					// watch out! For GeoPoint, first:latitude, second:longitude
					gp2 = new GeoPoint(
							(int) (Double.parseDouble(coords[1]) * 1E6),
							(int) (Double.parseDouble(coords[0]) * 1E6));

					pO = new PathOverlay(gp1, gp2, 2, color);
					pathlist.addItem(pO, mapView);
					Log.d("xxx", "pair:" + pairs[i]);
				}
				pO = new PathOverlay(dest, dest, 3);
				// mapView.getOverlays().add(pO);
				pathlist.addItem(pO, mapView);
				// mapView.getOverlays().add(pathlist);
				mapView.invalidate();
				
							
				

				// use
				// the
				// default
				// color
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

}
