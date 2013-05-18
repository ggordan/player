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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLInput;
import java.util.*;

public class TrackList extends Activity {

    // Define the media path ( Location of all the songs )
    final static String MEDIA_PATH = "/sdcard/Music/";
    // Define the artwork cache location
    final static String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.vevolt.player/cache/artwork/";
    MPDHelper dbHelper;
    SQLiteDatabase db;
    Preferences prefs;
    
    public TrackList(Context context ) {
        dbHelper = new MPDHelper(context);
        prefs = new Preferences(context);
    }

    public void loadTracks() {

        final MediaMetadataRetriever mmr = new MediaMetadataRetriever();            	
        SQLiteDatabase wdb = dbHelper.getReadableDatabase();

    	// Retrieve all the songs available on the device
        File file = new File(MEDIA_PATH);
        File list[] = file.listFiles();
        
        /*
         * Check if the tracks table already exists, and if it does empty it.
         */
        
        wdb.execSQL("DROP TABLE " + MPDHelper.TRACK_TABLE);
        wdb.execSQL("DROP TABLE " + MPDHelper.PLAYLIST_TABLE);
        wdb.execSQL("DROP TABLE " + MPDHelper.QUEUE_TABLE);
        wdb.execSQL(MPDHelper.TRACK_TABLE_SQL);
        wdb.execSQL(MPDHelper.PLAYLIST_TABLE_SQL);
        wdb.execSQL(MPDHelper.QUEUE_TABLE_SQL);
        wdb.close();
        
        /*
         * Check if the cache directory exists. If it doesn't, create it.
         */
        
    	if (!new File(CACHE_PATH).exists()) {
    		new File(CACHE_PATH).mkdirs(); 
    	}
    	
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();        	        
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inSampleSize = 8;  	
    	
        /*
         * Iterate through the song list, to load database, and extract metadata
         */
        for ( File song : list ) {
        	
            ContentValues values = new ContentValues();
            List<String> storedAlbums = new ArrayList<String>();
            
            /*
             * Set the data source for the MediaMetadataRetrievier to the current song in the list
             */
            Log.w("Song", song.toString());
            mmr.setDataSource(song.toString());
            values.put(MPDHelper.TRACK_TITLE, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            values.put(MPDHelper.TRACK_ARTIST, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            values.put(MPDHelper.TRACK_GENRE, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            
            final String imageLocation = CACHE_PATH 
            								+ mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).replaceAll("\\s","") 
            								+ mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).replaceAll("\\s", "");
            
            /*
             * Check if the current song has album artwork stored in its metadata 
             */
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
            	
            values.put(MPDHelper.TRACK_ALBUM, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            values.put(MPDHelper.TRACK_LOCATION, song.toString());
            values.put(MPDHelper.TRACK_LOVED, 0);
            values.put(MPDHelper.TRACK_PLAYS, 0);
            values.put(MPDHelper.TRACK_LAST_PLAYED, "");
            
            // Insert the data into the database
            db.insert(MPDHelper.TRACK_TABLE, null, values);                
        }
        
        ContentValues cv = new ContentValues();
        cv.put(MPDHelper.PLAYLIST_TITLE, "Loved");
        db.insert(MPDHelper.PLAYLIST_TABLE, null, cv);
        
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public List getSongListInfo(Integer songID) {

    	List<String> data = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT " + MPDHelper.TRACK_TITLE + ", " + MPDHelper.TRACK_ARTIST + ", " + MPDHelper.TRACK_ARTWORK + " FROM track WHERE Track_ID = '" + songID + "' LIMIT 1", null);

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

    	List<String> data = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Name, Artist, Album, Artwork, Plays, Loved, Genre, Last_Played, Track_ID FROM track WHERE Track_ID = '" + songID + "' LIMIT 1", null);

        cursor.moveToFirst();
        
        for (Integer i = 0; i < 9; i++)
        	data.add(cursor.getString(i));
        
        SQLiteDatabase.releaseMemory();
        db.close();
        return data;
    }        
    
    public String fetchSongLocation(Integer position) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + MPDHelper.TRACK_LOCATION + " FROM " + MPDHelper.TRACK_TABLE + " WHERE Track_ID = '" + position + "' LIMIT 1", null);
        
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
    	
    	SQLiteDatabase.releaseMemory();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track ORDER BY " + MPDHelper.TRACK_TITLE + " ASC ", null);
        List<Integer> songs = new ArrayList<Integer>();

        while (cursor.moveToNext())
            songs.add(cursor.getInt(0));
        
        db.close();
        return songs;
    }
    
    /*
     * Method to return query results
     */
    public List<Integer> returnQuery(String query) {
    	
    	SQLiteDatabase.releaseMemory();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track " +
        		"WHERE Name LIKE '%"+query+"%' OR Artist LIKE '%"+query+"%' OR Album LIKE '%"+query+"%' OR Genre LIKE '%"+query+"%' " +
        		" ORDER BY " + MPDHelper.TRACK_TITLE + " ASC ", null);
        List<Integer> songs = new ArrayList<Integer>();

        while (cursor.moveToNext())
            songs.add(cursor.getInt(0));
        
        db.close();
        return songs;
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
    
    public void incrementPlayCount(Integer SongID) {
    	
    	Integer playCount = Integer.parseInt(getSongDetail(SongID).get(4).toString());
    	Log.w("CurrentPlatCount", playCount + "");
    	
    	String strFilter = "Track_ID = " + SongID;
    	
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();   
    	ContentValues values = new ContentValues();
        values.put(MPDHelper.TRACK_PLAYS, playCount + 1);
        
        db.update(MPDHelper.TRACK_TABLE, values, strFilter, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
    
    /*
     * Method used for loving/unloving a track
     */
    public Boolean loveTrack(Integer SongID, String method) {
    	Integer status = Integer.parseInt(getSongDetail(SongID).get(5).toString());
    	
    	if (method == "get") {
    		if (status == 1) {
    			return true;
    		} else { return false; }
    	} else {
    	
    	String strFilter = "Track_ID = " + SongID;
    	
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();   
    	ContentValues values = new ContentValues();
    	
		if (status == 1) {
			values.put(MPDHelper.TRACK_LOVED, 0);
		} else { values.put(MPDHelper.TRACK_LOVED, 1); }
        
        db.update(MPDHelper.TRACK_TABLE, values, strFilter, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        
        return true;
    	}
    }
    
    /*
     * Handling Playlists
     */
    
    /**
     * Method to return all the playlists
     * 
     * @return
     */
    public List<String> returnPlaylists() {
    	
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Track_ID FROM track WHERE Loved = 1", null);
        List<String> playlists = new ArrayList<String>();

        while (cursor.moveToNext())
            playlists.add(cursor.getString(0));
        
        return playlists;
    }    
    public List<String> fetchQueue() {
    	
        db = dbHelper.getReadableDatabase();
        List<String> visibleQueue = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT Song_ID FROM queue", null);
        
        while (cursor.moveToNext()) {
        	String songName = (String) getSongDetail(Integer.parseInt(cursor.getString(0))).get(0);
        	visibleQueue.add(songName);
        }
        
        return visibleQueue;
    }        
}
