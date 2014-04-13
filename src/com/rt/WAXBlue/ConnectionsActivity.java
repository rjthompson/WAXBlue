package com.rt.WAXBlue;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

/**
 * Author: Rob Thompson
 * Date: 08/04/2014
 */
public class ConnectionsActivity extends Activity {

    private static final String TAG = "Connections Activity";   //Debugging tag
    private static final boolean D = true;                      //Flag to turn on or off debug logging

    private int mode = 128;                                     //Output mode
    private BluetoothConnector bluetoothConnector;              //Connector to set up and manage threads for BT devices
    private File storageDirectory;                              //Directory to store output files
    private ArrayList<String> locationsList;
    private ArrayList<DeviceToBeAdded> addedDevicesList;
    private GridView locationsGridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connections);

        Intent intent = getIntent();
        addedDevicesList= intent.getParcelableArrayListExtra(MainActivity.ADDED_DEVICE_LIST);
        locationsList = intent.getStringArrayListExtra(MainActivity.LOCATIONS_LIST);
        init();
    }

    //TODO FILL THESE IN
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init(){

        locationsGridView = (GridView) findViewById(R.id.connectionLocationGridView);
        ArrayAdapter<String> locationDisplayArrayAdapter = new ArrayAdapter<String>(this, R.layout.centeredtext, locationsList);
        locationsGridView.setAdapter(locationDisplayArrayAdapter);
        Log.d(TAG, locationsGridView.getChildCount()+ "");
        if (!isExternalStorageWritable()) {
            displayToast("Cannot Write to External Storage :(");
            finish();
        } else {
            if (!createDirectoryForStorage()) {
                displayToast("Cannot Log Data");
                finish();
            }
        }

    }

    /**
     * If directory does not exist, make it
     *
     * @return true if successful
     */
    private boolean createDirectoryForStorage() {
        //Default storage location is a directory called Data in downloads. Documents kept fucking around so wasn't used.
        storageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Data/");
        return storageDirectory.exists() || storageDirectory.mkdirs();
    }

    /**
     * Set the functionality associated with selecting a mode for the devices.
     *
     * @param view View element that was clicked
     */
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
           /* case R.id.binLong:                                       //Mode 129 not working atm :/
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
     * @param v View element that was clicked
     */
    public void connectClick(View v) {

        for(int i = 0; i < locationsGridView.getChildCount(); i++){
            TextView locationBox = (TextView) locationsGridView.getChildAt(i);
            locationBox.setTextColor(Color.WHITE);
            locationBox.setBackgroundResource(R.drawable.grid_background_locked);
        }

        //get rate from text input box
        int rate;
        EditText rateEntry = (EditText) findViewById(R.id.rateEntry);
        String rateText = rateEntry.getText().toString();
        //If rate is unset, default to 50Hz
        if (!rateText.equals("")) {
            rate = parseInt(rateText);
        } else {
            rate = 50;
        }

        //ensure mode has been set
        if (mode != -1) {
            //Get number of devices and initialise connection
            bluetoothConnector = new BluetoothConnector(addedDevicesList, storageDirectory, rate, mode);
        } else {
            displayToast("Please select an output mode");
        }
        //Set the streams running!
        if (D) Log.d(TAG, "Starting Stream");
        bluetoothConnector.runThreads();
    }


    /**
     * Stop the streams!!
     *
     * @param v Button that was clicked. Only used for OS functionality
     */
    public void stopClick(View v) {
        bluetoothConnector.stopThreads();
    }

    /**
     * Display string as toast
     *
     * @param s String toast text
     */
    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Checks if external storage is available for read and write
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

}
