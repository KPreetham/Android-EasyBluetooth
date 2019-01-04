package me.preetham.easybluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import me.preetham.easybluetooth.enums.Mode;
import me.preetham.easybluetooth.interfaces.SppConnectionListener;
import me.preetham.easybluetooth.interfaces.SppDataListener;

public class BluetoothSpp {
    private static final String TAG = BluetoothSpp.class.getSimpleName();

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private SppDataListener dataListener = null;
    private SppConnectionListener connectionListener = null;

    private Mode mode = Mode.INSECURE;

    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private int bufferSize = 1024;

    private void start(final BluetoothSocket bluetoothSocket) {
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            if (connectionListener != null) {
                connectionListener.onConnectionLost();
            }
            return;
        }

        Thread spp_thread = new Thread() {
            int bytes_read = -1;
            byte[] byte_data = new byte[bufferSize];

            @Override
            public void run() {
                while (true) {
                    try {
                        bytes_read = inputStream.read(byte_data);
                        if (dataListener != null) {
                            dataListener.onDataReceived(byte_data, bytes_read);
                        }
                    } catch (IOException e) {
                        if (connectionListener != null) {
                            connectionListener.onConnectionLost();
                        }
                        break;
                    }
                }
            }
        };

        spp_thread.start();
    }

    public void connect(BluetoothDevice targetDevice) {
        try {
            switch (mode) {
                case SECURE:
                    bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(uuid);
                    break;
                case INSECURE:
                    bluetoothSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                    break;
            }

            bluetoothSocket.connect();

            if (connectionListener != null) {
                connectionListener.onConnectionSuccess();
                start(bluetoothSocket);
            }
        } catch (IOException ex) {
            if (connectionListener != null) {
                connectionListener.onConnectionFailure();
            }
        }
    }

    public void disconnect() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }

            if (connectionListener != null) {
                connectionListener.onConnectionClosed();
            }
        } catch (IOException ignored) {

        }
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setConnectionListener(SppConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void setDataListener(SppDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
            if (dataListener != null) {
                dataListener.onDataSent(bytes);
            }
        } catch (IOException e) {
            connectionListener.onConnectionLost();
        }
    }

    public void write(String data_string) {
        write(data_string.getBytes());
    }
}
