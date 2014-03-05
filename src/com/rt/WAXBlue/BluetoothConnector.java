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

public class BluetoothConnector extends Thread {
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> waxDevices;
    private List<String> devices;
    private ExecutorService pool;
    private DeviceConnection[] threads;
    private static final String BLUETOOTH_UNSUPPORTED = "Bluetooth unsupported";
    private static final String BLUETOOTH_DISABLED = "Bluetooth disabled";
    private static final String NO_PAIRED_DEVICES = "No paired devices";
    private static final String NO_CWA_DEVICES = "No paired CWA devices";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String PREFIX = "";

    private static final String TAG = "Bluetooth Connector";
    private static final boolean D = true;

    /**
     * Constructor instantiates thread pool
     * @param devices list of device names to be streamed from
     *                TODO MAKE WORK?!?!
     */
    public BluetoothConnector(List<String> devices, Context context) {
        // Initialised later
        this.mContext = context;
        this.devices = devices;
        if(D) Log.d(TAG, "Devices: " + devices.toString());
        // Pool improves efficiency when managing and tracking multiple threads
        //pool = Executors.newFixedThreadPool(devices.size());

        threads = new DeviceConnection[devices.size()];

        try {
            if (connectionSuccessful()) {
                int counter = 0;
                Iterator<BluetoothDevice> itr = waxDevices.iterator();
                for(int i = 0; i < waxDevices.size(); i++){
                    BluetoothDevice d;
                    if(itr.hasNext()){
                        d = itr.next();
                    }else{
                        continue;
                    }
                    if(!(devices.contains(d.getName()))){
                        if(D) Log.d(TAG, d.getName()+" paired but not required");
                        continue;
                    }
                    if (D) Log.d(TAG, "Attempting to create new Device Connection with " + d.getName());

                    // pool.execute(new DeviceConnection(d, counter));

                    threads[counter] = new DeviceConnection(d, counter, mContext);
                    if(D) Log.d(TAG, "Created new Device Connection with " + d.getName());
                    //threads[i].run();
                    counter++;
                }

//                for (String s : devices) {
//                    Log.d(TAG, waxDevices.toString());
//					pool.execute(new DeviceConnection(s, waxDevices, counter));
//					counter++;
//				}
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
        if (mBluetoothAdapter == null) {
            throw new Exception(BLUETOOTH_UNSUPPORTED);
        }

        // Check if enabled


        // Fetch all paired devices
        Set<BluetoothDevice> pairedDevices = getPairedDevices();
        if (pairedDevices == null) {
            throw new Exception(NO_PAIRED_DEVICES);
        }

        // Find WAX devices
        waxDevices = getWAXDevices(pairedDevices);
        if (waxDevices.isEmpty()) {
            throw new Exception(NO_CWA_DEVICES);
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
