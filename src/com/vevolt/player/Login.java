package com.vevolt.player;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class Login extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
    }
    
    private class GetBookDataTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog dialog = new ProgressDialog(BookScanResult.this);

        private String response;
        private HttpHelper httpHelper = new HttpHelper();

        // can use UI thread here
        protected void onPreExecute() {
           dialog.setMessage("Retrieving HTTP data..");
           dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Void doInBackground(String... urls) {
           response = httpHelper.performGet(urls[0]);
           // use the response here if need be, parse XML or JSON, etc
           return null;
        }

        // can use UI thread here
        protected void onPostExecute(Void unused) {
           dialog.dismiss();
           if (response != null) {
              // use the response back on the UI thread here
              outputTextView.setText(response);
           }
        }
     }        
	
}
