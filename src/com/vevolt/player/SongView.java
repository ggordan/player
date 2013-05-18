package com.vevolt.player;

//import android.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.vevolt.player.MusicPlayer.LocalBinder;

public class SongView extends Activity {

    /** Called when the activity is first created. */
    public String myString;
    public List value;
    TrackList currentTrack = new TrackList(this);
    Preferences prefs;
    Engine engine;
    Integer duration;
    MusicPlayer mp;
    boolean mBound = false;
    
    @Override
    public void onCreate( Bundle savedInstanceState ) {
    	    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_detail);
        
        Intent mServiceIntent = new Intent(this, MusicPlayer.class);
        bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        
        updateInterface(0);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, final IBinder service) {
            MusicPlayer mService = ((LocalBinder) service).getService();
            ProgressBar pb = (ProgressBar)findViewById(R.id.songProgress);
            pb.setMax(mService.getSongDuration());
            
            final Integer maxDuration = mService.getSongDuration(); 
            
            new Thread(new Runnable() {
            	
                MusicPlayer mService = ((LocalBinder) service).getService();
                ProgressBar pb = (ProgressBar)findViewById(R.id.songProgress);
            	
  			  public void run() {
  				  
  				while (mService.getCurrentPosition() < maxDuration) {
   
  				  pb.setProgress(mService.getCurrentPosition());
   
  				  // your computer is too fast, sleep 1 second
  				  try {
  					Thread.sleep(100);
  				  } catch (InterruptedException e) {
  					e.printStackTrace();
  				  }
  				}
  			  }}).start();

          }

          public void onServiceDisconnected(ComponentName className) {
              // As our service is in the same process, this should never be called
          }
      };    

    private OnClickListener playPauseListener = new OnClickListener() {
        public void onClick(View v) {

           	Resources res = getResources();
            final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); 
            // design the shit out of the play pause button
            ImageButton playPause = (ImageButton) findViewById(R.id.pressplay);
           	   
      	   if(audioManager.isMusicActive()) {
     		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.playbutton);      
     	       playPause.setImageDrawable(pauseButton);   	
      	       Intent i = new Intent(MusicPlayer.ACTION_PAUSE);
      	       getApplicationContext().startService(i);           	       
     	   } else {
     		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.stopped);      
     	       playPause.setImageDrawable(pauseButton);		
     	       Intent i = new Intent(MusicPlayer.ACTION_PLAY);
     	       i.putExtra("SongID", -1);
     	       getApplicationContext().startService(i);       	       
     	   }
        }
    };    
    
    private OnClickListener playNextSong = new OnClickListener() {
        public void onClick(View v) {
        	
        	Intent i = new Intent(MusicPlayer.ACTION_SKIP);
        	Integer nextSongInQueue = new Engine(getApplicationContext()).getNextSongInQueue();
        	Log.w("sds", nextSongInQueue + "");
        	i.putExtra("SongID", nextSongInQueue);
        	startService(i);  
        	
        	// move to song detail view
            Intent intent = new Intent(SongView.this, SongView.class);
            intent.putExtra("Track_ID", nextSongInQueue);
            startActivity(intent);
        	//updateInterface(nextSongInQueue);
   		}
    };

    private OnClickListener playPrevSong = new OnClickListener() {
        public void onClick(View v) {
        	
        	Intent i = new Intent(MusicPlayer.ACTION_SKIP);
        	Integer prevSongInQueue = new Engine(getApplicationContext()).getPrevSongInQueue();
        	Log.w("sds", prevSongInQueue + "");
        	i.putExtra("SongID", prevSongInQueue);
        	startService(i);  
        	
        	// move to song detail view
            Intent intent = new Intent(SongView.this, SongView.class);
            intent.putExtra("Track_ID", prevSongInQueue);
            startActivity(intent);
   		}
    };
    
    private OnLongClickListener rewindSong = new OnLongClickListener() {
		public boolean onLongClick(View v) {
        	Intent i = new Intent(MusicPlayer.ACTION_REWIND);
        	startService(i);  
			return true;
   		}
    };    
    
    public void updateInterface(Integer SongID) {
    	    	
    	Log.w("asdasas", duration + "");
    	
        Resources res = getResources();
		ImageView pressPlay = (ImageView) findViewById(R.id.pressplay);
		Drawable stoppedButton = (Drawable) res.getDrawable(R.drawable.stopped);     	
		
		// define play, prev, and stop listeners
		((ImageButton)this.findViewById(R.id.pressplay)).setOnClickListener(playPauseListener);
		((ImageButton)this.findViewById(R.id.playnextsong)).setOnClickListener(playNextSong);
		((ImageButton)this.findViewById(R.id.playprevsong)).setOnClickListener(playPrevSong);
		((ImageButton)this.findViewById(R.id.playprevsong)).setOnLongClickListener(rewindSong);
		
		
		// default the button to stop
		pressPlay.setImageDrawable(stoppedButton);
        
        Handler refresh = new Handler();
        Bundle extras = getIntent().getExtras();

        if ( !extras.isEmpty() || SongID == 0) {
            value = currentTrack.getSongDetail(extras.getInt("Track_ID"));
        } else {
        	value = currentTrack.getSongDetail(SongID);
        }
 
        /*
         * Process the image display 
         */
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// will results in a much smaller image than the original
		//options.inSampleSize = 16;        
		ImageView lovedTrack = (ImageView) findViewById(R.id.lovedTrack);
		Boolean lovedStatus = currentTrack.loveTrack(extras.getInt("Track_ID"), "get");
		
		//final Integer trackID = extras.getInt("Track_ID");
		final Integer trackID = Integer.parseInt(value.get(8).toString());
		
		/*
		 * Handle the loved status of the song
		 */
		
		if (lovedStatus) {
			Drawable lovedIcon = (Drawable) res.getDrawable(R.drawable.loved);  
			lovedTrack.setImageDrawable(lovedIcon);
		} else {
			Drawable unlovedIcon = (Drawable) res.getDrawable(R.drawable.love);  
			lovedTrack.setImageDrawable(unlovedIcon);			
		}
		
		// Press handler
		((ImageButton)this.findViewById(R.id.lovedTrack)).setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	
	        	   // design the shit out of the play pause button
	             ImageButton LovedTrack = (ImageButton) findViewById(R.id.lovedTrack);
	            	Resources res = getResources();
	     		Boolean lovedStatus = currentTrack.loveTrack(trackID, "get");

	       	   if(lovedStatus) {
	      		   Drawable unlove = (Drawable) res.getDrawable(R.drawable.love);      
	      	       LovedTrack.setImageDrawable(unlove);
	      	       currentTrack.loveTrack(trackID, "set");
	      	   } else {
	      		   Drawable love = (Drawable) res.getDrawable(R.drawable.loved);      
	      	       LovedTrack.setImageDrawable(love);
	      	       currentTrack.loveTrack(trackID, "set");
	      	       new Scrobble(getApplicationContext()).loveSong(trackID);
	      	       
	      	   }
	 	   
	         }
	     });    
		
		/*
		 * Populate metadata
		 */
		
        TextView title = (TextView) findViewById(R.id.songName);
        title.setText(value.get(0).toString());

        TextView artist = (TextView) findViewById(R.id.artistName);
        artist.setText(value.get(1).toString());
        
        TextView album = (TextView) findViewById(R.id.albumName);
        album.setText(value.get(2).toString());        
        
		if (value.get(3).toString().length() > 0){
			
			final Bitmap b = BitmapFactory.decodeFile(value.get(3).toString(), options);
    		ImageView trackArtwork = (ImageView) findViewById(R.id.artwork);
    		trackArtwork.setImageBitmap(b);
		}		
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	// move to song detail view
            Intent intent = new Intent(SongView.this, Vevolt.class);
            startActivity(intent);        	
        }
        return super.onKeyDown(keyCode, event);
    }
    
    
    /*
     * 
     * 
     */
    
      
}