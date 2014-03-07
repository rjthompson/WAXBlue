package com.rt.WAXBlue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;
import java.util.Calendar;

public class ConnectedThread implements Runnable {

    private OutputStream outStream;
    private InputStream inStream;
    private OutputStream parseOutStream;
    private File file;
    private BufferedWriter buf;
    private BluetoothSocket socket;
    private volatile boolean running = true;

    private int id;
    private int rate;
    private static final String TAG = "Connected Thread";
    private static final boolean D = true;


    //TODO Write methods efficientise.

    /**
     * Constructor for performing socket read/write
     * @param socket Bluetooth socket
     */
    public ConnectedThread(BluetoothSocket socket, int id, File storageDirectory, String location, int rate) {
        this.socket = socket;
        if(D) Log.d(TAG, "Creating ConnectedThread");
        try {
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();
            this.id = id;
            this.rate = rate;
            Calendar c = Calendar.getInstance();
            this.file = new File(storageDirectory + "/"+id+"log_" + location + "_"+ c.get(Calendar.DATE)+"_"+c.get(Calendar.DAY_OF_YEAR)+".csv");
            buf = new BufferedWriter(new FileWriter(file, true));

        } catch (IOException e) {
            Log.e(TAG, "Couldn't construct thread: "+e.getMessage());
        }


    }

    /**
     * Write bytes to socket output stream
     * @param bytes Bytes to write
     */
    public void write(byte[] bytes) {
        try {
            outStream.write(bytes);
            if(D) Log.d(TAG, "Writing: " + new String(bytes));
        } catch (IOException e) { }
    }

    public void setRate(int rate) {
        try {
            outStream.write(("rate x " + rate + "\r\n\r\n").getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to device: "+e.getMessage());
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
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close Socket: "+e.getMessage());
        }
        running = false;

    }

    @Override
    public synchronized void run() {

        byte[] buffer = new byte[2048];
        int bytes;
        setRate(rate);
        startStream();
        while (running) {
            try {

                bytes = inStream.read(buffer);

                //parseOutStream.write(buffer, 0, bytes);

                String data = new String(buffer, 0, bytes);

                buf.append(data);

                //TODO Check data against regex

            } catch (IOException e) {
                break;
            }

        }
        try{
            outStream.flush();
            outStream.close();
            inStream.close();
        }catch(IOException e){

        }
    }
}
