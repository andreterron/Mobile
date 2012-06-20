package br.unicamp.busfinder;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class FavoritePoints extends ListPoints {

	private ArrayAdapter<String> favAdapter;

	
	public FavoritePoints(Drawable m, Context context) {
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

		dialog.setButton("Set Destination",
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
						
						
						
												
						//ServerOperations.Point2Point(item.getPoint(), BusFinderActivity.map, context,now);
						
						

						/*Toast.makeText(
								context,
								"destination is "
										+ BusFinderActivity.GeoDistance(
												BusFinderActivity.myPoint,
												item.getPoint()) + "m away",
								Toast.LENGTH_SHORT).show();*/

					}
				});

		dialog.setButton2("Remove", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				removePinpoint(item);
				BusFinderActivity.map.invalidate();

			}
		});

		dialog.setButton3("Cancel", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				//

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
		super.removeBackupPinpoint(item);
		favAdapter.remove(item.getTitle());
	}

	public ArrayAdapter<String> getAdapter() {
		return favAdapter;
	}



}
