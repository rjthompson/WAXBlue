package com.rt.WAXBlue;

import android.app.Activity;
import android.content.Context;
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

    private static final String TAG = "Profiles Activity";       //Logging tag
    private static boolean D = true;                             //Logging flag

    public static final String PROFILES = "WAX_Profiles.conf";  //name of file containing details of the profiles stored locally.


    private ArrayList<Profile> profilesList;                //List of profiles read from profiles file
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

        profileNames = new ArrayList<String>();
        profilesList = new ArrayList<Profile>();

        FileInputStream fis = null;
        FileOutputStream fos = null;

        //Either open or create the profiles file
        try {
            fis = this.openFileInput(PROFILES);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error load profiles file for reading: " + e.getMessage());

        }

        ObjectInputStream ois = null;

        //If file was opened parse contents
        try {
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //populate list with available profiles
        while(true){
            try {
                if (ois != null) {
                    profilesList.add((Profile) ois.readObject());
                }else{break;}
            } catch (EOFException e) {
                if(D) Log.d(TAG, "End of File");
                try {
                    ois.close();
                } catch (IOException e1) {
                    Log.e(TAG, e.getMessage());
                }
                break;

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        for(Profile p : profilesList){
            profileNames.add(p.getName());
        }
        profilesListView = (ListView) findViewById(R.id.profileListView);
        profilesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, profileNames);
        profilesListView.setAdapter(profilesAdapter);
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