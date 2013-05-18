package com.vevolt.player;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

public class Preferences {

    Context context;
    public static final String PREFS = "VevoltPreferences";

    public Preferences(Context xcontext) {
        context = xcontext;
    }

    public String configureOptionsMenu() {

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        boolean shuffle = settings.getBoolean("shuffle", true);
        String buttonText = "Hello";
        
        if (shuffle) {
            buttonText = " Off ";
        } else {
            buttonText = " On ";
        }
        
        return buttonText;
    }

    public Integer getCurrentActiveSong() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getInt("activeSong", -1);
    }    
    
    public void setActiveSong(Integer songID) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activeSong", songID);
        editor.commit();
    }

    public Integer getCurrentActiveSongQueueID() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getInt("activeQueue", 1);
    }    
    
    public void setActiveSongSongQueueID(Integer songID) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activeQueue", songID);
        editor.commit();
    }    
    
    public Boolean getShuffleStatus() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getBoolean("shuffle", false);
    }
    
    public void setShuffleStatus(Boolean status) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shuffle", status);
        editor.commit();
    }
    
    public void setLongClickValue(Integer integerr) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("contextitem", integerr);
        editor.commit();  	
    }
    
    public Integer getLongClickValue() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getInt("contextitem", 1);
    }    
    
    public void setQueueSong(Integer songID) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("queueitem", songID);
        editor.commit();  	
    }
    
    public Integer getQueueSong() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getInt("queueitem", 0);
    }
    
    /*
     * Last.fm username and password
     */

    public void setUsername(String username) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.commit();  	
    }
    
    public String getUsername() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getString("username", "");
    }    
    
    public void setPassword(String password) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("password", password);
        editor.commit();  	
    }
    
    public String getpassword() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getString("password", "");
    }
    
    public void setSession(String session) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("session", session);
        editor.commit();  	    	
    }

    public String getSession() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getString("session", "901d08e9dc385e097c0e2c76c3361731");	    	
    }    
    
    public void setPlayStart(String timestamp) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("timestamp", timestamp);
        editor.commit();  	    	    	
    }
    public String getPlayStart() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getString("timestamp", "");	 	    	    	
    }  
    

    public void setrunTime(Integer i) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("time", 1);
        editor.commit();  	    	    	
    }    
    public Integer getrunTime() {
        SharedPreferences settings = context.getSharedPreferences(PREFS, 0);
        return settings.getInt("time", 0);	 	    	    	
    }      
    
}
