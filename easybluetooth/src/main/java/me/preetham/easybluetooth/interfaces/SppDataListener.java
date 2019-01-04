package me.preetham.easybluetooth.interfaces;

public interface SppDataListener {
    void onDataReceived(byte[] data, int bytes_read);
    void onDataSent(byte[] data);
}
