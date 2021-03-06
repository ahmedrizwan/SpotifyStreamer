package app.minimize.com.spotifystreamer.Adapters;

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

import app.minimize.com.spotifystreamer.Parcelables.ArtistParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;

/**
 * Created by ahmedrizwan on 6/8/15.
 */
public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.RecyclerViewHolderArtists> {

    private Context context;
    private List<ArtistParcelable> mData = Collections.emptyList();
    private ArtistsEventListener artistsEventListener;
    private String selectedArtist;

    public ArtistsAdapter(ArtistsEventListener artistsEventListener, List<ArtistParcelable> data) {
        this.artistsEventListener = artistsEventListener;
        this.context = artistsEventListener.getContext();
        mData = data;
    }

    public void updateList(List<ArtistParcelable> data) {
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
        ArtistParcelable artistModel = (ArtistParcelable) mData.get(position);
        holder.textViewArtistName.setText(artistModel.artistName);
        int images = artistModel.artistImageUrls.size();
        //if there are artistImageUrls available
        if (images > 0) {
            //first cancel the previous request
            Picasso.with(context)
                    .cancelRequest(holder.imageViewArtist);
            //request the smallest image
            Picasso.with(context)
                    .load(artistModel.artistImageUrls.get(images - 1))
                    .placeholder(R.drawable.ic_not_available)
                    .into(holder.imageViewArtist);
        } else {
            if (Utility.isVersionLollipopAndAbove())
                holder.imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available, null));
            else
                holder.imageViewArtist.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_not_available));
        }

        if (Utility.isVersionLollipopAndAbove())
            holder.imageViewArtist.setTransitionName(context.getString(R.string.artists_image_transition) + position);


        holder.itemView.setOnClickListener(view -> {
            selectedArtist = artistModel.id;
            notifyDataSetChanged();
            artistsEventListener.artistClicked(artistModel, holder);
        });

        holder.itemView.setSelected(artistModel.id.equals(selectedArtist));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public String getSelectedArtist() {
        return selectedArtist;
    }

    public void setSelectedArtist(final String selectedArtist) {
        this.selectedArtist = selectedArtist;
    }

    public class RecyclerViewHolderArtists extends RecyclerView.ViewHolder {
        public TextView textViewArtistName;
        public ImageView imageViewArtist;

        public RecyclerViewHolderArtists(final View itemView) {
            super(itemView);
            textViewArtistName = (TextView) itemView.findViewById(R.id.textViewArtistName);
            imageViewArtist = (ImageView) itemView.findViewById(R.id.imageViewArtist);
        }
    }

    //Interface for events
    public interface ArtistsEventListener {
        public void artistClicked(ArtistParcelable artistModel, RecyclerViewHolderArtists holder);

        public Context getContext();
    }

}