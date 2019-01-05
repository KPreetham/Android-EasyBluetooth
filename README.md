[![Release](https://jitpack.io/v/KPreetham/Android-EasyBluetooth.svg)](https://jitpack.io/#KPreetham/Android-EasyBluetooth) ![Travis-ci](https://api.travis-ci.org/KPreetham/Android-EasyBluetooth.svg)

# Android-EasyBluetooth
A collection of easy to use bluetooth utilities for Android.


## Getting started

### Simple Bluetooth SPP
Add the following line under repositories to the project root build.gradle file.
```gradle
maven { url 'https://jitpack.io' }
```

Then add the dependency in your app build.gradle file
```gradle
implementation 'com.github.KPreetham:Android-EasyBluetooth:RELEASE_VERSION'
```

Refer to `JitPack` badge above for RELEASE_VERSION.

#### Basic Usage
Implement dataListener.
```java
dataListener = new SppDataListener() {
    @Override
    public void onDataReceived(byte[] data, int bytes_read) {
        // received `data` of length `bytes_read`
    }

    @Override
    public void onDataSent(byte[] data) {
        // `data` was successfully written to the stream.
    }
};
```

Implement connection listener
```java
connectionListener = new SppConnectionListener() {
    @Override
    public void onConnectionSuccess() {
        // Successfully connected to bluetooth device.
    }

    @Override
    public void onConnectionFailure() {
        // Connection attempt failed.
    }

    @Override
    public void onConnectionLost() {
        // Connection is lost. Socket might have been closed abruptly.
    }

    @Override
    public void onConnectionClosed() {
        // Connection was closed succesfully.
    }
};
```

Create a BluetoothSpp object.
```java
bluetoothSpp = new BluetoothSpp.Builder()
    .setConnectionListener(connectionListener)
    .setDataListener(dataListener)
    .setBufferSize(2048)
    .build();
```

To connect to a ```BluetoothDevice``` call ```connect(BluetoothDevice)``` on bluetoothSpp object.
```java
bluetoothSpp.connect(device);
```

To disconnect
```java
bluetoothSpp.disconnect();
```
