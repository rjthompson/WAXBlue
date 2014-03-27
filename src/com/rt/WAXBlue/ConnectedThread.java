package com.rt.WAXBlue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;
import java.util.Calendar;
import java.util.LinkedList;

public class ConnectedThread implements Runnable {

    private OutputStream outStream;
    private BufferedInputStream inStream;
    private BluetoothSocket socket;
    private volatile boolean running = true;
    private Writer writerThread1;
    private int mode;
    private LinkedList<byte[]> bigBuffer = new LinkedList<byte[]>();
    private ReadyCounter ready;
    private int rate;
    private static final String TAG = "Connected Thread";
    private static final boolean D = false;


    //TODO Write methods efficientise.

    /**
     * Constructor for performing socket read/write
     * @param socket Bluetooth socket
     */
    public ConnectedThread(BluetoothSocket socket, File storageDirectory, String location, int rate, int mode, ReadyCounter ready) {

        this.socket = socket; //Bluetooth socket
        this.mode = mode;
        this.rate = rate;
        this.ready = ready;

        Calendar c = Calendar.getInstance();
        location = location.replaceAll("\\s+", "");
        String fType;
        if(mode == 0 || mode == 128){
            fType = ".csv";
        }else{
            fType = "";
        }
        File file = new File(storageDirectory + "/log_" + location + "_" + c.get(Calendar.DATE) + "_" + c.get(Calendar.MONTH) +
                "_" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + fType);

        writerThread1 = new Writer(file, bigBuffer);

        if(D) Log.d(TAG, "Creating ConnectedThread");

        try {
            inStream = new BufferedInputStream(socket.getInputStream(), 2048);

            outStream = socket.getOutputStream();

        } catch (IOException e) {

            Log.e(TAG, "Couldn't construct thread: "+e.getMessage());
        }

    }

    public void setRate(int rate) {
        try {
            outStream.write(("rate x " + rate + "\r\n\r\n").getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: "+e.getMessage());
        }
    }

    public void setDataMode(int mode){

        if(!(mode == 0 || mode == 128 || mode == 1 || mode == 129)){
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
        try {
            outStream.write("\r\n".getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: " + e.getMessage());
        }
        try {
            Thread.sleep(200);
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close Socket: "+e.getMessage());
        } catch (InterruptedException e) {
        }
        running = false;

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted");
        }

        writerThread1.shutdown();

    }

    @Override
    public void run() {

        try{
            writerThread1.start();

            try {

                Thread.sleep(200);
                setRate(rate);
                Thread.sleep(200);
                setDataMode(mode);
                Thread.sleep(200);

                ready.decrement();
                while(ready.getValue()!=0){
                    Thread.sleep(100);
                }
                startStream();

                //TODO semaphore for all threads to go.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep Interrupted");
            }

            while (running) {
                byte[] buffer = new byte[1024];

                synchronized (writerThread1) {
                    bigBuffer.add(buffer);
                    writerThread1.notify();

                }
            }
        }finally{

            try {
                outStream.flush();
                outStream.close();
                inStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams");
            }

        }

    }

}
