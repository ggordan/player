package com.vevolt.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Interface extends LinearLayout {

    public Preferences prefs;
    public Context context;
    
    public Interface(Context xcontext) {
        super(xcontext);
        context = xcontext;
        prefs = new Preferences(xcontext);
    }
}
