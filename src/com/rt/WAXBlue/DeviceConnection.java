package com.rt.WAXBlue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Attempt a new Bluetooth connection on a new thread
 * @author Rob Thompson
 *
 */
public class DeviceConnection{

    private BluetoothDevice waxDevice;
    private BluetoothSocket mSocket;
    private Thread mConnected;
    private File storageDirectory;
    private ConnectedThread connection;
    private final int rate;
    private static final String TAG = "Device Connection";
    private static final boolean D = true;
    private String location;
    private ReadyCounter ready;
    private static final String UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb";

    public DeviceConnection(BluetoothDevice waxDevice, final int id, File storageDirectory, String location, int rate, ReadyCounter ready) {
        if (D) Log.d(TAG, "\nConstructing DeviceConnection\nDevice: " + waxDevice.getName() + "\nID: "+id);

        this.ready = ready;
        this.storageDirectory = storageDirectory;
        this.waxDevice = waxDevice;
        this.location = location;
        Log.d(TAG, "Device Connection for " + waxDevice.getName() + " on " + location + " created.");
        this.rate = rate;
        mSocket = null;

    }


    public void init() {
        if (D) Log.d(TAG, "Running Device Connection");

        if(D) Log.d(TAG, "Device: " + waxDevice.getName());

        // Open Bluetooth socket
        try {
            if (D) Log.d(TAG, "Opening Socket with " + waxDevice.getName());

            mSocket = waxDevice.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING));
        } catch (IOException e) {
            if (D) Log.e(TAG, "Error opening Socket: "+e.getMessage());
        }


        try {
            mSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Error Connecting to Socket on "+waxDevice.getName() +": "+ e.getMessage());
            try {
                mSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "Error closing socket: " + e2.getMessage());
            }
            return;
        }

        // Start communication
        Log.d(TAG, "Creating Thread for " + location);
        //TODO put mode var in
        connection = new ConnectedThread(mSocket, storageDirectory, location, rate, 128, ready);
        mConnected = new Thread(connection);
        mConnected.start();


    }


    public void stopStream(){
        if(connection!=null)
            connection.stopStream();
        if(mConnected!=null) if (mConnected.isAlive()) {
            try {
                mConnected.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted whilst waiting for mConnected to terminate.");
            }
        }
    }

}
