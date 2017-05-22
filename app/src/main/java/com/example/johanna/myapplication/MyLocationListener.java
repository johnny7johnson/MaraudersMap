package com.example.johanna.myapplication;

import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;


/**
 * Created by Johanna on 16.12.2016.
 */

public class MyLocationListener implements LocationListener{

    private boolean hasNeighborsCalled;
    public MapsActivity mapsActivity;
    private LocationRequest mLocationRequest;
    private boolean wasFirsLocation;
    private Location lastLocation;
    private Location newLocation;

    public MyLocationListener(MapsActivity mapsActivity){
        lastLocation = null;
        this.mapsActivity=mapsActivity;
        this.hasNeighborsCalled = false;
        this.wasFirsLocation = true;
    }

    /**
     *
     * @param location
     */
    //LocationListener

    @Override
    public void onLocationChanged(Location location) {
        this.newLocation = location;
        mapsActivity.handleNewLocation(newLocation);
        if (!hasNeighborsCalled){
            mapsActivity.getNeighbourLocations(newLocation);
            hasNeighborsCalled = true;
        }
        if(wasFirsLocation){
            mapsActivity.zoomTo(newLocation);
            wasFirsLocation=false;
        }
        lastLocation = newLocation;

    }

    /**
     *
     * @return mlocationRequest
     */
    protected LocationRequest createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }



}
