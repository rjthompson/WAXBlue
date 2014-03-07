package com.rt.WAXBlue;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author: Rob Thompson
 * Date: 07/03/2014
 */
public class ParserThread implements Runnable {

    private WriterThread wt;
    private InputStream in;
    private OutputStream out;

    public ParserThread(byte[] buffer){

       // wt = new WriterThread(parse(buffer));



    }

    private String parse(byte[] buffer){


        return "";
    }


    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
