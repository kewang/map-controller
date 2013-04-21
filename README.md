# map-controller

Control Google Maps v2 for Android

## Prerequisite

You must know how to set up your Maps v2 from [official article](https://developers.google.com/maps/documentation/android/start)

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

Typically, you can use `MapController.moveToMyLocation(false)` to show your location at onCreate.

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

