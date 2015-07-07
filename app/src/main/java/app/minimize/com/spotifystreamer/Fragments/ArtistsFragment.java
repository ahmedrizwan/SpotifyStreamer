package app.minimize.com.spotifystreamer.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;

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
import app.minimize.com.spotifystreamer.databinding.FragmentArtistsBinding;
import app.minimize.com.spotifystreamer.databinding.IncludeProgressBinding;
import butterknife.ButterKnife;
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

    private FragmentArtistsBinding mFragmentArtistsBinding;
    private IncludeProgressBinding mIncludeProgressBinding;
    
    List<ArtistParcelable> mArtists = Collections.emptyList();
    ArtistsAdapter mArtistsAdapter;

    private void clearState() {
        mFragmentArtistsBinding.editTextSearch.setText("");
        mFragmentArtistsBinding.imageButtonClear.setVisibility(View.GONE);
        mArtists = new ArrayList<>();
        mArtistsAdapter.updateList(mArtists);
        mIncludeProgressBinding.textViewError.setText(getString(R.string.search_artists_begin));
        mIncludeProgressBinding.textViewError.setVisibility(View.VISIBLE);
        mIncludeProgressBinding.progressBar.setVisibility(View.GONE);
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
        mFragmentArtistsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_artists,
                container, false);

        mIncludeProgressBinding = DataBindingUtil.bind(mFragmentArtistsBinding.getRoot().findViewById(R.id.progressLayout));
        mIncludeProgressBinding.textViewError.setText("Hello");
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

        mFragmentArtistsBinding.editTextSearch.setOnKeyListener(this);

        Observable.create(RxObservables.getSearchObservable(mFragmentArtistsBinding.editTextSearch))
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


        mFragmentArtistsBinding.recyclerViewArtists.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFragmentArtistsBinding.recyclerViewArtists.setAdapter(mArtistsAdapter);

        showTextViewSearchArtists();

        mFragmentArtistsBinding.imageButtonClear.setOnClickListener(v -> clearState());

        return mFragmentArtistsBinding.getRoot();
    }


    private void showOrHideCancel(final String textArtistName) {
        //show clear button if there is text in EditText
        if (textArtistName.length() > 0 && mFragmentArtistsBinding.imageButtonClear.getVisibility() == View.GONE) {
            ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
                    (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
            animation.setInterpolator(new BounceInterpolator());
            animation.setDuration(500);
            mFragmentArtistsBinding.imageButtonClear.setVisibility(View.VISIBLE);
            mFragmentArtistsBinding.imageButtonClear.startAnimation(animation);
        } else if (textArtistName.length() == 0) {
            ScaleAnimation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF,
                    (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
            animation.setInterpolator(new BounceInterpolator());
            animation.setDuration(500);
            mFragmentArtistsBinding.imageButtonClear.setVisibility(View.VISIBLE);
            mFragmentArtistsBinding.imageButtonClear.startAnimation(animation);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {

                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    mFragmentArtistsBinding.imageButtonClear.setVisibility(View.GONE);
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
            mIncludeProgressBinding.textViewError.setText(getString(R.string.search_artists_begin));
            mIncludeProgressBinding.textViewError.setVisibility(View.VISIBLE);
        }
    }

    private void searchForArtists(final String artistName) {
        mIncludeProgressBinding.textViewError.setVisibility(View.GONE);
        if (artistName.length() == 0) {
            mFragmentArtistsBinding.imageButtonClear.setVisibility(View.GONE);
            mArtists = new ArrayList<>();
            mArtistsAdapter.updateList(mArtists);
            mIncludeProgressBinding.progressBar.setVisibility(View.GONE);
            showTextViewSearchArtists();
        } else {
            mFragmentArtistsBinding.imageButtonClear.setVisibility(View.VISIBLE);
            mIncludeProgressBinding.progressBar.setVisibility(View.VISIBLE);
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
            mIncludeProgressBinding.textViewError.setText(getString(R.string.network_error));
            mIncludeProgressBinding.textViewError.setVisibility(View.VISIBLE);
            mIncludeProgressBinding.progressBar.setVisibility(View.GONE);
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
            mIncludeProgressBinding.textViewError.setText(getString(R.string.tv_no_artists));
            mIncludeProgressBinding.textViewError.setVisibility(View.VISIBLE);
        }

        mArtistsAdapter.updateList(mArtists);
        mIncludeProgressBinding.progressBar.setVisibility(View.GONE);
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
        ButterKnife.unbind(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void artistClicked(final ArtistParcelable artistParcelable, final ArtistsAdapter.RecyclerViewHolderArtists holder) {
        int container = ((ContainerActivity) getActivity()).isTwoPane() ? R.id.tracksContainer : R.id.container;
        //Shared Element transition using fragments if lollipop and above
        TracksFragment tracksFragment = new TracksFragment();
        if (Utility.isVersionLollipopAndAbove())
            tracksFragment.setImageTransitionName(holder.imageViewArtist.getTransitionName());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Keys.KEY_ARTIST_PARCELABLE, artistParcelable);
        Utility.runOnWorkerThread(() -> {
            int vibrantColor = Palette.from(((BitmapDrawable) holder.imageViewArtist.getDrawable()).getBitmap())
                    .generate()
                    .getVibrantColor(Color.BLACK);
            bundle.putInt(Keys.COLOR_ACTION_BAR, vibrantColor);
            tracksFragment.setArguments(bundle);
            Utility.launchFragmentWithSharedElements(isTwoPane, this, tracksFragment, container, holder.imageViewArtist);
            return null;
        });
    }

    @Override
    public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
        // If the event is a key-down event on the "enter" button
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            // Perform action on key press
            searchForArtists(mFragmentArtistsBinding.editTextSearch.getText()
                    .toString());
            // code to hide the soft keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mFragmentArtistsBinding.editTextSearch.getApplicationWindowToken(), 0);
            return true;
        }
        return false;
    }

}
