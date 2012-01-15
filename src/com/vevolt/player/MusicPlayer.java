package com.vevolt.player;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MusicPlayer extends MediaPlayer {

    TrackList TL;
    Context context;
    Preferences prefs = new Preferences(context);

    MusicPlayer(Context xcontext ) {
        context = xcontext;
        TL = new TrackList(xcontext);
    }

    public void playSong(Integer songID) {

    	String songPath = TL.fetchSongLocation(songID);
    	
        try {

            reset();
            setDataSource(songPath);
            prepare();
            start();

            //TL.populateSongQueue(songID);
            //TL.getNextSongInQueue();

            // Setup listener so next song starts automatically
            setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);

                public void onCompletion(MediaPlayer arg0) {
                    if(audioManager.isMusicActive()) {

                    }
                    stop();
                }
            });

        } catch (IOException e) {
            Log.v("MusicPlayer", "Error in playSong");
        }
    }
    public void queueSongs(Integer songID) {

    }
    
    public String getNextSong() {

        return "Hello";
    }
}
