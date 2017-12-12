package com.example.averygrimes.phone_wallet_keys;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.io.Serializable;
import java.util.Set;

public class DeviceModel implements Serializable
{
    private String name;
    private String status;
    
    public DeviceModel()
    {

    }
    
    public DeviceModel(String name, String status)
    {
        this.name = name;
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
