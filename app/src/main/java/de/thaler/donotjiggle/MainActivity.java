package de.thaler.donotjiggle;

/*
 * Copyright 2026 M. Malum
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "myLog MainActivity";
    @SuppressLint("StaticFieldLeak")
    public static MainActivity mainActivity;
    SharedPreferences mPreferenz;
    SharedPreferences.Editor mPreferenzEditor;
    private TextView textViewConnect;
    public static final int REQUEST_BLUETOOTH_CONNECT = 1001;
    private static final String[] locationPerms = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    public boolean isBluetoothEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }
    public Boolean DeviceConnected = false;
    static HandlerThread LoopThread;
    static Handler bgHandler;
    static Runnable loop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mainActivity = this;
        mPreferenz = this.getSharedPreferences("myPreferences", 0);
        textViewConnect = mainActivity.findViewById(R.id.textViewBTConnect);

        ActivityCompat.requestPermissions(this, locationPerms, REQUEST_BLUETOOTH_CONNECT);

        ListView listViewDevices = findViewById(R.id.listViewDevices);
        ArrayAdapter<String> myArrayAdapter =
                new ArrayAdapter<>(
                        mainActivity,
                        R.layout.list_row,
                        R.id.quote_text,
                        Objects.requireNonNull(
                                BluetoothConnectionMonitor.listPairedDevicesByName()));
        listViewDevices.setAdapter(myArrayAdapter);

        connectBluetoothDevice();
    }
    private void connectBluetoothDevice() {
        // bluetooth verbindung prüfen und wenn nötig herstellen
        String DeviceName = mPreferenz.getString("DeviceName", "Nothing Phone (3a) Pro");
        Log.i(TAG, DeviceName);

        Intent intent = new Intent(mainActivity, SensorListener.class);
        startService(intent);
        // create handler
        LoopThread = new HandlerThread("LoopTimerBT");
        LoopThread.start();
        bgHandler = new Handler(LoopThread.getLooper());
        loop = new Runnable() {
            @Override
            public void run() {
                startService(intent);
                if (!isBluetoothEnabled()) { //Log.i(TAG, "isBluetoothEnabled " + isBluetoothEnabled());
                    textViewConnect.setText("BT einschalten");
                    DeviceConnected = false;
                } else {
                    textViewConnect.setText("wait ...");
                    BluetoothDevice device = BluetoothConnectionMonitor.devicesByName(DeviceName);
                    BluetoothConnectionMonitor.connectedBT(device);
                }
                bgHandler.postDelayed(loop, 5000L);
            }
        };
        bgHandler.removeCallbacks(loop);
        bgHandler.post(loop);
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "permission okay");
        } else { Log.e(TAG, "permission error"); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothConnectionMonitor.bluetoothConnectionMonitor != null) {
            BluetoothConnectionMonitor.bluetoothConnectionMonitor.close();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (BluetoothConnectionMonitor.bluetoothConnectionMonitor == null) {
            connectBluetoothDevice();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (BluetoothConnectionMonitor.bluetoothConnectionMonitor == null) {
            connectBluetoothDevice();
        }
    }
}