package com.esime.controlledble;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;

public class BleViewModel extends ViewModel {
    private MutableLiveData<List<BluetoothDevice>> listDevice;
    private MutableLiveData<HashMap<String, BluetoothDevice>> devices;

    private BleController bleController;

    public BleViewModel (){
        bleController = BleController.getInstance(MyApp.getContext());
    }

}
