package com.rt.WAXBlue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

/**
 * Author: Rob Thompson
 * Date: 16/04/2014
 *
 * Activity to facilitate the creation of a new profile
 */
public class CreateProfileActivity extends Activity {

    private static final boolean D = true;                              //Logging Flag
    private static final String TAG = "Create Profile";
    private Profile profile;                                            //The profile to be created
    private String locationName;

    private ArrayList<String> locations;                                //Array list to hold the names of the locations
    private ArrayAdapter<String> locationsAdapter;                      //Array adapter for the display of the locations
    private ListView locationsListView;                                 //List view to display the locations as they are added/

    private int selectedLocation;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile);

        //TODO fill in
        init();

    }

    /**
     * Initialisation method
     */
    private void init(){

        this.profile = new Profile();

        selectedLocation = -1;

        locations = new ArrayList<String>();

        locationsListView = (ListView) findViewById(R.id.profileLocationsListView);
        locationsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locations);
        locationsListView.setAdapter(locationsAdapter);

        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selectedLocation!=-1) {
                    locationsListView.getChildAt(selectedLocation).setBackgroundResource(Color.TRANSPARENT);
                }
                selectedLocation = i;
                locationsListView.getChildAt(i).setBackgroundResource(R.drawable.grid_background_highlighted);
            }
        });

    }

    /**
     * Add a location to the profile
     * @param v View element that has been clicked. For OS use only
     */
    public void addLocation(View v){

        if(D) Log.d(TAG, "Add Location Clicked");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Location");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                locationName = input.getText().toString();

                //add the input text to the array list of locations
                locations.add(locationName);

                //update the array adapter
                locationsAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    /**
     * Removes the currently highlighted location
     * @param v View element that has been clicked. For OS use only
     */
    public void deleteLocation(View v){
        if (selectedLocation != -1) {
            //Create confirmation dialogue
            new AlertDialog.Builder(this)
                    .setTitle("Finish")
                    .setMessage("Are you sure you want to delete this Location?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s;
                            s = (String) ((TextView) locationsListView.getChildAt(selectedLocation)).getText();
                            locations.remove(s);
                            locationsAdapter.notifyDataSetChanged();
                            selectedLocation = -1;                    }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }


    public void editLocation(View v){

        if (selectedLocation != -1) {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Location");

            // Set up the input
            final EditText input = new EditText(this);
            input.setText(((TextView)locationsListView.getChildAt(selectedLocation)).getText());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    locationName = input.getText().toString();

                    //add the input text to the array list of locations
                    locations.set(selectedLocation, locationName);

                    //update the array adapter
                    locationsAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }
        else{
            Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save the profile in it's current state.
     */
    public void saveProfile(View v){

        String name = (String)((TextView)findViewById(R.id.nameEntry)).getText();
        if(!name.equals("")){
            profile.setName(name);
            profile.setLocations((String[])locations.toArray());

            //WRITE TO FILE

        }else{
            //prompt name entry
            Toast.makeText(this, "Please Name your Profile", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cancels profile creation and exits
     * @param v View element that was clicked. For OS use only
     */
    public void cancel(View v){

        //Create confirmation dialogue
        new AlertDialog.Builder(this)
                .setTitle("Finish")
                .setMessage("Are you sure you want to cancel this entry?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Kill activity
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();

    }

}