package com.vevolt.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import java.io.IOException;

public class MusicPlayer extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener  {
		
    // The tag we put on debug messages
    final static String TAG = "VevoltMusicPlayer";

    // Get the tracklist
    TrackList TL;
    Preferences prefs;
    
    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK = "com.vevolt.player.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.vevolt.player.action.PLAY";
    public static final String ACTION_PAUSE = "com.vevolt.player.action.PAUSE";
    public static final String ACTION_STOP = "com.vevolt.player.action.STOP";
    public static final String ACTION_SKIP = "com.vevolt.player.action.SKIP";
    public static final String ACTION_REWIND = "com.vevolt.player.action.REWIND";
    public static final String ACTION_URL = "com.vevolt.player.action.URL";

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    // our media player
    MediaPlayer mPlayer = null;

    // indicates the state our service:
    enum State { Retrieving, Stopped, Preparing, Playing, Paused };

    State mState = State.Retrieving;

    // if in Retrieving mode, this flag indicates whether we should start playing immediately
    // when we are ready or not.
    boolean mStartPlayingAfterRetrieve = false;

    // if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
    // start playing when we are ready. If null, we should play a random song from the device
    Uri mWhatToPlayAfterRetrieve = null;

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    };

    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // The component name of MusicIntentReceiver, for use with media button and remote control
    // APIs
    ComponentName mMediaButtonReceiverComponent;

    AudioManager mAudioManager;
    NotificationManager mNotificationManager;

    Notification mNotification = null;

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
    	
        if ( mPlayer == null ) {
        	
        	mPlayer = new MediaPlayer();
            //mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else mPlayer.reset();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "debug: Creating service");
    	createMediaPlayerIfNeeded();
    	TL = new TrackList(getApplicationContext());
    	prefs = new Preferences(getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	String action = intent.getAction();
        
        if (action.equals(ACTION_PLAY)) {
				try {
					playSelectedSong(intent);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        } else if (action.equals(ACTION_PAUSE)) {
			pauseSong();
        }
        
        return START_NOT_STICKY;
    }
        
    private void playSelectedSong(Intent intent) throws IllegalStateException, IOException {
    	
    	Bundle extras = intent.getExtras();
    	
    	if(extras.getInt("SongID") != -1) {
        	
    			
            	String songLocation = TL.fetchSongLocation(extras.getInt("SongID"));
            	mPlayer.reset();
            	mPlayer.setDataSource(songLocation);   
            	Log.w("SongID", songLocation);
            	mPlayer.prepare();
            	mPlayer.start();
            	prefs.setActiveSong(extras.getInt("SongID"));
            	TL.populateSongQueue(extras.getInt("SongID"));
            	mState = State.Playing;
    	} else {
    		TL.populateSongQueue(prefs.getCurrentActiveSong());
    		mPlayer.start();    		
        	mState = State.Playing;    	
    	}
    }
    
    private void pauseSong() {
    	mPlayer.pause();
    	mState = State.Paused;
    }

    private void processTogglePlaybackRequest() {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        mPlayer.release();
        mPlayer.reset();
        mPlayer.stop();
    }

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		// Catches an error with MediaPlayer ready state to allow playing of track from active song
		if ( what == -38 && extra == 0 ) {
			
			if ( prefs.getCurrentActiveSong() != -1 ) {
				
     	       Intent i = new Intent(MusicPlayer.ACTION_PLAY);
     	       i.putExtra("SongID", prefs.getCurrentActiveSong());
     	       getApplicationContext().startService(i);    
			}
		}
		return false;
	}

	
	
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }	
}
