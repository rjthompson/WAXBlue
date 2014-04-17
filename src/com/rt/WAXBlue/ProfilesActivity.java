package com.rt.WAXBlue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.*;
import java.util.ArrayList;

/**
 * Author: Rob Thompson
 * Date: 13/04/2014
 */
public class ProfilesActivity extends Activity {

    private static final String TAG = "Profiles Activity";

    private static final String PROFILES = "WAX_Profiles.xml";  //name of file containing details of the profiles stored locally.



    private ArrayList<String> profileNames;                 //List of names of profiles currently held on device.
    private ArrayAdapter<String> profilesAdapter;           //Array Adapter for the display of the profile names.
    private ListView profilesListView;                      //List view to display the names of available profiles.

    private ArrayList<String> locationsList;                //List of locations in the currently selected profile.
    private ArrayAdapter<String> locationsAdapter;          //Array Adapter for the display of the locations.
    private ListView locationsListView;                     //List view to display the locations in the profile.

    private Profile current;                                //Currently selected profile.


    private FileOutputStream out;
    private FileInputStream in;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profiles);


        //Either open or create the profiles file
        try {
            out = openFileOutput(PROFILES, MODE_APPEND);
            in = openFileInput(PROFILES);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error with profile file: "+ e.getMessage());

        }

        int size = 0;
        byte[] buffer = new byte[size];

        try {
            size = in.available();

            if(size>0){
                in.read(buffer);
            }

        } catch (IOException e) {
            Log.e(TAG, "Failed Reading from profiles file: " + e.getMessage());
        }


        //If file was opened parse contents


        //populate list with available profiles


    }

    /**
     *Get a list of previously created profiles from local storage.
     * @return A list of previously created profiles.
     */
    private ArrayList<Profile> getProfiles(){

        ArrayList<Profile> profiles = null;

        return profiles;

    }

    /**
     * Highlights a profile in the list
     */
    private void highlightProfile(){

    }

    /**
     * Returns the locations associated with a profile passed as a parameter
     * @param p The profile to query
     * @return A List of the locations associated with the desired profile
     */
    private ArrayList<String> getCurrentLocations(Profile p){

        ArrayList<String> locations = null;

        return locations;

    }

    /**
     * Launch main activity once profile has been selected
     * @param v view element that was clicked. For OS use only.
     */
    private void launch(View v){

    }

    /**
     * Launch Activity to create a new profile.
     * @param v view element that was clicked. For OS use only.
     */
    public void createProfile(View v){

        Intent intent = new Intent(this, CreateProfileActivity.class);
        startActivity(intent);
    }

    /**
     * Launch Activity to edit an existing profile
     * @param v view element that was clicked. For OS use only.
     */
    private void editProfile(View v){

    }





}