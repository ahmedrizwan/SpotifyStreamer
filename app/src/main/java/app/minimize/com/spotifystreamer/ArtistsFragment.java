package app.minimize.com.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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

    private static final String ARTIST = "Artists";
    private List<ArtistParcelable> mArtists = Collections.emptyList();
    private ArtistsAdapter mArtistsAdapter;
    private ImageButton mImageButtonClear;
    private ProgressBar mProgressBar;
    private TextView mTextViewError;
    private EditText mEditTextSearch;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);
        ((AppCompatActivity) getActivity()).setTitle(getString(R.string.app_name));
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            //Clear the subtitle, as I'm re-using the actionBar for both fragments
            actionBar.setSubtitle("");
        }

        if (savedInstanceState != null) {
            mArtists = savedInstanceState.getParcelableArrayList(ARTIST);
            //RecyclerView
            mArtistsAdapter = new ArtistsAdapter(this, mArtists);
        } else {
            mArtistsAdapter = new ArtistsAdapter(this, mArtists);
        }

        //Clear Button
        mImageButtonClear = (ImageButton) rootView.findViewById(R.id.imageButtonClear);

        //RecyclerView
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewArtists);

        //ProgressBar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //TextView Error Message
        mTextViewError = (TextView) rootView.findViewById(R.id.textViewError);

        //EditText
        mEditTextSearch = (EditText) rootView.findViewById(R.id.editTextSearch);

        mEditTextSearch.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                searchForArtists(mEditTextSearch.getText()
                        .toString());
                // code to hide the soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditTextSearch.getApplicationWindowToken(), 0);
                return true;
            }
            return false;
        });

        mEditTextSearch.addTextChangedListener(this);

        mImageButtonClear.setOnClickListener(view -> {
            //clear the editText
            mEditTextSearch.setText("");
            mImageButtonClear.setVisibility(View.GONE);
            mArtists = new ArrayList<ArtistParcelable>();
            mArtistsAdapter.updateList(mArtists);
            mTextViewError.setText(getString(R.string.search_artists_begin));
            mTextViewError.setVisibility(View.VISIBLE);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(mArtistsAdapter);

        searchArtistsInitialMessage();

        setRetainInstance(true);
        return rootView;
    }

    private void searchArtistsInitialMessage() {
        if (mArtists.size() == 0) {
            mTextViewError.setText(getString(R.string.search_artists_begin));
            mTextViewError.setVisibility(View.VISIBLE);
        }
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
            //method that gets me the artists and their artistImageUrls
            Utility.runOnWorkerThread(() -> {
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                //artists
                spotifyService.searchArtists(artistName, new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager artistsPager, final Response response) {
                        artistsFound(artistsPager);
                    }

                    @Override
                    public void failure(final RetrofitError error) {
                        artistsNotFound(error);
                    }
                });
                return null;
            });
        }
    }

    private void artistsNotFound(final RetrofitError error) {
        Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                mTextViewError.setText(getString(R.string.network_error));
                mTextViewError.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
            return null;
        });
    }

    private void artistsFound(final ArtistsPager artistsPager) {
        mArtists = new ArrayList<ArtistParcelable>();
        //iterate and save in the list
        for (Artist artist : artistsPager.artists.items) {
            mArtists.add(new ArtistParcelable(artist.id, artist.name, artist.images));
        }
        //update the adapter
        Utility.runOnUiThread(((AppCompatActivity) getActivity()), () -> {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void artistClicked(final ArtistParcelable artistModel, final ArtistsAdapter.RecyclerViewHolderArtists holder) {
        if (Utility.isVersionLollipopAndAbove()) {
            final TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new ChangeImageTransform());
            transitionSet.addTransition(new ChangeBounds());
            transitionSet.addTransition(new ChangeTransform());
            transitionSet.setDuration(300);

            setSharedElementReturnTransition(transitionSet);
            setSharedElementEnterTransition(transitionSet);

            TracksFragment tracksFragment = new TracksFragment();
            tracksFragment.setImageTransitionName(holder.imageViewArtist.getTransitionName());
            tracksFragment.setTextTransitionName(holder.textViewArtistName.getTransitionName());
            tracksFragment.setSharedElementEnterTransition(transitionSet);
            Bundle bundle = new Bundle();

            if (artistModel.artistImageUrls.size() > 0)
                bundle.putString(TracksFragment.IMAGE_URL, artistModel.artistImageUrls.get(artistModel.artistImageUrls.size() - 2));
            bundle.putString(TracksFragment.ARTIST_NAME, artistModel.name);
            bundle.putString(TracksFragment.ARTIST_ID, artistModel.id);
            tracksFragment.setArguments(bundle);

            tracksFragment.setSharedElementEnterTransition(transitionSet);

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container, tracksFragment)
                    .addToBackStack(null)
                    .addSharedElement(holder.imageViewArtist, holder.imageViewArtist.getTransitionName())
                    .addSharedElement(holder.textViewArtistName,
                            holder.textViewArtistName.getTransitionName())
                    .commit();
        } else {
            TracksFragment fragment = new TracksFragment();
            Bundle bundle = new Bundle();
            if (artistModel.artistImageUrls.size() > 0)
                bundle.putString(TracksFragment.IMAGE_URL, artistModel.artistImageUrls.get(artistModel.artistImageUrls.size() - 2));
            bundle.putString(TracksFragment.ARTIST_NAME, artistModel.name);
            bundle.putString(TracksFragment.ARTIST_ID, artistModel.id);
            fragment.setArguments(bundle);
            FragmentTransaction trans = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            trans.replace(R.id.container, fragment);
            trans.addToBackStack(null);
            trans.commit();
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        //save artist name for orientation change, so that the app doesn't do search again
        outState.putParcelableArrayList(ARTIST, (ArrayList<? extends Parcelable>) mArtists);
    }

    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

    }

    @Override
    public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
        if (mEditTextSearch.getText()
                .length() > 0)
            mImageButtonClear.setVisibility(View.VISIBLE);
    }

    @Override
    public void afterTextChanged(final Editable editable) {

    }
}
