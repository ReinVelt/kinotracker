/**
 * 
 */
package com.mechanicape.kinotracker;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import com.mechanicape.kinotracker.DatabaseHelper;

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
    public DatabaseHelper db ;
    public Location myLocation;
    public Map<String, Location> trackerLocations = new HashMap<String, Location>();
    private final Context mContext;
    protected LocationManager locationManager;
 // flag for GPS status
    public boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;
    String debugRSSI;
    String debugTDM[];
    String debugCFG[];
    public boolean isUpdatedSinceLast=true;
    //public Location[] track=new Location[100];
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
       //myLocation.setLongitude(0);
        //myLocation.setLatitude(0);
       // myLocation.setBearing(0);
      
        db = new DatabaseHelper(context);
        this.mContext = context;
        getLocation();
    }
    
    public void addDebugData(String sentence)
    {
       if (sentence.length()>1 && sentence.charAt(0)=='L')
       {   
           debugRSSI=sentence;
           Toast.makeText(mContext, "DEBUG:"+sentence, Toast.LENGTH_SHORT).show();
       }
      
    }
    
    public void addNmeaData(String sentence)
    {

        

                try 
                {
                    
                    String[] strValues = sentence.split("\t");  
                       String date=strValues[0].trim();
                        String time=strValues[1].trim();
                        int age=Integer.parseInt(strValues[2].trim());
                        String name=strValues[3].trim();
                        double latitude=this.parseNmeaLatitude(strValues[4].trim());
                        double longitude=this.parseNmeaLongitude(strValues[5].trim());
                        float course = Float.parseFloat(strValues[6].trim());
                        float speed = Float.parseFloat(strValues[7].trim());
                        int sats=Integer.parseInt(strValues[8].trim());
                        int battery=Integer.parseInt(strValues[9].trim());
                         if (latitude!=0 && longitude!=0)
                        {
                            Location remote=new Location(name);
                            remote.setProvider(name);
                            remote.setTime(getLongDate(date+" "+time));
                            remote.setSpeed(speed);
                            remote.setLatitude(latitude);
                            remote.setLongitude(longitude);
                            remote.setBearing(course);
                            remote.setAccuracy((float)sats);
                            trackerLocations.put(name, remote);
                            this.addToTrack(name,getLongDate(date+" "+time),(float)latitude,(float)longitude,speed,course,sats,battery);
                            
                        }
                    
                    
                    
                    //System.out.println("remote::latlon="+Double.toString(latitude)+","+Double.toString(longitude));
                } 
                catch (Exception e)
                {
                    //Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                
                
                
       
        
    }
    
    
    public void addToTrack(String name,long date,float latitude,float longitude,float speed,float course,int sats,int battery)
    {
        db.addToTrail(name,date,latitude, longitude, course, speed, sats, battery);
        
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
        return Double.parseDouble(lat);
    }
    
    public double parseNmeaLongitude(String lon){
         return Double.parseDouble(lon);
    }
   
    public static Long getLongDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //This creates a date object from your string
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //getTime() always returns milliseconds since 01/01/1970
        return date.getTime();
    }
   
}
