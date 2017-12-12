package com.example.averygrimes.phone_wallet_keys;

public class User {
    private String bName;
    private String bTime;
    private String bDate;
    private String bStatus;

    public User(String Dname, String Dtime, String Ddate, String Dstatus){
        bName = Dname;
        bTime = Dtime;
        bDate = Ddate;
        bStatus = Dstatus;
    }

    public String getbName(){
        return bName;
    }

    public String getbTime(){
        return bTime;
    }

    public String getbDate(){
        return bDate;
    }

    public String getbStatus(){ return bStatus; }
}