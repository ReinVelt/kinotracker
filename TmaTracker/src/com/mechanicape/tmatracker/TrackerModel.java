/**
 * 
 */
package com.mechanicape.tmatracker;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * @author rein
 *
 */
public final class TrackerModel implements LocationListener{

    public Location myLocation;
    public Location remoteLocation;
    private final Context mContext;
    protected LocationManager locationManager;
 // flag for GPS status
    public boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;
    public boolean isUpdatedSinceLast=true;
    public Location[] track=new Location[100];
 // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    /**
     * 
     */
    public TrackerModel(Context context) {
        // TODO Auto-generated constructor stub
        myLocation=new Location("me");
        myLocation.setLongitude(0);
        myLocation.setLatitude(0);
        myLocation.setBearing(0);
        remoteLocation=new Location("tracker");
        remoteLocation.setLongitude(0);
        remoteLocation.setLatitude(0);
        remoteLocation.setBearing(0);
        
        this.mContext = context;
        getLocation();
    }
    
    public void addNmeaData(String sentences[])
    {

        String sentence="";
        //Location previousLocation=this.remoteLocation;
        for (int lineNr=0;lineNr<sentences.length;lineNr++)
        {
            sentence=sentences[lineNr];
            //if (sentence.startsWith("$GPRMC")) {
                try 
                {
                    
                    String[] strValues = sentence.split(" ");
                    
                   
                    String datetime=strValues[0]+" "+strValues[1];
                    int age=Integer.parseInt(strValues[2]);
                    double latitude=this.parseNmeaLatitude(strValues[3]);
                    double longitude=this.parseNmeaLongitude(strValues[4]);
                    float course = Float.parseFloat(strValues[5]);
                    float speed = Float.parseFloat(strValues[6]);
                    int sats=Integer.parseInt(strValues[7]);
                    
                    if (latitude!=0 && longitude!=0)
                    {
                        remoteLocation.setLatitude(latitude);
                        remoteLocation.setLongitude(longitude);
                        remoteLocation.setBearing(course);
                        remoteLocation.setAccuracy(1);
                        
                    }
                    this.addToTrack(remoteLocation);
                    
                    //Toast.makeText(mContext, remoteLocation.toString(), Toast.LENGTH_SHORT).show();
                    System.out.println("remote::latlon="+Double.toString(latitude)+","+Double.toString(longitude));
                } 
                catch (Exception e)
                {
                }
                
                
                
                
            //}
        }
        
    }
    
    
    public void addToTrack(Location location)
    {
        /*int i=0;
        if (track.length>0)
        {
            for (i=1; i<this.track.length;i++)
            {
                this.track[i-1]=this.track[i];
            }
            this.track[this.track.length-1]=location;
        }
        else
        {
            this.track[i]=location;
        }*/
       
    }
    
    /**
     * Function to get the user's current location
     * 
     * @return
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    myLocation=null;
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    myLocation=null;
                    if (myLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //Toast.makeText(mContext, myLocation.toString(), Toast.LENGTH_LONG).show();
        return myLocation;
    }
    
    @Override
    public void onLocationChanged(Location location) {
        this.isUpdatedSinceLast=true;
        //Toast.makeText(mContext, location.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public double parseNmeaLatitude(String lat){
        double latitude = 0.0;
        if (lat != null  && !lat.equals("") ){
            double temp1 = Double.parseDouble(lat);
            double temp2 = Math.floor(temp1/100); 
            double temp3 = (temp1/100 - temp2)/0.6;
            latitude = (temp2+temp3);
        }
        return latitude;
    }
    
    public double parseNmeaLongitude(String lon){
        double longitude = 0.0;
        if (lon != null  && !lon.equals("") ){
            double temp1 = Double.parseDouble(lon);
            double temp2 = Math.floor(temp1/100); 
            double temp3 = (temp1/100 - temp2)/0.6;
            longitude = (temp2+temp3);
            
        }
        return longitude;
    }
   
   
    public byte computeChecksum(String s){
        byte checksum = 0;
        for (char c : s.toCharArray()){
            checksum ^= (byte)c;            
        }
        return checksum;
    }
}
