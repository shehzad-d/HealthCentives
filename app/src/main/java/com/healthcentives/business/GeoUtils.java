package com.healthcentives.business;

import android.location.Location;

/**
 * Created by shehz on 03/03/2018.
 */

public class GeoUtils {

    public static String GEO_ID_TAG = "ID";
    public static String GEO_START_TIME = "START_TIME";

    public static String SESSIONS_TAG = "SESSIONS";
    public static String SECONDS_SPENT_TAG = "SECONDS_SPENT";

    public static boolean intersects(GeoLocation g, Location l) {
        float[] distance = new float[2];

        Location.distanceBetween( l.getLatitude(), l.getLongitude(),
                g.getLatitude(), g.getLongtitude(), distance);

        return distance[0] < g.getRadius();
    }
}
