# map-controller

Control Google Maps v2 for Android

## Prerequisite

You must know how to set up your Maps v2 from [official article](https://developers.google.com/maps/documentation/android/start).

## Set up library

Because the library project always contains the newest official library project, you just only import the library project to your Android project.

## How to use

### Attach & Detach

At first, you must attach map and context to controller at onCreate.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	try {
		MapController.attach(this, mv.getMap());
	} catch (GooglePlayServicesNotAvailableException e) {
		e.printStackTrace();
	}
}
```

And onDestroy detach map and associate variable.

```java
@Override
protected void onDestroy() {
	MapController.detach();

	super.onDestroy();
}
```

### Show my location

Typically, you can use `MapController.showMyLocation()` to show your location at onCreate.

### Move to my location

You can use `MapController.moveToMyLocation(false)` to move your current location. Also you can use `MapController.animateToMyLocation(false)` to move smoothly.

### Get my location

You can use `MapController.getMyLocation()` to get your current location.

### Tracking my location

If you want to track your location at runtime and do something. You can use `MapController.moveToMyLocation(true)` like this:

```java
MapController.moveToMyLocation(true, new MoveMyLocation() {
	@Override
	public void moved(GoogleMap map, Location location) {
		Toast.makeText(TrackingMyLocation.this, location.toString(), Toast.LENGTH_SHORT).show();
	}
});
```

### Move to specific location

If you want to move to specific location, you can use `MapController.moveTo(LatLng)` or `MapController.animateTo(LatLng)` like this:

```java
LatLng latLng = new LatLng(25.03338, 121.56463);

MapController.animateTo(latLng, new Move() {
	@Override
	public void moved(GoogleMap map, CameraPosition position) {
		Toast.makeText(ShowSpecificLocation.this, position.toString(), Toast.LENGTH_SHORT).show();
	}
});
```

### Add marker

You can use `MapController.add(MarkerOptions)` to add marker to map, like this:

```java
MapController.add(opts, new MarkerAdd() {
	@Override
	public void markerAdded(GoogleMap map, Marker marker) {
		Toast.makeText(AddMarker.this, marker.getId(), Toast.LENGTH_SHORT).show();
	}
});
```

### Add bulk markers

You can also use `MapController.addAll(ArrayList<MarkerOptions>)` to add bulk markers to map.