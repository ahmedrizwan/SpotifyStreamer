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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TracksFragment extends Fragment implements TracksAdapter.TracksEventListener {

    private String mUrl, mName, mId;
    private RecyclerView mRecyclerViewTracks;
    private TracksAdapter mTracksAdapter;
    private List<Track> mData = Collections.emptyList();
    private ProgressBar mProgressBar;
    private TextView mTextViewError;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        //ProgressBar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        //TextView Error Message
        mTextViewError = (TextView) rootView.findViewById(R.id.textViewError);

        //Get Artist info from the parent activity
        Intent activityIntent = getActivity().getIntent();
        mUrl = activityIntent.getStringExtra(TracksActivity.IMAGE_URL);
        mName = activityIntent.getStringExtra(TracksActivity.ARTIST_NAME);
        mId = activityIntent.getStringExtra(TracksActivity.ARTIST_ID);

        //Recycler View init
        mRecyclerViewTracks = (RecyclerView) rootView.findViewById(R.id.recyclerViewTracks);
        mRecyclerViewTracks.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (mUrl == null)
            mUrl = "";

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
            options.put(SpotifyService.COUNTRY, "US");
            spotifyService.getArtistTopTrack(mId, options, new Callback<Tracks>() {
                @Override
                public void success(final Tracks tracks, final Response response) {
                    mData = new ArrayList<Track>();
                    for (Track track : tracks.tracks) {
                        Log.e("Track", track.name);
                        mData.add(track);
                    }
                    Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                        mProgressBar.setVisibility(View.GONE);
                        ((TracksAdapter) mRecyclerViewTracks.getAdapter()).updateList(mData);
                        return null;
                    });
                }

                @Override
                public void failure(final RetrofitError error) {
                    Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                        //Handle network error
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            mTextViewError.setText(getString(R.string.network_error));
                            mTextViewError.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);
                        }
                        return null;
                    });
                }
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
