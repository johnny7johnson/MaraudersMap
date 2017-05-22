package com.example.johanna.myapplication;

/**
 * Created by Johanna on 13.12.2016.
 */

public class LocationContent {
    private double latitude;
    private double longitude;

    /**
     *
     * @param lat
     * @param lon
     */
    public LocationContent(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    /**
     *
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     *
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }
}
