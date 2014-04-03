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

    private static final String TAG = "Device Connection";   //Logging tag
    private static final boolean D = false;                  //Logging flag

    private static final String UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb";     //UUID of device

    private BluetoothDevice waxDevice;          //The device to connect to
    private BluetoothSocket mSocket;            //The devices Bluetooth socket
    private Thread mConnected;                  //A thread to handle the connection once established
    private ConnectedThread connection;         //Runnable implementation for the connection

    private File storageDirectory;              //Directory to store log files
    private String location;                    //Location at which the device will be attached
    private final int rate;                     //Rate at which the device should stream
    private int mode;                           //Mode of streaming for device
    private ReadyCounter ready;                 //Semaphore to indicate that all devices are ready to start streaming

    /**
     *
     * @param waxDevice         Device to connect to
     * @param id                Id of connection
     * @param storageDirectory  Directory for the storage of log files
     * @param location          Location at which device will be attached
     * @param rate              Rate at which device will stream
     * @param mode              Mode in which device will stream
     * @param ready             Semaphore to indicate device is ready to stream
     */
    public DeviceConnection(BluetoothDevice waxDevice, final int id, File storageDirectory, String location, int rate,
                            int mode, ReadyCounter ready) {

        if (D) Log.d(TAG, "Constructing device connection on: " + waxDevice.getName() + " ID: "+id);

        this.waxDevice = waxDevice;
        this.mode = mode;
        this.rate = rate;
        this.ready = ready;
        this.storageDirectory = storageDirectory;
        this.location = location;

        mSocket = null;
    }

    /**
     * Initialise the connection to the device
     */
    public void init() {
        if (D) Log.d(TAG, "Running Device Connection: " + waxDevice.getName());

        // Open device's Bluetooth socket
        try {
            if (D) Log.d(TAG, "Opening Socket with " + waxDevice.getName());

            mSocket = waxDevice.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING));

        } catch (IOException e) {
            if (D) Log.e(TAG, "Error opening Socket: "+e.getMessage());
        }

        //Establish connection to socket
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

        //Create connection runnable
        connection = new ConnectedThread(mSocket, storageDirectory, location, rate, mode, ready);
        //Create thread using connection runnable
        mConnected = new Thread(connection);
        mConnected.start();


    }

    /**
     * Stop the device from streaming and close the connection;
     */
    public void stopStream(){

        //Ensure the connection exists
        if(connection!=null){
            //Call stop stream on the connection thread
            connection.stopStream();
        }

        //Hold until the connection thread has finished. TODO Check!!
        if(mConnected!=null) if (mConnected.isAlive()) {
            try {
                mConnected.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted whilst waiting for mConnected to terminate.");
            }
        }
    }

}
