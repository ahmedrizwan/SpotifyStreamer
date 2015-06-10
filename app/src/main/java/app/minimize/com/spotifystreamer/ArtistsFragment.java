package app.minimize.com.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ArtistsFragment extends Fragment implements ArtistsAdapter.ArtistsEventListener, TextWatcher {

    private List<Artist> mArtists = Collections.emptyList();
    private ArtistsAdapter mArtistsAdapter;
    private ImageButton mImageButtonClear;
    private ProgressBar mProgressBar;
    private TextView mTextViewError;
    private EditText mEditTextSearch;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        //Clear Button
        mImageButtonClear = (ImageButton) rootView.findViewById(R.id.imageButtonClear);

        //RecyclerView
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewArtists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mArtistsAdapter = new ArtistsAdapter(this, mArtists);
        recyclerView.setAdapter(mArtistsAdapter);

        //ProgressBar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //TextView Error Message
        mTextViewError = (TextView) rootView.findViewById(R.id.textViewError);
        searchArtistsInitialMessage();

        //EditText
        mEditTextSearch = (EditText) rootView.findViewById(R.id.editTextSearch);
        mEditTextSearch.addTextChangedListener(this);

        mImageButtonClear.setOnClickListener(view -> {
            //clear the editText
            mEditTextSearch.setText("");
            mImageButtonClear.setVisibility(View.GONE);
        });

        setRetainInstance(true);
        return rootView;
    }

    private void searchArtistsInitialMessage() {
        mTextViewError.setText(getString(R.string.search_artists_begin));
        mTextViewError.setVisibility(View.VISIBLE);
    }

    private void searchForArtists(final String artistName) {
        mTextViewError.setVisibility(View.GONE);
        if (artistName.length() == 0) {
            mImageButtonClear.setVisibility(View.GONE);
            mArtists = new ArrayList<>();
            mArtistsAdapter.updateList(mArtists);
            mProgressBar.setVisibility(View.GONE);
            searchArtistsInitialMessage();
        } else {
            mImageButtonClear.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            //method that gets me the artists and their images
            Utility.runOnWorkerThread(() -> {
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                //artists
                spotifyService.searchArtists(artistName, new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager artistsPager, final Response response) {
                        mArtists = new ArrayList<Artist>();
                        //iterate and save in the list
                        for (Artist artist : artistsPager.artists.items) {
                            mArtists.add(artist);
                        }

                        //update the adapter
                        Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
                            //if no artists found, show textViewError
                            if (mArtists.size() == 0) {
                                mTextViewError.setText(getString(R.string.tv_no_artists));
                                mTextViewError.setVisibility(View.VISIBLE);
                            }

                            mArtistsAdapter.updateList(mArtists);
                            mProgressBar.setVisibility(View.GONE);
                            return null;
                        });
                    }

                    @Override
                    public void failure(final RetrofitError error) {
                        Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
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
    public void artistClicked(final Artist artistModel, final RecyclerViewHolderArtists holder,ImageView imageView) {
        final TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new ChangeImageTransform());
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.addTransition(new ChangeTransform());
        transitionSet.setDuration(300);

        setSharedElementReturnTransition(transitionSet);
        setSharedElementEnterTransition(transitionSet);

        Fragment fragment = new TracksFragment();
        fragment.setSharedElementEnterTransition(transitionSet);
        Bundle bundle = new Bundle();

        if (artistModel.images.size() > 0)
            bundle.putString(TracksActivity.IMAGE_URL, artistModel.images.get(artistModel.images.size() - 2).url);
        bundle.putString(TracksActivity.ARTIST_NAME, artistModel.name);
        bundle.putString(TracksActivity.ARTIST_ID, artistModel.id);
        fragment.setArguments(bundle);

        fragment.setSharedElementEnterTransition(transitionSet);

        FragmentTransaction trans = getActivity().getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.container, fragment);
        trans.addToBackStack(null);
        trans.addSharedElement(imageView, getString(R.string.artists_image_transition));
        trans.commit();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

    }

    @Override
    public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
        searchForArtists(charSequence.toString());
    }

    @Override
    public void afterTextChanged(final Editable editable) {

    }


}
