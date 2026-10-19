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

import static de.thaler.donotjiggle.MainActivity.REQUEST_BLUETOOTH_CONNECT;
import static de.thaler.donotjiggle.MainActivity.mainActivity;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Set;

public final class BluetoothConnectionMonitor {
    static String TAG = "myLog BluetoothConnectionMonitor";
    public interface Listener {
        void onConnected();
        void onDisconnected(int status);
    }
    private final Context context;
    private final Listener listener;
    public static BluetoothGatt bluetoothGatt;
    public static ArrayList<String> bluetoothDevices;
    public static BluetoothConnectionMonitor bluetoothConnectionMonitor;

    private boolean stopped = false;
    @RequiresApi(api = Build.VERSION_CODES.S)
    public BluetoothConnectionMonitor(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static BluetoothDevice devicesByName(String deviceName) {
        bluetoothDevices = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT
            );
            return null;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(mainActivity, "Bluetooth wird nicht unterstützt",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            //Log.i(TAG, "found: " + device.getName());
            if (deviceName.equals(device.getName())) return device;
        }
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void connectedBT(BluetoothDevice device) {
        bluetoothConnectionMonitor = new BluetoothConnectionMonitor(mainActivity,
                new Listener() {
                    final TextView textViewConnect = mainActivity.findViewById(R.id.textViewBTConnect);
                    @Override
                    public void onConnected() {
                        Log.e(TAG, "verbunden");
                        textViewConnect.setText("verbunden");
                        mainActivity.DeviceConnected = true;
                    }
                    @Override
                    public void onDisconnected(int status) {
                        Log.e(TAG, "getrennt " + status);
                        textViewConnect.setText("getrennt ");
                        mainActivity.DeviceConnected = false;
                    }
                });
        bluetoothConnectionMonitor.connect(device);
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static String[] listPairedDevicesByName() {
        bluetoothDevices = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT
            );
            return null;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(mainActivity, "Bluetooth wird nicht unterstützt",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            bluetoothDevices.add(device.getName());
            Log.d(TAG, (device.getName() != null ? device.getName() : "Untitled")
                    + " — " + device.getAddress());
        }
        return bluetoothDevices.toArray(new String[0]);
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void connect(BluetoothDevice device) {
        stopped = false;
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT
            );
            return;
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void close() {
        stopped = true;
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT
            );
            return;
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onConnectionStateChange(
                BluetoothGatt gatt,
                int status,
                int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt = gatt;
                listener.onConnected();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (ContextCompat.checkSelfPermission(
                        mainActivity, Manifest.permission.BLUETOOTH_CONNECT) ==
                        PackageManager.PERMISSION_GRANTED) {
                    mainActivity.requestPermissions(
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_BLUETOOTH_CONNECT
                    );
                    return;
                }
                gatt.close();
                if (!stopped) {
                    listener.onDisconnected(status);
                }
                bluetoothGatt = null;
            }
        }
    };
}