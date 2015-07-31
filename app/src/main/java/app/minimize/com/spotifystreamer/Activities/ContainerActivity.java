package app.minimize.com.spotifystreamer.Activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import app.minimize.com.spotifystreamer.Fragments.ArtistsFragment;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Rx.RxBus;
import app.minimize.com.spotifystreamer.databinding.ActivityContainerBinding;
import app.minimize.com.spotifystreamer.databinding.IncludeNowPlayingBinding;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
        mActivityContainerBinding = DataBindingUtil.setContentView(this, R.layout.activity_container);
        mIncludeNowPlayingBinding = DataBindingUtil.bind(mActivityContainerBinding.getRoot().findViewById(R.id.layoutNowPlaying));

        setSupportActionBar(mActivityContainerBinding.mainToolbar);
        mActivityContainerBinding.mainToolbar.setTitle(getString(R.string.app_name));

        //Check for twoPanes
//        if (findViewById(R.id.tracksContainer) != null) {
//            mTwoPane = true;
//        } else {
        mTwoPane = false;
        //make transaction for the artists fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ArtistsFragment())
                    .commit();
        }

//        }

        //start service to retrieve the status of player
        startServiceForStatusRetrieval();

        RxBus.getInstance()
                .toObserverable()
                .observeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(final Throwable e) {}

                    @Override
                    public void onNext(final Object o) {
                        Log.e(TAG, "onNext "+o.getClass());
                         if(o instanceof MediaPlayerHandler.StoppedEvent){
                            //hide the nowPlayingCard
                            setNowPlayingVisibile(false);
                        } else if(o instanceof MediaPlayerHandler.PlayingEvent){
                            //show the nowPlayingCard
                            setNowPlayingVisibile(true);
                            cardPlaying();
                        } else if(o instanceof MediaPlayerHandler.PausedEvent){
                            cardPaused();
                        }
                    }
                });

        mIncludeNowPlayingBinding.buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MediaPlayerHandler.getInstance(ContainerActivity.this).togglePlayPause();
            }
        });
    }

    public void refreshNowPlayingCardState() {
        //get the state of the player
        MediaPlayerHandler.MediaPlayerState playerState = MediaPlayerHandler
                .getPlayerState();

        if(playerState== MediaPlayerHandler.MediaPlayerState.Playing){
            cardPlaying();
        } else if(playerState == MediaPlayerHandler.MediaPlayerState.Paused){
            cardPaused();
        } else {
            setNowPlayingVisibile(false);
        }
    }

    public void startServiceForStatusRetrieval() {
        Intent intent = new Intent(this,
                MediaPlayerService.class);
        intent.putExtra(Keys.KEY_GET_STATUS, true);
        startService(intent);
    }

//    public void onEventMainThread(TrackParcelable trackParcelable) {
//        if (MediaPlayerHandler.getPlayerState() == MediaPlayerHandler.MediaPlayerState.Idle) {
//            //Hide the NowPlaying
//            setNowPlayingVisibile(false);
//        } else {
//            setNowPlayingVisibile(true);
//            setCardTrackAndAlbumNames(trackParcelable.songName,trackParcelable.albumName);
//            int size = trackParcelable.albumImageUrls.size();
//            if (size > 0)
//                Picasso.with(ContainerActivity.this)
//                        .load(trackParcelable.albumImageUrls.get(size - 1))
//                        .into(new Target() {
//                            @Override
//                            public void onBitmapLoaded(final Bitmap bitmap,
//                                                       final Picasso.LoadedFrom from) {
//                                mIncludeNowPlayingBinding.imageViewAlbum.setImageBitmap(bitmap);
//                            }
//
//                            @Override
//                            public void onBitmapFailed(final Drawable errorDrawable) {}
//
//                            @Override
//                            public void onPrepareLoad(final Drawable placeHolderDrawable) {}
//                        });
//            else
//                mIncludeNowPlayingBinding.imageViewAlbum.
//                        setImageDrawable((ContextCompat.getDrawable(ContainerActivity.this, R.drawable.ic_not_available)));
//        }
//    }

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

    public void setNowPlayingVisibile(boolean visible) {
        Log.e(TAG, "setNowPlayingVisibile "+visible);
        if (visible) {
            mIncludeNowPlayingBinding.getRoot().setVisibility(View.VISIBLE);
        } else {
            mIncludeNowPlayingBinding.getRoot().setVisibility(View.GONE);
        }
    }

    public void cardPlaying(){
        //set the button to play mode
        mIncludeNowPlayingBinding.buttonPlayPause.setMode(false);
    }

    public void cardPaused(){
        mIncludeNowPlayingBinding.buttonPlayPause.setMode(true);
    }
}
