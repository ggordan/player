package com.vevolt.player;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;

public class MusicPlayer extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener  {
		
    // The tag we put on debug messages
    final static String TAG = "VevoltMusicPlayer";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        MusicPlayer getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicPlayer.this;
        }
    }
    
    // definitions
    TrackList TL;
    Engine engine;
    Preferences prefs;
    Scrobble sc;
    
    public static final String ACTION_PLAY = "com.vevolt.player.action.PLAY";
    public static final String ACTION_PAUSE = "com.vevolt.player.action.PAUSE";
    public static final String ACTION_PREVIOUS = "com.vevolt.player.action.PREVIOUS";
    public static final String ACTION_SKIP = "com.vevolt.player.action.SKIP";
    public static final String ACTION_REWIND = "com.vevolt.player.action.REWIND";

    // our media player
    MediaPlayer mPlayer = null;

    // indicates the state our service:
    enum State { Retrieving, Stopped, Preparing, Playing, Paused };

    State mState = State.Retrieving;

    AudioManager mAudioManager;

    void createMediaPlayerIfNeeded() {
    	
        if ( mPlayer == null ) {
        	
        	mPlayer = new MediaPlayer();

            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else mPlayer.reset();
    }

    @Override
    public void onCreate() {
    	createMediaPlayerIfNeeded();
    	TL = new TrackList(getApplicationContext());
    	engine = new Engine(getApplicationContext());
    	sc = new Scrobble(getApplicationContext());
    	prefs = new Preferences(getApplicationContext());
    	
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

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
        } else if (action.equals(ACTION_SKIP)) {
        	try {
				playNext(intent);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else if (action.equals(ACTION_REWIND)) {
				try {
					rewindSong();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
        
        return START_NOT_STICKY;
    }
    
    private void rewindSong() throws IllegalStateException, IOException {
    	mPlayer.seekTo(0);
    }
    
    public Integer getSongDuration() {
    	return mPlayer.getDuration();
    }
    
    public Integer getCurrentPosition() {
    	return mPlayer.getCurrentPosition();
    }
    
    
    private void playSelectedSong(Intent intent) throws IllegalStateException, IOException {
    	    	
    	Bundle extras = intent.getExtras();
    	Integer s = null;
    	Boolean loved = false;
    	
    	s = extras.getInt("loved");
    	
    	long unixTime = System.currentTimeMillis() / 1000L;
    	prefs.setPlayStart(String.valueOf(unixTime));
    	
    	if(extras.getInt("SongID") != -1) {
    			
            	String songLocation = TL.fetchSongLocation(extras.getInt("SongID"));
            	mPlayer.reset();
            	mPlayer.setDataSource(songLocation);   
            	Log.w("SongID", songLocation);
            	mPlayer.prepare();
            	mPlayer.start();
            	prefs.setActiveSong(extras.getInt("SongID"));
            	if (extras.getInt("loved") != 1) {
            		engine.populateSongQueue(extras.getInt("SongID"));
            	} else {
            		engine.populateLovedSongQueue(extras.getInt("SongID"));
            	}
            	mState = State.Playing;
    	} else {
    			mPlayer.start();    		
            	mState = State.Playing;    			
    	}
    }
    
    private void pauseSong() {
    	mPlayer.pause();
    	mState = State.Paused;
    }
    
    private void playNext(Intent intent) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
    	
    	Bundle extras = intent.getExtras();
    	
            	String songLocation = TL.fetchSongLocation(extras.getInt("SongID"));
            	Log.w("NextSong", TL.fetchSongLocation(extras.getInt("SongID")));
            	mPlayer.reset();
            	mPlayer.setDataSource(songLocation);   
            	Log.w("SongID", songLocation);
            	mPlayer.prepare();
            	mPlayer.start();
            	
            	long unixTime = System.currentTimeMillis() / 1000L;
            	prefs.setPlayStart(String.valueOf(unixTime));
            	
            	prefs.setActiveSong(extras.getInt("SongID"));
            	mState = State.Playing;
            	TL.incrementPlayCount(prefs.getCurrentActiveSong());
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
		
	       Intent i = new Intent(MusicPlayer.ACTION_PLAY);
	       sc.scrobbleSong(prefs.getCurrentActiveSong());
	       TL.incrementPlayCount(prefs.getCurrentActiveSong());
	       i.putExtra("SongID", engine.getNextSongInQueue());
	       getApplicationContext().startService(i); 
	}
	
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }	
    
    /*
     * Called when all Activities are unbound
     */
    public boolean onUnbind(Intent intent){
        /*
         * I don't really need this
         * If you clean up here, you will need
         * to reinitialise in onBind(), ONCE,
         * when it is next called.
         */ 
	return true;
    }    
}
