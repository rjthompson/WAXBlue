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

        while(true){
            doWrite();
        }



    }

    public void shutdown(){
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close buffered writer");
        }
    }

    private synchronized void doWrite(){

        if (!isRunning) {
            try {wait();} catch (InterruptedException e) {}

            toWrite = "";

            for (ConnectedThread.BufferWithSize b : bigBuffer) {
                String data = new String(b.getBuffer(), 0, b.getSize());
                toWrite = toWrite + data;
            }
            //Log.d(TAG, "Data: " + toWrite);
            try {
                //if (D) Log.d(TAG, "Writing");
                bufferedWriter.append(toWrite);
            } catch (IOException e2) {
                Log.e(TAG, "Failed Writing to file");
            }
            bigBuffer.clear();
            pause();
        }
    }

}
