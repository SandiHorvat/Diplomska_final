package com.diplomska.diplomska_wear_phone_2;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseLongArray;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.PutDataMapRequest;
import com.mobvoi.android.wearable.PutDataRequest;
import com.mobvoi.android.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceClient implements  MobvoiApiClient.ConnectionCallbacks, MobvoiApiClient.OnConnectionFailedListener  {

    private Context context;
    private static final String TAG = "/DeviceClient";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;
    private int filterId;
    private SparseLongArray lastSensorData;
    private ExecutorService executorService;
    private MobvoiApiClient mMobvoiApiClient;

    public static DeviceClient instance;

    public static DeviceClient getInstance(Context context){
        if(instance == null){
            instance = new DeviceClient(context.getApplicationContext());
        }

        return instance;
    }


    private DeviceClient(Context context){
        this.context = context;

        mMobvoiApiClient = new MobvoiApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();


    }




    public void setSensorFilter(int filterId){
        Log.d(TAG,"Now filtering by sensor: " + filterId);

        this.filterId = filterId;
    }


    public void sendSonsorData(final int sensorType, final int accuracy, final long timestamp, final float[] values){
        long t = System.currentTimeMillis();

        long lastTimestamp = lastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (lastTimestamp != 0) {
            if (filterId == sensorType && timeAgo < 100) {
                return;
            }
            if (filterId != sensorType && timeAgo < 3000) {
                return;
            }
        }

        lastSensorData.put(sensorType, t);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, accuracy, timestamp, values);
            }
        });

    }


    private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp, float[] values){

        // dodaj sensorType filter pozneje


        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);

        dataMap.getDataMap().putInt(DataMapKeys.ACCURACY, accuracy);
        dataMap.getDataMap().putLong(DataMapKeys.TIMESTAMP, timestamp);
        dataMap.getDataMap().putFloatArray(DataMapKeys.VALUES, values);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);

    }

    private void send(PutDataRequest putDataRequest){
        if(validateConnection()){
            Wearable.DataApi.putDataItem(mMobvoiApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }

    }


    private boolean validateConnection(){
        if(mMobvoiApiClient.isConnected()){
            return true;
        }

        ConnectionResult result = mMobvoiApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
