package tw.kewang.mapcontroller;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * @author kewang
 */
public class MapController {
	public static enum MapType {
		MAP_TYPE_NONE, MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
	}

	private static Context context;
	private static GoogleMap map;
	private static ArrayList<Marker> markers;
	private static OnCameraChangeListener ccListener;
	private static OnMyLocationChangeListener mlListener;

	/**
	 * attach and initialize Google Maps
	 * 
	 * @param context
	 * @param map
	 */
	public static void attach(Context context, GoogleMap map)
			throws GooglePlayServicesNotAvailableException {
		MapsInitializer.initialize(context);

		MapController.context = context;
		MapController.map = map;
	}

	/**
	 * detach Google Maps
	 */
	public static void detach() {
		context = null;
		map = null;
		markers = null;
		ccListener = null;
		mlListener = null;
	}

	/**
	 * return map's instance
	 * 
	 * @return
	 */
	public static GoogleMap getMap() {
		return map;
	}

	/**
	 * sets the type of map tiles that should be displayed
	 * 
	 * @param type
	 */
	public static void setType(MapType type) {
		map.setMapType(type.ordinal());
	}

	/**
	 * move to my current location
	 * 
	 * @param tracking
	 * @param callback
	 */
	public static void moveToMyLocation(final boolean tracking,
			final ChangeMyLocation callback) {
		showMyLocation();

		if (mlListener == null) {
			mlListener = new OnMyLocationChangeListener() {
				@Override
				public void onMyLocationChange(Location location) {
					map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
							location.getLatitude(), location.getLongitude())));

					if (!tracking) {
						mlListener = null;
						map.setOnMyLocationChangeListener(null);
					}

					if (callback != null) {
						callback.changed(map, location);
					}
				}
			};
		}

		map.setOnMyLocationChangeListener(mlListener);
	}

	/**
	 * move to my current location
	 * 
	 * @param tracking
	 */
	public static void moveToMyLocation(boolean tracking) {
		moveToMyLocation(tracking, null);
	}

	/**
	 * move to my current location
	 * 
	 * @param tracking
	 * @param callback
	 */
	public static void animateToMyLocation(final boolean tracking,
			final ChangeMyLocation callback) {
		showMyLocation();

		if (mlListener == null) {
			mlListener = new OnMyLocationChangeListener() {
				@Override
				public void onMyLocationChange(Location location) {
					map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
							location.getLatitude(), location.getLongitude())));

					if (!tracking) {
						mlListener = null;
						map.setOnMyLocationChangeListener(null);
					}

					if (callback != null) {
						callback.changed(map, location);
					}
				}
			};
		}

		map.setOnMyLocationChangeListener(mlListener);
	}

	/**
	 * move to my current location
	 * 
	 * @param tracking
	 */
	public static void animateToMyLocation(boolean tracking) {
		animateToMyLocation(tracking, null);
	}

	/**
	 * return my current location
	 * 
	 * @return
	 */
	public static Location getMyLocation() {
		if (!map.isMyLocationEnabled()) {
			showMyLocation();
		}

		return map.getMyLocation();
	}

	/**
	 * show my current location
	 */
	public static void showMyLocation() {
		map.setMyLocationEnabled(true);
	}

	/**
	 * animate to specific location
	 * 
	 * @param latLng
	 * @param zoom
	 * @param callback
	 */
	public static void animateTo(LatLng latLng, int zoom,
			final ChangePosition callback) {
		if (ccListener == null) {
			ccListener = new OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition position) {
					map.setOnCameraChangeListener(null);

					ccListener = null;

					if (callback != null) {
						callback.changed(map, position);
					}
				}
			};

			map.setOnCameraChangeListener(ccListener);
		}

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
	}

	/**
	 * animate to specific location
	 * 
	 * @param latLng
	 */
	public static void animateTo(LatLng latLng) {
		animateTo(latLng, (int) map.getCameraPosition().zoom, null);
	}

	/**
	 * animate to specific location
	 * 
	 * @param latLng
	 * @param callback
	 */
	public static void animateTo(LatLng latLng, ChangePosition callback) {
		animateTo(latLng, (int) map.getCameraPosition().zoom, callback);
	}

	/**
	 * animate to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param callback
	 */
	public static void animateTo(double lat, double lng, ChangePosition callback) {
		animateTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom,
				callback);
	}

	/**
	 * animate to specific location
	 * 
	 * @param lat
	 * @param lng
	 */
	public static void animateTo(double lat, double lng) {
		animateTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom,
				null);
	}

	/**
	 * animate and zoom to specific location
	 * 
	 * @param latLng
	 * @param zoom
	 */
	public static void animateTo(LatLng latLng, int zoom) {
		animateTo(latLng, zoom, null);
	}

	/**
	 * animate and zoom to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param zoom
	 */
	public static void animateTo(double lat, double lng, int zoom) {
		animateTo(new LatLng(lat, lng), zoom, null);
	}

	/**
	 * animate and zoom to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param zoom
	 * @param callback
	 */
	public static void animateTo(double lat, double lng, int zoom,
			ChangePosition callback) {
		animateTo(new LatLng(lat, lng), zoom, callback);
	}

	/**
	 * move to specific location
	 * 
	 * @param latLng
	 * @param callback
	 */
	public static void moveTo(LatLng latLng, int zoom,
			final ChangePosition callback) {
		if (ccListener == null) {
			ccListener = new OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition position) {
					map.setOnCameraChangeListener(null);

					ccListener = null;

					if (callback != null) {
						callback.changed(map, position);
					}
				}
			};

			map.setOnCameraChangeListener(ccListener);
		}

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
	}

	/**
	 * move to specific location
	 * 
	 * @param latLng
	 */
	public static void moveTo(LatLng latLng) {
		moveTo(latLng, (int) map.getCameraPosition().zoom, null);
	}

	/**
	 * move to specific location
	 * 
	 * @param latLng
	 * @param callback
	 */
	public static void moveTo(LatLng latLng, ChangePosition callback) {
		moveTo(latLng, (int) map.getCameraPosition().zoom, callback);
	}

	/**
	 * move to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param callback
	 */
	public static void moveTo(double lat, double lng, ChangePosition callback) {
		moveTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom,
				callback);
	}

	/**
	 * move to specific location
	 * 
	 * @param lat
	 * @param lng
	 */
	public static void moveTo(double lat, double lng) {
		moveTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom, null);
	}

	/**
	 * move and zoom to specific location
	 * 
	 * @param latLng
	 * @param zoom
	 */
	public static void moveTo(LatLng latLng, int zoom) {
		moveTo(latLng, zoom, null);
	}

	/**
	 * move and zoom to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param zoom
	 */
	public static void moveTo(double lat, double lng, int zoom) {
		moveTo(new LatLng(lat, lng), zoom, null);
	}

	/**
	 * move and zoom to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param zoom
	 * @param callback
	 */
	public static void moveTo(double lat, double lng, int zoom,
			ChangePosition callback) {
		moveTo(new LatLng(lat, lng), zoom, callback);
	}

	/**
	 * @param southwest
	 * @param northeast
	 * @param padding
	 * @param smooth
	 * @param callback
	 */
	public static void setBound(LatLng southwest, LatLng northeast,
			int padding, boolean smooth, final ChangePosition callback) {
		if (ccListener == null) {
			ccListener = new OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition position) {
					map.setOnCameraChangeListener(null);

					ccListener = null;

					if (callback != null) {
						callback.changed(map, position);
					}
				}
			};

			map.setOnCameraChangeListener(ccListener);
		}

		if (smooth) {
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(
					new LatLngBounds(southwest, northeast), padding));
		} else {
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(
					new LatLngBounds(southwest, northeast), padding));
		}
	}

	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @param padding
	 * @param smooth
	 */
	public static void setBound(double swLat, double swLng, double neLat,
			double neLng, int padding, boolean smooth) {
		setBound(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding,
				smooth, null);
	}

	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @param padding
	 */
	public static void setBound(double swLat, double swLng, double neLat,
			double neLng, int padding) {
		setBound(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding,
				true, null);
	}

	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @param padding
	 * @param callback
	 */
	public static void setBound(double swLat, double swLng, double neLat,
			double neLng, int padding, ChangePosition callback) {
		setBound(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding,
				true, callback);
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 * @param smooth
	 * @param callback
	 */
	public static void zoomTo(int zoom, boolean smooth, ChangePosition callback) {
		if (smooth) {
			animateTo(map.getCameraPosition().target, zoom, callback);
		} else {
			moveTo(map.getCameraPosition().target, zoom, callback);
		}
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 */
	public static void zoomTo(int zoom) {
		zoomTo(zoom, true, null);
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 * @param callback
	 */
	public static void zoomTo(int zoom, ChangePosition callback) {
		zoomTo(zoom, true, callback);
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 * @param smooth
	 */
	public static void zoomTo(int zoom, boolean smooth) {
		zoomTo(zoom, smooth, null);
	}

	/**
	 * zoom map in
	 */
	public static void zoomIn() {
		zoomTo((int) (map.getCameraPosition().zoom + 1), true, null);
	}

	/**
	 * zoom map in
	 * 
	 * @param callback
	 */
	public static void zoomIn(ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom + 1), true, callback);
	}

	/**
	 * zoom map in
	 * 
	 * @param smooth
	 * @param callback
	 */
	public static void zoomIn(boolean smooth, ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom + 1), smooth, callback);
	}

	/**
	 * zoom map out
	 */
	public static void zoomOut() {
		zoomTo((int) (map.getCameraPosition().zoom - 1), true, null);
	}

	/**
	 * zoom map out
	 * 
	 * @param callback
	 */
	public static void zoomOut(ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom - 1), true, callback);
	}

	/**
	 * zoom map out
	 * 
	 * @param smooth
	 * @param callback
	 */
	public static void zoomOut(boolean smooth, ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom - 1), smooth, callback);
	}

	/**
	 * when map is clicked
	 * 
	 * @param callback
	 */
	public static void whenMapClick(final MapClick callback) {
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				callback.mapClicked(map, latLng);
			}
		});
	}

	/**
	 * when info window is clicked
	 * 
	 * @param callback
	 */
	public static void whenInfoWindowClick(final InfoWindowClick callback) {
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				callback.markerInfoWindowClicked(map, marker);
			}
		});
	}

	/**
	 * when marker is clicked
	 * 
	 * @param callback
	 */
	public static void whenMarkerClick(final MarkerClick callback) {
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				callback.markerClicked(map, marker);

				return true;
			}
		});
	}

	/**
	 * when marker is dragged
	 * 
	 * @param callback
	 */
	public static void whenMarkerDrag(final MarkerDrag callback) {
		map.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker marker) {
				callback.markerDragStart(map, marker);
			}

			@Override
			public void onMarkerDrag(Marker marker) {
				callback.markerDrag(map, marker);
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				callback.markerDragEnd(map, marker);
			}
		});
	}

	/**
	 * add marker to map
	 * 
	 * @param opts
	 * @param callback
	 */
	public static void addMarker(MarkerOptions opts, MarkerAdd callback) {
		Marker marker = map.addMarker(opts);

		if (markers == null) {
			markers = new ArrayList<Marker>();
		}

		markers.add(marker);

		if (callback != null) {
			callback.markerAdded(map, marker);
		}
	}

	/**
	 * add marker to map
	 * 
	 * @param opts
	 */
	public static void addMarker(MarkerOptions opts) {
		addMarker(opts, null);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @param opts
	 */
	public static void addMarker(LatLng latLng, MarkerOptions opts) {
		addMarker(opts.position(latLng), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @param opts
	 */
	public static void addMarker(double lat, double lng, MarkerOptions opts) {
		addMarker(opts.position(new LatLng(lat, lng)), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 */
	public static void addMarker(LatLng latLng) {
		addMarker(new MarkerOptions().position(latLng), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 */
	public static void addMarker(double lat, double lng) {
		addMarker(new MarkerOptions().position(new LatLng(lat, lng)), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @param opts
	 * @param callback
	 */
	public static void addMarker(LatLng latLng, MarkerOptions opts,
			MarkerAdd callback) {
		addMarker(opts.position(latLng), callback);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @param opts
	 * @param callback
	 */
	public static void addMarker(double lat, double lng, MarkerOptions opts,
			MarkerAdd callback) {
		addMarker(opts.position(new LatLng(lat, lng)), callback);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @param callback
	 */
	public static void addMarker(LatLng latLng, MarkerAdd callback) {
		addMarker(new MarkerOptions().position(latLng), callback);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @param callback
	 */
	public static void addMarker(double lat, double lng, MarkerAdd callback) {
		addMarker(new MarkerOptions().position(new LatLng(lat, lng)), callback);
	}

	/**
	 * add all markers to map
	 * 
	 * @param allOpts
	 * @param callback
	 */
	public static void addMarkers(ArrayList<MarkerOptions> allOpts,
			MarkerAdd callback) {
		if (markers == null) {
			markers = new ArrayList<Marker>();
		}

		for (MarkerOptions opts : allOpts) {
			Marker marker = map.addMarker(opts);

			markers.add(marker);

			if (callback != null) {
				callback.markerAdded(map, marker);
			}
		}
	}

	/**
	 * add all markers to map
	 * 
	 * @param allOpts
	 */
	public static void addMarkers(ArrayList<MarkerOptions> allOpts) {
		addMarkers(allOpts, null);
	}

	/**
	 * return all markers
	 * 
	 * @return
	 */
	public static ArrayList<Marker> getMarkers() {
		return markers;
	}

	/**
	 * return specific marker
	 * 
	 * @param index
	 * @return
	 */
	public static Marker getMarker(int index) {
		return markers.get(index);
	}

	/**
	 * clear all markers
	 */
	public static void clearMarkers() {
		map.clear();

		markers.clear();
	}

	/**
	 * find specific location
	 * 
	 * @param location
	 * @param callback
	 */
	public static void find(String location, FindResult callback) {
		Geocoder geocoder = new Geocoder(context);
		ArrayList<Address> addresses = new ArrayList<Address>();

		try {
			addresses = (ArrayList<Address>) geocoder.getFromLocationName(
					location, 5);
		} catch (IOException e) {
			e.printStackTrace();
		}

		findCallback(callback, addresses);
	}

	/**
	 * find specific location
	 * 
	 * @param location
	 */
	public static void find(String location) {
		find(location, null);
	}

	/**
	 * find specific location
	 * 
	 * @param location
	 * @param callback
	 */
	public static void findAsync(final String location,
			final FindResult callback) {
		new AsyncTask<Void, Void, Void>() {
			private ProgressDialog dialog;
			private ArrayList<Address> addresses;

			@Override
			protected void onPreExecute() {
				dialog = new ProgressDialog(context);

				dialog.setMessage("Loading...");
				dialog.setCancelable(false);
				dialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				Geocoder geocoder = new Geocoder(context);

				try {
					addresses = (ArrayList<Address>) geocoder
							.getFromLocationName(location, 5);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (dialog.isShowing()) {
					dialog.dismiss();
				}

				findCallback(callback, addresses);
			}
		}.execute();
	}

	/**
	 * find specific location
	 * 
	 * @param location
	 */
	public static void findAsync(String location) {
		findAsync(location, null);
	}

	private static void findCallback(FindResult callback,
			ArrayList<Address> addresses) {
		if (callback != null) {
			callback.foundResult(map, addresses);
		} else {
			for (Address address : addresses) {
				MarkerOptions opts = new MarkerOptions();
				LatLng latLng = new LatLng(address.getLatitude(),
						address.getLongitude());

				opts.position(latLng);
				opts.title(address.toString());
				opts.snippet(latLng.toString());

				addMarker(opts);
			}

			animateTo(new LatLng(addresses.get(0).getLatitude(), addresses.get(
					0).getLongitude()));
		}
	}

	public interface ChangeMyLocation {
		public void changed(GoogleMap map, Location location);
	}

	public interface ChangePosition {
		public void changed(GoogleMap map, CameraPosition position);
	}

	public interface MapClick {
		public void mapClicked(GoogleMap map, LatLng latLng);
	}

	public interface MarkerAdd {
		public void markerAdded(GoogleMap map, Marker marker);
	}

	public interface InfoWindowClick {
		public void markerInfoWindowClicked(GoogleMap map, Marker marker);
	}

	public interface MarkerClick {
		public void markerClicked(GoogleMap map, Marker marker);
	}

	public interface MarkerDrag {
		public void markerDragStart(GoogleMap map, Marker marker);

		public void markerDrag(GoogleMap map, Marker marker);

		public void markerDragEnd(GoogleMap map, Marker marker);
	}

	public interface FindResult {
		public void foundResult(GoogleMap map, ArrayList<Address> addresses);
	}
}