package br.unicamp.busfinder;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PathList extends Overlay {
	List<PathOverlay> pathlist = new ArrayList<PathOverlay>();

	public void addItem(PathOverlay o, MapView m) {
		pathlist.add(o);
		m.getOverlays().add(o);
	}

	public void removeItem(PathOverlay o,MapView m) {
		//pathlist.remove(o);
		m.getOverlays().remove(o);
		
	}

	public void clearList() {
		pathlist.clear();
	}

	public void clearPath(MapView map) {

		for (PathOverlay p : pathlist) {
			//map.getOverlays().remove(p);
			 removeItem(p,map);
		}
		clearList();

	}

}
