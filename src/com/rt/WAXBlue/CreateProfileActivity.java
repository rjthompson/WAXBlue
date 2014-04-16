package com.rt.WAXBlue;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Author: Rob Thompson
 * Date: 16/04/2014
 *
 * Activity to facilitate the creation of a new profile
 */
public class CreateProfileActivity extends Activity {

    private Profile profile;                                            //The profile to be created
    private String name;                                                //The name of the profile to be created

    private ArrayList<String> locations;                                //Array list to hold the names of the locations
    private ArrayAdapter<String> locationsAdapter;                      //Array adapter for the display of the locations
    private ListView locationsListView;                                 //List view to display the locations as they are added/




    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    /**
     * Add a location to the profile
     * @param v View element that has been clicked. For OS use only
     */
    private void addLocation(View v){

    }

    /**
     * Removes the currently highlighted location
     * @param v View element that has been clicked. For OS use only
     */
    private void removeLocation(View v){

    }

    /**
     * Save the profile in it's current state.
     */
    private void saveProfile(){

    }

    /**
     * Cancels profile creation and exits
     */
    private void cancel(){

    }
}