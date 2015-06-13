package app.minimize.com.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
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

    @InjectView(R.id.recyclerViewTracks)
    RecyclerView recyclerViewTracks;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.textViewError)
    TextView textViewError;
    @InjectView(R.id.imageViewArtist)
    ImageView imageViewArtist;
    @InjectView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @InjectView(R.id.appbar)
    AppBarLayout appbar;
    @InjectView(R.id.main_content)
    CoordinatorLayout mainContent;

    private String mUrl, mName, mId;
    private TracksAdapter mTracksAdapter;
    private List<TrackParcelable> mData = Collections.emptyList();
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
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(mName);
        }

        recyclerViewTracks.hasFixedSize();
        recyclerViewTracks.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Restore state
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(IMAGE_URL);
            mName = savedInstanceState.getString(ARTIST_NAME);
            mId = savedInstanceState.getString(ARTIST_ID);
            mData = savedInstanceState.getParcelableArrayList(TRACKS);
            if ((mData != null ? mData.size() : 0) == 0)
                textViewError.setVisibility(View.VISIBLE);
        } else {
            //Get Artist info from the arguments
            Bundle activityIntent = getArguments();
            mUrl = activityIntent.getString(IMAGE_URL);
            mName = activityIntent.getString(ARTIST_NAME);
            mId = activityIntent.getString(ARTIST_ID);
            progressBar.setVisibility(View.VISIBLE);
            loadTracks();
        }

        if (Utility.isVersionLollipopAndAbove()) {
            imageViewArtist.setTransitionName(imageTransitionName);
        }

        if (mUrl != null && !mUrl.equals(""))
            Picasso.with(getActivity())
                    .load(mUrl)
                    .into(imageViewArtist);

        if (mUrl == null)
            mUrl = "";

        mTracksAdapter = new TracksAdapter(this, mData);
        recyclerViewTracks.setAdapter(mTracksAdapter);
        ButterKnife.inject(this, rootView);
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
                            textViewError.setText(getString(R.string.tv_no_tracks));
                            textViewError.setVisibility(View.VISIBLE);
                        }

                        progressBar.setVisibility(View.GONE);
                        ((TracksAdapter) recyclerViewTracks.getAdapter()).updateList(mData);
                        return null;
                    });
                }

                @Override
                public void failure(final RetrofitError error) {
                    Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                        //Handle network error
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            textViewError.setText(getString(R.string.network_error));
                            textViewError.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
