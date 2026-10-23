package de.thaler.donotjiggle;

/*
 * Copyright 2026 Klaus Thaler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static de.thaler.donotjiggle.MainActivity.mainActivity;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SensorListener extends Service implements SensorEventListener {
    String TAG = "myLog SensorListener";
    SharedPreferences mPreference;
    TextView textViewJiggle, textViewConnect;
    private SensorManager sensorManager;
    static MediaPlayer player;
    private long lastShakeTime = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        mPreference = this.getSharedPreferences("myPreferences", 0);
        //mPreferenzEditor = mPreferenz.edit();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(
                        this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        textViewJiggle = mainActivity.findViewById(R.id.textViewJiggle);
        textViewConnect = mainActivity.findViewById(R.id.textViewBTConnect);
        textViewJiggle.setText("verfügbar");
        return START_STICKY; }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0] / SensorManager.GRAVITY_EARTH;
        float y = event.values[1] / SensorManager.GRAVITY_EARTH;
        float z = event.values[2] / SensorManager.GRAVITY_EARTH;
        float gForce = (float) Math.sqrt(x * x + y * y + z * z);

        long now = System.currentTimeMillis();
        if (gForce > mPreference.getFloat("gForce",1.1F)
                && now - lastShakeTime > mPreference.getLong("shakeTime", 1000L)) {
            // gForce = empfindlichkeit, shakeTime = pause dazwischen

            lastShakeTime = now; // neue zeit für den nächsten schüttler
            Toast.makeText(this, "Geschüttelt!", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Geschüttelt!");
            textViewJiggle.setText("geschüttelt");
            TimerTask task = new TimerTask() {
                public void run() { // nach einer zeit den text wieder zurücksetzen
                    textViewJiggle.setText("");
                }
            };
            Timer timer = new Timer("Timer");
            timer.schedule(task, 2000L);

            // prüfen ob verbindung verloren ist. dann alarm
            if (mainActivity.statusConnection == 2) {
                Log.i(TAG, "play music");
                play(this, "mDong.mp3");
                //play(this, "us_sirene.mp3");
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*wird nicht gebraucht*/}

    @Override
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Started service, not bound
    }
    public static void play (Context context, String path) {
        AssetFileDescriptor assetFileDescriptor;
        player = new MediaPlayer();
        try {
            //if (player.isPlaying()) player.stop();
            assetFileDescriptor = context.getAssets().openFd(path);
            player.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength()
            );
            assetFileDescriptor.close();
            player.prepare();
            player.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}