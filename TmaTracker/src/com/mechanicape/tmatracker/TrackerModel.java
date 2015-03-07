/**
 * 
 */
package com.mechanicape.tmatracker;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.mechanicape.tmatracker.DatabaseHelper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

/**
 * @author rein
 *
 */
public final class TrackerModel implements LocationListener{
    public DatabaseHelper db ;
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
        myLocation.setLongitude(0);
        myLocation.setLatitude(0);
        myLocation.setBearing(0);
        remoteLocation=new Location("tracker");
        remoteLocation.setLongitude(0);
        remoteLocation.setLatitude(0);
        remoteLocation.setBearing(0);
        db = new DatabaseHelper(context);
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
                    //int battery=Integer.parseInt(strValues[9].trim());
                    //Toast.makeText(mContext, String.valueOf(latitude), Toast.LENGTH_SHORT).show();
                    if (latitude!=0 && longitude!=0)
                    {
                        remoteLocation.setProvider(name);
                        remoteLocation.setTime(getLongDate(date+" "+time));
                        remoteLocation.setSpeed(speed);
                        remoteLocation.setLatitude(latitude);
                        remoteLocation.setLongitude(longitude);
                        remoteLocation.setBearing(course);
                        remoteLocation.setAccuracy((float)sats);
                        this.addToTrack(remoteLocation);
                        
                    }
                    
                    
                    //Toast.makeText(mContext, remoteLocation.toString(), Toast.LENGTH_SHORT).show();
                    //System.out.println("remote::latlon="+Double.toString(latitude)+","+Double.toString(longitude));
                } 
                catch (Exception e)
                {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                
                
                
                
            //}
        }
        
    }
    
    
    public void addToTrack(Location location)
    {
        db.addToTrail(location.getProvider(),location.getTime(), (float) location.getLatitude(), (float) location.getLongitude(), (float) location.getBearing(), (float) location.getSpeed(), 0, 0);
        
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
