package br.unicamp.busfinder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ListPoints extends ItemizedOverlay<PItem> {

	private ArrayList<PItem> pinpoints = new ArrayList<PItem>();
	private ArrayList<PItem> backup = new ArrayList<PItem>();
	protected Context context;
	protected static final String TAG = "PointsList";

	public ListPoints(Drawable m, Context context) {
		super(boundCenter(m));
		this.populate();
		this.context = context;
	}

	@Override
	protected PItem createItem(int i) {
		return getPinpoints().get(i);

	}

	@Override
	public int size() {
		return getPinpoints().size();
	}

	/*
	 * public void insertPinpoint(OverlayItem item) { getPinpoints().add(item);
	 * this.populate();
	 */
	public void insertPinpoint(PItem item) {
		if (!getPinpoints().contains(item)) {
			getPinpoints().add(item);
			backup.add(item);
			setLastFocusedIndex(-1);
			this.populate();
		}

	}
	
	public void updateShownPoints(String s) {
		/*ArrayList<PItem> limbo = new ArrayList<PItem>();
		ArrayList<PItem> aux = new ArrayList<PItem>();
		for (PItem i: pinpoints) {
			if (!i.getTitle().contains(s)) {
				limbo.add(i);
			}
		}
		for (PItem i: limbo) {
			pinpoints.remove(i);
		}
		for (PItem i: backup) {
			if (i.getTitle().contains(s)) {
				pinpoints.add(i);
				aux.add(i);
			}
		}
		for (PItem i: aux) {
			backup.remove(i);
		}
		for (PItem i: limbo) {
			backup.add(i);
		}*/
		pinpoints.clear();
		for (PItem i: backup) {
			if (i.getTitle().toUpperCase().contains(s.toUpperCase())) {
				pinpoints.add(i);
			}
		}
		this.setLastFocusedIndex(-1);
		this.populate();
	}
	
	public void clear(){
		this.pinpoints.clear();
		this.backup.clear();
	}
	
	public void removeBackupPinpoint(PItem item) {
		backup.remove(item);
	}

	public void removePinpoint(PItem item) {
		// getBlacklist().add(index);

		if (pinpoints.contains(item)) {
			backup.add(item);
			pinpoints.remove(item);
		}
		this.setLastFocusedIndex(-1);
		this.populate();

	}

	public ArrayList<PItem> getPinpoints() {
		return pinpoints;
	}

	public void setPinpoints(ArrayList<PItem> npinpoints) {
		this.pinpoints.clear();
		for (PItem p : npinpoints) {
			this.insertPinpoint(p);
		}

	}

}
