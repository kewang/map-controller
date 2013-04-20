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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapController {
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
	public static void attach(Context context, GoogleMap map) {
		MapController.context = context;

		try {
			MapsInitializer.initialize(context);
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}

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
	 * move to my location
	 * 
	 * @param tracking
	 * @param callback
	 */
	public static void moveToMyLocation(final boolean tracking,
			final MoveMyLocation callback) {
		map.setMyLocationEnabled(true);

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
						callback.moved(map, location);
					}
				}
			};
		}

		map.setOnMyLocationChangeListener(mlListener);
	}

	/**
	 * move to my location
	 * 
	 * @param tracking
	 */
	public static void moveToMyLocation(boolean tracking) {
		moveToMyLocation(tracking, null);
	}

	/**
	 * animate to specific latlng
	 * 
	 * @param latLng
	 * @param callback
	 */
	public static void animateTo(LatLng latLng, final Move callback) {
		if (ccListener == null) {
			ccListener = new OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition position) {
					map.setOnCameraChangeListener(null);

					ccListener = null;

					if (callback != null) {
						callback.moved(map, position);
					}
				}
			};

			map.setOnCameraChangeListener(ccListener);
		}

		map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
	}

	/**
	 * animate to specific latlng
	 * 
	 * @param latLng
	 */
	public static void animateTo(LatLng latLng) {
		animateTo(latLng, null);
	}

	/**
	 * move to specific latlng
	 * 
	 * @param latLng
	 * @param callback
	 */
	public static void moveTo(LatLng latLng, final Move callback) {
		if (ccListener == null) {
			ccListener = new OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition position) {
					map.setOnCameraChangeListener(null);

					ccListener = null;

					if (callback != null) {
						callback.moved(map, position);
					}
				}
			};

			map.setOnCameraChangeListener(ccListener);
		}

		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	}

	/**
	 * move to specific latlng
	 * 
	 * @param latLng
	 */
	public static void moveTo(LatLng latLng) {
		moveTo(latLng, null);
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
	public static void add(MarkerOptions opts, MarkerAdd callback) {
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
	public static void add(MarkerOptions opts) {
		add(opts, null);
	}

	/**
	 * return all markers
	 * 
	 * @return
	 */
	public static ArrayList<Marker> getAllMarkers() {
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
	public static void clearAllMarkers() {
		map.clear();

		markers.clear();
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 */
	public static void zoom(int zoom) {
		map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
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

				add(opts);
			}

			animateTo(new LatLng(addresses.get(0).getLatitude(), addresses.get(
					0).getLongitude()));
		}
	}

	public interface MoveMyLocation {
		public void moved(GoogleMap map, Location location);
	}

	public interface Move {
		public void moved(GoogleMap map, CameraPosition position);
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