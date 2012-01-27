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
}
