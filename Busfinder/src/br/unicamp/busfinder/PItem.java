package br.unicamp.busfinder;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PItem extends OverlayItem implements Parcelable {

	// OverlayItem item;
	private static final String TAG = "PITEM";
	int mData;

	public int describeContents() {
		Log.d(TAG, "describecontents");
		return this.hashCode();

	}

	public void writeToParcel(Parcel out, int flags) {

		Log.d(TAG, "w2Parcel"+ mTitle);
		if(out==null)Log.d(TAG,"null");
		out.writeInt(mPoint.getLatitudeE6());
		out.writeInt(mPoint.getLongitudeE6());
		out.writeString(mTitle);
		out.writeString(mSnippet);
		

		out.writeInt(mData);

	}

	public static final Parcelable.Creator<PItem> CREATOR = new Parcelable.Creator<PItem>() {
		public PItem createFromParcel(Parcel in) {
			Log.d(TAG, "createFromParcel");
			
			
			
			return new PItem(new GeoPoint(in.readInt(), in.readInt()), in.readString(),in.readString());
		}

		public PItem[] newArray(int size) {
			Log.d(TAG, "newArray");
			return new PItem[size];
		}
	};


	public PItem(GeoPoint gp, String title, String snippet) {
		super(gp, title, snippet);
		//Log.d(TAG, "Pitem:"+title);

	}
	

}
