package com.healthcentives;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.healthcentives.business.GeoUtils;
import com.healthcentives.log.Tags;
import com.healthcentives.service.LocationSvc;

public class MainActivity extends AppCompatActivity {

    private final ServiceConnection serviceConnection = new ServiceConnectionImpl();
    private Intent serviceIntent;
    private final Messenger messenger = new Messenger(new MessageHandler());
    private Messenger locationMessenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loadData();

        serviceIntent = new Intent(getApplicationContext(), LocationSvc.class);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

    }

    private void loadData(Bundle bundle) {

        ImageView r = findViewById(R.id.imageView);
        ImageView g = findViewById(R.id.imageView3);

        if ( bundle.getString(GeoUtils.GEO_ID_TAG, null) == null) {
            r.setVisibility(View.VISIBLE);
            g.setVisibility(View.INVISIBLE);
        } else {
            g.setVisibility(View.VISIBLE);
            r.setVisibility(View.INVISIBLE);
        }

        int sessions = bundle.getInt(GeoUtils.SESSIONS_TAG, 0);
        long timeSpentSecs = bundle.getLong(GeoUtils.SECONDS_SPENT_TAG, 0);

        double coins = timeSpentSecs * 0.1;

        TextView coinView = findViewById(R.id.gymSessions6);
        coinView.setText(String.format("%.1f", coins));


        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        long seconds = timeSpentSecs % SECONDS_IN_A_MINUTE;
        long totalMinutes = timeSpentSecs / SECONDS_IN_A_MINUTE;
        long minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        long hours = totalMinutes / MINUTES_IN_AN_HOUR;

        String timeText;

        if(hours > 0) {
            timeText = String.format("%d hours", hours);
        } else if(minutes > 0){
            timeText = String.format("%d minutes", minutes);
        } else {
            timeText = String.format("%d seconds", seconds);
        }

        TextView sessionView = findViewById(R.id.gymSessions3);
        sessionView.setText(timeText);

        TextView gymView = findViewById(R.id.gymSessions2);
        gymView.setText(String.valueOf(sessions));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults[0] != -1) {
            startService(serviceIntent);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private class ServiceConnectionImpl implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            locationMessenger = new Messenger(iBinder);

            Message msg = Message.obtain(null, Tags.APP_REG, 0, 0);

            msg.replyTo = messenger;
            try {
                locationMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(Tags.APP_TAG, e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadData(msg.getData());
                }
            });
        }
    }
}
