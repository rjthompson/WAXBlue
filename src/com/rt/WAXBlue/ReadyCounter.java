package com.rt.WAXBlue;

/**
 * Author: Rob Thompson
 * Date: 27/03/2014
 */
public class ReadyCounter {

    private int value;

    public ReadyCounter(int initial){
        value = initial;
    }

    public synchronized void decrement(){
        if(value > 0)
            value--;
    }

    public synchronized int getValue(){
        return value;
    }





}
