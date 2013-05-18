package com.vevolt.player;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/** Helper to the database, manages versions and creation */
public class MPDHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "vevolt.db";
    private static final int DATABASE_VERSION = 1;

    /*
     * Define the tracks table
     */
    
    public static final String TRACK_TABLE = "track";
    public static final String TRACK_TABLE_SQL =  "CREATE TABLE " + TRACK_TABLE + " ( "
                                        + "Track_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                                        + "Name TEXT, "
                                        + "Artist TEXT, "
                                        + "Album TEXT, "
                                        + "Artwork TEXT, "
                                        + "Location TEXT, "
                                        + "Plays INTEGER, "
                                        + "Loved INTEGER, "
                                        + "Genre TEXT, "
                                        + "Last_Played TEXT"
                                        + ")";

    // Columns
    public static final String TRACK_ID = "TRACK_ID";
    public static final String TRACK_TITLE = "Name";
    public static final String TRACK_ALBUM = "Album";
    public static final String TRACK_ARTWORK = "Artwork";
    public static final String TRACK_ARTIST = "Artist";
    public static final String TRACK_PLAYS = "Plays";
    public static final String TRACK_GENRE = "Genre";
    public static final String TRACK_LOVED = "Loved";
    public static final String TRACK_LAST_PLAYED = "Last_Played";
    public static final String TRACK_LOCATION = "Location";

    /*
     * Define the queue table
     */
    
    public static final String QUEUE_TABLE = "queue";
    public static final String QUEUE_TABLE_SQL =  "CREATE TABLE " + QUEUE_TABLE + " ( "
            							+ "Queue_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            							+ "Song_ID INTEGER, "
            							+ "Active INTEGER, "
            							+ "Manual INTEGER"
            							+ ")";

    public static final String QUEUE_ID = "Queue_ID";
    public static final String QUEUE_ACTIVE = "Active";
    public static final String QUEUE_SONG_ID = "Song_ID";
    public static final String QUEUE_MANUAL = "Manual";

    /*
     * Define the playlists table
     */
    
    public static final String PLAYLIST_TABLE = "playlists";
    public static final String PLAYLIST_TABLE_SQL =  "CREATE TABLE " + PLAYLIST_TABLE + " ( "
                                        + "Playlist_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                                        + "Name TEXT ,"
                                        + "Tracks TEXT ,"
                                        + "Plays INTEGER"
                                        + ")";

    // Columns
    public static final String PLAYLIST_ID = "TRACK_ID";
    public static final String PLAYLIST_TITLE = "Name";
    public static final String PLAYLIST_TRACKS = "Tracks";
    public static final String PLAYLIST_PLAYS = "Plays";
    
    
    public MPDHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRACK_TABLE_SQL);
        db.execSQL(QUEUE_TABLE_SQL);
        db.execSQL(PLAYLIST_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion > newVersion) {
//            db.execSQL( "DROP DATABASE " + DATABASE_NAME );
//        }
    }
    
    public static Integer getNextSongID() {
    	
    	return 1;
    }
    
}