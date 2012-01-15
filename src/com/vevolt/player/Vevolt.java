package com.vevolt.player;
//http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/view/List4.html
import android.app.Activity;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.util.*;

import static android.content.ClipData.Item;

public class Vevolt extends Activity {

    Preferences prefs = new Preferences(this);
    TrackList TL = new TrackList(this);
    MusicPlayer mp = new MusicPlayer(this);
    Integer currentPosition = 0;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);       

        //TL.addEvent();
        // Fetch all song ID's
		List listOfSongs = TL.returnSongs();
        
        // The listView that will display all the tracks
        final ListView lv= (ListView)findViewById(R.id.listview);
        
        // enable long clicking on list items
        lv.setLongClickable(true);

        // set the list adapted using custom adapter for SongItems
        lv.setAdapter(new SongItem(this, listOfSongs));
        
        // create an OnClick listener for list items
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
            	// get the selected item SongID
                Integer selected = Integer.parseInt(lv.getItemAtPosition(position).toString());
                
                if(audioManager.isMusicActive()) {
                    mp.stop();
                } else {
                	Log.w("SongID", selected.toString());
                    mp.playSong(selected);
                }
                Intent intent = new Intent(Vevolt.this, SongView.class);
                intent.putExtra("Track_ID", selected);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("test", "Welcome back to Android");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	MenuInflater inflater = getMenuInflater();
        MenuInflater iinflater = getMenuInflater();
        
        iinflater.inflate(R.menu.main_activity, menu);
        
        inflater.inflate(R.menu.menu, menu);
        MenuItem shuffle = menu.getItem(1);
        if (prefs.getShuffleStatus()) {
            shuffle.setTitle("Turn SmartShuffle on");
        } else {
            shuffle.setTitle("Turn SmartShuffle off");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // app icon in action bar clicked; go home
    	
        switch (item.getItemId()) {
        	case R.id.menu_save:
                Intent inntent = new Intent(Vevolt.this, Login.class);
                startActivity(inntent);
	            break;
            case R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, Vevolt.class);            	
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.text1:
                finish();
                break;
            case R.id.shuffle:

                if (prefs.getShuffleStatus()) {
                    item.setTitle("Turn SmartShuffle on");
                    prefs.setShuffleStatus(false);
                } else {
                    item.setTitle("Turn SmartShuffle off");
                    prefs.setShuffleStatus(true);
                }

                break;
            case R.id.icontext:
                Toast.makeText(this, "You pressed the icon and text!", Toast.LENGTH_LONG).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}