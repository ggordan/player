package com.vevolt.player;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActiveSong extends RelativeLayout  {
	
    private LayoutInflater inflater;
    private Activity activeActivity;
    private Context context;
    Preferences prefs;
    TrackList TL;
    Engine engine;
    
    public ActiveSong(Context xcontext, AttributeSet attrs) {	
        super(xcontext, attrs);
               
        context = xcontext;
        
        TL = new TrackList(context.getApplicationContext());
        engine = new Engine(context.getApplicationContext());        
        
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
 
        li = (LayoutInflater) getContext().getSystemService(infService);
        li.inflate(R.layout.active_song, this, true);
        
        this.updateCurrentTrack();
        
        ((RelativeLayout)this.findViewById(R.id.activeSongLayout)).setOnClickListener(viewSongDetails);
        ((ImageButton)this.findViewById(R.id.playpause)).setOnClickListener(screenshotOnClickListener);
        ((ImageButton)this.findViewById(R.id.playnext)).setOnClickListener(playNextListener);
        
    }

    public void setActivity(Activity activity) {
        // set init otherwise of ctor and call externally...
        activeActivity = activity;
    }
    
    public void updateCurrentTrack() {
    	
    	prefs = new Preferences(context.getApplicationContext());

        if (prefs.getCurrentActiveSong() != -1) {

        	List songInfo = TL.getSongDetail(prefs.getCurrentActiveSong());

            TextView currentTitle = (TextView) this.findViewById(R.id.currentTitle);
        	currentTitle.setText(songInfo.get(0).toString());

            TextView currentArtist = (TextView) this.findViewById(R.id.currentArtist);
        	currentArtist.setText(songInfo.get(1).toString());    

    		final BitmapFactory.Options options = new BitmapFactory.Options();
			final Bitmap b = BitmapFactory.decodeFile(songInfo.get(3).toString(), options);
    		ImageView trackArtwork = (ImageView) this.findViewById(R.id.currentartwork);
    		trackArtwork.setImageBitmap(b);        	
        }
    }    
  
    private OnClickListener screenshotOnClickListener = new OnClickListener() {
        public void onClick(View v) {
        	
            final AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE); 
       	   // design the shit out of the play pause button
            ImageButton playPause = (ImageButton) findViewById(R.id.playpause);
           	Resources res = getResources();
           	   
      	   if(audioManager.isMusicActive()) {
     		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.play);      
     	       playPause.setImageDrawable(pauseButton);   	
      	       Intent i = new Intent(MusicPlayer.ACTION_PAUSE);
      	       context.getApplicationContext().startService(i);           	       
     	   } else {
     		   
     		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.pause);      
     	       playPause.setImageDrawable(pauseButton);		
     	       Intent i = new Intent(MusicPlayer.ACTION_PLAY);
     	       i.putExtra("SongID", -1);
     	       context.getApplicationContext().startService(i);       	       
     	   }
	   
        }
    };
        
    private OnClickListener playNextListener = new OnClickListener() {
        public void onClick(View v) {
       	
        	Intent i = new Intent(MusicPlayer.ACTION_SKIP);
        	i.putExtra("SongID", engine.getNextSongInQueue());
        	context.getApplicationContext().startService(i);        	
        	
        	final List songInfo = TL.getSongListInfo(prefs.getCurrentActiveSong());
        	TextView newSongActive = (TextView) findViewById(R.id.currentTitle);
        	newSongActive.setText(songInfo.get(0).toString());
        	TextView newAlbumActive = (TextView) findViewById(R.id.currentArtist);
        	newAlbumActive.setText(songInfo.get(1).toString());
        	
    		if (songInfo.get(2).toString().length() > 0){
    			
   			 ImageView trackArtwork = (ImageView) findViewById(R.id.currentartwork);
   			 final BitmapFactory.Options options = new BitmapFactory.Options();
   			 // will results in a much smaller image than the original
   			 options.inSampleSize = 4;
   			 final Bitmap b = BitmapFactory.decodeFile(songInfo.get(2).toString(), options);
   			trackArtwork.setImageBitmap(b);
   			
   		}
        }
    };    
    
    private OnClickListener viewSongDetails = new OnClickListener() {
        public void onClick(View v) {
        	
            Intent intent = new Intent(context.getApplicationContext(), SongView.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("Track_ID", prefs.getCurrentActiveSong());
            context.getApplicationContext().startActivity(intent);
        }
    };        
}
