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
    
    public ActiveSong(Context xcontext, AttributeSet attrs) {	
        super(xcontext, attrs);
               
        context = xcontext;
        
        TL = new TrackList(context.getApplicationContext());
                
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
 
        li = (LayoutInflater) getContext().getSystemService(infService);
        li.inflate(R.layout.active_song, this, true);
        
        this.updateCurrentTrack();
        
        ((ImageButton)this.findViewById(R.id.playpause)).setOnClickListener(screenshotOnClickListener);

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
}
