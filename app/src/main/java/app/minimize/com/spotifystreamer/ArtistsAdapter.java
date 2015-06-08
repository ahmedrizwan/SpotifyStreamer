package app.minimize.com.spotifystreamer;

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

/**
 * Created by ahmedrizwan on 6/8/15.
 */
public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.RecyclerViewHolder> {

    private Context context;
    private List<ArtistModel> mData = Collections.emptyList();

    private ArtistsEventListener artistsEventListener;

    public ArtistsAdapter(ArtistsEventListener artistsEventListener, List<ArtistModel> data) {
        this.artistsEventListener = artistsEventListener;
        this.context = artistsEventListener.getContext();
        mData = data;
    }

    public void updateList(List<ArtistModel> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(final ViewGroup parent,
                                                 final int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_artist, parent, false);

        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        ArtistModel artistModel = (ArtistModel) mData.get(position);
        holder.textViewArtistName.setText(artistModel.name);
        if (!artistModel.imageUrl.equals("")) {
            //first cancel the previous request
            Picasso.with(context)
                    .cancelRequest(holder.imageViewArtist);
            //request the image
            Picasso.with(context)
                    .load(artistModel.imageUrl)
                    .into(holder.imageViewArtist);
        } else {

            holder.imageViewArtist.setImageDrawable(context.getResources()
                    .getDrawable(R.drawable.ic_not_available));
        }

        holder.itemView.setOnClickListener(view -> {
            artistsEventListener.artistClicked(artistModel);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return super.getItemViewType(position);
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewArtistName;
        ImageView imageViewArtist;
        public RecyclerViewHolder(final View itemView) {
            super(itemView);
            textViewArtistName = (TextView) itemView.findViewById(R.id.textViewArtistName);
            imageViewArtist = (ImageView) itemView.findViewById(R.id.imageViewArtist);
        }
    }

    public static interface ArtistsEventListener{
        public void artistClicked(ArtistModel artistModel);
        public Context getContext();
    }

}