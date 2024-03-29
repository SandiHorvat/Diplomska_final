package com.diplomska.diplomska_wear_phone_2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.View;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.PutDataMapRequest;
import com.mobvoi.android.wearable.PutDataRequest;
import com.mobvoi.android.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends WearableActivity implements SensorEventListener, MobvoiApiClient.ConnectionCallbacks, MobvoiApiClient.OnConnectionFailedListener{


    private static final String TAG = "MainActivity";
    private TextView mTextView;
    private DeviceClient client;
    private MobvoiApiClient mMobvoiApiClient;
    private SensorManager mSensorManager;
    private Sensor mHeartRate;
    private ScheduledExecutorService mScheduler;
    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 1;
    private int filterId;
    private SparseLongArray lastSensorData;
    private ExecutorService executorService;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = DeviceClient.getInstance(this);

        mTextView = (TextView) findViewById(R.id.text);






        // Enables Always-on
        setAmbientEnabled();

        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        checkSelfPermission(Manifest.permission.BODY_SENSORS);








        executorService = Executors.newCachedThreadPool();
        lastSensorData = new SparseLongArray();

    }


    @Override
    public void onDestroy (){
        super.onDestroy();

        stopMeasure();
    }




    public void OnClickBtn(View v){
         checkSelfPermission(Manifest.permission.BODY_SENSORS);
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS},
                    MY_PERMISSIONS_REQUEST_BODY_SENSORS);
            if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
                    != PackageManager.PERMISSION_GRANTED){
                System.out.print(" Second if statement.");
            }
            else{
                System.out.print(" Secondd else.");
            }
        }
        else{
            System.out.println("NOt working. ");
        }
        startMeasure();
    }

    public void OnClickBtnStop (View v){
        stopMeasure();
    }


/*

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
    final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    return Asset.createFromBytes(byteStream.toByteArray());
    }
    */


    private void startMeasure() {


        mSensorManager =
                (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRate = mSensorManager.getDefaultSensor(
                Sensor.TYPE_HEART_RATE);

        if (mHeartRate != null) {

            final int measurementDuration   = 30;   // Seconds
            final int measurementBreak = 15; // Seconds

            mScheduler = Executors.newScheduledThreadPool(1);
            mScheduler.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "register Heartrate Sensor");
                            mSensorManager.registerListener(MainActivity.this, mHeartRate, SensorManager.SENSOR_DELAY_FASTEST);

                            try {
                                Thread.sleep(measurementDuration * 1000);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                            }

                            Log.d(TAG, "unregister Heartrate Sensor");
                            mSensorManager.unregisterListener(MainActivity.this, mHeartRate);
                        }
                    }
                    , 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);
        }  else {
            Log.d(TAG, "No Heartrate Sensor found");
        }




        //boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_FASTEST);
        //Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    private void stopMeasure() {
       // mSensorManager.unregisterListener(this);
       // mScheduler.shutdown();

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }



    public void onClickBtnI(View v){
        System.out.println("PRoba");
    }

    public void OnClickBtnS(View v){
        logAvailableSensors();
    }




    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
        // Now you can use the Data Layer API
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "SendSensor data is in progress");
        client.sendSensorData(sensorEvent.sensor.getType(), sensorEvent.accuracy, sensorEvent.timestamp, sensorEvent.values);
        // Printing message and loging just to see if method is called
        System.out.println("SendSensorData success!!!");
        Log.d(TAG, "SendSensor data success");


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

        Log.d(TAG, "This is accuracy changed");
    }

    private void logAvailableSensors() {
        final List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
        Log.d(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", "SensorName", "StringType", "Type"));
        for (Sensor sensor : sensors) {
            Log.v(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", sensor.getName(), sensor.getStringType(), sensor.getType()));
        }

        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
    }


}
