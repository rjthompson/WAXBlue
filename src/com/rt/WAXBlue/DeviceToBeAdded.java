package com.rt.WAXBlue;

import android.bluetooth.BluetoothDevice;

/**
 * Author: Rob Thompson
 * Date: 05/03/2014
 */
public class DeviceToBeAdded {

    private BluetoothDevice d;
    private String location;

    public DeviceToBeAdded(BluetoothDevice d, String location) {
        this.d = d;
        this.location = location;
    }

    public BluetoothDevice getDevice(){
        return this.d;
    }

    public String getDeviceName() {
        return d.getName();
    }

    public String getDeviceAddress() {
        return d.getAddress();
    }

    public String getLocation() {
        return this.location;
    }

    @Override
    public String toString() {
        return this.getDeviceName() + ", " + this.getDeviceAddress() + ", " + this.getLocation();
    }

}
