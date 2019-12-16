package com.example.phonesensors;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Timestamp;
import java.sql.Time;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    TextView curLat, curLon, sensorValue, locUpdTS, sensorUpdTS;
    private SensorManager sensorManager;
    LocationManager locMngr;
    private long lastUpdate;
    Time time;
    boolean isLocListening;
    int locCounter, sensorCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curLat = findViewById(R.id.curLat);
        curLon = findViewById(R.id.curLon);
        locUpdTS = findViewById(R.id.locUpdTS);
        sensorValue = findViewById(R.id.sensorValue);
        sensorUpdTS = findViewById(R.id.sensorUpdTS);
        isLocListening = false;
        locCounter = 0; sensorCounter = 0;
        try {
            locMngr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locMngr.isProviderEnabled(LocationManager.GPS_PROVIDER) || locMngr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(this, "requestLocationUpdates", Toast.LENGTH_SHORT).show();
                locMngr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        catch (Exception ex){
            Toast.makeText(this, "exception", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        if (location != null)
        {
            curLat.setText("Current Lat: " + location.getLatitude());
            curLon.setText("Current Lon: " + location.getLongitude());
            locCounter++;
            //time = new Time(System.currentTimeMillis());
            //locUpdTS.setText(time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds());
            time = new Time(System.currentTimeMillis());
            locUpdTS.setText("Update Time: " + String.format("%02d",time.getHours()) + ":" + String.format("%02d", time.getMinutes()) + ":" + String.format("%02d", time.getSeconds()) + " (" + locCounter + ")");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        //LoggerHelper.debug(provider + "=" + String.valueOf(status));
        Toast.makeText(this, "StatusChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        //LoggerHelper.debug(provider + " enabled");
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        //LoggerHelper.debug(provider + " disabled");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //sensorValue.setText("Sensor Values: (0) @" + event.values[0] + ", (1) @" + event.values[1] + ", (2) @" + event.values[2]);
            //time = new Time(System.currentTimeMillis());
            //sensorUpdTS.setText("Update Time: " +String.format("%02d",time.getHours()) + ":" + String.format("%02d", time.getMinutes()) + ":" + String.format("%02d", time.getSeconds()));

            float[] values = event.values;
            // Movement
            float x = values[0];
            float y = values[1];
            float z = values[2];
            double l_scalarVal = Math.sqrt(
                    Math.pow(x, 2) +
                            Math.pow(y, 2) +
                            Math.pow(z, 2)
            );
            sensorValue.setText(String.valueOf(l_scalarVal - 9.86));
            long actualTime = System.currentTimeMillis();
            if (Math.abs(l_scalarVal - 9.86) >= 1.5){
                if(!isLocListening){
                    isLocListening = true;
                    locMngr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
                if(actualTime - lastUpdate < 200){
                    return;
                }
                sensorCounter++;
                lastUpdate = actualTime;
                time = new Time(actualTime);
                sensorUpdTS.setText("Update Time: " +String.format("%02d",time.getHours()) + ":" + String.format("%02d", time.getMinutes()) + ":" + String.format("%02d", time.getSeconds())  + " (" + locCounter + ")");


            }
            else{
                int duration = 30*1000;
                if(System.currentTimeMillis() > lastUpdate + duration){
                    isLocListening =false;
                    locMngr.removeUpdates(this);
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
