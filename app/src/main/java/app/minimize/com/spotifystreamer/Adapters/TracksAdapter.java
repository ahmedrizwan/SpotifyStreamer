package app.minimize.com.spotifystreamer.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;

/**
 * Created by ahmedrizwan on 6/9/15.
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.RecyclerViewHolderTracks> {

    private Context context;
    private List<TrackParcelable> mData = Collections.emptyList();

    private TracksEventListener tracksEventListener;
    private String artistName;
    private String artistUrl;

    public TracksAdapter(TracksEventListener tracksEventListener, List<TrackParcelable> data) {
        this.tracksEventListener = tracksEventListener;
        this.artistName = artistName;
        this.artistUrl = artistUrl;
        this.context = tracksEventListener.getContext();
        mData = data;
    }

    public void updateList(List<TrackParcelable> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolderTracks onCreateViewHolder(final ViewGroup parent,
                                                 final int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_view_track, parent, false);
            return new RecyclerViewHolderTracks(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolderTracks holder, final int position) {
            TrackParcelable track = (TrackParcelable) mData.get(position);
            holder.textViewTrackName.setText(track.songName);
            holder.textViewTrackAlbum.setText(track.albumName);
            int images = track.albumImageUrls.size();
            //if there are artistImageUrls available
            if (images > 0) {
                //first cancel the previous request
                Picasso.with(context)
                        .cancelRequest((holder).imageViewAlbum);
                //request the smallest image
                Picasso.with(context)
                        .load(track.albumImageUrls.get(images - 1))
                        .into((holder).imageViewAlbum);
            } else {
                (holder).imageViewAlbum.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available));
            }

            holder.itemView.setOnClickListener(view -> {
                tracksEventListener.trackClicked(track, (holder));
            });
    }

    @Override
    public int getItemCount() {
        //return size of items + 1 header
        return mData.size();
    }


    //Interface for events
    public static interface TracksEventListener{
        public void trackClicked(TrackParcelable track, RecyclerViewHolderTracks holder);
        public Context getContext();
    }

    public class RecyclerViewHolderTracks extends RecyclerView.ViewHolder {

        TextView textViewTrackName;
        TextView textViewTrackAlbum;
        ImageView imageViewAlbum;

        public RecyclerViewHolderTracks(final View itemView) {
            super(itemView);
            textViewTrackName = (TextView) itemView.findViewById(R.id.textViewTrackName);
            textViewTrackAlbum = (TextView) itemView.findViewById(R.id.textViewTrackAlbum);
            imageViewAlbum = (ImageView) itemView.findViewById(R.id.imageViewAlbum);
        }
    }
}