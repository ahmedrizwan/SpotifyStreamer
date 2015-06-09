package app.minimize.com.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class ArtistsFragment extends Fragment implements ArtistsAdapter.ArtistsEventListener {

    List<Artist> mArtists = Collections.emptyList();
    RecyclerView mRecyclerView;
    ArtistsAdapter mArtistsAdapter;
    Thread fetchArtistsThread;

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
                    //method that gets me the artists and their images
                    Utility.runOnWorkerThread(() -> {
                        SpotifyApi spotifyApi = new SpotifyApi();
                        SpotifyService spotifyService = spotifyApi.getService();
                        //artists
                        ArtistsPager artistsPager = spotifyService.searchArtists(editText.getText()
                                .toString());
                        mArtists = new ArrayList<Artist>();
                        //iterate and save in the list
                        for (Artist artist : artistsPager.artists.items) {
                            mArtists.add(artist);
                        }
                        //
                        Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                            ((ArtistsAdapter) mRecyclerView.getAdapter()).updateList(mArtists);
                            return null;
                        });
                        return null;
                    });
                }
            }
        });
        return rootView;
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
    public void artistClicked(final Artist artistModel, final RecyclerViewHolderArtists holder) {
        if (!((ArtistsActivity) getActivity()).isTwoPane()) {
            //Start Activity
            Intent intent = new Intent(getActivity(), TracksActivity.class);
            // Pass data object in the bundle and populate details activity.
            if (artistModel.images.size() > 0)
                intent.putExtra(TracksActivity.IMAGE_URL, artistModel.images.get(artistModel.images.size()-2).url);
            intent.putExtra(TracksActivity.ARTIST_NAME, artistModel.name);
            intent.putExtra(TracksActivity.ARTIST_ID,artistModel.id);

            Pair<View, String> pairImageView = new Pair<>(holder.imageViewArtist, getString(R.string.artists_image_transition));
            Pair<View, String> pairTextView = new Pair<>(holder.textViewArtistName, getString(R.string.artists_text_transition));

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), pairImageView, pairTextView);
            getActivity().startActivity(intent, options.toBundle());
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
