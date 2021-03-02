package com.esime.controlledble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BleControllListener {
    private TextView tvStatus;
    private Button btnOnLed, btnOffLed;
    private ImageButton btnSearchBlue;
    private ListView listViewDevicesBle;
    private LinearLayout linearLayoutBtnLed;
    private List<BluetoothDevice> listDevices = new LinkedList<>();
    private ArrayAdapter<String> adapter;
    private List<String> listBle = new ArrayList<>();
    private HashMap<String, BluetoothDevice> devices;

    private BleController bleController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getById();
        checkPermissions();
        checkBLESupport();
        adapter = new ArrayAdapter<>(MyApp.getContext(), android.R.layout.simple_list_item_1,listBle);
        bleController = BleController.getInstance(MyApp.getContext());


        btnSearchBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MyApp.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MyApp.getContext(), "Searching...", Toast.LENGTH_SHORT).show();
                    btnSearchBlue.setImageResource(R.drawable.cancel);
                    bleController.init();
                }
            }
        });

        listViewDevicesBle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bleController.connectToDevice(listDevices.get(position));
            }
        });

        btnOnLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleController.sendData("1");
            }
        });

        btnOffLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleController.sendData("0");
            }
        });

        tvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleController.readData();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.bleController = BleController.getInstance(MyApp.getContext());
        this.bleController.addBLEControllListener(this);

    }


    private void getById() {
        tvStatus = findViewById(R.id.tvStatusBle);
        btnOnLed = findViewById(R.id.btnONLed);
        btnOffLed = findViewById(R.id.btnOFFLed);
        btnSearchBlue = findViewById(R.id.imgBtnSearch);
        listViewDevicesBle = findViewById(R.id.listDevicesBle);
        linearLayoutBtnLed = findViewById(R.id.linearLayoutBtnLED);
    }


    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Whitout this permission Blutooth devices cannot be searched!", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    42);
        }
    }

    private void checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void BLEControllerConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayoutBtnLed.setVisibility(View.VISIBLE);
                Toast.makeText(MyApp.getContext(), "Connected", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void BLEControllerDisconnected() {

    }

    @Override
    public void BLEDeviceFound(BluetoothDevice Devices) {
        this.listDevices.add(Devices);
        this.listViewDevicesBle.setAdapter(this.adapter);
            String device;
            if(Devices.getName() == null){
                device = "Unknow\n";
            }else{
                device = Devices.getName()+"\n";
            }
            device += Devices.getAddress();
            this.adapter.add(device);
            this.adapter.notifyDataSetChanged();

    }

    @Override
    public void BLEReadDataDevice(String mData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("Data: "+mData);
            }
        });

    }
}