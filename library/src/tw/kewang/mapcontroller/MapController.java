package tw.kewang.mapcontroller;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
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
	public enum MapType {
		MAP_TYPE_NONE, MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
	}

	public enum TrackType {
		TRACK_TYPE_MOVE, TRACK_TYPE_ANIMATE, TRACK_TYPE_NONE
	}

	private static Context context;

	private GoogleMap map;
	private ArrayList<Marker> markers;
	private OnCameraChangeListener ccListener;
	private LocationClient lClient;

	/**
	 * initialize Google Maps
	 * 
	 * @param context
	 * @throws GooglePlayServicesNotAvailableException
	 */
	public static void initialize(Context context)
			throws GooglePlayServicesNotAvailableException {
		MapController.context = context;

		MapsInitializer.initialize(context);
	}

	/**
	 * attach Google Maps
	 * 
	 * @param map
	 */
	public MapController(GoogleMap map) {
		this.map = map;
	}

	public MapController() {
	}

	/**
	 * return map's instance
	 * 
	 * @return
	 */
	public GoogleMap getMap() {
		return map;
	}

	/**
	 * sets the type of map tiles that should be displayed
	 * 
	 * @param type
	 */
	public void setType(MapType type) {
		map.setMapType(type.ordinal());
	}

	/**
	 * return the type of map that's currently displayed.
	 * 
	 * @return
	 */
	public MapType getType() {
		return MapType.valueOf(String.valueOf(map.getMapType()));
	}

	/**
	 * start tracking my current location
	 * 
	 * @param map
	 * @param interval
	 * @param numUpdates
	 * @param type
	 * @param callback
	 */
	public void startTrackMyLocation(final GoogleMap map, final long interval,
			final int numUpdates, final TrackType type,
			final ChangeMyLocation callback) {
		if (map != null) {
			showMyLocation();
		}

		lClient = new LocationClient(context, new ConnectionCallbacks() {
			@Override
			public void onDisconnected() {
			}

			@Override
			public void onConnected(Bundle connectionHint) {
				LocationRequest request = LocationRequest.create()
						.setInterval(interval).setFastestInterval(16)
						.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

				if (numUpdates != 0) {
					request.setNumUpdates(numUpdates);
				}

				lClient.requestLocationUpdates(request, new LocationListener() {
					@Override
					public void onLocationChanged(Location location) {
						if (map != null) {
							CameraUpdate latLng = CameraUpdateFactory
									.newLatLng(new LatLng(location
											.getLatitude(), location
											.getLongitude()));

							if (type == TrackType.TRACK_TYPE_MOVE) {
								map.moveCamera(latLng);
							} else if (type == TrackType.TRACK_TYPE_ANIMATE) {
								map.animateCamera(latLng);
							}
						}

						if (callback != null) {
							callback.changed(map, location);
						}
					}
				});
			}
		}, new OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult result) {
			}
		});

		lClient.connect();
	}

	/**
	 * start tracking my current location
	 * 
	 * @param callback
	 */
	public void startTrackMyLocation(ChangeMyLocation callback) {
		startTrackMyLocation(null, 5000, 0, TrackType.TRACK_TYPE_NONE, callback);
	}

	/**
	 * start tracking my current location
	 * 
	 * @param interval
	 * @param numUpdates
	 * @param callback
	 */
	public void startTrackMyLocation(long interval, int numUpdates,
			ChangeMyLocation callback) {
		startTrackMyLocation(null, interval, numUpdates,
				TrackType.TRACK_TYPE_NONE, callback);
	}

	/**
	 * stop tracking my current location
	 */
	public void stopTrackMyLocation() {
		if (lClient != null) {
			lClient.disconnect();
		}
	}

	/**
	 * move to my current location
	 * 
	 * @param callback
	 */
	public void moveToMyLocation(ChangeMyLocation callback) {
		startTrackMyLocation(map, 5000, 1, TrackType.TRACK_TYPE_MOVE, callback);
	}

	/**
	 * move to my current location
	 */
	public void moveToMyLocation() {
		moveToMyLocation(null);
	}

	/**
	 * move to my current location
	 * 
	 * @param callback
	 */
	public void animateToMyLocation(ChangeMyLocation callback) {
		startTrackMyLocation(map, 5000, 1, TrackType.TRACK_TYPE_ANIMATE,
				callback);
	}

	/**
	 * move to my current location
	 */
	public void animateToMyLocation() {
		animateToMyLocation(null);
	}

	/**
	 * return my current location
	 * 
	 * @return
	 */
	public Location getMyLocation() {
		if (!map.isMyLocationEnabled()) {
			showMyLocation();
		}

		return map.getMyLocation();
	}

	/**
	 * show my current location
	 */
	public void showMyLocation() {
		map.setMyLocationEnabled(true);
	}

	/**
	 * animate to specific location
	 * 
	 * @param latLng
	 * @param zoom
	 * @param callback
	 */
	public void animateTo(LatLng latLng, int zoom, final ChangePosition callback) {
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
	public void animateTo(LatLng latLng) {
		animateTo(latLng, (int) map.getCameraPosition().zoom, null);
	}

	/**
	 * animate to specific location
	 * 
	 * @param latLng
	 * @param callback
	 */
	public void animateTo(LatLng latLng, ChangePosition callback) {
		animateTo(latLng, (int) map.getCameraPosition().zoom, callback);
	}

	/**
	 * animate to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param callback
	 */
	public void animateTo(double lat, double lng, ChangePosition callback) {
		animateTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom,
				callback);
	}

	/**
	 * animate to specific location
	 * 
	 * @param lat
	 * @param lng
	 */
	public void animateTo(double lat, double lng) {
		animateTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom,
				null);
	}

	/**
	 * animate and zoom to specific location
	 * 
	 * @param latLng
	 * @param zoom
	 */
	public void animateTo(LatLng latLng, int zoom) {
		animateTo(latLng, zoom, null);
	}

	/**
	 * animate and zoom to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param zoom
	 */
	public void animateTo(double lat, double lng, int zoom) {
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
	public void animateTo(double lat, double lng, int zoom,
			ChangePosition callback) {
		animateTo(new LatLng(lat, lng), zoom, callback);
	}

	/**
	 * move to specific location
	 * 
	 * @param latLng
	 * @param callback
	 */
	public void moveTo(LatLng latLng, int zoom, final ChangePosition callback) {
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
	public void moveTo(LatLng latLng) {
		moveTo(latLng, (int) map.getCameraPosition().zoom, null);
	}

	/**
	 * move to specific location
	 * 
	 * @param latLng
	 * @param callback
	 */
	public void moveTo(LatLng latLng, ChangePosition callback) {
		moveTo(latLng, (int) map.getCameraPosition().zoom, callback);
	}

	/**
	 * move to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param callback
	 */
	public void moveTo(double lat, double lng, ChangePosition callback) {
		moveTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom,
				callback);
	}

	/**
	 * move to specific location
	 * 
	 * @param lat
	 * @param lng
	 */
	public void moveTo(double lat, double lng) {
		moveTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom, null);
	}

	/**
	 * move and zoom to specific location
	 * 
	 * @param latLng
	 * @param zoom
	 */
	public void moveTo(LatLng latLng, int zoom) {
		moveTo(latLng, zoom, null);
	}

	/**
	 * move and zoom to specific location
	 * 
	 * @param lat
	 * @param lng
	 * @param zoom
	 */
	public void moveTo(double lat, double lng, int zoom) {
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
	public void moveTo(double lat, double lng, int zoom, ChangePosition callback) {
		moveTo(new LatLng(lat, lng), zoom, callback);
	}

	/**
	 * @param southwest
	 * @param northeast
	 * @param padding
	 * @param smooth
	 * @param callback
	 */
	public void setBounds(LatLng southwest, LatLng northeast, int padding,
			boolean smooth, final ChangePosition callback) {
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
	 * @param southwest
	 * @param northeast
	 * @param padding
	 * @param smooth
	 */
	public void setBounds(LatLng southwest, LatLng northeast, int padding,
			boolean smooth) {
		setBounds(southwest, northeast, padding, smooth, null);
	}

	/**
	 * @param southwest
	 * @param northeast
	 * @param padding
	 */
	public void setBounds(LatLng southwest, LatLng northeast, int padding) {
		setBounds(southwest, northeast, padding, true, null);
	}

	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @param padding
	 * @param smooth
	 */
	public void setBounds(double swLat, double swLng, double neLat,
			double neLng, int padding, boolean smooth) {
		setBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding,
				smooth, null);
	}

	/**
	 * @param swLat
	 * @param swLng
	 * @param neLat
	 * @param neLng
	 * @param padding
	 */
	public void setBounds(double swLat, double swLng, double neLat,
			double neLng, int padding) {
		setBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding,
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
	public void setBounds(double swLat, double swLng, double neLat,
			double neLng, int padding, ChangePosition callback) {
		setBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding,
				true, callback);
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 * @param smooth
	 * @param callback
	 */
	public void zoomTo(int zoom, boolean smooth, ChangePosition callback) {
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
	public void zoomTo(int zoom) {
		zoomTo(zoom, true, null);
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 * @param callback
	 */
	public void zoomTo(int zoom, ChangePosition callback) {
		zoomTo(zoom, true, callback);
	}

	/**
	 * zoom map
	 * 
	 * @param zoom
	 * @param smooth
	 */
	public void zoomTo(int zoom, boolean smooth) {
		zoomTo(zoom, smooth, null);
	}

	/**
	 * zoom map in
	 */
	public void zoomIn() {
		zoomTo((int) (map.getCameraPosition().zoom + 1), true, null);
	}

	/**
	 * zoom map in
	 * 
	 * @param callback
	 */
	public void zoomIn(ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom + 1), true, callback);
	}

	/**
	 * zoom map in
	 * 
	 * @param smooth
	 * @param callback
	 */
	public void zoomIn(boolean smooth, ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom + 1), smooth, callback);
	}

	/**
	 * zoom map out
	 */
	public void zoomOut() {
		zoomTo((int) (map.getCameraPosition().zoom - 1), true, null);
	}

	/**
	 * zoom map out
	 * 
	 * @param callback
	 */
	public void zoomOut(ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom - 1), true, callback);
	}

	/**
	 * zoom map out
	 * 
	 * @param smooth
	 * @param callback
	 */
	public void zoomOut(boolean smooth, ChangePosition callback) {
		zoomTo((int) (map.getCameraPosition().zoom - 1), smooth, callback);
	}

	/**
	 * replace the default info-window
	 * 
	 * @param v
	 * @see #setInfoWindowAdapter(InfoWindowAdapter)
	 * @deprecated please use to
	 *             {@link #setInfoWindowAdapter(InfoWindowAdapter)}
	 */
	@Deprecated
	public void setInfoWindow(final View v) {
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker marker) {
				return v;
			}

			@Override
			public View getInfoContents(Marker marker) {
				return null;
			}
		});
	}

	/**
	 * replace the info-window contents
	 * 
	 * @param v
	 * @see #setInfoWindowAdapter(InfoWindowAdapter)
	 * @deprecated please use to
	 *             {@link #setInfoWindowAdapter(InfoWindowAdapter)}
	 */
	@Deprecated
	public void setInfoContents(final View v) {
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {
				return v;
			}
		});
	}

	/**
	 * set the info-window adpater
	 * 
	 * @param adapter
	 */
	public void setInfoWindowAdapter(InfoWindowAdapter adapter) {
		map.setInfoWindowAdapter(adapter);
	}

	/**
	 * when map is clicked
	 * 
	 * @param callback
	 */
	public void whenMapClick(final ClickCallback callback) {
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				callback.clicked(map, latLng);
			}
		});
	}

	/**
	 * when map is long clicked
	 * 
	 * @param callback
	 */
	public void whenMapLongClick(final ClickCallback callback) {
		map.setOnMapLongClickListener(new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				callback.clicked(map, latLng);
			}
		});
	}

	/**
	 * when info window is clicked
	 * 
	 * @param callback
	 */
	public void whenInfoWindowClick(final MarkerCallback callback) {
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				callback.invokedMarker(map, marker);
			}
		});
	}

	/**
	 * when marker is clicked
	 * 
	 * @param callback
	 */
	public void whenMarkerClick(final MarkerCallback callback) {
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				callback.invokedMarker(map, marker);

				return true;
			}
		});
	}

	/**
	 * when marker is dragged
	 * 
	 * @param callback
	 */
	public void whenMarkerDrag(final MarkerDrag callback) {
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
	 * @return
	 */
	public Marker addMarker(MarkerOptions opts, MarkerCallback callback) {
		Marker marker = map.addMarker(opts);

		if (markers == null) {
			markers = new ArrayList<Marker>();
		}

		markers.add(marker);

		if (callback != null) {
			callback.invokedMarker(map, marker);
		}

		return marker;
	}

	/**
	 * add marker to map
	 * 
	 * @param opts
	 * @return
	 */
	public Marker addMarker(MarkerOptions opts) {
		return addMarker(opts, null);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @param opts
	 * @return
	 */
	public Marker addMarker(LatLng latLng, MarkerOptions opts) {
		return addMarker(opts.position(latLng), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @param opts
	 * @return
	 */
	public Marker addMarker(double lat, double lng, MarkerOptions opts) {
		return addMarker(opts.position(new LatLng(lat, lng)), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @return
	 */
	public Marker addMarker(LatLng latLng) {
		return addMarker(new MarkerOptions().position(latLng), null);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public Marker addMarker(double lat, double lng) {
		return addMarker(new MarkerOptions().position(new LatLng(lat, lng)),
				null);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @param opts
	 * @param callback
	 * @return
	 */
	public Marker addMarker(LatLng latLng, MarkerOptions opts,
			MarkerCallback callback) {
		return addMarker(opts.position(latLng), callback);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @param opts
	 * @param callback
	 * @return
	 */
	public Marker addMarker(double lat, double lng, MarkerOptions opts,
			MarkerCallback callback) {
		return addMarker(opts.position(new LatLng(lat, lng)), callback);
	}

	/**
	 * add marker to map
	 * 
	 * @param latLng
	 * @param callback
	 * @return
	 */
	public Marker addMarker(LatLng latLng, MarkerCallback callback) {
		return addMarker(new MarkerOptions().position(latLng), callback);
	}

	/**
	 * add marker to map
	 * 
	 * @param lat
	 * @param lng
	 * @param callback
	 * @return
	 */
	public Marker addMarker(double lat, double lng, MarkerCallback callback) {
		return addMarker(new MarkerOptions().position(new LatLng(lat, lng)),
				callback);
	}

	/**
	 * add all markers to map
	 * 
	 * @param allOpts
	 * @param callback
	 */
	public void addMarkers(ArrayList<MarkerOptions> allOpts,
			MarkerCallback callback) {
		if (markers == null) {
			markers = new ArrayList<Marker>();
		}

		for (MarkerOptions opts : allOpts) {
			Marker marker = map.addMarker(opts);

			markers.add(marker);

			if (callback != null) {
				callback.invokedMarker(map, marker);
			}
		}
	}

	/**
	 * add all markers to map
	 * 
	 * @param allOpts
	 */
	public void addMarkers(ArrayList<MarkerOptions> allOpts) {
		addMarkers(allOpts, null);
	}

	/**
	 * return all markers
	 * 
	 * @return
	 */
	public ArrayList<Marker> getMarkers() {
		return markers;
	}

	/**
	 * return specific marker
	 * 
	 * @param index
	 * @return
	 */
	public Marker getMarker(int index) {
		return markers.get(index);
	}

	/**
	 * clear all markers
	 */
	public void clearMarkers() {
		map.clear();

		if (markers != null) {
			markers.clear();
		}
	}

	/**
	 * show traffic layer
	 * 
	 * @param enabled
	 */
	public void showTraffic(boolean enabled) {
		map.setTrafficEnabled(enabled);
	}

	/**
	 * show indoor layer
	 * 
	 * @param enabled
	 */
	public void showIndoor(boolean enabled) {
		map.setIndoorEnabled(enabled);
	}

	/**
	 * find specific location
	 * 
	 * @param location
	 * @param callback
	 */
	public void find(String location, FindResult callback) {
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
	public void find(String location) {
		find(location, null);
	}

	/**
	 * find specific location
	 * 
	 * @param location
	 * @param callback
	 */
	public void findAsync(final String location, final FindResult callback) {
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
	public void findAsync(String location) {
		findAsync(location, null);
	}

	private void findCallback(FindResult callback, ArrayList<Address> addresses) {
		if (callback != null) {
			callback.found(map, addresses);
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

	public interface ClickCallback {
		public void clicked(GoogleMap map, LatLng latLng);
	}

	public interface MarkerCallback {
		public void invokedMarker(GoogleMap map, Marker marker);
	}

	public interface MarkerDrag {
		public void markerDragStart(GoogleMap map, Marker marker);

		public void markerDrag(GoogleMap map, Marker marker);

		public void markerDragEnd(GoogleMap map, Marker marker);
	}

	public interface FindResult {
		public void found(GoogleMap map, ArrayList<Address> addresses);
	}
}