package br.unicamp.busfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MyLoc extends ListPoints {

	Context c;

	public MyLoc(Drawable m, Context context) {
		super(m, context);
		this.c = context;
	}

	@Override
	public boolean onTap(final int index) {

		Log.d("MyLOC", "OnTap");

		final PItem item = getPinpoints().get(index);

		AlertDialog alert = new AlertDialog.Builder(c).create();
		alert.setTitle("Your Location");
		alert.setMessage("Pick Option");
		alert.setButton("Add to Favorites",
				
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						AlertDialog alert2 = new AlertDialog.Builder(c)
								.create();

						alert2.setTitle("Enter new point name");

						final EditText inputName = new EditText(c);
						inputName.setHint("Point Name");

						final EditText inputDesc = new EditText(c);
						inputDesc.setHint("Point Description");

						LinearLayout v = new LinearLayout(c);
						v.setOrientation(LinearLayout.VERTICAL);
						v.addView(inputName);
						v.addView(inputDesc);

						alert2.setView(v);

						alert2.setButton("ok",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										Toast.makeText(c, "added new point",
												Toast.LENGTH_SHORT).show();


										PItem item = new PItem(
												BusFinderActivity.myPoint, inputName
														.getText()
														.toString(),
												inputDesc.getText()
														.toString());

										BusFinderActivity.favorites
												.insertPinpoint(item);

										BusFinderActivity.map.invalidate();


									}
								});
						alert2.show();

					}
				});
		
		//alert.setButton2("Closest Bus Stops", new DialogInterface.OnClickListener() {
			
		//	public void onClick(DialogInterface dialog, int which) {
				
		//		
				
		//	}
		//});
		alert.setCanceledOnTouchOutside(true);

		alert.show();

		return false;

	}

}
