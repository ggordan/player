package com.vevolt.player;

//import android.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SongView extends Activity {

    /** Called when the activity is first created. */
    public String myString;
    public List value;
    TrackList currentTrack = new TrackList(this);
    
    @Override
    public void onCreate( Bundle savedInstanceState ) {
    	    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_detail);

        Handler refresh = new Handler();
        Bundle extras = getIntent().getExtras();

        if ( !extras.isEmpty() ) {
            value = currentTrack.getSongDetail(extras.getInt("Track_ID"));
        } else {
        	Log.wtf("howdya", "gethere");
        }
 
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// will results in a much smaller image than the original
		//options.inSampleSize = 16;        
        
        TextView title = (TextView) findViewById(R.id.songName);
        title.setText(value.get(0).toString());

        TextView artist = (TextView) findViewById(R.id.artistName);
        artist.setText(value.get(1).toString());
        
        TextView album = (TextView) findViewById(R.id.albumName);
        album.setText(value.get(2).toString());        
        
		if (value.get(3).toString().length() > 0){
			
//			runOnUiThread(new Runnable() {
//			     public void run() {
//					 new Thread(new Runnable() {
//						    public void run() {
//								final Bitmap b = BitmapFactory.decodeFile(value.get(3).toString(), options);
//								
//						    		ImageView trackArtwork = (ImageView) findViewById(R.id.artwork);
//						    		trackArtwork.setImageBitmap(b);
//						    }
//					 }).start();
//			    }
//			});	
			
			final Bitmap b = BitmapFactory.decodeFile(value.get(3).toString(), options);
			
    		ImageView trackArtwork = (ImageView) findViewById(R.id.artwork);
    		trackArtwork.setImageBitmap(b);
    		
		}        
        
    }
    

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myString = savedInstanceState.getString("test");
    }

}