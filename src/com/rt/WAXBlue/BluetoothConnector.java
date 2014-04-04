package com.rt.WAXBlue;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.io.File;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Class to handle and coordinate multiple device connections
 */
public class BluetoothConnector{

    private static final String TAG = "Bluetooth Connector";   //Logging tag
    private static final boolean D = true;                     //Logging flag

    private DeviceConnection[] connections;                    //Array of all connections to be made
    private CyclicBarrier ready;                               //Barrier to signal devices are ready to stream


    /**
     *
     * @param devices List of devices to be connect to
     * @param storageDirectory Path to the storage directory
     * @param rate Sampling rate (Hz)
     * @param mode Output format
     */
    public BluetoothConnector(List<DeviceToBeAdded> devices, File storageDirectory, int rate, int mode) {

        if(D) Log.d(TAG, "Devices: " + devices.toString());

        //Initialise ready semaphore
        ready = new CyclicBarrier(devices.size());

        //Initialise connections array
        connections = new DeviceConnection[devices.size()];

        //Counter for Connection IDs
        int counter = 0;

        //Loop through all devices to be connected to
        for(DeviceToBeAdded d : devices){

            BluetoothDevice device = d.getDevice();

            if (D) Log.d(TAG, "Attempting to create new Device Connection with " + device.getName() + " on " + d.getLocation());

            //Create a new connection and add it to the array.
            connections[counter] = new DeviceConnection(device, counter, storageDirectory, d.getLocation(), rate, mode, ready);

            if (D) Log.d(TAG, "New Device Connections Created Successfully");

            counter++;
        }


    }

    /**
     * Initialise all connections
     */
    public void runThreads(){

        if(D) Log.d(TAG, "Initialising connections");

        for (DeviceConnection connection : connections) {
            connection.init();
        }
    }

    /**
     * Stop streams on all connections    TODO change to semamphore!
     */
    public void stopThreads(){

        for (DeviceConnection connection : connections) {
            connection.stopStream();
        }
    }
}
