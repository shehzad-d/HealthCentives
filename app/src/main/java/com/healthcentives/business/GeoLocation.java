package com.healthcentives.business;

import java.util.Date;

/**
 * Created by shehz on 03/03/2018.
 */

public class GeoLocation {

    private final String id;
    private final double latitude;
    private final double longtitude;
    private final int radius;

    private boolean inside;
    private Date startTime;

    public GeoLocation(String id, double latitude, double longitude, int radius) {
        this.id = id;
        this.latitude = latitude;
        this.longtitude = longitude;
        this.radius = radius;
    }

    public boolean isInside() {
        return inside;
    }

    public void setInside(boolean inside) {
        this.inside = inside;
    }

    public Date getStartTime() {
        return startTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public int getRadius() {
        return radius;
    }

    public String getId() {
        return id;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
