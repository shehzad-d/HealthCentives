package com.healthcentives.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.healthcentives.business.GeoLocation;
import com.healthcentives.business.GeoLocations;
import com.healthcentives.business.GeoUtils;
import com.healthcentives.log.Tags;

import java.util.Date;

/**
 * Created by shehz on 03/03/2018.
 */

public class LocationSvc extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final Messenger messenger = new Messenger(new MessageHandler());
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private Messenger client = null;

    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;

    private GeoLocations locations;

    @Override
    public void onCreate() {
        super.onCreate();

        locations = new GeoLocations(getApplicationContext());

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        client = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Tags.APP_TAG, "Google API connected");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(Tags.APP_TAG, "Permission not grantted");
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                for (GeoLocation g : locations.getLocations()) {

                    if (g.isInside() && !GeoUtils.intersects(g, location)) {
                        //has left the area
                        g.setInside(false);
                        Date startTime = g.getStartTime();
                        Date end = new Date();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(GeoUtils.GEO_ID_TAG, null);

                        long diff = (end.getTime() - startTime.getTime()) / 1000;

                        long totalSecs = prefs.getLong(GeoUtils.SECONDS_SPENT_TAG, 0) + diff;
                        editor.putLong(GeoUtils.SECONDS_SPENT_TAG, totalSecs);
                        int sessions =  prefs.getInt(GeoUtils.SESSIONS_TAG, 0) + 1;
                        editor.putInt(GeoUtils.SESSIONS_TAG, sessions);

                        editor.commit();
                        //editor.apply();

//                        try {
//                            client.send(Message.obtain(null, Tags.SVC_REFRESH, 0, 0));
//                        } catch (RemoteException e) {
//                            Log.e(Tags.APP_TAG, e.toString());
//                        }

                        sendData();

                    } else if (!g.isInside() && GeoUtils.intersects(g, location)) {
                        //has entered area
                        g.setInside(true);
                        g.setStartTime(new Date());

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(GeoUtils.GEO_ID_TAG, g.getId());
                        editor.putLong(GeoUtils.GEO_START_TIME, g.getStartTime().getTime());

                        editor.commit();
                        //editor.apply();
//
//                        try {
//                            client.send(Message.obtain(null, Tags.SVC_REFRESH, 0, 0));
//                        } catch (RemoteException e) {
//                            Log.e(Tags.APP_TAG, e.toString());
//                        }

                        sendData();
                    }
                }

            }
        });
    }

    private void sendData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(GeoUtils.GEO_ID_TAG, prefs.getString(GeoUtils.GEO_ID_TAG, null));
        bundle.putLong(GeoUtils.SECONDS_SPENT_TAG, prefs.getLong(GeoUtils.SECONDS_SPENT_TAG, 0));
        bundle.putInt(GeoUtils.SESSIONS_TAG, prefs.getInt(GeoUtils.SESSIONS_TAG, 0));

        msg.setData(bundle);

        try {
            client.send(msg);
        } catch (RemoteException e) {
            Log.e(Tags.APP_TAG, e.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(Tags.APP_TAG, "Google API connection failed");
    }


    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == Tags.APP_REG) {
                client = msg.replyTo;

                sendData();
            }
        }
    }
}
