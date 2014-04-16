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
 * Activity to facilitate the editing of profiles.
 */
public class EditProfileActivity extends Activity {

    private Profile profile;                                //Profile to be edited.

    private ArrayList<String> locations;                    //Locations associated with this profile
    private ArrayAdapter<String> locationsAdapter;          //Array adapter for the display of the locations
    private ListView locationsListView;                     //List view to display the locations


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Populates the List view with locations associated with this profile.
     */
    private void populateLocations(){


    }

    /**
     * Highlights a location in the list view
     */
    private void highlightLocation(){

    }

    /**
     * Edits the name of the highlighted location through a dialogue
     */
    private void editLocation(){

    }

    /**
     * Edit name of the profile
     */
    private void editName(){

    }

    /**
     * Adds a new location to the profile
     * @param v View element that has been clicked. For OS use only
     */
    private void addLocation(View v){

    }

    /**
     * Removes the location from the profile
     */
    private void deleteLocation(){

    }

    /**
     * Deletes the profile from local storage
     */
    private void deleteProfile(){

    }

    /**
     * Saves the profile to local storage
     */
    private void saveProfile(){

    }

    /**
     * Saves the profile as a new instance in the local storage
     */
    private void saveProfileAs(){

    }

}