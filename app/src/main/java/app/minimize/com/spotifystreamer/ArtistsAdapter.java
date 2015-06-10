package app.minimize.com.spotifystreamer;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by ahmedrizwan on 6/8/15.
 */
public class ArtistsAdapter extends RecyclerView.Adapter<RecyclerViewHolderArtists> {

    private Context context;
    private List<Artist> mData = Collections.emptyList();

    private ArtistsEventListener artistsEventListener;

    public ArtistsAdapter(ArtistsEventListener artistsEventListener, List<Artist> data) {
        this.artistsEventListener = artistsEventListener;
        this.context = artistsEventListener.getContext();
        mData = data;
    }

    public void updateList(List<Artist> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolderArtists onCreateViewHolder(final ViewGroup parent,
                                                        final int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_artist, parent, false);
        return new RecyclerViewHolderArtists(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolderArtists holder, final int position) {
        Artist artistModel = (Artist) mData.get(position);
        holder.textViewArtistName.setText(artistModel.name);
        int images = artistModel.images.size();
        //if there are images available
        if (images > 0) {
            //first cancel the previous request
            Picasso.with(context)
                    .cancelRequest(holder.imageViewArtist);
            //request the smallest image
            Picasso.with(context)
                    .load(artistModel.images.get(images - 1).url)
                    .into(holder.imageViewArtist);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                holder.imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available, null));
            else
                holder.imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available));
        }

        holder.itemView.setOnClickListener(view -> {
            artistsEventListener.artistClicked(artistModel, holder, holder.imageViewArtist);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    //Interface for events
    public static interface ArtistsEventListener {
        public void artistClicked(Artist artistModel, RecyclerViewHolderArtists holder, ImageView imageView);

        public Context getContext();
    }

}