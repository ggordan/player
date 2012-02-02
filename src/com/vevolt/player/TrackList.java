package com.vevolt.player;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLInput;
import java.util.*;

public class TrackList extends Activity {

    // the media path ( Location of all the songs )
    final static String MEDIA_PATH = "/sdcard/Music/";
    final static String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.vevolt.player/cache/artwork/";
    MPDHelper dbHelper;
    SQLiteDatabase db;
    Preferences prefs;
    
    public TrackList(Context context ) {
        dbHelper = new MPDHelper(context);
        prefs = new Preferences(context);
    }

    public void addEvent() {

        File file = new File(MEDIA_PATH);
        File list[] = file.listFiles();
        List songs = new ArrayList();
        SQLiteDatabase adb = dbHelper.getWritableDatabase();
        SQLiteDatabase wdb = dbHelper.getReadableDatabase();
        wdb.execSQL("DROP TABLE " + MPDHelper.TRACK_TABLE);
        wdb.execSQL(MPDHelper.TRACK_TABLE_SQL);
        wdb.close();
        
    	if (new File(CACHE_PATH).exists()) {
    		new File(CACHE_PATH).mkdirs();    		
    	}
    	
        final MediaMetadataRetriever mmr = new MediaMetadataRetriever();            	
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();        	        
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inSampleSize = 8;  	
    	
        // loop through songlist
        for ( File song : list ) {      	
            ContentValues values = new ContentValues();
            List storedAlbums = new ArrayList();
            
            mmr.setDataSource(song.toString());
        	Log.w("ProcessStart", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            values.put(MPDHelper.TRACK_TITLE, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            values.put(MPDHelper.TRACK_ARTIST, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            
           final String imageLocation = CACHE_PATH + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).replaceAll("\\s","") + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).replaceAll("\\s", "");
            
            if (!storedAlbums.contains(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))) {
	            if (mmr.getEmbeddedPicture() != null) {
							    	try {
						        	FileOutputStream f = new FileOutputStream(imageLocation);
						        	final Bitmap bmp = BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(), 0, mmr.getEmbeddedPicture().length);
									bmp.compress(Bitmap.CompressFormat.JPEG, 96, f);
									bmp.recycle();
								    } catch (Exception e) {
								        e.printStackTrace();
								 	}
						storedAlbums.add(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));	
						values.put(MPDHelper.TRACK_ARTWORK, imageLocation);
	            } else {
					values.put(MPDHelper.TRACK_ARTWORK, "");
	            }
            } else {
				values.put(MPDHelper.TRACK_ARTWORK, imageLocation);
            }
            	
        	Log.w("ProcessEnd", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            values.put(MPDHelper.TRACK_ALBUM, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            values.put(MPDHelper.TRACK_LOCATION, song.toString());
            values.put(MPDHelper.TRACK_LOVED, 0);
            values.put(MPDHelper.TRACK_PLAYS, 0);
            
            db.insert(MPDHelper.TRACK_TABLE, null, values);                  
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List getSongListInfo(Integer songID) {

    	List data = new ArrayList();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        //Log.w("track", "SELECT " + MPDHelper.TRACK_TITLE + ", " + MPDHelper.TRACK_ALBUM + ", " + MPDHelper.TRACK_ARTWORK + " FROM track WHERE Track_ID = '" + songID + "' LIMIT 1");        
        Cursor cursor = db.rawQuery("SELECT " + MPDHelper.TRACK_TITLE + ", " + MPDHelper.TRACK_ARTIST + ", " + MPDHelper.TRACK_ARTWORK + " FROM track WHERE Track_ID = '" + songID + "' LIMIT 1", null);
        startManagingCursor(cursor);

        cursor.moveToFirst();
        data.add(cursor.getString(0));
        data.add(cursor.getString(1));
        data.add(cursor.getString(2));
        db.setTransactionSuccessful();
        db.endTransaction();
        SQLiteDatabase.releaseMemory();
        return data;
    }

    public List getSongDetail(Integer songID) {

    	List data = new ArrayList();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Log.w("track", "SELECT " + MPDHelper.TRACK_TITLE + ", " + MPDHelper.TRACK_ALBUM + ", " + MPDHelper.TRACK_ARTWORK + " FROM track WHERE Track_ID = '" + songID + "' LIMIT 1");        
        Cursor cursor = db.rawQuery("SELECT Name, Artist, Album, Artwork, Plays, Loved FROM track WHERE Track_ID = '" + songID + "' LIMIT 1", null);
        startManagingCursor(cursor);

        cursor.moveToFirst();
        data.add(cursor.getString(0));
        data.add(cursor.getString(1));
        data.add(cursor.getString(2));
        data.add(cursor.getString(3));
        data.add(cursor.getString(4));        
        data.add(cursor.getString(5));
        SQLiteDatabase.releaseMemory();
        db.close();
        return data;
    }        
    
    public String fetchSongLocation(Integer position) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + MPDHelper.TRACK_LOCATION + " FROM " + MPDHelper.TRACK_TABLE + " WHERE Track_ID = '" + position + "' LIMIT 1", null);
        startManagingCursor(cursor);
        
        cursor.moveToFirst();
        Log.w("ReturnedLocation", cursor.getString(0));
        SQLiteDatabase.releaseMemory();
        return cursor.getString(0);
    }

    /**
     * Method to return the list of songs in the users library
     * 
     * @return
     */
    public List<Integer> returnSongs() {
    	
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track ORDER BY " + MPDHelper.TRACK_TITLE + " ASC ", null);
        List<Integer> songs = new ArrayList<Integer>();
        startManagingCursor(cursor);

        while (cursor.moveToNext()) {
            //Log.w("Song", cursor.getString(1) + " " + cursor.getInt(0));
            songs.add(cursor.getInt(0));
        }
        SQLiteDatabase.releaseMemory();
        return songs;
    }
    
    
    
    /**
     * Method for populating the song queue
     * 
     * @param songID The ID of the song that has been selected by the user.
     * 
     */
    public void populateSongQueue(Integer songID) {
    	SQLiteDatabase.releaseMemory();
    	ContentValues values = new ContentValues();
    	List<Integer> availableSongs = returnSongs();
    	List<Integer> songQueue = new ArrayList<Integer>();
    	Boolean inQueue = false;
    	Integer LoopCounter = 1;

    	db.execSQL("DROP TABLE queue");
        db.execSQL(MPDHelper.QUEUE_TABLE_SQL);    	
    	db.beginTransaction();

    	for (Integer song : availableSongs) {
    		
    		if (LoopCounter >= 26)
    			break;
    		
    		if ( inQueue == true ) {
    			if (LoopCounter <= 25) {
    				values = new ContentValues();
    				Log.w("InQueue", song + "");
    				values.put(MPDHelper.QUEUE_SONG_ID, song);
    				db.insert(MPDHelper.QUEUE_TABLE, null, values);
    		    	SQLiteDatabase.releaseMemory();
    	    		++LoopCounter;
    			}
    		}

    		if (song == (Integer) prefs.getCurrentActiveSong())
    			inQueue = true;
    	}
    	
    	db.setTransactionSuccessful();
    	db.endTransaction();
    	db.close();
    }
    
    
    
    /**
     * Method for retrieving the next song in the users queue
     * 
     */
    public Integer getNextSongInQueue() { 
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor cursor = db.rawQuery("SELECT Song_ID FROM " + MPDHelper.QUEUE_TABLE + "", null);
    	
    	while(cursor.moveToNext()) {
    		Log.w("Queued", cursor.getInt(0) + "");
    	}
    	
    	Log.w("NumRows", cursor.getCount() + "");
    	startManagingCursor(cursor);
    	cursor.moveToFirst();
    	Integer LoopCounter = 0;
    	Integer songID = cursor.getInt(0);
 
    	 SQLiteDatabase.releaseMemory();
    	 db.close();
    	 return songID;
    }
    
    
    
    /**
     * Method to return the number of songs the user has in their library
     * 
     * @return Integer 
     */
    public Integer getSongCount() {
    	
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + MPDHelper.TRACK_ID + " FROM " + MPDHelper.TRACK_TABLE + "", null);
        SQLiteDatabase.releaseMemory();
        Integer songcount = cursor.getCount();
        db.close();
        return songcount;
    }
    
}
