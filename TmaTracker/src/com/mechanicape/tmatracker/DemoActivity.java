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
 * Project home page: http://mechanicape.com/tmatracker/
 */

package com.mechanicape.tmatracker;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mechanicape.tmatracker.TrackerModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;;

/**
 * A sample Activity demonstrating USB-Serial support.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class DemoActivity extends Activity {

    private final String TAG = DemoActivity.class.getSimpleName();

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
    private GoogleMap map;
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
            DemoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DemoActivity.this.updateReceivedData(data);
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
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.setContentDescription("Map with location of tracked object");
        map.setTrafficEnabled(true);
        trackerModel=new TrackerModel(this);
        
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
        //Toast.makeText(this, "update", 0).show();
        try {
            
        
                //if (trackerModel.isUpdatedSinceLast)
                {
                    map.clear();
                  
                    
                    if(trackerModel.myLocation!=null)
                    {
                       //MarkerOptions myOptions=new MarkerOptions();
                        //myOptions.title("My");
                        //myOptions.position(new LatLng(trackerModel.myLocation.getLatitude(),trackerModel.myLocation.getLongitude()));
                        //myOptions.rotation(trackerModel.myLocation.getBearing());
                        //myOptions.flat(true);
                        //map.addMarker(myOptions);
                        //Toast.makeText(this, "my", 0).show();
                    }
                    
                    if (trackerModel.remoteLocation.getLatitude()!=0)
                    {
                        MarkerOptions remoteOptions=new MarkerOptions();
                        remoteOptions.title("Remote");
                        remoteOptions.position(new LatLng(trackerModel.remoteLocation.getLatitude(),trackerModel.remoteLocation.getLongitude()));
                        //String text="distance: "+Float.toString(trackerModel.myLocation.distanceTo(trackerModel.remoteLocation))+" meter\n";;
                        //remoteOptions.snippet(text);
                        remoteOptions.flat(false);
                        BitmapDescriptor remoteIcon=BitmapDescriptorFactory.fromResource(R.drawable.icon_targetlocation);
                        remoteOptions.icon(remoteIcon);
                        map.addMarker(remoteOptions);
                        //Toast.makeText(this, "remote", 0).show();
                    }
                    
                    //float toTargetDirection=trackerModel.myLocation.bearingTo(trackerModel.remoteLocation);
                    //float toTargetDistance=trackerModel.myLocation.distanceTo(trackerModel.remoteLocation);
                    LatLng cameraLatLng=new LatLng(trackerModel.myLocation.getLatitude(),trackerModel.myLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,17));
                    trackerModel.isUpdatedSinceLast=false;
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
        
        if(mDumpTextView.getText().length()>500)
        {
            String[] buffer=mDumpTextView.getText().toString().split("\n");
            if (buffer.length>0)
            {
                mDumpTextView.setText(buffer[buffer.length-1]);
                this.trackerModel.addNmeaData(buffer);
            }
            updateMap();
        }
        
        
    }

}
