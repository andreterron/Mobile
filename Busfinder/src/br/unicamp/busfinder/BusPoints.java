package br.unicamp.busfinder;

import java.sql.Time;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;

public class BusPoints extends ListPoints {

	private ArrayAdapter<String> favAdapter;

	public BusPoints(Drawable m, Context context) {
		super(m, context);
		favAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line);
	}

	@Override
	public boolean onTap(final int index) {
		Log.d(TAG, "onTap:" + index);

		final PItem item = getPinpoints().get(index);

		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle("You selected: "
				+ item.getTitle().toString().toUpperCase());
		dialog.setMessage(item.getSnippet());

		dialog.setButton("View Buses", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});

		dialog.setButton2("Set Destination",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						
						

						AlertDialog alert2 = new AlertDialog.Builder(context)
						.create();
						alert2.setTitle("When do you want to go?");
						
						alert2.setButton("Now", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {

								ServerOperations.Point2Point(item.getPoint(), BusFinderActivity.map, context,Calendar.getInstance());
								
							}
						});
						
						
						
						alert2.setButton2("Choose Time", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
								final Calendar now = Calendar.getInstance();
								
								
								TimePickerDialog.OnTimeSetListener mTimeSetListener =
									    new TimePickerDialog.OnTimeSetListener() {
									        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
									          
									        	now.setTime(new Time(hourOfDay, minute, 0));
									        	ServerOperations.Point2Point(item.getPoint(), BusFinderActivity.map, context,now);
									        	
									        	
									        }
									    };
								
								TimePickerDialog tp = new TimePickerDialog(context, mTimeSetListener, now.getTime().getHours(),now.getTime().getMinutes(), true);
								tp.show();
								tp.setCanceledOnTouchOutside(true);
								
							}
						});
						alert2.show();
						
						/*
						
						
						GeoPoint src = BusFinderActivity
								.getCurrentPosition(context);

						TouchOverlay.DrawPath(src, item.getPoint(), Color.CYAN,
								null, false);

						Toast.makeText(
								context,
								"destination is "
										+ BusFinderActivity.GeoDistance(
												BusFinderActivity.myPoint,
												item.getPoint()) + "m away",
								Toast.LENGTH_SHORT).show();
								*/

						//item.setMarker(boundCenter(getBusIcon()));
					}
				});

		dialog.setButton3("StreetView", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Intent streetView = new Intent(
						android.content.Intent.ACTION_VIEW, Uri
								.parse("google.streetview:cbll="
										+ (double) item.getPoint()
												.getLatitudeE6()
										/ 1e6
										+ ","
										+ (double) item.getPoint()
												.getLongitudeE6() / 1e6
										+ "&cbp=1,99.56,,1,-5.27&mz=21"));
				context.startActivity(streetView);

			}
		});

		dialog.setCanceledOnTouchOutside(true);

		dialog.show();

		return true;

	}

	@Override
	public void insertPinpoint(PItem item) {
		super.insertPinpoint(item);
		favAdapter.add(item.getTitle());
	}

	@Override
	public void removePinpoint(PItem item) {
		super.removePinpoint(item);
		favAdapter.remove(item.getTitle());
	}

	public ArrayAdapter<String> getAdapter() {
		return favAdapter;
	}

	private Drawable getBusIcon() {

		Drawable icon = context.getResources().getDrawable(R.drawable.favbus);

		icon.setBounds(0, 0, icon.getIntrinsicWidth(),
				icon.getIntrinsicHeight());

		return icon;

	}

}
