package com.rt.WAXBlue;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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
    private Context mContext;
    private final int id;
    private final int rate;
    private static final String TAG = "Device Connection";
    private static final boolean D = true;
    private String location;

    private static final String UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb";

    public DeviceConnection(BluetoothDevice waxDevice, final int id, File storageDirectory, String location, int rate) {
        if (D) Log.d(TAG, "\nConstructing DeviceConnection\nDevice: " + waxDevice.getName() + "\nID: "+id);
        this.storageDirectory = storageDirectory;
        this.waxDevice = waxDevice;
        this.id = id;
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
        connection = new ConnectedThread(mSocket, id, storageDirectory, location, rate);
        mConnected = new Thread(connection);
        mConnected.start();


    }


    public void stopStream(){
        connection.stopStream();
        try {
            mConnected.join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
