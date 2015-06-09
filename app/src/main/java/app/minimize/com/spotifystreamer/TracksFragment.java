package app.minimize.com.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TracksFragment extends Fragment implements TracksAdapter.TracksEventListener {

    String mUrl, mName, mId;

    RecyclerView mRecyclerViewTracks;
    TracksAdapter mTracksAdapter;
    List<Track> mData = Collections.emptyList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        ImageView imageViewArtist = (ImageView) rootView.findViewById(R.id.imageViewArtist);
        TextView textViewName = (TextView) rootView.findViewById(R.id.textViewArtistName);

        Intent activityIntent = getActivity().getIntent();
        mUrl = activityIntent.getStringExtra(TracksActivity.IMAGE_URL);
        mName = activityIntent.getStringExtra(TracksActivity.ARTIST_NAME);
        mId = activityIntent.getStringExtra(TracksActivity.ARTIST_ID);

        if (mUrl != null && !mUrl.equals(""))
            Picasso.with(getActivity())
                    .load(mUrl)
                    .into(imageViewArtist);
        if (mName != null)
            textViewName.setText(mName);

        mRecyclerViewTracks = (RecyclerView) rootView.findViewById(R.id.recyclerViewTracks);
        mRecyclerViewTracks.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTracksAdapter = new TracksAdapter(this, mData, mName, mUrl);
        mRecyclerViewTracks.setAdapter(mTracksAdapter);
        loadTracks();

        return rootView;
    }

    private void loadTracks() {
        Utility.runOnWorkerThread(() -> {
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");
            Tracks tracks = spotifyService.getArtistTopTrack(mId, options);
            mData = new ArrayList<Track>();
            for (Track track : tracks.tracks) {
                Log.e("Track", track.name);
                mData.add(track);
            }

            Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                ((TracksAdapter) mRecyclerViewTracks.getAdapter()).updateList(mData);
                return null;
            });


            return null;
        });
    }

    @Override
    public void trackClicked(final Track track, final RecyclerViewHolderTracks holder) {
        Toast.makeText(getActivity(), "Track Clicked!", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
