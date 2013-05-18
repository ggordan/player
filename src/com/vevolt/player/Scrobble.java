package com.vevolt.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.CallException;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.cache.Cache;
import de.umass.lastfm.scrobble.ScrobbleResult;
import de.umass.lastfm.scrobble.Scrobbler;

import android.app.Activity;
import android.content.Context;
import android.database.CursorJoiner.Result;
import android.util.Log;

public class Scrobble extends Activity {

	final static String Key = "09ab65e7929a1953c1fd08fa1333ac2e";
	final static String Secret = "681ac00f3096bbf784b6a7766d80bf72";
	final static String URL = "http://ws.audioscrobbler.com/2.0/";
	
	HttpClient httpclient;
	HttpPost httppost;
	HttpGet httpget;
	Preferences prefs;
	TrackList TL;
	public Scrobble(Context context) {
		
		httpclient = new DefaultHttpClient();
		httppost = new HttpPost(URL);
		prefs = new Preferences(context);
		TL = new TrackList(context);
		
	}
	
	public void getSessionKey() {
		new Thread(new Runnable() {
			
		    public void run() {
				try {
				    
					String authToken = md5(prefs.getUsername() + md5(prefs.getpassword()));
					String api_sig = md5("api_key" + Key + "authToken" + authToken + "methodauth.getMobileSessionusername" + prefs.getUsername() + Secret);
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("api_key", Key));
					nameValuePairs.add(new BasicNameValuePair("api_sig", api_sig));
					nameValuePairs.add(new BasicNameValuePair("authToken", authToken));	
					nameValuePairs.add(new BasicNameValuePair("method", "auth.getMobileSession"));
					nameValuePairs.add(new BasicNameValuePair("username", prefs.getUsername()));
					
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					BufferedReader BR = new BufferedReader(new InputStreamReader(is));
					StringBuilder SB = new StringBuilder();
					String line1 = null;

					while ((line1 = BR.readLine()) != null) {
						SB.append(line1 + "\n");
						Log.w("a", (line1 + "\n"));
					}
					
					Integer LoopCounter = 0;
					String[] tokens = SB.toString().split("\n");
					
					if (tokens.length == 7) {
						for (String t : tokens) {
							if (LoopCounter == 4) {
								t = t.replace(" ", "");
								t = t.replace("<key>", "");
								t = t.replace("</key>", ""); 
								prefs.setSession(t);
							}
							LoopCounter++;  
						}
					} else {
						prefs.setSession("SessionInvalid");
					}
					
					
				} catch (IOException e) { e.printStackTrace(); }
		    }
		  }).start();
	}
	
	public void scrobbleSong(Integer trackID) {
		
		final Integer songID = trackID;
		
		new Thread(new Runnable() {
		    public void run() {
				try {
									
					
					List detail = TL.getSongDetail(songID);

					String api_sig = md5("api_key" + Key + "artist" + detail.get(1).toString() + "methodtrack.scrobble" + "timestamp" + prefs.getPlayStart() + "track" + detail.get(0).toString() + "sk" + prefs.getSession() + Secret);
					
					//String api_sig = md5("artist" + detail.get(1).toString() + "track" + detail.get(0).toString() + "timestamp" + prefs.getPlayStart() + "methodtrack.scrobble" + "api_key" + Key + "sk" + prefs.getSession() + Secret);
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("api_key", Key));
					nameValuePairs.add(new BasicNameValuePair("api_sig", api_sig));
					nameValuePairs.add(new BasicNameValuePair("artist", detail.get(1).toString()));
					nameValuePairs.add(new BasicNameValuePair("method", "track.scrobble"));
					nameValuePairs.add(new BasicNameValuePair("timestamp", prefs.getPlayStart()));
					nameValuePairs.add(new BasicNameValuePair("track", detail.get(0).toString()));
					nameValuePairs.add(new BasicNameValuePair("sk", prefs.getSession()));
			
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					BufferedReader BR = new BufferedReader(new InputStreamReader(is));
					StringBuilder SB = new StringBuilder();
					String line1 = null;

					while ((line1 = BR.readLine()) != null) {
						SB.append(line1 + "\n");
					}
					
					Log.w("sds", SB.toString());
					
				} catch (IOException e) { e.printStackTrace(); }
		    }
		  }).start();
	}	
	
	public void loveSong(Integer trackID) {
		
		final Integer songID = trackID;
		
		new Thread(new Runnable() {
		    public void run() {
				try {
									
					
					List detail = TL.getSongDetail(songID);

					Log.w("Session", prefs.getSession());
					//String api_sig = md5("artist" + detail.get(1).toString() + "track" + detail.get(0).toString() + "timestamp" + prefs.getPlayStart() + "methodtrack.scrobble" + "api_key" + Key + "sk" + prefs.getSession() + Secret);
					String api_sig = md5("api_key" + Key + "artist" + detail.get(1).toString() + "methodtrack.lovetrack" + detail.get(0).toString() + "sk" + prefs.getSession() + Secret);

					Log.w("key", api_sig);
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("api_key", Key));
					nameValuePairs.add(new BasicNameValuePair("api_sig", api_sig));
					nameValuePairs.add(new BasicNameValuePair("artist", detail.get(1).toString()));
					nameValuePairs.add(new BasicNameValuePair("method", "track.love"));
					nameValuePairs.add(new BasicNameValuePair("track", detail.get(0).toString()));
					nameValuePairs.add(new BasicNameValuePair("sk", prefs.getSession()));
			
			        httppost.setHeader("User-Agent", "ggordan");
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					BufferedReader BR = new BufferedReader(new InputStreamReader(is));
					StringBuilder SB = new StringBuilder();
					String line1 = null;
					while ((line1 = BR.readLine()) != null) {
						SB.append(line1 + "\n");
					}
					
					Log.w("sds", SB.toString());
					
				} catch (IOException e) { e.printStackTrace(); }
		    }
		  }).start();
	}		
	
	
	public String md5(String in) {
		
	    MessageDigest digest;
	    try {
	        digest = MessageDigest.getInstance("MD5");
	        digest.reset();
	        digest.update(in.getBytes());
	        byte[] a = digest.digest();
	        int len = a.length;
	        StringBuilder sb = new StringBuilder(len << 1);
	        for (int i = 0; i < len; i++) {
	            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
	            sb.append(Character.forDigit(a[i] & 0x0f, 16));
	        }
	        return sb.toString();
	    } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
	    return null;
	}
}
