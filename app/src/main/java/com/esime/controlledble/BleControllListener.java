package com.esime.controlledble;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public interface BleControllListener {

    public void BLEControllerConnected();
    public void BLEControllerDisconnected();
    public void BLEDeviceFound(BluetoothDevice Devices);
    public void BLEReadDataDevice(String mData);

}
