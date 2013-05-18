package com.vevolt.player;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Queue extends ListActivity {
	
    TrackList TL = new TrackList(this);
	Preferences prefs = new Preferences(this);
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			String searchKeywords = queryIntent.getStringExtra(SearchManager.QUERY);
		}
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.playlists, TL.fetchQueue()));
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setLongClickable(true);
        registerForContextMenu(lv);
        
        /*
         * Store the currently selected item in the preferences
         */
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				String songName = lv.getItemAtPosition(position).toString();
                prefs.setQueueSong(new Engine(getApplicationContext()).getSongIDFromName(songName));
				return false;
			}
		});        
        
    }
    
    /*
     * Handles the CONTEXT menu
     */
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_queue, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
    	// define the duration of the toast
    	int duration = Toast.LENGTH_SHORT;
    	
    	/*
    	 * Define behaviour for different options: queue, playlist
    	 */
        switch (item.getItemId()) {
            case R.id.deleteQueue:
            	new Engine(this).removeSongFromQueue(prefs.getQueueSong());
                Intent intent = new Intent(Queue.this, Queue.class);
                startActivity(intent);
                
            	Toast.makeText(this, "The song has been removed from the Queue." + prefs.getQueueSong(), duration).show();
            	
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	// move to song detail view
            Intent intent = new Intent(Queue.this, Vevolt.class);
            startActivity(intent);        	
        }
        return super.onKeyDown(keyCode, event);
    }
    
}

