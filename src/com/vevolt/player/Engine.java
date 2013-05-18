package com.vevolt.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Engine extends Activity {
	
    MPDHelper dbHelper;
    SQLiteDatabase db;
    Preferences prefs;
    TrackList TL;
    final static String MEDIA_PATH = "/sdcard/Music/";
    
	public Engine(Context context) { 
        dbHelper = new MPDHelper(context);
        prefs = new Preferences(context);
        TL = new TrackList(context);
	}
	
    /*
     * Returns the tracks with least amount of listens based on genre
     */
    public List<Integer> returnSongsbyListens(String order) {
    	
        List<Integer> songs = new ArrayList<Integer>();
        db = dbHelper.getReadableDatabase();
    	
        Cursor cursor = db.rawQuery("SELECT * FROM track ORDER BY " + MPDHelper.TRACK_PLAYS + " " + order + " LIMIT 50 ", null);

        while (cursor.moveToNext())
            songs.add(cursor.getInt(0));

        Collections.shuffle(songs);
        return songs;
    }
    
    public List<Integer> getLovedTracks() {
    	
        List<Integer> songs = new ArrayList<Integer>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track WHERE Loved = 1 LIMIT 100 ", null);

        while (cursor.moveToNext())
            songs.add(cursor.getInt(0));
        
        Collections.shuffle(songs);
        return songs;    	
    }
	
	public List<Integer> recommendedSongs(Integer i) {
		
		// Stores all the available songs
		HashMap<Integer, Integer> songStore = new HashMap<Integer, Integer>();
		Integer LoopCounter = 1;
		List<Integer> lovedWithSameGenre = new ArrayList<Integer>(); // weight 10
		List<Integer> popularWithSameGenre = new ArrayList<Integer>(); // weight 4
		List<Integer> unpopularWithSameGenre = new ArrayList<Integer>(); // weight 5
		List<Integer> PopularSongs = returnSongsbyListens("DESC"); // weight 1
		List<Integer> UnPopularSongs = returnSongsbyListens("ASC"); // weight 2
		List<Integer> lovedSongs = getLovedTracks(); // weight 3
		List<Integer> recommendedSongs = new ArrayList<Integer>();
		
		List<?> detail = TL.getSongDetail(i);
		String trackGenre = detail.get(6).toString();

		// Returned the loved songs with the same genre
		for ( Integer song : lovedSongs ) {
			List<?> jdetail = TL.getSongDetail(song);
			String genre = jdetail.get(6).toString();
			if ( genre.indexOf( trackGenre ) > -1 )
				lovedWithSameGenre.add(song);
		}
		
		// Returned the popular songs with the same genre
		for ( Integer song : PopularSongs ) {
			List<?> jdetail = TL.getSongDetail(song);
			String genre = jdetail.get(6).toString();
			if ( genre.indexOf( trackGenre ) > -1 )
				popularWithSameGenre.add(song);
		}
		
		// Returned the popular songs with the same genre
		for ( Integer song : UnPopularSongs ) {
			List<?> jdetail = TL.getSongDetail(song);
			String genre = jdetail.get(6).toString();
			if ( genre.indexOf( trackGenre ) > -1 )
				unpopularWithSameGenre.add(song);
		}
		
		for (Integer j = 0; j <= 5; j++) {
			List<Integer> current = new ArrayList<Integer>();
			Integer weight = 0;
			
			switch (j) {
				case 0:
					current = UnPopularSongs;
					weight = 1;
					break;
				case 1:
					current = PopularSongs;
					weight = 2;
					break;
				case 2:
					current = lovedSongs;
					weight = 3;
					break;
				case 3:
					current = unpopularWithSameGenre;
					weight = 0;
					break;
				case 4:
					current = popularWithSameGenre;
					weight = 5;
					break;
				case 5:
					current = lovedWithSameGenre;
					weight = 10;
			}
			
			for ( Integer item : current) {
				songStore.put(item, weight);
			}
		}
		
		
		HashMap sortedSongStore = sortByWeight(songStore);
		
		Iterator shift = songStore.entrySet().iterator();
		Integer counter = 1;
		Boolean start = false;
		HashMap<Integer, Integer> recommended = new HashMap<Integer, Integer>(); 
		List<Integer> occupied = new ArrayList<Integer>();
		
	    while (shift.hasNext()) {
	        Map.Entry pairs = (Map.Entry) shift.next();
	        recommendedSongs.add(Integer.parseInt(pairs.getKey().toString()));
	        shift.remove(); // avoids a ConcurrentModificationException
	        songStore.remove(pairs.getKey());
	    }
	    
		return recommendedSongs;
	}
	
	public void populateLovedSongQueue(Integer songID) {
	
	 	SQLiteDatabase.releaseMemory();
    	ContentValues values = new ContentValues();
    	
    	// get the recommended songs to play from the recommendation engine
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Track_ID FROM track WHERE Loved = 1", null);
        List<String> playlists = new ArrayList<String>();

        while (cursor.moveToNext())
            playlists.add(cursor.getString(0));

    	db.execSQL("DROP TABLE IF EXISTS " + MPDHelper.QUEUE_TABLE);
        db.execSQL(MPDHelper.QUEUE_TABLE_SQL);    	
    	db.beginTransaction();
    	Boolean a = false;
    	
    	for (String song : playlists) {
    	
			values = new ContentValues();
			
	 		if (!a) {
				values.put(MPDHelper.QUEUE_SONG_ID, prefs.getCurrentActiveSong());
				values.put(MPDHelper.QUEUE_ACTIVE, 1);
				prefs.setActiveSongSongQueueID(1);
				db.insert(MPDHelper.QUEUE_TABLE, null, values);
		    	SQLiteDatabase.releaseMemory();
		    	a = true;
    		} else {
    		
				values.put(MPDHelper.QUEUE_SONG_ID, song);
				db.insert(MPDHelper.QUEUE_TABLE, null, values);
		    	SQLiteDatabase.releaseMemory();
    		}
    	}
    	db.setTransactionSuccessful();
    	db.endTransaction();
    	db.close();
	}
	
		    
	/**
     * Method for populating the song queue. Populates the first 25 songs, to ensure that SQL queries execute promptly
     * 
     * @param songID The ID of the song that has been selected by the user.
     * 
     */
    public void populateSongQueue(Integer songID) {
    	
    	SQLiteDatabase.releaseMemory();
    	ContentValues values = new ContentValues();
    	
    	// get the recommended songs to play from the recommendation engine
    	List<Integer> availableSongs = recommendedSongs(songID);
    	
    	Boolean inQueue = false;
    	Integer LoopCounter = 1;

    	/*
    	 * Clear the queue table (it is only generated once the user selects a song)
    	 */
    	
    	db.execSQL("DROP TABLE IF EXISTS " + MPDHelper.QUEUE_TABLE);
        db.execSQL(MPDHelper.QUEUE_TABLE_SQL);    	
    	db.beginTransaction();

    	Log.w("songstotal", availableSongs.size() + "");
    	
    	Collections.shuffle(availableSongs);
    	Boolean a = false;
    	
    	for (Integer song : availableSongs) {

    		values = new ContentValues();

    		
    		if (!a) {
				values.put(MPDHelper.QUEUE_SONG_ID, prefs.getCurrentActiveSong());
				values.put(MPDHelper.QUEUE_ACTIVE, 1);
				prefs.setActiveSongSongQueueID(1);
				db.insert(MPDHelper.QUEUE_TABLE, null, values);
		    	SQLiteDatabase.releaseMemory();
		    	a = true;
    		} else {
    		
				values.put(MPDHelper.QUEUE_SONG_ID, song);
				db.insert(MPDHelper.QUEUE_TABLE, null, values);
		    	SQLiteDatabase.releaseMemory();
    		}
    		
//    		if ( inQueue == true ) {
//    			if (LoopCounter <= availableSongs.size()) {
//    				values = new ContentValues();
//    				Log.w("InQueue", song + "");
//    				values.put(MPDHelper.QUEUE_SONG_ID, song);
//    				db.insert(MPDHelper.QUEUE_TABLE, null, values);
//    		    	SQLiteDatabase.releaseMemory();
//    	    		++LoopCounter;
//    			}
//    		}
//
//    		if (song == (Integer) prefs.getCurrentActiveSong()) {
//    			inQueue = true;
//				values.put(MPDHelper.QUEUE_SONG_ID, song);
//				values.put(MPDHelper.QUEUE_ACTIVE, 1);
//				prefs.setActiveSongSongQueueID(1);
//				db.insert(MPDHelper.QUEUE_TABLE, null, values);
//		    	SQLiteDatabase.releaseMemory();
//    		}
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
    	SQLiteDatabase.releaseMemory();
    	
    	// Check songs manually added to database
    	Cursor cursor = db.rawQuery("SELECT Queue_ID FROM " + MPDHelper.QUEUE_TABLE + " WHERE Active = 1 LIMIT 1", null);
    	Log.w("queucount", cursor.getCount() + "");
    	
    	cursor.moveToFirst();
    	Integer currentActive = cursor.getInt(0);

    	// Check songs manually added to database
    	Cursor manualQueue = db.rawQuery("SELECT Queue_ID FROM " + MPDHelper.QUEUE_TABLE + " WHERE Manual = 1", null);
    	
    	if (manualQueue != null && manualQueue.moveToFirst()) {
    		
    		ContentValues args = new ContentValues();
    		Integer firstManual = manualQueue.getInt(0);
    		
    		Cursor newCursor = db.rawQuery("SELECT Song_ID FROM " + MPDHelper.QUEUE_TABLE + " WHERE Queue_ID = " + firstManual + " LIMIT 1", null);
        	newCursor.moveToFirst();
        	Integer songID = newCursor.getInt(0);
    		
    		String whereClause = "Queue_ID=" + firstManual;
    		db.delete(MPDHelper.QUEUE_TABLE, whereClause, null);
    		db.close();
    		return songID;
    	}
    	
    	Integer nextSong = currentActive + 1;
    			
    	Cursor newCursor = db.rawQuery("SELECT Queue_ID, Song_ID FROM " + MPDHelper.QUEUE_TABLE + " WHERE Queue_ID > " + currentActive + " LIMIT 1", null);
    	ContentValues args = new ContentValues();
    	
    	newCursor.moveToFirst();

    	try {
    		newCursor.getInt(0);
    	} catch (CursorIndexOutOfBoundsException E) {
    		return prefs.getCurrentActiveSong();
    	}
    	
    	String strFilter = "Queue_ID=" + (currentActive);
    	args.put("Active", 0);
    	db.update(MPDHelper.QUEUE_TABLE, args, strFilter, null);
    	strFilter = "Queue_ID=" + nextSong;
    	args.put("Active", 1);
    	db.update(MPDHelper.QUEUE_TABLE, args, strFilter, null);
    	
    	Integer songID = newCursor.getInt(1);
    	 SQLiteDatabase.releaseMemory();
    	 db.close();
    	 return songID;
    }
    
    /**
     * Method for retrieving the next song in the users queue
     * 
     */
    public Integer getPrevSongInQueue() { 

    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	SQLiteDatabase.releaseMemory();
    	
    	// Check songs manually added to database
    	Cursor cursor = db.rawQuery("SELECT Queue_ID FROM " + MPDHelper.QUEUE_TABLE + " WHERE Active = 1 LIMIT 1", null);

    	Log.w("queucount", cursor.getCount() + "");
    	
    	cursor.moveToFirst();
    	Integer currentSong = cursor.getInt(0);
    			
    	Cursor newCursor = db.rawQuery("SELECT Queue_ID, Song_ID FROM " + MPDHelper.QUEUE_TABLE + " WHERE Queue_ID < " + currentSong + " ORDER BY Queue_ID DESC LIMIT 1", null);
    	ContentValues args = new ContentValues();
    	
    	newCursor.moveToFirst();

    	try {
    		newCursor.getInt(0);
    	} catch (CursorIndexOutOfBoundsException E) {
    		return prefs.getCurrentActiveSong();
    	}    	
    	
    	String strFilter = "Queue_ID=" + (currentSong);
    	args.put("Active", 0);
    	db.update(MPDHelper.QUEUE_TABLE, args, strFilter, null);
    	strFilter = "Queue_ID=" + newCursor.getInt(0);
    	args.put("Active", 1);
    	db.update(MPDHelper.QUEUE_TABLE, args, strFilter, null);
    	
    	newCursor.moveToFirst();
    	Integer songID = newCursor.getInt(1);
    	 SQLiteDatabase.releaseMemory();
    	 db.close();
    	 return songID;
    }
    
    public boolean addSongToQueue(Integer SongID) {
    	
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	SQLiteDatabase.releaseMemory();
    	ContentValues cv = new ContentValues();
    	cv.put(MPDHelper.QUEUE_SONG_ID, SongID);
    	cv.put(MPDHelper.QUEUE_MANUAL, 1);
    	db.insert(MPDHelper.QUEUE_TABLE, null, cv);
    	db.close();
    	
    	return true;
    }
    
    public Integer getSongIDFromName(String SongName) {
    	
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	SQLiteDatabase.releaseMemory();
    	
    	// Check songs manually added to database
    	Cursor cursor = db.rawQuery("SELECT Track_ID FROM " + MPDHelper.TRACK_TABLE + " WHERE Name = \""+SongName+"\" LIMIT 1", null);
    	cursor.moveToFirst();
    	
    	Integer TrackID = cursor.getInt(0);
    	
    	return TrackID;
    }
    
    public boolean removeSongFromQueue(Integer TrackID) {
    	
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	SQLiteDatabase.releaseMemory();

    	String whereClause = "Song_ID=" + TrackID;
		db.delete(MPDHelper.QUEUE_TABLE, whereClause, null);
    	db.close();
    	
    	return true;
    }    
    
    /*
     * Sort HashMap by Value
     * @source Fritz Meissner
     * 
     */
    static HashMap<Integer, Integer> sortByWeight(HashMap<Integer, Integer> map) {
		List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
                  return ((Comparable) ((Map.Entry) (o1)).getValue())
                 .compareTo(((Map.Entry) (o2)).getValue());
             }
        });
        
       Collections.reverse(list);
       HashMap result = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
           Map.Entry entry = (Map.Entry)it.next();
           result.put(entry.getKey(), entry.getValue());
       }
       return result;
   } 
}