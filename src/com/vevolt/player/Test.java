//package com.vevolt.player;
//
//import java.util.Random;
//
//import android.app.ListActivity;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.widget.SimpleCursorAdapter;
//
//public class Test extends ListActivity {
//    private MPDAdapter db;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        db = new MPDAdapter(this, "queue");
//        db.open();
//
//    }
//
//    @Override
//    protected void onPause() {
//        db.close();
//        super.onPause();
//    }
//
//}