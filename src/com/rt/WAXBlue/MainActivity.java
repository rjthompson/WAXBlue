package com.rt.WAXBlue;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private BluetoothConnector bConn;                           //Connector to set up and manage threads for BT devices
    private BluetoothAdapter mBluetoothAdapter;                 //Default Bluetooth adapter
    private Set<BluetoothDevice> pairedDevicesSet;              //Set of devices paired with phone
    private List<String> pairedDevicesList;                     //List of device names paired with phone
    private List<DeviceToBeAdded> addedDevicesList;             //List of devices to be connected to
    private ArrayAdapter<String> deviceDisplayArrayAdapter;     //Paired devices array adapter


    private GridView locationsGridView;                         //GridView to display the locations at which the devices will be attached
    private String[] locations = {                              //Array of locations to which the devices can be attached
            "Helmet", "Saddle", "Front Left", "Back Left",
            "Front Right", "Back Right"
    };
    private ArrayList<String> locationsList;                    //ArrayList of locations to be passed to array adapter
    private ArrayAdapter<String> locationDisplayArrayAdapter;   //Array Adapter for GridView

    private int selected = -1;                                  //Int representing which location has been selected
    private static final int REQUEST_ENABLE_BT = 1;             //Int to allow for BT enabling request

    private static final String TAG = "Main Activity";          //Debugging tag
    private static final boolean D = true;                      //Flag to turn on or off debug logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    /**
     * Initialise Activity variables
     */
    private void init(){

        ListView pairedDeviceListView = (ListView) this.findViewById(R.id.deviceListView);

        addedDevicesList = new ArrayList<DeviceToBeAdded>();

        pairedDevicesList = new ArrayList<String>();

        deviceDisplayArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevicesList);

        pairedDeviceListView.setAdapter(deviceDisplayArrayAdapter);
        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check that a location is currently selected
                if (selected != -1) {

                    //Initialise the TextView to control the text on the button
                    TextView locView = (TextView) locationsGridView.getChildAt(selected);

                    //Un-highlight the button
                    locView.setBackgroundResource(R.drawable.gridbackground);

                    //Update the button to show the device associated with that location
                    String s = locationsList.get(selected);
                    locationsList.set(selected, s + "\n" + pairedDevicesList.get(i));
                    locationDisplayArrayAdapter.notifyDataSetChanged();

                    //Associate the selected device with the location by creating a new
                    //DeviceToBeAdded instance, and adding it to addedDevicesList.
                    boolean found = false;
                    for (BluetoothDevice d : pairedDevicesSet) {
                        if (d.getName().equals(pairedDevicesList.get(i)) && !found) {
                            if (D) Log.d(TAG, "Adding Device: " + d.getName() + " to added devices list");
                            addedDevicesList.add(new DeviceToBeAdded(d, locations[selected]));
                            found = true;
                        }
                    }
                    //Remove the added device from the list of devices being displayed, device still exists
                    //in pairedDevicesSet
                    pairedDevicesList.remove(i);

                    selected = -1;
                }
            }
        });

        if(!isExternalStorageWritable()){
            displayToast("Cannot Write to External Storage :(");
            finish();
        }

        //Fetch Bluetooth Adapter and run BT queries
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        queryBT(mBluetoothAdapter);

        locationsGridView = (GridView) findViewById(R.id.locationGridView);
        locationsList = new ArrayList<String>();
        Collections.addAll(locationsList, locations);
        locationDisplayArrayAdapter = new ArrayAdapter<String>(this, R.layout.centeredtext, locationsList);
        locationsGridView.setAdapter(locationDisplayArrayAdapter);

        locationsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selected != i){
                    view.setBackgroundResource(R.drawable.gridbackgroundhigh);
                    selected = i;
                }

            }
        });



    }

    private void queryBT(BluetoothAdapter btA){
        if (!btA.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //Retrieve set of paired devices
        pairedDevicesSet = mBluetoothAdapter.getBondedDevices();
        populatePairedDevices();
    }

    private void populatePairedDevices(){
        //Populate Array Adapter with devices for display in list view

        if (pairedDevicesSet.size() > 0) {
            for (BluetoothDevice d : pairedDevicesSet) {
                pairedDevicesList.add(d.getName());
                //deviceDisplayArrayAdapter.add(d.getName());
            }
        }
    }

    /**
     * Initiate Bluetooth connection
     * @param v View
     */
    public void startConnect(View v) {
        // Get number of devices
        // Start bluetooth connection
        bConn = new BluetoothConnector(pairedDevicesList, getApplicationContext());
        bConn.start();
    }

    public void startStream(View v){
        if(D) Log.d(TAG, "Starting Stream");
        bConn.runThreads();
    }

    public void stopStream(View v){
        bConn.stopThreads();
    }

    public void clearClick(View v){
        pairedDevicesList.clear();
        populatePairedDevices();
        locationsList.clear();
        for (int i = 0; i < locations.length; i++) {
            locationsList.add(locations[i]);
        }
        deviceDisplayArrayAdapter.notifyDataSetChanged();
        locationDisplayArrayAdapter.notifyDataSetChanged();
        //TODO more clear code
    }

    public void finishClick(View v){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Display string as toast
     * @param s String toast text
     */
    public void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public class DeviceToBeAdded{

        private BluetoothDevice d;
        private String location;

        public DeviceToBeAdded(BluetoothDevice d, String location){
            this.d = d;
            this.location = location;
        }

        public String getDeviceName(){
            return d.getName();
        }

        public String getDeviceAddress(){
            return d.getAddress();
        }

        public String getLocation(){
            return this.location;
        }

        @Override
        public String toString(){
            return this.getDeviceName() + ", " + this.getDeviceAddress() + ", " + this.getLocation();
        }

    }
}


