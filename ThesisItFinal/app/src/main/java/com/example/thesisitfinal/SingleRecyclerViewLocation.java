package com.example.thesisitfinal;

import com.mapbox.geojson.Point;

public class SingleRecyclerViewLocation
{
    private String name;
    private Point location;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setLocation(Point location)
    {
        this.location = location;
    }

    public Point getLocation()
    {
        return location;
    }
}
