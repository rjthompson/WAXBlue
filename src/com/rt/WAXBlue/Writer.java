package com.rt.WAXBlue;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Author: Rob Thompson
 * Date: 24/03/2014
 */
public class Writer extends Thread{

    private static final String TAG = "Writer Thread";
    private static final boolean D = true;

    private String toWrite;
    private volatile BufferedWriter bufferedWriter;
    private LinkedList<ConnectedThread.BufferWithSize> bigBuffer;
    private boolean isRunning=false;

    public volatile static byte[] buffer;

    public Writer(File file, LinkedList<ConnectedThread.BufferWithSize> bigBuffer){
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Log.e(TAG, "Failed to created Buffered Writer");
        }
        this.bigBuffer = bigBuffer;

    }

    public synchronized void go(){
        isRunning = true;
        notify();
    }

    public synchronized void pause(){

        isRunning = false;
    }

    @Override
    public void run() {

        while(isRunning){
            doWrite();
        }



    }

    public void shutdown(){
        try {
            this.notify();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close buffered writer");
        }
    }

    private synchronized void doWrite(){

        // Wait until we have data
        try {wait();} catch (InterruptedException e) {}

        // Write all the data we have
        for(;;) {
            ConnectedThread.BufferWithSize bws = null;

            // Safely get the next thing to write
            synchronized (bigBuffer) {
                if (bigBuffer.size() > 0) {
                    bws = bigBuffer.pop();
                }
            }

            // Break out when we have no more data
            if (bws == null) { break; }
            String data = new String(bws.getBuffer(), 0, bws.getSize());

            try {
                //if (D) Log.d(TAG, "Writing");
                bufferedWriter.append(data);
            } catch (IOException e2) {
                Log.e(TAG, "Failed Writing to file");
            }

            //pause();
        }
    }

}
