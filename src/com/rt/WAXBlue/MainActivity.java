package com.rt.WAXBlue;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class MainActivity extends Activity {

    private static final String TAG = "Main Activity";          //Debugging tag
    private static final boolean D = true;                      //Flag to turn on or off debug logging

    private BluetoothConnector bluetoothConnector;              //Connector to set up and manage threads for BT devices
    private ListView pairedDeviceListView;                      //View to display devices that are paired with phone
    private GridView locationsGridView;                         //GridView to display the locations at which the devices will be attached
    private List<DeviceToBeAdded> addedDevicesList;             //List of devices to be connected to
    private List<String> pairedDevicesList;                     //List of device names paired with phone
    private ArrayList<String> locationsList;                    //ArrayList of locations to be passed to array adapter
    private Set<BluetoothDevice> pairedDevicesSet;              //Set of devices paired with phone
    private String[] locations = {                              //Array of locations to which the devices can be attached
            "Helmet", "Saddle", "Front Left", "Back Left",
            "Front Right", "Back Right"
    };
    private ArrayList<Integer> usedLocations;                   //Array list to hold indices of locations already assigned
    private ArrayAdapter<String> locationDisplayArrayAdapter;   //Array Adapter for GridView
    private ArrayAdapter<String> deviceDisplayArrayAdapter;     //Paired devices array adapter
    private File storageDirectory;                              //Directory to store output files
    private int selectedItem = -1;                              //Int representing which location has been selected
    private boolean locked = false;                             //Flag to indicate status of buttons
    private boolean selected = false;                           //Flag to indicate if any location is currently selected
    private static final int REQUEST_ENABLE_BT = 1;             //Int to allow for BT enabling request
    private int mode = 128;                                     //Output mode


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


        pairedDeviceListView = (ListView) this.findViewById(R.id.deviceListView);

        //Initialise Lists
        addedDevicesList = new ArrayList<DeviceToBeAdded>();
        pairedDevicesList = new ArrayList<String>();
        usedLocations = new ArrayList<Integer>();

        //Perform Bluetooth Checks, paired devices list must be initialised first.
        if (!checkBluetooth()) {
            finish();
            //TODO make sure no other options
        }

        //Create display adapter for paired Devices view
        deviceDisplayArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                pairedDevicesList);

        pairedDeviceListView.setAdapter(deviceDisplayArrayAdapter);
        pairedDeviceListView.getBackground().setAlpha(92);

        //Create the click functionality for the devices
        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check that a location is currently selected
                if (selectedItem != -1 && !locked && !usedLocations.contains(selectedItem)) {

                    //Initialise the TextView to control the text on the button
                    TextView locView = (TextView) locationsGridView.getChildAt(selectedItem);

                    //Un-highlight the button
                    locView.setBackgroundResource(R.drawable.grid_background_default);

                    //Update the button to show the device associated with that location
                    String s = locationsList.get(selectedItem);
                    locationsList.set(selectedItem, s + "\n" + pairedDevicesList.get(i));
                    locationDisplayArrayAdapter.notifyDataSetChanged();

                    /*Associate the selected device with the location by creating a new
                    DeviceToBeAdded instance, and adding it to addedDevicesList.*/
                    //Flag to indicate when device has been found in list
                    boolean found = false;
                    //loop through paired devices
                    for (BluetoothDevice d : pairedDevicesSet) {
                        if (d.getName().equals(pairedDevicesList.get(i)) && !found) {
                            //add device to added devices list, associate with location
                            addedDevicesList.add(new DeviceToBeAdded(d, locations[selectedItem]));
                            //set found flag
                            found = true;
                        }
                    }
                    if(D)Log.d(TAG, "Location: " + locations[selectedItem] + ", i: " + i+", Device: "+pairedDevicesList
                            .get(i));

                    //Remove the added device from the list of devices being displayed, device still exists
                    //in pairedDevicesSet
                    pairedDevicesList.remove(i);
                    deviceDisplayArrayAdapter.notifyDataSetChanged();
                    usedLocations.add(selectedItem);
                    //Deselect location
                    View v = locationsGridView.getChildAt(selectedItem);
                    deselectItem(v);
                }
            }
        });

        if(!isExternalStorageWritable()){
            displayToast("Cannot Write to External Storage :(");
            finish();
        }else{
            if (!createDirectoryForStorage()) {
                displayToast("Cannot Log Data");
                finish();
            }
        }

        locationsGridView = (GridView) findViewById(R.id.locationGridView);
        locationsList = new ArrayList<String>();
        Collections.addAll(locationsList, locations);
        locationDisplayArrayAdapter = new ArrayAdapter<String>(this, R.layout.centeredtext, locationsList);
        locationsGridView.setAdapter(locationDisplayArrayAdapter);

        locationsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!locked){
                    if(!selected){
                        selectItem(view, i);
                    }else if(selectedItem == i){
                        deselectItem(view);
                    }else{
                        //deselect current item
                        View preView = locationsGridView.getChildAt(selectedItem);
                        deselectItem(preView);
                        //select new item
                        selectItem(view, i);

                    }
                }
            }
        });



    }

    private boolean createDirectoryForStorage() {

        storageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/Data/");
        return storageDirectory.exists() || storageDirectory.mkdirs();
    }


    private void deselectItem(View view){
        view.setBackgroundResource(R.drawable.grid_background_default);
        selected = false;
        selectedItem = -1;
    }

    private void selectItem(View view, int item){
        view.setBackgroundResource(R.drawable.grid_background_highlighted);
        if(!selected){
            selectedItem = item;
            selected = true;
        }
    }
    /**
     * Checks that bluetooth is supported, enabled and that there are devices paired.
     * @return true if bluetooth connection successful
     */
    private boolean checkBluetooth() {

        //Retrieve adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check that Bluetooth is supported on Android Device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", 0).show();
            finish();
        }

        // Check if enabled
        if (!(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //Retrieve set of paired devices
        assert mBluetoothAdapter != null;
        pairedDevicesSet = mBluetoothAdapter.getBondedDevices();
        populatePairedDevices();

        //TODO if number of paired devices is less than number of locations, open up dialogue for discovery of new items

        if (D) Log.d(TAG, "Connected Successfully to Adapter");
        return true;
    }

    /**
     * Populates just the paired devices list from the set.
     */
    private void populatePairedDevices(){
        //Populate Array Adapter with devices for display in list view

        if (pairedDevicesSet.size() > 0) {
            for (BluetoothDevice d : pairedDevicesSet) {
                pairedDevicesList.add(d.getName());
            }
        }
    }

    public void clearClick(View v){

        //Clear old paired devices list
        pairedDevicesList.clear();
        //Repopulate with all paired devices
        populatePairedDevices();
        //Clear list of locations
        locationsList.clear();
        //Repopulate
        Collections.addAll(locationsList, locations);
        //Notify display adapters of changes
        deviceDisplayArrayAdapter.notifyDataSetChanged();
        locationDisplayArrayAdapter.notifyDataSetChanged();
        //Release all locations for use again
        usedLocations.clear();
        //Clear added devices list
        addedDevicesList.clear();
        //Deselect any location that might currently be selected
        if(selectedItem != -1){
            View selView = locationsGridView.getChildAt(selectedItem);
            deselectItem(selView);
        }
        //TODO more clear code
    }

    public void clearSelectedClick(View v){
        if(!selected){
            displayToast("Nothing to Clear");
        }else{
            TextView t = (TextView)locationsGridView.getChildAt(selectedItem);
            String deviceName = null;
            DeviceToBeAdded[] itList = addedDevicesList.toArray(new DeviceToBeAdded[0]);
            for(DeviceToBeAdded d : itList){
                String s = (String)t.getText();
                if(s.contains(d.getDeviceName())){
                    deviceName = d.getDeviceName();
                    pairedDevicesList.add(d.getDeviceName());
                    addedDevicesList.remove(d);
                }
            }
            for(int i = 0; i< locationsList.size(); i++){
                String s = locationsList.get(i);
                if(s.contains(deviceName) && deviceName != null){
                    locationsList.set(i, locations[selectedItem]);
                }
            }
            usedLocations.remove((Integer)selectedItem);
            deselectItem(t);
            deviceDisplayArrayAdapter.notifyDataSetChanged();
            locationDisplayArrayAdapter.notifyDataSetChanged();
        }
    }

    public void finishClick(View v){

        new AlertDialog.Builder(this)
                .setTitle("Finish")
                .setMessage("Are you sure you want to accept this pairing?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        prepForConnection();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.text:
                if (checked)
                    mode = 0;
                break;
            case R.id.textLong:
                if (checked)
                    mode = 128;
                break;
            case R.id.binary:
                if (checked)
                    mode = 1;
                break;
           /* case R.id.binLong:
                if (checked)
                    mode = 129;
                break;         */
            default:
                mode = -1;
                break;
        }
    }


    /**
     * Initiate Bluetooth connection
     *
     * @param v View
     */
    public void connectClick(View v) {

        int rate;
        //get rate from text input box
        EditText rateEntry = (EditText) findViewById(R.id.rateEntry);
        String rateText = rateEntry.getText().toString();
        if(!rateText.equals("")){
            rate = parseInt(rateText);
        }else{
            rate = 50;
        }

        //ensure mode has been set
        if(mode!=-1){
            //Get number of devices and initialise connection
            bluetoothConnector = new BluetoothConnector(addedDevicesList, storageDirectory, rate, mode);
        }else{
            displayToast("Please select an output mode");
        }

    }


    public void streamClick(View v) {
        if (D) Log.d(TAG, "Starting Stream");
        bluetoothConnector.runThreads();
    }

    public void stopClick(View v) {
        //TODO fix crash
        bluetoothConnector.stopThreads();
    }

    private void prepForConnection() {
        //Remove the paired devices list
        ((RelativeLayout) pairedDeviceListView.getParent()).removeView(pairedDeviceListView);

        hideSetupButtons();
        lockLocations();
        showConnectionButtons();

    }

    private void showConnectionButtons() {
        findViewById(R.id.connectButtonGroup).setVisibility(View.VISIBLE);
        findViewById(R.id.rateGroup).setVisibility(View.VISIBLE);
        findViewById(R.id.modeGroup).setVisibility(View.VISIBLE);
    }

    private void lockLocations(){
        for (int i = 0; i < locations.length; i++) {
            TextView locView = (TextView) locationsGridView.getChildAt(i);
            locView.setBackgroundResource(R.drawable.grid_background_locked);
            locView.setTextColor(Color.WHITE);
            locked = true;
        }
    }

    private void hideSetupButtons(){
        findViewById(R.id.clearButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.clearSelectedButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.finishButton).setVisibility(View.INVISIBLE);
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

}


