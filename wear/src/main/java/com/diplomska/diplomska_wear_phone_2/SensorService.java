package com.diplomska.diplomska_wear_phone_2;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.concurrent.ScheduledExecutorService;

public class SensorService extends Service implements SensorEventListener {

    private static final String TAG = "SensorService";
    SensorManager mSensorManager;
    private DeviceClient client;
    private ScheduledExecutorService mScheduler;







    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
