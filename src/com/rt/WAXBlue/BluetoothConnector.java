package com.rt.WAXBlue;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

public class BluetoothConnector extends Thread {
    private DeviceConnection[] threads;

    private static final String TAG = "Bluetooth Connector";
    private static final boolean D = true;

    /**
     * Constructor
     * @param devices list of device names to be streamed from
     */
    public BluetoothConnector(List<DeviceToBeAdded> devices, File storageDirectory) {

        // Initialised later
        if(D) Log.d(TAG, "Devices: " + devices.toString());

        threads = new DeviceConnection[devices.size()];

        try {


            //Counter for Connection IDs
            int counter = 0;

            for(DeviceToBeAdded d : devices){

                BluetoothDevice device = d.getDevice();

                if (D) Log.d(TAG, "Attempting to create new Device Connection with " + device.getName());
                threads[counter] = new DeviceConnection(device, counter, storageDirectory);
                if (D) Log.d(TAG, "New Device Connections Created Successfully");
                counter++;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runThreads(){
        if(D) Log.d(TAG, "Running Threads");
        for (DeviceConnection thread : threads) {
            thread.run();
        }
    }

    public void stopThreads(){
        for (DeviceConnection thread : threads) {
            thread.stopStream();
        }
    }

}
