package com.vevolt.player;

import java.lang.ref.WeakReference;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
		options.inSampleSize = 4;
		
		class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
			
		    private final WeakReference<ImageView> imageViewReference;

		    public BitmapDownloaderTask(ImageView imageView) {
		        imageViewReference = new WeakReference<ImageView>(imageView);
		    }

		    @Override
		    // Actual download method, run in the task thread
		    protected Bitmap doInBackground(String... params) {
		         // params comes from the execute() call: params[0] is the url.
		         return getArtwork(params[0]);
		    }

		    @Override
		    // Once the image is downloaded, associates it to the imageView
		    protected void onPostExecute(Bitmap bitmap) {
		        if (isCancelled()) {
		            bitmap = null;
		        }

		        if (imageViewReference != null) {
		            ImageView imageView = imageViewReference.get();
		            if (imageView != null) {
		                imageView.setImageBitmap(bitmap);
		            }
		        }
		    }
		}
		
		
		final List songInfo = TL.getSongListInfo(Integer.parseInt(values.get(position).toString()));
		
		if (songInfo.get(2).toString().length() > 0){
			
			 ImageView trackArtwork = (ImageView) rowView.findViewById(R.id.track_artwork);
			 new BitmapDownloaderTask(trackArtwork).execute(songInfo.get(2).toString());
		}
	
		trackTitle.setText(songInfo.get(0).toString());		
		trackArtist.setText(songInfo.get(1).toString());
		
		return rowView;
	}
	
	public Bitmap getArtwork(String artworkLocation) {
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// will results in a much smaller image than the original
		options.inSampleSize = 4;
		final Bitmap b = BitmapFactory.decodeFile(artworkLocation, options);
		return b;
	}
	
}
