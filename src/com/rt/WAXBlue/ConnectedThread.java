package com.rt.WAXBlue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;

public class ConnectedThread extends Thread {

    private InputStream inStream;
    private OutputStream outStream;
    private File file;
    private BufferedWriter buf;

    private int id;
    private static final String TAG = "Connected Thread";
    private static final boolean D = true;


    //TODO Write methods efficientise.

    /**
     * Constructor for performing socket read/write
     * @param socket Bluetooth socket
     */
    public ConnectedThread(BluetoothSocket socket, int id, File storageDirectory) {

        if(D) Log.d(TAG, "Creating ConnectedThread");
        try {
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();
            this.id = id;
            this.file = new File(storageDirectory + "/logID" + id + ".csv");
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
    }

    @Override
    public synchronized void run() {
        byte[] buffer = new byte[128];
        int bytes;
        while (true) {
            try {

                bytes = inStream.read(buffer);
                String data = new String(buffer, 0, bytes);

                buf.append(data);

                //TODO Check data against regex

            } catch (IOException e) {
                break;
            }

        }
    }
}
