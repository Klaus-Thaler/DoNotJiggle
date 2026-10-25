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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class BluetoothService extends Service {
    String TAG = "myLog BluetoothService";
    SharedPreferences mPreference;
    private static final String CHANNEL_ID = "bluetooth_connection";
    private static final int NOTIFICATION_ID = 1;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        mPreference = this.getSharedPreferences("myPreferences", 0);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification("in connection with "
            + mPreference.getString("connectDevice", "nothing")));

        BluetoothDevice device = BluetoothConnectionMonitor.devicesByName(
                mPreference.getString("connectDevice", "nothing"));

        Log.v(TAG, "create BT Service for " + mPreference.getString("connectDevice", "nothing"));

        BluetoothConnectionMonitor.connectedBT(device);
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Do not jiggle in action")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Bluetooth-Connection",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
