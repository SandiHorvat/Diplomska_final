package com.diplomska.diplomska_wear_phone_2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends WearableActivity implements SensorEventListener, MobvoiApiClient.ConnectionCallbacks, MobvoiApiClient.OnConnectionFailedListener {


    private static final String TAG = "MainActivity";
    private TextView mTextView;
    private DeviceClient client;
    private MobvoiApiClient mMobvoiApiClient;
    private SensorManager mSensorManager;
    private Sensor mHeartRate;
    private ScheduledExecutorService mScheduler;
    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        mSensorManager =
                (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRate = mSensorManager.getDefaultSensor(
                Sensor.TYPE_HEART_RATE);

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
                System.out.print(" Drugi if stavek.");
            }
            else{
                System.out.print(" Drugi else.");
            }
        }
        else{
            System.out.print(" NE Deluje.");
        }
        startMeasure();
    }

    public void OnClickBtnStop (View v){
        stopMeasure();
    }




    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        return Asset.createFromBytes(byteStream.toByteArray());
    }



    private void startMeasure() {

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

        //boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_FASTEST);
        //Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
        mScheduler.shutdown();
    }

    private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp, float[] values){


        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);

        dataMap.getDataMap().putInt(DataMapKeys.ACCURACY, accuracy);
        dataMap.getDataMap().putLong(DataMapKeys.TIMESTAMP, timestamp);
        dataMap.getDataMap().putFloatArray(DataMapKeys.VALUES, values);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);

    }

    private void send(PutDataRequest putDataRequest){
        Wearable.DataApi.putDataItem(mMobvoiApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
            }
        });
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

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
