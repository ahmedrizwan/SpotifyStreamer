package app.minimize.com.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerViewHolderArtists extends RecyclerView.ViewHolder {
        TextView textViewArtistName;
        ImageView imageViewArtist;
        public RecyclerViewHolderArtists(final View itemView) {
            super(itemView);
            textViewArtistName = (TextView) itemView.findViewById(R.id.textViewArtistName);
            imageViewArtist = (ImageView) itemView.findViewById(R.id.imageViewArtist);
        }
    }