package com.vevolt.player;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.io.IOException;
import java.util.*;

import static android.content.ClipData.Item;

public class Vevolt extends Activity {

    Preferences prefs = new Preferences(this);
    TrackList TL = new TrackList(this);
    Integer currentPosition = 0;
    ActiveSong activeSong;
//    Loader loader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
        
        //TL.loadTracks();

        setContentView(R.layout.main);
        List<Integer> listOfSongs;
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	String query = intent.getStringExtra(SearchManager.QUERY);
        	listOfSongs = TL.returnQuery(query);
        	
        	if (listOfSongs.size() > 0) {
	            TextView songCount = (TextView) findViewById(R.id.songCount);
	            songCount.setText(  listOfSongs.size() + " song(s) found using '"+query+"'");
        	} else {
                TextView songCount = (TextView) findViewById(R.id.songCount);
                songCount.setText("No songs found with that query. Viewing all songs...");   
            	listOfSongs = TL.returnSongs();
        	}
            
        } else {
        	listOfSongs = TL.returnSongs();
            TextView songCount = (TextView) findViewById(R.id.songCount);
            songCount.setText(  listOfSongs.size() + " songs");
        }
        
        if (listOfSongs.size() != 0) {
        
        // Set the Active song in the bottom menu
        activeSong = (ActiveSong) findViewById(R.id.active_song);
        activeSong.setActivity(this);
        // Set the Active song in the bottom menu
//        loader = (Loader) findViewById(R.id.loader);
//        loader.setActivity(this);
        
        /*
         * Display the song count
         */

        
        final ListView lv= (ListView)findViewById(R.id.listview);
        lv.setAdapter(new SongItem(this, listOfSongs));        

        /* 
         * Enable long clicks on song items ( For context menu)
         */
        lv.setLongClickable(true);
        registerForContextMenu(lv);
        
        /*
         * Define the on click listener
         */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
            	// get the selected item SongID
                Integer selected = Integer.parseInt(lv.getItemAtPosition(position).toString());
                
            	// Start the music service
            	Intent i = new Intent(MusicPlayer.ACTION_PLAY);
            	i.putExtra("SongID", selected);
            	Log.w("SelectedSongID", selected+"");
            	startService(i);
            	
                // move to song detail view
                Intent intent = new Intent(Vevolt.this, SongView.class);
                intent.putExtra("Track_ID", selected);
                startActivity(intent);
            }
        });
        
        /*
         * Store the currently selected item in the preferences
         */
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Integer selected = Integer.parseInt(lv.getItemAtPosition(position).toString());
				prefs.setLongClickValue(selected);
				return false;
			}
		});
        
        } else {
        	Toast.makeText(this, "Please add some music to the /Music folder then restart the application", Toast.LENGTH_LONG).show();
        }
    }
    
    /*
     * Handles the CONTEXT menu
     */
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
    	// define the duration of the toast
    	int duration = Toast.LENGTH_SHORT;
    	
    	/*
    	 * Define behaviour for different options: queue, playlist
    	 */
        switch (item.getItemId()) {
            case R.id.queueSong:
            	new Engine(this).addSongToQueue(prefs.getLongClickValue());
            	Toast.makeText(this, "The song has been added to the music queue.", duration).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    /* 
     * Handles the OPTIONS menu
     */    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	MenuInflater inflater = getMenuInflater();
        MenuInflater iinflater = getMenuInflater();
        
        iinflater.inflate(R.menu.main_activity, menu);
        inflater.inflate(R.menu.menu, menu);        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // app icon in action bar clicked; go home
    	
        switch (item.getItemId()) {
        	case R.id.menu_save:
                Intent inntent = new Intent(Vevolt.this, Loved.class);
                startActivity(inntent);
	            break;
            case R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, Vevolt.class);            	
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.viewqueue:
            	Intent queue = new Intent(this, Queue.class);            	
                queue.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(queue);
                break;
            case R.id.lastfmsettings:
            	Intent last = new Intent(this, Lastfm.class);            	
                last.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(last);            	
                break;
            case R.id.icontext:
            	Intent j = new Intent(Intent.ACTION_MAIN);
            	j.addCategory(Intent.CATEGORY_HOME);
            	j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(j);
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);       
        
        activeSong.updateCurrentTrack();

  	   ImageButton playPause = (ImageButton) findViewById(R.id.playpause);
  	   Resources res = getResources();

  	   if(audioManager.isMusicActive()) {
  		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.pause);      
  	       playPause.setImageDrawable(pauseButton);
  	       
  	   } else {
  		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.play);      
  	       playPause.setImageDrawable(pauseButton);		   
  	   } 	
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	// move to song detail view
            Intent intent = new Intent(this, Vevolt.class);
            startActivity(intent);        	
        }
        return super.onKeyDown(keyCode, event);
    }
}
