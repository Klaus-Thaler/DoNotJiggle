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

import static de.thaler.donotjiggle.BluetoothConnectionMonitor.bluetoothConnectionMonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    SharedPreferences mPreference;
    SharedPreferences.Editor mPreferenzEditor;;
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
    private Intent intentBT, sensorIntent;
    private TextView textViewConnect;
    private Button buttonStart;
    public int statusConnection = 0;

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

        ActivityCompat.requestPermissions(this, locationPerms, REQUEST_BLUETOOTH_CONNECT);

        mainActivity = this;

        // return button in nav bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mPreference = this.getSharedPreferences("myPreferences", 0);

        // Views
        textViewConnect = mainActivity.findViewById(R.id.textViewBTConnect);
        buttonStart = findViewById(R.id.buttonStart);

        // bluetooth Connect Listener Service
        intentBT = new Intent(mainActivity, BluetoothService.class);
        startService(intentBT);

        BluetoothStatus bluetoothStatus = new BluetoothStatus();
        bluetoothStatus.start();

        // all available Devices
        ListView listViewDevices = findViewById(R.id.listViewDevices);
        ArrayAdapter<String> myArrayAdapter =
                new ArrayAdapter<>(
                        mainActivity,
                        R.layout.list_row,
                        R.id.quote_text,
                        Objects.requireNonNull(BluetoothConnectionMonitor.listPairedDevicesByName()));
        listViewDevices.setAdapter(myArrayAdapter);
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

    /**
     * @param item The menu item that was selected.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            stopService(intentBT);
            //stopService(sensorIntent);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "in destroy");
        if (bluetoothConnectionMonitor != null) {
            bluetoothConnectionMonitor.close();
        }
        Intent intent = new Intent(mainActivity, SensorListener.class);
        stopService(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "in pause");
        //enabledBluetooth();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "in resume");
        //enabledBluetooth();
    }
}