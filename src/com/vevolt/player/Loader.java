//package com.vevolt.player;
//
//import java.util.List;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.Drawable;
//import android.media.AudioManager;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//public class Loader extends RelativeLayout  {
//	
//    private Activity activeActivity;
//    private Context context;
//    Preferences prefs;
//    TrackList TL;
//    Engine engine;
//    
//    public Loader(Context xcontext, AttributeSet attrs) {	
//        super(xcontext, attrs);
//               
//        context = xcontext;
//        
//        TL = new TrackList(context.getApplicationContext());
//        engine = new Engine(context.getApplicationContext());        
//        
//        String infService = Context.LAYOUT_INFLATER_SERVICE;
//        LayoutInflater li;
// 
//        li = (LayoutInflater) getContext().getSystemService(infService);
//        li.inflate(R.layout.loader, this, true);
//        
//        ProgressBar loader = (ProgressBar) this.findViewById(R.id.loaderProgress);
//        // set the maximum duration 
//        loader.setMax(10);
//        
//        for (Integer i = 0; i < 10; i++ ) {
//        	loader.setProgress(i);
//        }
//        
//    }
//
//    public void setActivity(Activity activity) {
//        // set init otherwise of ctor and call externally...
//        activeActivity = activity;
//    }
//}
