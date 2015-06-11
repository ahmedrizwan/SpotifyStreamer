package app.minimize.com.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TracksFragment extends Fragment implements TracksAdapter.TracksEventListener {

    public static final String IMAGE_URL = "ImageUrl";
    public static final String ARTIST_NAME = "ArtistName";
    public static final String ARTIST_ID = "ArtistId";
    private static final String TRACKS = "Tracks";

    private String mUrl, mName, mId;
    private RecyclerView mRecyclerViewTracks;
    private TracksAdapter mTracksAdapter;
    private List<TrackParcelable> mData = Collections.emptyList();
    private ProgressBar mProgressBar;
    private TextView mTextViewError;
    private String imageTransitionName;
    private String textTransitionName;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        //ActionBar
        ((AppCompatActivity) getActivity()).setTitle(getString(R.string.activity_tracks_title));
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle(mName);

        //ProgressBar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //TextView Error Message
        mTextViewError = (TextView) rootView.findViewById(R.id.textViewError);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageViewArtist);

        //Recycler View init
        mRecyclerViewTracks = (RecyclerView) rootView.findViewById(R.id.recyclerViewTracks);
        mRecyclerViewTracks.hasFixedSize();
        mRecyclerViewTracks.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Restore state
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(IMAGE_URL);
            mName = savedInstanceState.getString(ARTIST_NAME);
            mId = savedInstanceState.getString(ARTIST_ID);
            mData = savedInstanceState.getParcelableArrayList(TRACKS);
            if((mData != null ? mData.size() : 0) ==0)
                mTextViewError.setVisibility(View.VISIBLE);
        } else {
            //Get Artist info from the arguments
            Bundle activityIntent = getArguments();
            mUrl = activityIntent.getString(IMAGE_URL);
            mName = activityIntent.getString(ARTIST_NAME);
            mId = activityIntent.getString(ARTIST_ID);
            mProgressBar.setVisibility(View.VISIBLE);
            loadTracks();
        }

        if (Utility.isVersionLollipopAndAbove()) {
            imageView.setTransitionName(imageTransitionName);
        }

        if (mUrl != null && !mUrl.equals(""))
            Picasso.with(getActivity())
                    .load(mUrl)
                    .into(imageView);

        if (mUrl == null)
            mUrl = "";

        mTracksAdapter = new TracksAdapter(this,mData);
        mRecyclerViewTracks.setAdapter(mTracksAdapter);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(IMAGE_URL, mUrl);
        outState.putString(ARTIST_ID, mId);
        outState.putString(ARTIST_NAME, mName);
        outState.putParcelableArrayList(TRACKS, (ArrayList<? extends Parcelable>) mData);
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
                    mData = new ArrayList<TrackParcelable>();
                    for (Track track : tracks.tracks) {
                        mData.add(new TrackParcelable(track.name, track.album.name, track.album.images));
                    }
                    Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                        if (mData.size() == 0) {
                            mTextViewError.setText(getString(R.string.tv_no_tracks));
                            mTextViewError.setVisibility(View.VISIBLE);
                        }

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
    public void trackClicked(final TrackParcelable track, final TracksAdapter.RecyclerViewHolderTracks holder) {
        Toast.makeText(getActivity(), track.songName + " Clicked!", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    public void setImageTransitionName(final String imageTransitionName) {
        this.imageTransitionName = imageTransitionName;
    }

    public void setTextTransitionName(final String textTransitionName) {
        this.textTransitionName = textTransitionName;
    }
}
