package app.minimize.com.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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