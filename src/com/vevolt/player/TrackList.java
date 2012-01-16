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
import java.util.*;

public class TrackList extends Activity {

    // the media path ( Location of all the songs )
    final static String MEDIA_PATH = "/sdcard/Music/";
    final static String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.vevolt.player/cache/artwork/";
    MPDHelper dbHelper;
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
        db.close();  
    }

    /**
     * Returns an alphabetical list of songs in the users SD card
     * @return List
     */
    public static List alphabeticalSongList() {

        File file = new File(MEDIA_PATH);
        File list[] = file.listFiles();
        List songs = new ArrayList();

        // loop through songlist
        for ( File song : list ) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(song.toString());
            songs.add(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        }
        return songs;
    }
    
    public String getSongInfo(Integer songID, String row) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.w("track", "SELECT " + row + " FROM track WHERE Track_ID = '" + songID + "' LIMIT 1");        
        Cursor cursor = db.rawQuery("SELECT " + row + " FROM track WHERE Track_ID = '" + songID + "' LIMIT 1", null);
        startManagingCursor(cursor);

        cursor.moveToFirst();
        
        db.close();
        Log.w("Track", cursor.getString(0));
        
    	return cursor.getString(0);
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
        db.close();
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
        db.close();
        return data;
    }        
    
    public String fetchSongLocation(Integer position) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + MPDHelper.TRACK_LOCATION + " FROM " + MPDHelper.TRACK_TABLE + " WHERE Track_ID = '" + position + "' LIMIT 1", null);
        startManagingCursor(cursor);
        
        cursor.moveToFirst();
        Log.w("ReturnedLocation", cursor.getString(0));
        db.close();
        return cursor.getString(0);
    }

    /**
     * Returns a hashmap of the songs.
     * @return HashMap Track_ID => Song Details
     */
    public HashMap returnEvents() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track ORDER BY " + MPDHelper.TRACK_TITLE + " ASC ", null);
        //Cursor cursor = db.query(MPDHelper.TRACK_TABLE, null, null, null, null, null, null);
        HashMap<String, Integer> songs = new HashMap<String, Integer>();
        ArrayList<String> temp = new ArrayList<String>();

        startManagingCursor(cursor);

        while (cursor.moveToNext()) {
            Log.w("Song", cursor.getString(1) + " " + cursor.getInt(0));
            songs.put(cursor.getString(1), cursor.getInt(0));
        }
        db.close();
        return songs;
    }
    
    public List returnSongs() {
    	
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track ORDER BY " + MPDHelper.TRACK_TITLE + " ASC ", null);
        List songs = new ArrayList();
        startManagingCursor(cursor);

        while (cursor.moveToNext()) {
            Log.w("Song", cursor.getString(1) + " " + cursor.getInt(0));
            songs.add(cursor.getInt(0));
        }
        db.close();
        return songs;
    }
    
    public void populateSongQueue(Integer songID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteDatabase wdb = dbHelper.getWritableDatabase();
        Cursor cursor;
        ContentValues values = new ContentValues();
        List<Integer> tracks = new ArrayList<Integer>();
        
        db.execSQL("DROP TABLE queue");
        db.execSQL(MPDHelper.QUEUE_TABLE_SQL);

        Log.w("ShuffleStatus", " = " + prefs.getShuffleStatus());

        HashMap songs = returnEvents();
        Iterator it = songs.entrySet().iterator();

        Boolean start = false;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
        
            //Log.w("sids", pairs.getValue().toString());
            if (start) {
                tracks.add(Integer.parseInt(pairs.getValue().toString()));
                //Log.w("Tracks", "" + pairs.getValue().toString());
                Log.w("YEP", "" + pairs.getValue().toString());
            } else {
                if (Integer.parseInt(pairs.getValue().toString()) > songID) {
                    start = true;
                    Log.w("NOPE", "" + pairs.getValue().toString());
                }
            }
        }
        
        db.close();
    }
    
    public void getNextSongInQueue() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        
        //cursor = db.rawQuery("SELECT " + MPDHelper.QUEUE_SONG_ID + " FROM " + MPDHelper.QUEUE_TABLE + " ORDER BY Queue_ID ASC", null);
        cursor = db.rawQuery("SELECT * FROM queue", null);
        db.close();
       // Log.w("SQL", "SELECT Queue_ID, " + MPDHelper.QUEUE_SONG_ID + " FROM " + MPDHelper.QUEUE_TABLE + " LIMIT 1");
        startManagingCursor(cursor);
        	
        while(cursor.moveToNext()) {
            //Log.w("QueueItems", " = " + cursor.getInt(1));
        }
        
        //cursor.moveToFirst();
        //Log.w("SQL", "s" + cursor.getInt(0) + 2);
    }
    
    public Integer getSongCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Track_ID FROM track", null);
        return cursor.getCount();
    }
}
