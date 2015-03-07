package com.mechanicape.tmatracker;


//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
//import java.util.Locale;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
//@TODO - This file needs some refactoring. To many code in one file
//        better split it up...one file per model should be nice
//        but it works for now.
public class DatabaseHelper extends SQLiteOpenHelper {
	 
    // Logcat tag
    private static final String LOG = "DatabaseHelper";
 
    // Database Version
    private static final int DATABASE_VERSION = 3;
 
    // Database Name
    private static final String DATABASE_NAME = "tmatrackerdb";
 
    // Table Names
    // Common column names
    private static final String TABLE_TRAIL = "track";
    private static final String TRAIL_ID="id";
    private static final String TRAIL_TRACKERID="trackerId";
    private static final String TRAIL_TIMESTAMP = "timestamp";
    private static final String TRAIL_LATITUDE = "latitude";
    private static final String TRAIL_LONGITUDE = "longitude";
    private static final String TRAIL_COURSE = "course";
    private static final String TRAIL_SPEED = "speed";
    private static final String TRAIL_SATS = "satelliteCount";
    private static final String TRAIL_BATTERY ="battery";
 
   
   
 
    // Table Create Statements
    private static final String CREATE_TABLE_TRAIL = "CREATE TABLE "+ TABLE_TRAIL + "(" + 
            TRAIL_ID +          " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE," + 
            TRAIL_TIMESTAMP+    " TIMESTAMP NOT NULL," + 
            TRAIL_TRACKERID+    " STRING NOT NULL, "+
            TRAIL_LATITUDE +    " FLOAT NOT NULL,"+
            TRAIL_LONGITUDE+    " FLOAT NOT NULL,"  + 
            TRAIL_COURSE+       " FLOAT NOT NULL, "+
            TRAIL_SPEED +       " FLOAT NOT NULL," + 
            TRAIL_SATS +        " INTEGER NOT NULL,"+
            TRAIL_BATTERY+      " INTEGER NOT NULL"  + ")";
    
    
    private static final String INSERT_TEST_TRAIL="INSERT INTO "+TABLE_TRAIL+" VALUES (null,0,'Test',52.086335, 4.285606,0,0,0,0)";
   
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
 
        // creating required tables
        db.execSQL(CREATE_TABLE_TRAIL);
        db.execSQL(INSERT_TEST_TRAIL);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAIL);
        
        // create new tables
        onCreate(db);
    }
    
    
    
    
    
    
    //model helpers players
    /**
     * Creating a player
     */
    public long addToTrail(String trackerId, long timestamp, float lat, float lon, float course, float speed, int sats, int battery) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRAIL_TRACKERID, trackerId);
        values.put(TRAIL_TIMESTAMP, timestamp);
        values.put(TRAIL_TRACKERID, trackerId);
        values.put(TRAIL_LATITUDE, lat);
        values.put(TRAIL_LONGITUDE, lon);
        values.put(TRAIL_COURSE, course);
        values.put(TRAIL_SPEED, speed);
        values.put(TRAIL_SATS, sats);
        values.put(TRAIL_BATTERY, battery);
       
        // insert row
        long trail_id = db.insert(TABLE_TRAIL, null, values);
        return trail_id;
    }
 
    /**
     * get single player
     */
    /*public Player getPlayer(long player_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + PLAYER_ID + " = " + player_id;
        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null)
            c.moveToFirst();
        Player p = new Player();
        p.setId(c.getInt(c.getColumnIndex(PLAYER_ID)));
        p.setName((c.getString(c.getColumnIndex(PLAYER_NAME))));
        p.setHandicap(c.getInt(c.getColumnIndex(PLAYER_HANDICAP)));
        p.setKey(c.getInt(c.getColumnIndex(PLAYER_KEY)));
        p.setCreated(c.getInt(c.getColumnIndex(PLAYER_CREATED)));
        c.close();
        return p;
    } 
 
    /**
     * getting all players (why?)
     * *//*
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<Player>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS;
        Log.e(LOG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Player p = new Player();
                p.setId(c.getInt(c.getColumnIndex(PLAYER_ID)));
                p.setName((c.getString(c.getColumnIndex(PLAYER_NAME))));
                p.setHandicap(c.getInt(c.getColumnIndex(PLAYER_HANDICAP)));
                p.setKey(c.getInt(c.getColumnIndex(PLAYER_KEY)));
                p.setCreated(c.getInt(c.getColumnIndex(PLAYER_CREATED)));
                // adding to todo list
                players.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return players;
    }
    
    /**
     * getting all players belonging to group
     * */
    public List<LatLng> getTrailByName(String name) 
    {
        List<LatLng> trail = new ArrayList<LatLng>();
        String selectQuery = 
        	"SELECT  * FROM " + 
               TABLE_TRAIL +
            " WHERE "+ 
               TRAIL_TRACKERID + "='"+name+"'"+
            " ORDER BY "+TRAIL_TIMESTAMP +" DESC"+
            " LIMIT 100000";
        Log.e(LOG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        int cnt=0;
        if (c.moveToFirst()) {
            do {
                
                LatLng p = new LatLng(c.getDouble(c.getColumnIndex(TRAIL_LATITUDE)),c.getFloat(c.getColumnIndex(TRAIL_LONGITUDE)));
               
                
                // adding to todo list
                trail.add(cnt++,p);
            } while (c.moveToNext());
        }
        c.close();
        return trail;
    }
    
    
    

}




