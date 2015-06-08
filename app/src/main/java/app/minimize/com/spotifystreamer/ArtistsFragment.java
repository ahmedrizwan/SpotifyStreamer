package app.minimize.com.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class ArtistsFragment extends Fragment implements ArtistsAdapter.ArtistsEventListener {

    List<ArtistModel> mArtists = Collections.emptyList();
    RecyclerView mRecyclerView;
    ArtistsAdapter mArtistsAdapter;
    Thread fetchArtistsThread;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.secondary_toolbar);


        ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.button_clear);
        imageButton.setOnClickListener(view -> {

        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewArtists);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mArtistsAdapter = new ArtistsAdapter(this, mArtists);
        mRecyclerView.setAdapter(mArtistsAdapter);

        EditText editText = (EditText) rootView.findViewById(R.id.editTextSearch);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int start, final int before, final int count) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int count, final int i1, final int i2) {


            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() == 0) {
                    imageButton.setVisibility(View.GONE);
                } else {
                    imageButton.setVisibility(View.VISIBLE);
                    runOnWorkerThread(() -> {
                        SpotifyApi spotifyApi = new SpotifyApi();
                        SpotifyService spotifyService = spotifyApi.getService();
                        ArtistsPager artistsPager = spotifyService.searchArtists(editText.getText()
                                .toString());
                        mArtists = new ArrayList<ArtistModel>();
                        for (Artist artist : artistsPager.artists.items) {
                            try {
                                Log.e(artist.name, artist.images.get(artist.images.size() - 1).url);
                                mArtists.add(new ArtistModel(artist.name, artist.images.get(2).url));
                            } catch (Exception e) {
                                mArtists.add(new ArtistModel(artist.name));
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ArtistsAdapter) mRecyclerView.getAdapter()).updateList(mArtists);
                            }
                        });
                        return null;
                    });
                }
            }
        });


        return rootView;
    }

    private void runOnWorkerThread(Callable callable) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    callable.call();
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                }

            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public void artistClicked(final ArtistModel artistModel) {
        //Start Activity
        Snackbar.make(mRecyclerView, artistModel.name, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
