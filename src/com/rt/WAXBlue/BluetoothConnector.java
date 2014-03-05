package com.rt.WAXBlue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class BluetoothConnector extends Thread {
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> waxDevices;
    private DeviceConnection[] threads;
    private static final String PREFIX = "";

    private static final String TAG = "Bluetooth Connector";
    private static final boolean D = true;

    /**
     * Constructor
     * @param devices list of device names to be streamed from
     */
    public BluetoothConnector(List<DeviceToBeAdded> devices, Context context) {

        // Initialised later
        this.mContext = context;

        if(D) Log.d(TAG, "Devices: " + devices.toString());

        threads = new DeviceConnection[devices.size()];

        try {
            if (connectionSuccessful()) {

                //Counter for Connection IDs
                int counter = 0;

                for(DeviceToBeAdded d : devices){

                    BluetoothDevice device = d.getDevice();

                    if (D) Log.d(TAG, "Attempting to create new Device Connection with " + device.getName());
                    threads[counter] = new DeviceConnection(device, counter, mContext);

                    counter++;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return true if connection is successful
     * @throws Exception
     */
    public boolean connectionSuccessful() throws Exception {

        // Fetch adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check that Bluetooth is supported on Android Device
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, "Bluetooth is not supported on this device", 0).show();
            //fail this shoudl really be done on app start up.
        }

        // Check if enabled


        // Fetch all paired devices
        Set<BluetoothDevice> pairedDevices = getPairedDevices();
        if (pairedDevices == null) {
        }

        // Find WAX devices
        if (waxDevices.isEmpty()) {
        }
        if (D) Log.d(TAG, "Connected Successfully to Adapter");
        return true;
    }

    /**
     * Returns paired WAX devices from the set of all devices
     * @param pairedDevices Set of paired Bluetooth devices
     * @return Set of paired WAX devices
     */
    private Set<BluetoothDevice> getWAXDevices(Set<BluetoothDevice> pairedDevices) {

        Set<BluetoothDevice> devices = new HashSet<BluetoothDevice>();
        for (BluetoothDevice d : pairedDevices) {
            if (d.getName().contains(PREFIX)) {
                devices.add(d);
                if (D) Log.d(TAG, d.getName() + " is paired to Phone");
            }
        }
        return devices;
    }

    /**
     * Returns the set of all paired Bluetooth devices
     * @return Set of all paired Bluetooth devices
     */
    private Set<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            return pairedDevices;
        }
        if (D) Log.e(TAG, "No Devices Paired");

        return null;
    }

    public void runThreads(){
        if(D) Log.d(TAG, "Running Threads");
        for(int i = 0; i < threads.length; i++){
            threads[i].run();
        }
    }

    public void stopThreads(){
        for (int i = 0; i < threads.length; i++) {
            threads[i].stopStream();
        }
    }

}
