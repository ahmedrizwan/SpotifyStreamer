package app.minimize.com.spotifystreamer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ahmedrizwan on 6/9/15.
 */
public class TracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private List<Track> mData = Collections.emptyList();

    private TracksEventListener tracksEventListener;
    private String artistName;
    private String artistUrl;

    public TracksAdapter(TracksEventListener tracksEventListener, List<Track> data, String artistName, String artistUrl) {
        this.tracksEventListener = tracksEventListener;
        this.artistName = artistName;
        this.artistUrl = artistUrl;
        this.context = tracksEventListener.getContext();
        mData = data;
    }

    public void updateList(List<Track> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                 final int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_view_track, parent, false);
            return new RecyclerViewHolderTracks(view);
        } else if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_view_track_header, parent, false);
            return new RecyclerViewHolderArtists(view);
        }

        throw new RuntimeException("ViewType Incorrect");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof RecyclerViewHolderTracks) {
            Track track = (Track) mData.get(position-1);
            ((RecyclerViewHolderTracks) holder).textViewTrackName.setText(track.name);
            ((RecyclerViewHolderTracks) holder).textViewTrackAlbum.setText(track.album.name);
            int images = track.album.images.size();
            //if there are images available
            if (images > 0) {
                //first cancel the previous request
                Picasso.with(context)
                        .cancelRequest(((RecyclerViewHolderTracks) holder).imageViewAlbum);
                //request the smallest image
                Picasso.with(context)
                        .load(track.album.images.get(images - 1).url)
                        .into(((RecyclerViewHolderTracks) holder).imageViewAlbum);
            } else {
                ((RecyclerViewHolderTracks) holder).imageViewAlbum.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available));
            }

            holder.itemView.setOnClickListener(view -> {
                tracksEventListener.trackClicked(track, ((RecyclerViewHolderTracks) holder));
            });

        } else {
            //header
            ((RecyclerViewHolderArtists) holder).textViewArtistName.setText(artistName);
            //if there are images available
            if (!artistUrl.equals("")) {
                //first cancel the previous request
                Picasso.with(context)
                        .cancelRequest(((RecyclerViewHolderArtists) holder).imageViewArtist);
                //request the smallest image
                Picasso.with(context)
                        .load(artistUrl)
                        .into(((RecyclerViewHolderArtists) holder).imageViewArtist);
            } else {
                ((RecyclerViewHolderArtists) holder).imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available));
            }
        }
    }

    @Override
    public int getItemCount() {
        //return size of items + 1 header
        return mData.size()+1;
    }

    @Override
    public int getItemViewType(final int position) {
        if(position==0)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    //Interface for events
    public static interface TracksEventListener{
        public void trackClicked(Track track, RecyclerViewHolderTracks holder);
        public Context getContext();
    }

}