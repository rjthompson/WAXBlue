package com.rt.WAXBlue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class ConnectedThread implements Runnable {

    private OutputStream outStream;
    private BufferedInputStream inStream;
    private BluetoothSocket socket;
    private volatile boolean running = true;
    private Writer writerThread1;
    private Writer writerThread2;
    private LinkedList<BufferWithSize> bigBuffer1 = new LinkedList<BufferWithSize>();
    private LinkedList<BufferWithSize> bigBuffer2 = new LinkedList<BufferWithSize>();

    private int id;
    private int rate;
    private static final String TAG = "Connected Thread";
    private static final boolean D = false;


    //TODO Write methods efficientise.

    /**
     * Constructor for performing socket read/write
     * @param socket Bluetooth socket
     */
    public ConnectedThread(BluetoothSocket socket, int id, File storageDirectory, String location, int rate) {

        this.socket = socket; //Bluetooth socket

        this.id = id; //ID of sensor-location pair

        this.rate = rate;

        Calendar c = Calendar.getInstance();

        File file = new File(storageDirectory + "/log_" + location + "_" + c.get(Calendar.DATE) + "_" + c.get(Calendar.MONTH) +
                "_" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + ".csv");

        writerThread1 = new Writer(file, bigBuffer1); //runnable to do the writing
        writerThread2 = new Writer(file, bigBuffer2); //runnable to do the writing

        if(D) Log.d(TAG, "Creating ConnectedThread");

        try {
            inStream = new BufferedInputStream(socket.getInputStream(), 2048);

            outStream = socket.getOutputStream();

            //bufferedWriter = new BufferedWriter(new FileWriter(file, true));

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

    public void setDataMode(boolean longMode){
        int mode = -1;
        if(longMode){
            mode = 128;
        }else{
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
            Log.e(TAG, "Interrupted");
        }
        running = false;
        writerThread1.shutdown();
        writerThread2.shutdown();

    }

    @Override
    public synchronized void run() {

        try{
            writerThread1.start();
            writerThread2.start();
            int bytes;
            boolean useBuffer1 = true;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep Interrupted");
            }
            setRate(rate);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep Interrupted");
            }


            setDataMode(true);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep Interrupted");
            }
            startStream();
            //TODO semaphore for all threads to go.
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep Interrupted");
            }

            while (running) {

                try {
                    byte[] buffer = new byte[1024];
                    bytes = inStream.read(buffer);

                    if(useBuffer1){

                        bigBuffer1.add(new BufferWithSize(buffer, bytes));

                        if(bigBuffer1.size() > 1000){
                            writerThread1.go();
                            useBuffer1 = false;
                        }



                    }else{

                        bigBuffer2.add(new BufferWithSize(buffer, bytes));

                        if (bigBuffer2.size() > 1000) {
                            writerThread2.go();
                            useBuffer1 = true;
                        }
                    }


                    //parseOutStream.write(buffer, 0, bytes);
                    //String data = new String(buffer, 0, bytes);



                    //bufferedWriter.append(data);

                    //TODO Check data against regex

                } catch (IOException e) {
                    break;
                }

            }
        }finally{

            try {
                outStream.flush();
                outStream.close();
                inStream.close();
            } catch (IOException e) {

            }

        }

    }

    public static class BufferWithSize {

        private byte[] buffer;
        private int size;

        public BufferWithSize(byte[] buffer, int size) {
            this.buffer = buffer;
            this.size = size;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public int getSize() {
            return size;
        }
    }
}
