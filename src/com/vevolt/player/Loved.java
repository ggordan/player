package com.vevolt.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Loved extends Activity {
	
    TrackList TL = new TrackList(this);
    Preferences prefs = new Preferences(this);
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView songCount = (TextView) findViewById(R.id.songCount);
        songCount.setText(  TL.returnPlaylists().size() + " loved songs");
        
        final ListView lv= (ListView)findViewById(R.id.listview);
        lv.setAdapter(new SongItem(this, TL.returnPlaylists()));   
        lv.setTextFilterEnabled(true);
        registerForContextMenu(lv);

       lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           	
           	// get the selected item SongID
               Integer selected = Integer.parseInt(lv.getItemAtPosition(position).toString());
               
           	// Start the music service
           	Intent i = new Intent(MusicPlayer.ACTION_PLAY);
           	i.putExtra("SongID", selected);
            i.putExtra("loved", 1);
           	Log.w("SelectedSongID", selected+"");
           	startService(i);
           	
               // move to song detail view
               Intent intent = new Intent(Loved.this, SongView.class);
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
        
    }
}

