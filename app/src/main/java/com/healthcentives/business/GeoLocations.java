package com.healthcentives.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shehz on 03/03/2018.
 */

public class GeoLocations {

    private final List<GeoLocation> locations = new LinkedList<>();

    public GeoLocations(Context context) {
        loadTestData();
    }

    private void loadData(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String id = prefs.getString(GeoUtils.GEO_ID_TAG, null);

        if (id != null) {
            for (GeoLocation g : locations) {
                if (g.getId().equals(id)) {

                    g.setInside(true);

                    //time
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(prefs.getLong(GeoUtils.GEO_START_TIME, -1));
                    g.setStartTime(calendar.getTime());

                    break;
                }
            }
        }

    }

    private void loadTestData() {
        locations.add(new GeoLocation("My Gym", 51.4282, 2.5708, 5));
        locations.add(new GeoLocation("My Gym", 51.6053, 0.2272, 15));
    }

    public List<GeoLocation> getLocations() {
        return locations;
    }

}
