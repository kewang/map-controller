# map-controller

Control Google Maps v2 for Android

## Prerequisite

You must know how to set up your Maps v2 from [official article](https://developers.google.com/maps/documentation/android/start).

## Set up library

Because the library project always contains the newest official library project, you just only import the library project to your Android project.

## How to use

### Initialize

At first, you must use to `MapController#initialize(Context)` to initial Google Maps at `android.app.Application` and remember to update `AndroidManifest.xml`.

```java
@Override
public void onCreate() {
  super.onCreate();

  try {
    MapController.initialize(this);
  } catch (GooglePlayServicesNotAvailableException e) {
    e.printStackTrace();

    Toast.makeText(this, R.string.common_google_play_services_enable_text, Toast.Length_SHORT).show();
  }
}
```

### Attach

When using it, You must create an instance to attach map from `MapView#getMap()` / `MapFragment#getMap()`.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	MapController mc = new MapController(mv.getMap());
}
```

### Show my location

Typically, you can use `MapController#showMyLocation()` to show your location.

### Move to my location

You can use `MapController#moveToMyLocation(false)` to move your current location. Also you can use `MapController#animateToMyLocation(false)` to move smoothly.

### Get my location

You can use `MapController#getMyLocation()` to get your current location.

### Tracking my location

If you want to track your location at runtime and do something. You can use `MapController#moveToMyLocation(true)` like this:

```java
mc.moveToMyLocation(true, new ChangeMyLocation() {
	@Override
	public void changed(GoogleMap map, Location location) {
		Toast.makeText(TrackingMyLocation.this, location.toString(), Toast.LENGTH_SHORT).show();
	}
});
```

### Move to specific location

If you want to move to specific location, you can use `MapController#moveTo(LatLng)` or `MapController#animateTo(LatLng)` like this:

```java
LatLng latLng = new LatLng(25.03338, 121.56463);

mc.animateTo(latLng, new ChangePosition() {
	@Override
	public void changed(GoogleMap map, CameraPosition position) {
		Toast.makeText(ShowSpecificLocation.this, position.toString(), Toast.LENGTH_SHORT).show();
	}
});
```

### Add marker

You can use `MapController#addMarker(MarkerOptions)` to add marker to map, like this:

```java
mc.addMarker(opts, new MarkerCallback() {
	@Override
	public void invokedMarker(GoogleMap map, Marker marker) {
		Toast.makeText(AddMarker.this, marker.getId(), Toast.LENGTH_SHORT).show();
	}
});
```

### Add bulk markers

You can also use `MapController#addMarkers(ArrayList<MarkerOptions>)` to add bulk markers to map.