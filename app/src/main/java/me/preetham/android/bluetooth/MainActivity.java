package me.preetham.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import me.preetham.easybluetooth.BluetoothSpp;
import me.preetham.easybluetooth.interfaces.SppConnectionListener;
import me.preetham.easybluetooth.interfaces.SppDataListener;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothSpp bluetoothSpp;
    private SppConnectionListener connectionListener;
    private SppDataListener dataListener;

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private String mac_id = "34:81:F4:18:B7:60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupListeners();
        setupBluetoothSpp();
    }

    @Override
    protected void onDestroy() {
        bluetoothSpp.disconnect();
        super.onDestroy();
    }

    private void setupBluetoothSpp() {
        bluetoothSpp = new BluetoothSpp();
        bluetoothSpp.setConnectionListener(connectionListener);
        bluetoothSpp.setDataListener(dataListener);

        BluetoothDevice device = adapter.getRemoteDevice(mac_id);
        bluetoothSpp.connect(device);
    }

    private void setupListeners() {
        dataListener = new SppDataListener() {
            @Override
            public void onDataReceived(byte[] data, int bytes_read) {
                Log.e(TAG, "Received: " + new String(data, 0, bytes_read));
                Date currentTime = Calendar.getInstance().getTime();

                bluetoothSpp.write(currentTime.toString());
            }

            @Override
            public void onDataSent(byte[] data) {
                Log.e(TAG, "Sent: " + new String(data));
            }
        };

        connectionListener = new SppConnectionListener() {
            @Override
            public void onConnectionSuccess() {
                Log.e(TAG, "onConnectionSuccess");
            }

            @Override
            public void onConnectionFailure() {
                Log.e(TAG, "onConnectionFailure");
            }

            @Override
            public void onConnectionLost() {
                Log.e(TAG, "onConnectionLost");
            }

            @Override
            public void onConnectionClosed() {
                Log.e(TAG, "onConnectionClosed");
            }
        };
    }
}
