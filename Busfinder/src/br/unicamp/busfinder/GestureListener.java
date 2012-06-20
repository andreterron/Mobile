package br.unicamp.busfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector.OnOverlayGestureListener;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.ZoomEvent;

public class GestureListener implements OnOverlayGestureListener {

	private static final String TAG = "GestureListener";
	Context c;

	public GestureListener(Context c) {
		this.c = c;
	}

	public boolean onDoubleTap(MotionEvent mE, ManagedOverlay mO, GeoPoint gP,
			ManagedOverlayItem item) {

		Log.d(TAG, "onDoubleTAp");

		mO.getMapView().getController().animateTo(gP);
		mO.getMapView().getController().setZoom(18);

		return false;
	}

	public void onLongPress(MotionEvent mE, ManagedOverlay mO) {

		Log.d(TAG, "LongPress");

	}

	public void onLongPressFinished(MotionEvent mE, ManagedOverlay mO,
			final GeoPoint gP, ManagedOverlayItem item) {

		Log.d(TAG, "LongPressFinished");

	}

	public boolean onScrolled(MotionEvent mE1, MotionEvent mE2, float x,
			float y, ManagedOverlay mO) {

		Log.d(TAG, "OnScrolled");
		return false;
	}

	public boolean onSingleTap(MotionEvent mE, ManagedOverlay mO, GeoPoint gP,
			ManagedOverlayItem item) {

		Log.d(TAG, "OnSingleTap");
		return false;
	}

	public boolean onZoom(ZoomEvent zE, ManagedOverlay mO) {
		Log.d(TAG, "onZoom");
		return false;
	}

}
