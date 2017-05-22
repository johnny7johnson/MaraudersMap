package com.example.johanna.myapplication;

import android.location.Location;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johanna on 15.12.2016.
 */

public class NeighbourLocations {

    public String name;
    public String description;
    public List<LocationContent> locations;

    /**
     *
     * @param name
     * @param description
     * @param locations
     */
    public NeighbourLocations(String name, String description, List<LocationContent> locations){
        this.name=name;
        this.description=description;
        this.locations = locations;
    }

    /**
     *
     * @return name
     */
    public String getName(){
        return name;
    }

}
