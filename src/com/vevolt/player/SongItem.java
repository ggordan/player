package com.vevolt.player;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Class for handling a custom view for track lists
 * 
 */
public class SongItem extends ArrayAdapter {

	private TrackList TL;
	private final Context context;
	private final List values;
	
	/**
	 * Constructor for class
	 * @param context Pass in application context
	 * @param values Pass in List values
	 */
	public SongItem(Context context, List values) {
		
		super(context, R.layout.list_item, values);

		this.context = context;
		this.values = values;
		
		TL = new TrackList(this.context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_item, parent, false);
		
		TextView trackTitle = (TextView) rowView.findViewById(R.id.track_title);
		TextView trackArtist = (TextView) rowView.findViewById(R.id.track_artist);
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// will results in a much smaller image than the original
		options.inSampleSize = 8;
		
		final List songInfo = TL.getSongListInfo(Integer.parseInt(values.get(position).toString()));
		
		// don't ever use a path to /sdcard like this, but I'm sure you have a sane way to do that
		// in this case nebulae.jpg is a 19MB 8000x3874px image
		if (songInfo.get(2).toString().length() > 0){
			 new Thread(new Runnable() {
				    public void run() {
						final Bitmap b = BitmapFactory.decodeFile(songInfo.get(2).toString(), options);
				      rowView.post(new Runnable() {
				        public void run() {
				    		ImageView trackArtwork = (ImageView) rowView.findViewById(R.id.track_artwork);
				    		trackArtwork.setImageBitmap(b);
				        }
				      });
				    }
				  }).start();
		}
	
		trackTitle.setText(songInfo.get(0).toString());		
		trackArtist.setText(songInfo.get(1).toString());
		
		return rowView;
	}
	
}
