package me.preetham.easybluetooth.interfaces;

public interface SppConnectionListener {
    void onConnectionSuccess();
    void onConnectionFailure();
    void onConnectionLost();
    void onConnectionClosed();
}
