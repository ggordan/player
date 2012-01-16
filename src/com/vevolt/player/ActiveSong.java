package com.vevolt.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class ActiveSong extends RelativeLayout {
	
    private LayoutInflater inflater;
    private Activity activeActivity;
    private Context context;
    
    public ActiveSong(Context xcontext, AttributeSet attrs) {	
        super(xcontext, attrs);
               
        context = xcontext;
        
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
 
        li = (LayoutInflater) getContext().getSystemService(infService);
        li.inflate(R.layout.active_song, this, true);
        
        ((ImageButton)this.findViewById(R.id.playpause)).setOnClickListener(screenshotOnClickListener);

    }

    public void setActivity(Activity activity) {
        // set init otherwise of ctor and call externally...
        activeActivity = activity;
    }
  
    private OnClickListener screenshotOnClickListener = new OnClickListener() {
        public void onClick(View v) {
       	   // design the shit out of the play pause button
       	   ImageButton playPause = (ImageButton) findViewById(R.id.playpause);
       	   Resources res = getResources();

       		   Drawable pauseButton = (Drawable) res.getDrawable(R.drawable.guardian);      
       	       playPause.setImageDrawable(pauseButton);		   
        }
    };      
}
