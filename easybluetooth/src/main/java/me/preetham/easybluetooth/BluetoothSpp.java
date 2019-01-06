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

    private UUID uuid;

    private SppDataListener dataListener;
    private SppConnectionListener connectionListener;

    private Mode mode;
    private int bufferSize;
    private boolean isRunning = false;

    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private BluetoothDevice targetDevice;


    private BluetoothSpp(Builder builder) {
        this.uuid = builder.uuid;
        this.dataListener = builder.dataListener;
        this.connectionListener = builder.connectionListener;
        this.mode = builder.mode;
        this.bufferSize = builder.bufferSize;
    }

    public static class Builder {
        private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private SppDataListener dataListener = null;
        private SppConnectionListener connectionListener = null;

        private Mode mode = Mode.INSECURE;
        private int bufferSize = 1024;

        public BluetoothSpp build() {
            return new BluetoothSpp(this);
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setConnectionListener(SppConnectionListener connectionListener) {
            this.connectionListener = connectionListener;
            return this;
        }

        public Builder setDataListener(SppDataListener dataListener) {
            this.dataListener = dataListener;
            return this;
        }

        public Builder setMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder setUuid(String uuid) {
            this.uuid = UUID.fromString(uuid);
            return this;
        }

        public Builder setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
    }

    private void start(final BluetoothSocket bluetoothSocket) {
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

            if (connectionListener != null) {
                connectionListener.onConnectionSuccess();
                isRunning = true;
            }
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
                while (isRunning) {
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
            this.targetDevice = targetDevice;

            start(bluetoothSocket);
        } catch (IOException ex) {
            if (connectionListener != null) {
                connectionListener.onConnectionFailure();
            }
        }
    }

    public void disconnect() {
        isRunning = false;
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

    public BluetoothDevice getTargetDevice() {
        return targetDevice;
    }

    public boolean isRunning() {
        return isRunning;
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
