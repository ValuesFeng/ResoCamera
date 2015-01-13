package io.values.camera.widget;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by valuesfeng on 14-7-30.
 */
public class ShakeListener implements SensorEventListener {

    public static final int LandscapeLeft = 0;
    public static final int LandscapeRight = 180;
    public static final int Portrait = 90;
    private SensorManager sensorManager;
    private static ShakeListener sensor1;
    private Sensor sensor;
    private OnShakeListener onShakeListener;

    public static ShakeListener newInstance() {
        if (sensor1 == null) {
            sensor1 = new ShakeListener();
        }
        return sensor1;
    }

    public void start(Context context) {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensorManager != null && sensor == null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (sensor != null) {
            try {
                sensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    public interface OnShakeListener {
        public void onShake(int orientation);
    }

    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float g = 9.8f;
        float ghysteresis = g / 5;
        float ghalf = g * (float) Math.sqrt(2) / 2;
        if (Math.abs(z) > ghalf) {
            return;
        }

        if (x <= -ghalf - ghysteresis / 2) {
            onShakeListener.onShake(LandscapeRight);
        } else if (y >= ghalf + ghysteresis / 2) {
            onShakeListener.onShake(Portrait);
        } else if (x >= ghalf + ghysteresis / 2) {
            onShakeListener.onShake(LandscapeLeft);
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}