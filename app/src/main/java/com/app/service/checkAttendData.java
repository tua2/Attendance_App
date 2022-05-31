package com.app.service;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class checkAttendData {
    String strCode, strLocation, strTchCode, strTchLocation;
    int dochinhxac = 4;
    int chenhlech = 10;
    int dis = 30;

    public checkAttendData(String strCode, String strLocation, String strTchCode, String strTchLocation) {
        this.strCode = strCode;
        this.strLocation = strLocation;
        this.strTchCode = strTchCode;
        this.strTchLocation = strTchLocation;
    }

    public boolean check() {
        if (checkCode() && checkLocation2()) return true;
        else
            return false;
    }

    public boolean checkCode() {
        if (strCode.equals(strTchCode)) return true;
        else
            return false;
    }

    public boolean checkLocation2(){

        String lat = strLocation.split(",")[0];
        String lng = strLocation.split(",")[1];
//        Log.d("CRE", "check " + lat.split("\\.")[0]);
        String latT = strTchLocation.split(",")[0];
        String lngT = strTchLocation.split(",")[1];

        Location mylocation = new Location("");
        Location dest_location = new Location("");
        String lat1 = latT;
        String lon1 = lngT;
        dest_location.setLatitude(Double.parseDouble(lat1));
        dest_location.setLongitude(Double.parseDouble(lon1));
        mylocation.setLatitude(Double.parseDouble(lat));
        mylocation.setLongitude(Double.parseDouble(lng));
        float distance = mylocation.distanceTo(dest_location);//in meters

        Log.d("LOCA",distance +"");
        if(distance < dis) return true;
        return false;

    }
}
