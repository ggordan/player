package com.vevolt.player;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
import android.drm.DrmStore.RightsStatus;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Lastfm extends Activity {
	
    TrackList TL = new TrackList(this);
	Preferences prefs = new Preferences(this);
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lastfm);
        
        // EditText
        EditText username = (EditText)findViewById(R.id.username);
        EditText password = (EditText)findViewById(R.id.password);
        
        if (prefs.getUsername() != "") {
        	username.setText(prefs.getUsername());
        }
        if (prefs.getpassword() != "") {
        	password.setText(prefs.getpassword());
        }
        
        Button save = (Button)findViewById(R.id.savelogin);
        
        save.setOnClickListener( new View.OnClickListener() {
        	public void onClick(View view) {
        	    // EditText
                EditText username = (EditText)findViewById(R.id.username);
                EditText password = (EditText)findViewById(R.id.password);
                
        		prefs.setUsername(username.getText().toString());
        		prefs.setPassword(password.getText().toString());
        		new Scrobble(getApplicationContext()).getSessionKey();
        		
        		checkSession();
        	}
        });
    }
    
    public void checkSession() {
        String session = prefs.getSession();
    	TextView createAccount = (TextView)findViewById(R.id.createac);

        if (session.length() != 32) { 
        	Toast.makeText(this, "The username and password combination you entered was incorrect.", Toast.LENGTH_SHORT).show();       	
        	
        	createAccount.setVisibility(View.VISIBLE);
        	createAccount.setOnClickListener( new View.OnClickListener() {
            	public void onClick(View view) {
            		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.last.fm/join"));
            		startActivity(browserIntent);
            	}
            });
        	
        	
        } else {
        	Toast.makeText(this, "You have been successfully authenticated to Last.fm", Toast.LENGTH_SHORT).show();
        	createAccount.setVisibility(View.GONE);
        }
    }
}

