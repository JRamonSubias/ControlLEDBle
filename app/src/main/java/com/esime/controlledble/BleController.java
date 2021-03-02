package com.esime.controlledble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleController {
    private static BleController instance;

    private HashMap<String, BluetoothDevice> devices = new HashMap<>();

    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner scanner;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic btGattChar = null;

    private ArrayList<BleControllListener> listeners = new ArrayList<>();



    private BleController(Context context){
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public static BleController getInstance(Context context){
        if(instance == null)
            instance = new BleController((context));
        return instance;
    }

    public void addBLEControllListener(BleControllListener l){
        if(!this.listeners.contains(l))
            this.listeners.add(l);
    }


    public void init(){
        this.scanner = this.bluetoothManager.getAdapter().getBluetoothLeScanner();
        scanner.startScan(bleCallback);
    }



    public void connectToDevice(BluetoothDevice device){
        this.scanner.stopScan(bleCallback);
        this.device = device;
        Log.i(Constantes.TAG,"Connect to device...");
        this.bluetoothGatt = device.connectGatt(MyApp.getContext(),false,this.bleConnectCallback);
    }


    public void sendData(String data) {
        btGattChar.setValue(data);
        bluetoothGatt.writeCharacteristic(btGattChar);
        Log.i(Constantes.TAG,"Characteristic "+data+" sent");
    }

    public void readData(){
        this.bluetoothGatt.readCharacteristic(this.btGattChar);
        Log.i(Constantes.TAG,"Characteristic to Read");
    }

    private BluetoothGattCallback bleConnectCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i(Constantes.TAG,"Connected to Gatt client. Attempting to start service dicovery");
                gatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(Constantes.TAG,"Disconnected from GATT client");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(null == btGattChar) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().toUpperCase().startsWith("19B10000")) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic bgc : gattCharacteristics) {
                            if (bgc.getUuid().toString().toUpperCase().startsWith("19B10001")) {
                                int chprop = bgc.getProperties();
                                if (((chprop & BluetoothGattCharacteristic.PROPERTY_WRITE) | (chprop & BluetoothGattCharacteristic.PROPERTY_READ)) > 0) {
                                    btGattChar = bgc;
                                    Log.i(Constantes.TAG, "CONNECTED and ready to send");
                                    fireConnected();

                                }
                            }
                        }
                    }
                }
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String data = characteristic.getStringValue(0);
            Log.i(Constantes.TAG,"Read: "+data);
            fireReadData(data);
        }
    };



    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if(!devices.containsKey(device.getAddress())){
                devices.put(device.getAddress(),device);
                foreDeviceFound(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(Constantes.TAG,"Scan failed with errorCode: "+ errorCode);
        }
    };


    private void foreDeviceFound(BluetoothDevice Devices) {
        for(BleControllListener l : this.listeners)
            l.BLEDeviceFound(Devices);
    }

    private void fireConnected() {
        for(BleControllListener l : this.listeners)
            l.BLEControllerConnected();
    }

    private void fireReadData(String mData) {
        for(BleControllListener l : this.listeners)
            l.BLEReadDataDevice(mData);
    }


}
