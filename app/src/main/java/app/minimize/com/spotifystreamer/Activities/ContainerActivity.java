package app.minimize.com.spotifystreamer.Activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import app.minimize.com.spotifystreamer.Fragments.ArtistsFragment;
import app.minimize.com.spotifystreamer.Fragments.PlayerDialogFragment;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.databinding.ActivityContainerBinding;
import app.minimize.com.spotifystreamer.databinding.IncludeNowPlayingBinding;
import de.greenrobot.event.EventBus;

public class ContainerActivity extends AppCompatActivity {

    private static final String TAG = "ContainerActivity";
    boolean mTwoPane;
    private ActivityContainerBinding mActivityContainerBinding;
    private IncludeNowPlayingBinding mIncludeNowPlayingBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        //register eventBus
        EventBus eventBus = EventBus.getDefault();
        eventBus.register(this);

        mActivityContainerBinding = DataBindingUtil.setContentView(this, R.layout.activity_container);
        mIncludeNowPlayingBinding = DataBindingUtil.bind(mActivityContainerBinding.getRoot()
                .findViewById(R.id.layoutNowPlaying));

        setSupportActionBar(mActivityContainerBinding.mainToolbar);
        mActivityContainerBinding.mainToolbar.setTitle(getString(R.string.app_name));

        //Check for twoPanes
        if (mActivityContainerBinding.tracksContainer != null) {
            mTwoPane = true;
            //load the artist fragment in the tracksContainer
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentArtists, new ArtistsFragment())
                    .commit();
        } else {
            mTwoPane = false;
            //make transaction for the artists fragment
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new ArtistsFragment())
                        .commit();
            }
        }

        //start service to retrieve the status of player
        startServiceForStatusRetrieval();

        mIncludeNowPlayingBinding.buttonPlayPause.setOnClickListener(v -> MediaPlayerHandler.getInstance(ContainerActivity.this)
                .togglePlayPause());

    }

    public void refreshNowPlayingCardState() {
        //get the state of the player
        MediaPlayerHandler.MediaPlayerState playerState = MediaPlayerHandler
                .getPlayerState();
        showCard();
        if (playerState == MediaPlayerHandler.MediaPlayerState.Playing) {
            cardPlaying();
        } else if (playerState == MediaPlayerHandler.MediaPlayerState.Paused) {
            cardPaused();
        } else {
            cardStopped();
        }
    }

    private void cardStopped() {
        mIncludeNowPlayingBinding.buttonPlayPause.setMode(true);
    }

    public void startServiceForStatusRetrieval() {
        Intent intent = new Intent(this,
                MediaPlayerService.class);
        intent.putExtra(Keys.KEY_GET_STATUS, true);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onEventMainThread(TrackParcelable trackParcelable) {
        showCard();
        refreshNowPlayingCardState();
        setCardTrackAndAlbumNames(trackParcelable.songName, trackParcelable.albumName);
        int size = trackParcelable.albumImageUrls.size();
        if (size > 0)
            Picasso.with(ContainerActivity.this)
                    .load(trackParcelable.albumImageUrls.get(size - 1))
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap,
                                                   final Picasso.LoadedFrom from) {
                            mIncludeNowPlayingBinding.imageViewAlbum.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(final Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        }
                    });
        else
            mIncludeNowPlayingBinding.imageViewAlbum.
                    setImageDrawable((ContextCompat.getDrawable(ContainerActivity.this, R.drawable.ic_not_available)));

        //click listner for the Card
        mIncludeNowPlayingBinding.layoutNowPlaying.setOnClickListener(v -> {
            //launch player dialog fragment
            if (mTwoPane) {
                //launch playerDialogFragment
                PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.key_tracks_parcelable), trackParcelable);
//                        bundle.putParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST, mTracksAdapter.getDataSet());
                int vibrantColor1 = Palette.from(((BitmapDrawable) mIncludeNowPlayingBinding.imageViewAlbum.getDrawable()).getBitmap())
                        .generate()
                        .getVibrantColor(Color.BLACK);
                bundle.putInt(Keys.COLOR_ACTION_BAR, vibrantColor1);
                playerDialogFragment.setArguments(bundle);
                playerDialogFragment.show(getSupportFragmentManager(), "Player");
            } else {
                //Launch the dialogFragment from here
                PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.key_tracks_parcelable), trackParcelable);
//                        bundle.putParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST, mTracksAdapter.getDataSet());
                int vibrantColor1 = Palette.from(((BitmapDrawable) mIncludeNowPlayingBinding.imageViewAlbum.getDrawable()).getBitmap())
                        .generate()
                        .getVibrantColor(Color.BLACK);
                bundle.putInt(Keys.COLOR_ACTION_BAR, vibrantColor1);
                playerDialogFragment.setArguments(bundle);

                if (Utility.isVersionLollipopAndAbove()) {
                    mIncludeNowPlayingBinding.imageViewAlbum.setTransitionName(trackParcelable.previewUrl);
                    playerDialogFragment.setImageViewAlbumTransitionName(trackParcelable.previewUrl);
                }
                Fragment fromFragment = getSupportFragmentManager().findFragmentById(R.id.container);
                Utility.launchFragmentWithSharedElements(mTwoPane, fromFragment,
                        playerDialogFragment, R.id.container, mIncludeNowPlayingBinding.imageViewAlbum);
            }
        });

    }

    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent) {
        showCard();
        //show the nowPlayingCard
        cardPlaying();
    }

    public void onEventMainThread(MediaPlayerHandler.PausedEvent pausedEvent) {
        showCard();
        cardPaused();
    }

    public void onEventMainThread(MediaPlayerHandler.StoppedEvent stoppedEvent) {
        showCard();
        cardStopped();
    }

    private void setCardTrackAndAlbumNames(final String songName, final String albumName) {
        mIncludeNowPlayingBinding.textViewArtistName.setText(albumName);
        mIncludeNowPlayingBinding.textViewTrackName.setText(songName);
    }

    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.about_message));
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.close), (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(R.color.color_primary));
            return true;
        } else if (id == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void logHelper(final String message) {
        Log.e("Container Activity", message);
    }

    public void showCard() {
        Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (!(activeFragment instanceof PlayerDialogFragment) && MediaPlayerHandler.getPlayerState() != MediaPlayerHandler.MediaPlayerState.Idle)
            mIncludeNowPlayingBinding.getRoot()
                    .setVisibility(View.VISIBLE);
    }

    public void cardPlaying() {
        //set the button to play mode
        mIncludeNowPlayingBinding.buttonPlayPause.setMode(false);
    }

    public void cardPaused() {
        mIncludeNowPlayingBinding.buttonPlayPause.setMode(true);
    }

    public void hideCard() {
        mIncludeNowPlayingBinding.getRoot()
                .setVisibility(View.GONE);
    }
}
