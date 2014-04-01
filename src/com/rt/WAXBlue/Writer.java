package com.rt.WAXBlue;

import android.util.Log;

import java.io.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;

/**
 * Author: Rob Thompson
 * Date: 24/03/2014
 */
public class Writer extends Thread {

    private static final String TAG = "Writer Thread";
    private static final boolean D = true;
    private volatile FileOutputStream fos;
    private volatile LinkedList<byte[]> bigBuffer;
    private volatile LinkedList<Integer> sizes;
    private boolean isRunning = true;
    private volatile boolean finished;

    public Writer(File file, LinkedList<byte[]> bigBuffer, LinkedList<Integer> sizes, boolean finished) {
        try {


            //bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            fos = new FileOutputStream(file, true);

        } catch (IOException e) {
            Log.e(TAG, "Failed to created Buffered Writer");
        }
        this.bigBuffer = bigBuffer;
        this.sizes = sizes;
        this.finished = finished;
    }

    public synchronized void go() {
        isRunning = true;
        notify();
    }

    @Override
    public synchronized void run() {

        long time = System.currentTimeMillis();
        try {
            fos.write((time + "\r\n").getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Write Error");
        }


        while (isRunning) {
            // Wait until we have data
            try {
                wait();

            } catch (InterruptedException ignored) {
            }

            // Write all the data we have
            for (; ; ) {


                byte[] buffer = null;
                int size = 0;
                // Safely get the next thing to write
                synchronized (bigBuffer) {
                    if (bigBuffer.size() > 0) {
                        buffer = bigBuffer.removeLast();
                        size = sizes.removeLast();
                    }
                }


                // Break out when we have no more data
                if (buffer == null) {
                    break;
                }
                buffer = Arrays.copyOf(buffer, size);

                try {
                    fos.write(buffer);
                } catch (IOException e2) {
                    Log.e(TAG, "Failed Writing to file: " + e2.getMessage());
                }

                //pause();
            }

        }

        time = System.currentTimeMillis();
        try {
            fos.write(("\r\n" + time).getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Write Error", e);
        }
        finished = true;
        notify();

    }

    public synchronized void shutdown() {

        isRunning = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted sleep");
        }
        try {
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to closed file output stream", e);
        }
        notify();
    }
}
