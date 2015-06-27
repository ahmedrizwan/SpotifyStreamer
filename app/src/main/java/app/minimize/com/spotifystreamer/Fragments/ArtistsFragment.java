package app.minimize.com.spotifystreamer.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.Adapters.ArtistsAdapter;
import app.minimize.com.spotifystreamer.Parcelables.ArtistParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Rx.RxObservables;
import app.minimize.com.spotifystreamer.Utility;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


public class ArtistsFragment extends Fragment implements ArtistsAdapter.ArtistsEventListener, View.OnKeyListener {

    private static final String ARTIST = "Artists";
    public static final String SELECTED_ARTIST = "SelectedArtist";

    @InjectView(R.id.imageViewSearch)
    ImageView imageViewSearch;
    @InjectView(R.id.imageButtonClear)
    ImageButton imageButtonClear;
    @InjectView(R.id.editTextSearch)
    EditText editTextSearch;
    @InjectView(R.id.secondaryToolbar)
    Toolbar secondaryToolbar;
    @InjectView(R.id.recyclerViewArtists)
    RecyclerView recyclerViewArtists;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.textViewError)
    TextView textViewError;

    List<ArtistParcelable> mArtists = Collections.emptyList();
    ArtistsAdapter mArtistsAdapter;


    @OnClick(R.id.imageButtonClear)
    public void imageButtonClearOnClick() {
        clearState();
    }

    private void clearState() {
        editTextSearch.setText("");
        imageButtonClear.setVisibility(View.GONE);
        mArtists = new ArrayList<ArtistParcelable>();
        mArtistsAdapter.updateList(mArtists);
        textViewError.setText(getString(R.string.search_artists_begin));
        textViewError.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        setActionBarTitle();
    }

    private void setActionBarTitle() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("");
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    boolean isTwoPane = false;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);
        ButterKnife.inject(this, rootView);

        //ActionBar
        setActionBarTitle();

        //is it twoPane
        isTwoPane = ((ContainerActivity) getActivity()).isTwoPane();

        if (isTwoPane) {
            //handle everything related to tablets here

        } else {
            //change actionBar and statusBar color
            Utility.setActionBarAndStatusBarColor(((AppCompatActivity) getActivity()),
                    Utility.getPrimaryColorFromSelectedTheme(getActivity()));
        }
        //retore state
        restoreState(savedInstanceState);

        editTextSearch.setOnKeyListener(this);

        Observable.create(RxObservables.getSearchObservable(editTextSearch))
                .debounce(600, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(final Throwable e) {

                    }

                    @Override
                    public void onNext(final String textArtistName) {
                        searchForArtists(textArtistName);
                        showOrHideCancel(textArtistName);
                    }
                });


        recyclerViewArtists.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerViewArtists.setAdapter(mArtistsAdapter);

        showTextViewSearchArtists();

        return rootView;
    }

    private void showOrHideCancel(final String textArtistName) {
        //show clear button if there is text in EditText
        if (textArtistName.length() > 0 && imageButtonClear.getVisibility() == View.GONE) {
            ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
                    (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
            animation.setInterpolator(new BounceInterpolator());
            animation.setDuration(500);
            imageButtonClear.setVisibility(View.VISIBLE);
            imageButtonClear.startAnimation(animation);
        } else if (textArtistName.length() == 0) {
            ScaleAnimation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF,
                    (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
            animation.setInterpolator(new BounceInterpolator());
            animation.setDuration(500);
            imageButtonClear.setVisibility(View.VISIBLE);
            imageButtonClear.startAnimation(animation);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {

                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    imageButtonClear.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {

                }
            });

        }
    }

    private void restoreState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mArtists = savedInstanceState.getParcelableArrayList(ARTIST);
            if (mArtists == null)
                mArtists = Collections.emptyList();
            //RecyclerView
            mArtistsAdapter = new ArtistsAdapter(this, mArtists);
            if (((ContainerActivity) getActivity()).isTwoPane())
                mArtistsAdapter.setSelectedArtist(savedInstanceState.getString(SELECTED_ARTIST));
        } else {
            mArtistsAdapter = new ArtistsAdapter(this, mArtists);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            //save artist name for orientation change, so that the app doesn't do search again
            outState.putParcelableArrayList(ARTIST, (ArrayList<? extends Parcelable>) mArtists);
            outState.putString(SELECTED_ARTIST, mArtistsAdapter.getSelectedArtist());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTextViewSearchArtists() {
        if (mArtists.size() == 0) {
            textViewError.setText(getString(R.string.search_artists_begin));
            textViewError.setVisibility(View.VISIBLE);
        }
    }

    private void searchForArtists(final String artistName) {
        textViewError.setVisibility(View.GONE);
        if (artistName.length() == 0) {
            imageButtonClear.setVisibility(View.GONE);
            mArtists = new ArrayList<>();
            mArtistsAdapter.updateList(mArtists);
            progressBar.setVisibility(View.GONE);
            showTextViewSearchArtists();
        } else {
            imageButtonClear.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            //method that gets me the artists and their artistImageUrls
            Utility.runOnWorkerThread(() -> {
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                //artists
                spotifyService.searchArtists(artistName, new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager artistsPager, final Response response) {
                        getActivity().runOnUiThread(() -> artistsFound(artistsPager));
                    }

                    @Override
                    public void failure(final RetrofitError error) {
                        getActivity().runOnUiThread(() -> artistsNotFound(error));
                    }
                });
                return null;
            });
        }
    }

    private void artistsNotFound(final RetrofitError error) {
        if (error.getKind() == RetrofitError.Kind.NETWORK) {
            textViewError.setText(getString(R.string.network_error));
            textViewError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void artistsFound(final ArtistsPager artistsPager) {
        mArtists = new ArrayList<>();
        //iterate and save in the list
        for (Artist artist : artistsPager.artists.items) {
            mArtists.add(new ArtistParcelable(artist));
        }
        //update the adapter
        if (mArtists.size() == 0) {
            textViewError.setText(getString(R.string.tv_no_artists));
            textViewError.setVisibility(View.VISIBLE);
        }

        mArtistsAdapter.updateList(mArtists);
        progressBar.setVisibility(View.GONE);
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
    public Context getContext() {
        return getActivity();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void artistClicked(final ArtistParcelable artistParcelable, final ArtistsAdapter.RecyclerViewHolderArtists holder) {
        int container = ((ContainerActivity) getActivity()).isTwoPane() ? R.id.tracksContainer : R.id.container;
        //Shared Element transition using fragments if lollipop and above
        TracksFragment tracksFragment = new TracksFragment();
        tracksFragment.setImageTransitionName(holder.imageViewArtist.getTransitionName());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Keys.KEY_ARTIST_PARCELABLE, artistParcelable);
        Utility.runOnWorkerThread(() -> {
            int vibrantColor = Palette.from(((BitmapDrawable) holder.imageViewArtist.getDrawable()).getBitmap())
                    .generate()
                    .getVibrantColor(Color.BLACK);
            bundle.putInt(Keys.COLOR_ACTION_BAR, vibrantColor);
            return null;
        });

        tracksFragment.setArguments(bundle);

        Utility.launchFragmentWithSharedElements(isTwoPane, this, tracksFragment, container, holder.imageViewArtist);

    }

    @Override
    public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
        // If the event is a key-down event on the "enter" button
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            // Perform action on key press
            searchForArtists(editTextSearch.getText()
                    .toString());
            // code to hide the soft keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(editTextSearch.getApplicationWindowToken(), 0);
            return true;
        }
        return false;
    }
}
