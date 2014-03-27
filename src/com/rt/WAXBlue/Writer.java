package com.rt.WAXBlue;

import android.util.Log;

import java.io.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

/**
 * Author: Rob Thompson
 * Date: 24/03/2014
 */
public class Writer extends Thread{

    private static final String TAG = "Writer Thread";
    private static final boolean D = true;

    private FileOutputStream fos;

    private LinkedList<byte[]> bigBuffer;
    private boolean isRunning = true;

    public Writer(File file, LinkedList<byte[]> bigBuffer){
        try {


            //bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            fos = new FileOutputStream(file, true);

        } catch (IOException e) {
            Log.e(TAG, "Failed to created File output stream: "+e.getMessage());
        }

        this.bigBuffer = bigBuffer;

    }

    @Override
    public synchronized void run() {
        /*
        long time = System.currentTimeMillis();
        try {
            fos.write((time+"\r\n").getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Write Error");
        }
        */


        while(isRunning){
            // Wait until we have data
            try {
                wait();

            } catch (InterruptedException e) {
            }

            // Write all the data we have
            for ( ; ; ) {




                byte[] buffer = null;

                // Safely get the next thing to write
                synchronized (bigBuffer) {
                    if (bigBuffer.size() > 0) {
                        buffer = bigBuffer.removeFirst();
                    }
                }

                // Break out when we have no more data
                if (buffer == null) {
                    break;
                }

                try {
                    fos.write(buffer);
                } catch (IOException e2) {
                    Log.e(TAG, "Failed Writing to file: "+ e2.getMessage());
                }

                //pause();
            }
        }



    }

    public synchronized void shutdown(){
       /*
        long time = System.currentTimeMillis();
        try {
            fos.write(("\r\n" + time).getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Write Error");
        } */
        isRunning = false;
        notify();

        try {

            fos.close();

        } catch (IOException e) {

            Log.e(TAG, "Failed to close file output stream: "+e.getMessage());

        }
    }
}
