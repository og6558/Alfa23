package com.example.alfa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothAlpha extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<BluetoothDevice> devicesList = new ArrayList<>();
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_BLUETOOTH_CONNECT = 4;
    private static final int REQUEST_BLUETOOTH_ENABLE = 5; // New constant for Bluetooth enable
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_alpha);

        // Check and request Bluetooth permissions if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_FINE_LOCATION
                );
            } else {
                // Permissions are already granted, initialize Bluetooth
                initializeBluetooth();
            }
        } else {
            // For versions below M, no runtime permissions are needed
            initializeBluetooth();
        }
    }

    private void initializeBluetooth() {
        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> startBluetoothDiscovery());

        ListView deviceListView = findViewById(R.id.deviceListView);
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(devicesAdapter);

        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            // Handle item click (connect to the selected device)
            BluetoothDevice selectedDevice = devicesList.get(position);
            connectToDevice(selectedDevice);
        });

        // Register the BroadcastReceiver for Bluetooth discovery
        registerBluetoothReceiver();
    }

    private void startBluetoothDiscovery() {
        // Check Bluetooth and location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION
            );
            return;
        }

        // Check if Bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // Bluetooth is not available or not enabled
            Toast.makeText(this, "Bluetooth is not available or not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isDiscovering()) {
            // Check Bluetooth state before canceling discovery
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                // Cancel discovery
                cancelBluetoothDiscovery();
            } else {
                // Bluetooth is not in STATE_ON, handle accordingly
                Toast.makeText(this, "Bluetooth is not in STATE_ON", Toast.LENGTH_SHORT).show();
            }
        }

        devicesAdapter.clear();
        devicesList.clear();
        bluetoothAdapter.startDiscovery();
    }

    private void cancelBluetoothDiscovery() {
        // Check Bluetooth permission before canceling discovery
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        // Check Bluetooth connect permission before proceeding
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT
            );
            return;
        }

        // Show connecting message
        String deviceName = getBluetoothName(device);
        if (deviceName != null) {
            Toast.makeText(this, "Connecting to: " + deviceName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Connecting to device", Toast.LENGTH_SHORT).show();
        }

        // Connect to the device in a background thread
        new Thread(() -> {
            try {
                // Create a secure (authenticated and encrypted) RFCOMM BluetoothSocket
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                // Connection successful, update UI in the main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Connected to: " + deviceName, Toast.LENGTH_SHORT).show();
                    // Implement your logic here
                });
            } catch (IOException e) {
                // Connection failed, update UI in the main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to connect to device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // In case of an error, you may want to try using createInsecureRfcommSocket or handle the error accordingly.
                });
            }
        }).start();
    }

    private String getBluetoothName(BluetoothDevice device) {
        // Check Bluetooth connect permission before proceeding
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, return null
            return null;
        }

        return device.getName();
    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    devicesList.add(device);
                    devicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (devicesList.isEmpty()) {
                    Toast.makeText(BluetoothAlpha.this, "No devices found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BluetoothAlpha.this, "Scanning finished", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered, ignore
            e.printStackTrace();
        }

        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                // Check if Bluetooth is in the STATE_ON before canceling discovery
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON && bluetoothAdapter.isDiscovering()) {
                    cancelBluetoothDiscovery();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_BLUETOOTH_SCAN:
            case REQUEST_BLUETOOTH_CONNECT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start Bluetooth discovery or connect to the device
                    if (requestCode == REQUEST_BLUETOOTH_SCAN) {
                        startBluetoothDiscovery();
                    } else if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
                        // Handle the case when connectToDevice() was called without Bluetooth connect permission
                        // You might want to reconnect to the selected device here.
                    }
                } else {
                    // Permission denied, show a message or handle accordingly
                    Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void next(View view) { startActivity(new Intent(this, WelcomeActivity.class));}

}
