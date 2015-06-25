package app.minimize.com.spotifystreamer.Fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.Adapters.TracksAdapter;
import app.minimize.com.spotifystreamer.Parcelables.ArtistParcelable;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;
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
    private static final String TAG = "TracksFragment";

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

    private TracksAdapter mTracksAdapter;
    private List<TrackParcelable> mData = Collections.emptyList();
    private String imageTransitionName;
    private String textTransitionName;
    private ArtistParcelable mArtistParcelable;
    private boolean isTwoPane = false;
    private int vibrantColor = Color.BLACK;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        ButterKnife.inject(this, rootView);

        //Restore state
        if (savedInstanceState != null) {
            //Artist
            mArtistParcelable = savedInstanceState.getParcelable(Keys.KEY_ARTIST_PARCELABLE);
            //Tracks
            mData = savedInstanceState.getParcelableArrayList(TRACKS);

        } else {
            //Get Artist info from the arguments
            Bundle activityIntent = getArguments();
            mArtistParcelable = activityIntent.getParcelable(Keys.KEY_ARTIST_PARCELABLE);
            //load up color for actionBar and status bar
            vibrantColor = activityIntent.getInt(Keys.COLOR_ACTION_BAR);
            //First launch so load tracks
            progressBar.setVisibility(View.VISIBLE);
            loadTracks();
        }

        isTwoPane = ((ContainerActivity) getActivity()).isTwoPane();
        //ActionBar
        refreshActionBar();


        //Message TextView
        if ((mData != null ? mData.size() : 0) == 0)
            textViewError.setVisibility(View.VISIBLE);

        //Transition
        if (Utility.isVersionLollipopAndAbove()) {
            imageViewArtist.setTransitionName(imageTransitionName);
        }

        //Artist Image
        loadArtistImage();

        //RecyclerView
        recyclerViewTracks.hasFixedSize();
        recyclerViewTracks.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTracksAdapter = new TracksAdapter(this, mData);
        recyclerViewTracks.setAdapter(mTracksAdapter);

        //NowPlaying view check if should be visible or not
        ((ContainerActivity) getActivity()).
                startServiceForStatusRetrieval();

        return rootView;
    }

    private void loadArtistImage() {
        int size = mArtistParcelable.artistImageUrls.size();

        if (size > 0) {
            Utility.loadImage(getActivity(),
                    mArtistParcelable.artistImageUrls.get(size - 1),
                    mArtistParcelable.artistImageUrls.get(0),
                    imageViewArtist,
                    null);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putParcelable(Keys.KEY_ARTIST_PARCELABLE, mArtistParcelable);
            outState.putParcelableArrayList(TRACKS, (ArrayList<? extends Parcelable>) mData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTracks() {
        Utility.runOnWorkerThread(() -> {
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, "US");

            spotifyService.getArtistTopTrack(mArtistParcelable.id, options, new Callback<Tracks>() {
                @Override
                public void success(final Tracks tracks, final Response response) {
                    mData = new ArrayList<TrackParcelable>();
                    for (Track track : tracks.tracks) {
                        mData.add(new TrackParcelable(track));
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

                    //Handle network error
                    if (error.getKind() == RetrofitError.Kind.NETWORK) {
                        textViewError.setText(getString(R.string.network_error));
                        textViewError.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }

                }
            });
            return null;
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void trackClicked(final TrackParcelable track, final TracksAdapter.RecyclerViewHolderTracks holder) {
        if (isTwoPane) {
            //launch playerDialogFragment
            PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.key_tracks_parcelable), track);
            bundle.putParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST, mTracksAdapter.getDataSet());
            Utility.runOnWorkerThread(() -> {
                int vibrantColor1 = Palette.from(((BitmapDrawable) holder.imageViewAlbum.getDrawable()).getBitmap())
                        .generate()
                        .getVibrantColor(Color.BLACK);
                bundle.putInt(Keys.COLOR_ACTION_BAR, vibrantColor1);
                playerDialogFragment.setArguments(bundle);
                playerDialogFragment.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(),"Player");
                return null;
            });


        } else {
            //Launch the dialogFragment from here
            PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.key_tracks_parcelable), track);
            bundle.putParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST, mTracksAdapter.getDataSet());
            Utility.runOnWorkerThread(new Callable() {
                @Override
                public Object call() throws Exception {
                    int vibrantColor = Palette.from(((BitmapDrawable) holder.imageViewAlbum.getDrawable()).getBitmap())
                            .generate()
                            .getVibrantColor(Color.BLACK);
                    bundle.putInt(Keys.COLOR_ACTION_BAR, vibrantColor);
                    return null;
                }
            });

            playerDialogFragment.setArguments(bundle);
            playerDialogFragment.setImageViewAlbumTransitionName(holder.imageViewAlbum.getTransitionName());
            Utility.launchFragmentWithSharedElements(isTwoPane, this,
                    playerDialogFragment, R.id.container, holder.imageViewAlbum);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume TracksFragment");
    }

    public void refreshActionBar() {
        //ActionBar
        ((AppCompatActivity) getActivity()).setTitle(getString(R.string.activity_tracks_title));
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (isTwoPane) {
                //handle tablets here

            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
                Utility.setActionBarAndStatusBarColor(((AppCompatActivity) getActivity()), vibrantColor);
            }
            actionBar.setTitle(getString(R.string.activity_tracks_title));
            actionBar.setSubtitle(mArtistParcelable.artistName);
        }
    }
}
