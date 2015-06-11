package app.minimize.com.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by ahmedrizwan on 6/8/15.
 */
public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.RecyclerViewHolderArtists> {

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
                .inflate(R.layout.item_view_artist, null);
        return new RecyclerViewHolderArtists(view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
            if (Utility.isVersionLollipopAndAbove())
                holder.imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available, null));
            else
                holder.imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available));
        }

        if(Utility.isVersionLollipopAndAbove()) {
            holder.imageViewArtist.setTransitionName(context.getString(R.string.artists_image_transition) + position);
            holder.textViewArtistName.setTransitionName(context.getString(R.string.artists_text_transition) + position);
        }

        holder.itemView.setOnClickListener(view -> {
            artistsEventListener.artistClicked(artistModel, holder);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class RecyclerViewHolderArtists extends RecyclerView.ViewHolder {
        TextView textViewArtistName;
        ImageView imageViewArtist;
        public RecyclerViewHolderArtists(final View itemView) {
            super(itemView);
            textViewArtistName = (TextView) itemView.findViewById(R.id.textViewArtistName);
            imageViewArtist = (ImageView) itemView.findViewById(R.id.imageViewArtist);
        }
    }

    //Interface for events
    public static interface ArtistsEventListener {
        public void artistClicked(Artist artistModel, RecyclerViewHolderArtists holder);

        public Context getContext();
    }


}