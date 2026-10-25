package de.thaler.donotjiggle;

import static de.thaler.donotjiggle.MainActivity.mainActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothStatus extends Thread {
    static String TAG = "myLog BluetoothStatus";
    private volatile boolean running = true;
    public Boolean DeviceIsConnected = false;
    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled(); }

    TextView textViewConnect = mainActivity.findViewById(R.id.textViewBTConnect);

    public void run() {
        while (running && !isInterrupted()) {
            doWork(); // hier die bt aktionen
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                interrupt(); // Interrupt-Status wiederherstellen
                break;
            }
        }
    }
    public void stopThread() {
        running = false;
        interrupt(); // beendet auch ein gerade laufendes sleep()
    }
    @SuppressLint("SetTextI18n")
    private void doWork() { Log.i(TAG, "status: " + mainActivity.statusConnection);
        if (!isBluetoothEnabled()) { // auf dem Geraet ist BT aus
            textViewConnect.setText(R.string.firstMessage_fail);
            textViewConnect.setTextColor(ContextCompat.getColor(mainActivity,R.color.red));
            DeviceIsConnected = false;
        } else if (isBluetoothEnabled() && mainActivity.statusConnection == 0) {
            if (BluetoothConnectionMonitor.bluetoothConnectionMonitor != null) {
                BluetoothConnectionMonitor.bluetoothConnectionMonitor.close();
            }
            BluetoothDevice device = BluetoothConnectionMonitor.devicesByName(
                    mainActivity.mPreference.getString("connectDevice", "nothing"));
            BluetoothConnectionMonitor.connectedBT(device);
        } else if (isBluetoothEnabled() && mainActivity.statusConnection == 1) {
            textViewConnect.setText(mainActivity.getString(R.string.bluetooth_is_on)
                    + "\n\""
                    + mainActivity.mPreference.getString("connectDevice", "nothing")
                    + "\""
                    + " is ready."
            );
            textViewConnect.setTextColor(ContextCompat.getColor(mainActivity,R.color.teal_100));
            Intent sensorIntent = new Intent(mainActivity, SensorListener.class);
            mainActivity.startService(sensorIntent);
        } else { // getrennten Kontakt wieder aufnehmen
            textViewConnect.setText(R.string.bluetooth_connection_lost);
            textViewConnect.setTextColor(ContextCompat.getColor(mainActivity,R.color.red_100));
            if (BluetoothConnectionMonitor.bluetoothConnectionMonitor != null) {
                BluetoothConnectionMonitor.bluetoothConnectionMonitor.close();
            }
            BluetoothDevice device = BluetoothConnectionMonitor.devicesByName(
                    mainActivity.mPreference.getString("connectDevice", "nothing"));
            BluetoothConnectionMonitor.connectedBT(device);
        }
    }
}
