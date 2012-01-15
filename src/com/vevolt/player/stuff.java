//package com.vevolt.player;
//
//import android.app.Activity;
//import android.app.ListActivity;
//import android.content.Context;
//import android.content.Intent;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.View;
//
//import android.widget.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//public class stuff extends ListActivity {
//
//    MediaPlayer mp = new MediaPlayer();
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        populateList();
//    }
//
//    public void populateList() {
//
//        List listOfSongs = ListSongs.returnListOfSongs();
//        File returnSongList[] = ListSongs.returnSongList();
//
//        try {
//            ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, R.layout.list_item, R.id.ssongList, returnSongList);
//            //ArrayAdapter<List> adapter = new ArrayAdapter<List>(this, R.layout.list_item, R.id.ssongList, listOfSongs);
//            //setListAdapter(adapter);
//            setListAdapter(adapter);
//
//        } catch (NullPointerException e) {
//            Log.w("fallback", "the sdcard access didn't work");
//            ;
//        }
//        //ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, R.layout.list_item, R.id.ssongList, listoffiles);
//        //setListAdapter(new ArrayAdapter<String>(this, R.id.songList, COUNTRIES));
//
//        final ListView lv = getListView();
//
//        lv.setTextFilterEnabled(true);
//
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                // When clicked, show a toast with the TextView text
//                String selected = lv.getItemAtPosition(position).toString();
//
//                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//                if(audioManager.isMusicActive()) {
//                    mp.stop();
//                } else {
//                    //playSong(selected);
//                }
//                Intent intent = new Intent(music_player_ai.this, SongDetail.class);
//                intent.putExtra("country", selected);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void playSong(String songPath) {
//        try {
//
//            mp.reset();
//            mp.setDataSource(songPath);
//            mp.prepare();
//            mp.start();
//
//            // Setup listener so next song starts automatically
//            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//                public void onCompletion(MediaPlayer arg0) {
//                    mp.stop();
//                }
//
//            });
//
//        } catch (IOException e) {
//            Log.v(getString(R.string.app_name), e.getMessage());
//        }
//    }
//
//}