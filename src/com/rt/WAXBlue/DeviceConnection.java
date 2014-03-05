package com.rt.WAXBlue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Attempt a new Bluetooth connection on a new thread
 * @author Rob Thompson
 *
 */
public class DeviceConnection implements Runnable {

    private BluetoothDevice waxDevice;
    private BluetoothSocket mSocket;
    private ConnectedThread mConnected;
    private Context mContext;
    private final int id;
    private static final String TAG = "Device Connection";
    private static final boolean D = true;


    private static final String UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb";

    public DeviceConnection(BluetoothDevice waxDevice, final int id, Context context) {
        if (D) Log.d(TAG, "\nConstructing DeviceConnection\nDevice: " + waxDevice.getName() + "\nID: "+id);
        this.mContext = context;
        this.waxDevice = waxDevice;
        this.id = id;
        mSocket = null;

    }

    @Override
    public void run() {
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
            Log.e(TAG, "Error Connecting to Socket: "+ e.getMessage());
            try {
                mSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "Error closing socket: " + e2.getMessage());
            }
            return;
        }

        // Start communication
        mConnected = new ConnectedThread(mSocket, id, mContext);
        mConnected.start();
        //mConnected.write("device\r\n\r\n".getBytes());
        mConnected.setRate(8);
        mConnected.startStream();
    }

    public void stopStream(){
        mConnected.stopStream();
    }

}
