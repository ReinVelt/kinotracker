/* Copyright 2015 Theo's Mechanische Aap.
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the GNU  General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: http://mechanicape.com/kinotracker/
 */

package com.mechanicape.kinotracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.usb.UsbManager;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mechanicape.kinotracker.TrackerModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * The main map with your location and the location of the dogs
 *
 * @author Rein Velt (rein@mechanicape.com)
 */
public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    /**
     * The device currently in use, or {@code null}.
     */
    private UsbSerialDriver mSerialDevice;

    /**
     * The system's USB service.
     */
    private UsbManager mUsbManager;
    public TrackerModel trackerModel;
    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;
    private ListView mTrackerList;
    private GoogleMap map;
    private int RSSICounter=0;
    private String[] receiveBuffer;
   /// public static final  int COMM_STATE_GPS=1;
   // public static final int COMM_STATE_AT=2;
    //public static final int COMM_STATE_SUB_RSSI=1;
    //public static final int COMM_STATE_SUB_TDM=2;
    //public static final int COMM_STATE_SUB_CFG=3;
    //private MyLocationOverlay myLocationOverlay;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.updateReceivedData(data);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mDumpTextView = (TextView) findViewById(R.id.demoText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        mTrackerList=(ListView) findViewById(R.id.trackerList);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (map!=null)
        {
            map.setMyLocationEnabled(true);
            map.setIndoorEnabled(true);
            map.setContentDescription("Kinotracker shows a map with the locations of tracked objects");
            map.setBuildingsEnabled(true);
            map.setTrafficEnabled(true);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            trackerModel=new TrackerModel(this);
            drawHistoryTrail();
            
            mTrackerList.setOnItemClickListener(new OnItemClickListener() {
    
    
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    
                    String selectedTrackerName=(String) parent.getItemAtPosition(position);        
                    Location thisLocation=trackerModel.trackerLocations.get(selectedTrackerName);
                    LatLng cameraLatLng=new LatLng(thisLocation.getLatitude(),thisLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,17));
                   
                }
    
            });
        }
        else
        {
            
            Toast.makeText(this.getApplicationContext(), "could not load google Maps", Toast.LENGTH_LONG).show();
        }
        
    }
    
    public void getRssiData()
    {
        
        try {
            if (mSerialDevice!=null)
            {
               
             
                mSerialDevice.write("+++".getBytes(), 500);
                Thread.sleep(1500);
               
                //comm_state_sub=COMM_STATE_SUB_CFG;
                //Thread.sleep(1000);
                //mSerialDevice.write("ATI5\r\n".getBytes(),500);
                //Thread.sleep(1000);
                //getdata rssi
              
                mSerialDevice.write("ATI7\r\n".getBytes(),500);
                Thread.sleep(1000);
                
                
                //getdata tdm
                
                mSerialDevice.write("ATI6\r\n".getBytes(),500);
                Thread.sleep(1000);
                mSerialDevice.write("ATO\r\n".getBytes(),500);
                //Thread.sleep(500);
              
            }
        }
        catch(Exception e)
        {
            Toast.makeText(this.getApplicationContext(), "WRITE:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
       
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (mSerialDevice != null) {
            try {
                mSerialDevice.close();
            } catch (IOException e) {
                // Ignore.
            }
            mSerialDevice = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //myLocationOverlay.enableMyLocation();
        mSerialDevice = UsbSerialProber.acquire(mUsbManager);
        Log.d(TAG, "Resumed, mSerialDevice=" + mSerialDevice);
        if (mSerialDevice == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            try {
                mSerialDevice.open();
                mSerialDevice.setBaudRate(57600);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    mSerialDevice.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialDevice = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + mSerialDevice);
           
           
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mSerialDevice != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mSerialDevice, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    
    private void updateMap()
    {
        Toast.makeText(this, "update", Toast.LENGTH_SHORT).show();
        try {
            
        
                //if (trackerModel.isUpdatedSinceLast)
                {
                    map.clear();
                  
                    
                    if(trackerModel.myLocation!=null)
                    {
                       /*MarkerOptions myOptions=new MarkerOptions();
                       myOptions.title("My");
                       myOptions.position(new LatLng(trackerModel.myLocation.getLatitude(),trackerModel.myLocation.getLongitude()));
                       myOptions.rotation(trackerModel.myLocation.getBearing());
                       myOptions.flat(false);
                       BitmapDescriptor myIcon=BitmapDescriptorFactory.fromResource(R.drawable.icon_mylocation);
                       myOptions.icon(myIcon);
                      
                       map.addMarker(myOptions);
                       Toast.makeText(this, "my", 0).show();*/
                    }
                    
                    ArrayList<String> myStringArray1 = new ArrayList<String>();
                    
                    Iterator<Entry<String, Location>> it = trackerModel.trackerLocations.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        String name=(String) pair.getKey();
                        Location trackerLocation=(Location)pair.getValue();
                        //System.out.println(pair.getKey() + " = " + pair.getValue());
                        //it.remove(); // avoids a ConcurrentModificationException
                    
                        if (trackerLocation.getLatitude()!=0)
                        {                                                                                                                                                                                                                                                                                                                                   
                            MarkerOptions remoteOptions=new MarkerOptions();
                            remoteOptions.title(trackerLocation.getProvider());
                            remoteOptions.position(new LatLng(trackerLocation.getLatitude(),trackerLocation.getLongitude()));
                            String text=trackerLocation.getProvider();;
                            remoteOptions.snippet(text);
                            //remoteOptions.rotation(trackerLocation.getBearing());
                            remoteOptions.flat(false);
                            BitmapDescriptor remoteIcon=BitmapDescriptorFactory.fromResource(R.drawable.icon_targetlocation);
                            remoteOptions.icon(remoteIcon);
                            map.addMarker(remoteOptions);
                            myStringArray1.add(name);
                            //Toast.makeText(this, "remote", 0).show();
                        }
                        ArrayAdapter<String> adapter;
                        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myStringArray1);  
                        mTrackerList.setAdapter(adapter);
                        //mTrackerList.addView(myListView);
                    }
             
                    
                    //float toTargetDirection=trackerModel.myLocation.bearingTo(trackerModel.remoteLocation);
                    //float toTargetDistance=trackerModel.myLocation.distanceTo(trackerModel.remoteLocation);
                    //LatLng cameraLatLng=new LatLng(trackerLocation.getLatitude(),trackerLocation.getLongitude());
                    //CameraPosition.builder(cameraLatLng).bearing(trackerModel.myLocation.getBearing()).build();
                   // map.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,17));
                    trackerModel.isUpdatedSinceLast=false;
                    
                    drawHistoryTrail();
                }
        }
        catch (Exception e)
        {
            
        }
       
    }

    private void updateReceivedData(byte[] data) {
        Charset charset = Charset.forName("UTF-8"); 
           mDumpTextView.append(new String(data,charset));
        
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
        
        if(mDumpTextView.getText().length()>500 )
        {
            receiveBuffer=mDumpTextView.getText().toString().split("\n");
            if (receiveBuffer.length>0)
            {
                String[] fields=null;
                mDumpTextView.setText("\n"+receiveBuffer[receiveBuffer.length-1]+"\n");
                for (int i=0;i<receiveBuffer.length-1;i++)
                {
                    fields=receiveBuffer[i].split("\t");
                    if (fields.length>0 && fields.length<12 )
                    {
                        if (fields.length==11)
                        {
                            this.trackerModel.addNmeaData(receiveBuffer[i]); 
                            RSSICounter++;
                            
                        }
                        else
                        {
                            this.trackerModel.addDebugData(receiveBuffer[i]);
                            mTitleTextView.setText(this.trackerModel.debugRSSI);
                        }
                    }
                }
               
               
                updateMap();
                if (RSSICounter>30)
                {
                    this.getRssiData();
                    RSSICounter=0;
                }
            }
           
        }
        
       
    }
    
    
    private void drawHistoryTrail()
    {
        List<LatLng> trail=trackerModel.db.getTrailByName("Kino");
        Polyline line = map.addPolyline(new PolylineOptions()
        .addAll(trail)
        .width(1)
        .color(Color.RED));
        
    
    
    }

}
