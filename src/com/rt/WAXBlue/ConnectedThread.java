package com.rt.WAXBlue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.LinkedList;

public class ConnectedThread implements Runnable {

    private static final String TAG = "Connected Thread";
    private static final boolean D = false;
    private final Writer writerThread1;
    private OutputStream outStream;
    private BufferedInputStream inStream;
    private BluetoothSocket socket;
    private volatile boolean running = true;
    private int mode;
    private volatile LinkedList<byte[]> bigBuffer = new LinkedList<byte[]>();
    private volatile LinkedList<Integer> sizes = new LinkedList<Integer>();

    private ReadyCounter ready;
    private int rate;


    //TODO Write methods efficientise.

    /**
     * Constructor for performing socket read/write
     *
     * @param socket Bluetooth socket
     */
    public ConnectedThread(BluetoothSocket socket, File storageDirectory, String location, int rate, int mode, ReadyCounter ready) {

        this.socket = socket; //Bluetooth socket
        this.mode = mode;     //Streaming mode
        this.rate = rate;     //Sampling rate
        this.ready = ready;   //ready semaphore

        Calendar c = Calendar.getInstance();

        location = location.replaceAll("\\s+", "");

        String fType;

        if (mode == 0 || mode == 128) {
            fType = ".csv";
        } else {
            fType = "";

        }
        int month = c.get(Calendar.MONTH);
        month++;

        File file = new File(storageDirectory + "/log_" + location + "_" + c.get(Calendar.DATE) + "_" + month +
                "_" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + fType);

        writerThread1 = new Writer(file, bigBuffer, sizes);

        if (D) Log.d(TAG, "Creating ConnectedThread");

        try {
            inStream = new BufferedInputStream(socket.getInputStream(), 2048);

            outStream = socket.getOutputStream();

        } catch (IOException e) {

            Log.e(TAG, "Couldn't construct thread: " + e.getMessage());
        }

    }


    public void setRate(int rate) {
        try {
            outStream.write(("rate x " + rate + "\r\n\r\n").getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: " + e.getMessage());
        }
    }

    public void setDataMode(int mode) {

        if (!(mode == 0 || mode == 128 || mode == 1 || mode == 129)) {
            mode = 0;
        }

        try {
            outStream.write(("datamode " + mode + "\r\n\r\n").getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: " + e.getMessage());
        }
    }

    public void startStream() {
        try {
            outStream.write("STREAM=1\r\n".getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: " + e.getMessage());
        }

    }

    public void stopStream() {

        //TODO make sure time to finish

        try {
            outStream.write("\r\n".getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: " + e.getMessage());
        }
        try {
            Thread.sleep(1000);
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close Socket: " + e.getMessage());
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted");
        }
        running = false;
        writerThread1.shutdown();

    }

    @Override
    public void run() {
        int bytes = 0;
        writerThread1.start();

        try {
            Thread.sleep(100);
            Thread.sleep(100);
            setRate(rate);
            Thread.sleep(100);
            setDataMode(mode);
            Thread.sleep(100);

            ready.decrement();
            while (ready.getValue() != 0) {
                Thread.sleep(100);
            }
            startStream();

            //Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep Interrupted");
        }

        while (running) {

            byte[] buffer = new byte[1024];

            try {
                bytes = inStream.read(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Failed to read: " + e.getMessage(), e);
            }

            synchronized (writerThread1) {
                if (bytes > 0) {
                    bigBuffer.add(buffer);
                    sizes.add(bytes);
                    writerThread1.notify();
                }
            }
        }


        try {
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing streams");
        }


    }

}
