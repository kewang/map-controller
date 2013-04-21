# map-controller

Control Google Maps v2 for Android

## Prerequisite

You must know to set up your Maps v2 from [official article](https://developers.google.com/maps/documentation/android/start)

### Attach & Detach

At first, you must attach map and context to controller at onCreate life cycle.

```java
try {
	MapController.attach(this, mv.getMap());
} catch (GooglePlayServicesNotAvailableException e) {
	e.printStackTrace();
}
```