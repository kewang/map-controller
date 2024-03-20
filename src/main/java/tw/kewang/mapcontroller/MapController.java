package tw.kewang.mapcontroller;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author kewang
 */
public class MapController {
    private static final String TAG = MapController.class.getSimpleName();

    private Context context;
    private GoogleMap map;
    private ArrayList<Marker> markers;
    private OnCameraIdleListener cameraIdleListener;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    /**
     * attach Google Maps
     *
     * @param map
     */
    public MapController(Context context, GoogleMap map) {
        if (map == null) {
            Log.e(TAG, "GoogleMap can't not be null.");

            throw new RuntimeException("GoogleMap can't not be null.");
        }

        this.map = map;
    }

    public MapController(MapView mapView, MapControllerReady callback) {
        mapView.getMapAsync(googleMap -> {
            this.map = googleMap;
            this.context = mapView.getContext();

            if (callback != null) {
                callback.already(this);
            }
        });
    }

    public MapController(MapFragment mapFragment, MapControllerReady callback) {
        mapFragment.getMapAsync(googleMap -> {
            this.map = googleMap;
            this.context = mapFragment.getContext();

            if (callback != null) {
                callback.already(this);
            }
        });
    }

    private MapController() {
    }

    /**
     * initialize Google Maps
     *
     * @param context
     */
    public static void initialize(Context context) {
        MapsInitializer.initialize(context);
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
     * return the type of map that's currently displayed.
     *
     * @return
     */
    public MapType getType() {
        return MapType.valueOf(String.valueOf(map.getMapType()));
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
     * start tracking my current location
     *
     * @param map
     * @param interval
     * @param numUpdates
     * @param type
     * @param callback
     */
    public void startTrackMyLocation(GoogleMap map, long interval, int numUpdates, TrackType type, ChangeMyLocation callback) {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }

        LocationRequest request = LocationRequest.create().setInterval(interval).setFastestInterval(16).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (numUpdates != 0) {
            request.setNumUpdates(numUpdates);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location1 = locationResult.getLastLocation();

                if (map != null) {
                    CameraUpdate latLng = CameraUpdateFactory.newLatLng(new LatLng(location1.getLatitude(), location1.getLongitude()));

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    map.setMyLocationEnabled(true);

                    if (type == TrackType.TRACK_TYPE_MOVE) {
                        map.moveCamera(latLng);
                    } else if (type == TrackType.TRACK_TYPE_ANIMATE) {
                        map.animateCamera(latLng);
                    }
                }

                if (callback != null) {
                    callback.changed(map, location1);
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null);
    }

    /**
     * start tracking my current location
     *
     * @param callback
     */
    public void startTrackMyLocation(ChangeMyLocation callback) {
        startTrackMyLocation(map, 5000, 0, TrackType.TRACK_TYPE_ANIMATE, callback);
    }

    /**
     * start tracking my current location
     *
     * @param interval
     * @param numUpdates
     * @param callback
     */
    public void startTrackMyLocation(long interval, int numUpdates, ChangeMyLocation callback) {
        startTrackMyLocation(map, interval, numUpdates, TrackType.TRACK_TYPE_ANIMATE, callback);
    }

    /**
     * stop tracking my current location
     */
    public void stopTrackMyLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        locationCallback = null;
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
        startTrackMyLocation(map, 5000, 1, TrackType.TRACK_TYPE_ANIMATE, callback);
    }

    /**
     * move to my current location
     */
    public void animateToMyLocation() {
        animateToMyLocation(null);
    }

    /**
     * show my current location
     */
    public void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        map.setMyLocationEnabled(true);
    }

    /**
     * animate to specific location
     *
     * @param latLng
     * @param zoom
     * @param callback
     */
    public void animateTo(LatLng latLng, int zoom, ChangePosition callback) {
        if (cameraIdleListener == null) {
            cameraIdleListener = () -> {
                map.setOnCameraIdleListener(null);

                cameraIdleListener = null;

                if (callback != null) {
                    callback.changed(map, map.getCameraPosition());
                }
            };

            map.setOnCameraIdleListener(cameraIdleListener);
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
        animateTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom, callback);
    }

    /**
     * animate to specific location
     *
     * @param lat
     * @param lng
     */
    public void animateTo(double lat, double lng) {
        animateTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom, null);
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
    public void animateTo(double lat, double lng, int zoom, ChangePosition callback) {
        animateTo(new LatLng(lat, lng), zoom, callback);
    }

    /**
     * move to specific location
     *
     * @param latLng
     * @param callback
     */
    public void moveTo(LatLng latLng, int zoom, ChangePosition callback) {
        if (cameraIdleListener == null) {
            cameraIdleListener = () -> {
                map.setOnCameraIdleListener(null);

                cameraIdleListener = null;

                if (callback != null) {
                    callback.changed(map, map.getCameraPosition());
                }
            };

            map.setOnCameraIdleListener(cameraIdleListener);
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
        moveTo(new LatLng(lat, lng), (int) map.getCameraPosition().zoom, callback);
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
    public void setBounds(LatLng southwest, LatLng northeast, int padding, boolean smooth, ChangePosition callback) {
        if (cameraIdleListener == null) {
            cameraIdleListener = () -> {
                map.setOnCameraIdleListener(null);

                cameraIdleListener = null;

                if (callback != null) {
                    callback.changed(map, map.getCameraPosition());
                }
            };

            map.setOnCameraIdleListener(cameraIdleListener);
        }

        if (smooth) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(southwest, northeast), padding));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(southwest, northeast), padding));
        }
    }

    /**
     * @param southwest
     * @param northeast
     * @param padding
     * @param smooth
     */
    public void setBounds(LatLng southwest, LatLng northeast, int padding, boolean smooth) {
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
    public void setBounds(double swLat, double swLng, double neLat, double neLng, int padding, boolean smooth) {
        setBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding, smooth, null);
    }

    /**
     * @param swLat
     * @param swLng
     * @param neLat
     * @param neLng
     * @param padding
     */
    public void setBounds(double swLat, double swLng, double neLat, double neLng, int padding) {
        setBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding, true, null);
    }

    /**
     * @param swLat
     * @param swLng
     * @param neLat
     * @param neLng
     * @param padding
     * @param callback
     */
    public void setBounds(double swLat, double swLng, double neLat, double neLng, int padding, ChangePosition callback) {
        setBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng), padding, true, callback);
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
     * {@link #setInfoWindowAdapter(InfoWindowAdapter)}
     */
    @Deprecated
    public void setInfoWindow(View v) {
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
     * {@link #setInfoWindowAdapter(InfoWindowAdapter)}
     */
    @Deprecated
    public void setInfoContents(View v) {
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
     * set the info-window adapter
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
        map.setOnMapClickListener(latLng -> callback.clicked(map, latLng));
    }

    /**
     * when map is long clicked
     *
     * @param callback
     */
    public void whenMapLongClick(final ClickCallback callback) {
        map.setOnMapLongClickListener(latLng -> callback.clicked(map, latLng));
    }

    /**
     * when info window is clicked
     *
     * @param callback
     */
    public void whenInfoWindowClick(final MarkerCallback callback) {
        map.setOnInfoWindowClickListener(marker -> callback.invokedMarker(map, marker));
    }

    /**
     * when marker is clicked
     *
     * @param callback
     */
    public void whenMarkerClick(final MarkerCallback callback) {
        map.setOnMarkerClickListener(marker -> {
            callback.invokedMarker(map, marker);

            return true;
        });
    }

    /**
     * when marker is dragged
     *
     * @param callback
     */
    public void whenMarkerDrag(MarkerDrag callback) {
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
            markers = new ArrayList<>();
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
        return addMarker(new MarkerOptions().position(new LatLng(lat, lng)), null);
    }

    /**
     * add marker to map
     *
     * @param latLng
     * @param opts
     * @param callback
     * @return
     */
    public Marker addMarker(LatLng latLng, MarkerOptions opts, MarkerCallback callback) {
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
    public Marker addMarker(double lat, double lng, MarkerOptions opts, MarkerCallback callback) {
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
        return addMarker(new MarkerOptions().position(new LatLng(lat, lng)), callback);
    }

    /**
     * add all markers to map
     *
     * @param allOpts
     * @param callback
     */
    public void addMarkers(ArrayList<MarkerOptions> allOpts, MarkerCallback callback) {
        if (markers == null) {
            markers = new ArrayList<>();
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
        ArrayList<Address> addresses = new ArrayList<>();

        try {
            addresses = (ArrayList<Address>) geocoder.getFromLocationName(location, 5);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
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
                    addresses = (ArrayList<Address>) geocoder.getFromLocationName(location, 5);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
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
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                opts.position(latLng);
                opts.title(address.toString());
                opts.snippet(latLng.toString());

                addMarker(opts);
            }

            animateTo(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));
        }
    }

    public enum MapType {
        MAP_TYPE_NONE, MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
    }

    public enum TrackType {
        TRACK_TYPE_MOVE, TRACK_TYPE_ANIMATE, TRACK_TYPE_NONE
    }

    public interface MapControllerReady {
        void already(MapController controller);
    }

    public interface ChangeMyLocation {
        void changed(GoogleMap map, Location location);
    }

    public interface ChangePosition {
        void changed(GoogleMap map, CameraPosition position);
    }

    public interface ClickCallback {
        void clicked(GoogleMap map, LatLng latLng);
    }

    public interface MarkerCallback {
        void invokedMarker(GoogleMap map, Marker marker);
    }

    public interface MarkerDrag {
        void markerDragStart(GoogleMap map, Marker marker);

        void markerDrag(GoogleMap map, Marker marker);

        void markerDragEnd(GoogleMap map, Marker marker);
    }

    public interface FindResult {
        void found(GoogleMap map, ArrayList<Address> addresses);
    }
}