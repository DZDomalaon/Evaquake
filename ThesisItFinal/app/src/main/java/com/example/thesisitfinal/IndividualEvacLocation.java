package com.example.thesisitfinal;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class IndividualEvacLocation
{
    LatLng location;
    String distance;

    public IndividualEvacLocation(LatLng loc, String dist)
    {
        location = loc;
        distance = dist;
    }

    public String getDistance()
    {
        return distance;
    }

    public void setDistance(String distance)
    {
        this.distance = distance;
    }

    public LatLng getLocation()
    {
        return location;
    }
}
