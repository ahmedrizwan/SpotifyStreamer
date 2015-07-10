package app.minimize.com.spotifystreamer.Activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import app.minimize.com.spotifystreamer.Fragments.ArtistsFragment;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Rx.RxBus;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.databinding.ActivityContainerBinding;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContainerActivity extends AppCompatActivity {

    private static final String TAG = "ContainerActivity";
    boolean mTwoPane;
    private ActivityContainerBinding mActivityContainerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        mActivityContainerBinding = DataBindingUtil.setContentView(this, R.layout.activity_container);
        mActivityContainerBinding.setToolBarTitle(getString(R.string.app_name));
        mActivityContainerBinding.setToolBarBackgroundColor(Utility.getPrimaryColorFromSelectedTheme(this));
        mActivityContainerBinding.setNowPlayingVisible(false);
        setSupportActionBar(mActivityContainerBinding.mainToolbar);

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
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(final Throwable e) {

                    }

                    @Override
                    public void onNext(final Object o) {
                        if (o instanceof TrackParcelable) {
                            onEventMainThread(((TrackParcelable) o));
                        }
                    }
                });
    }

    public void startServiceForStatusRetrieval() {
        Intent intent = new Intent(this,
                MediaPlayerService.class);
        intent.putExtra(Keys.KEY_GET_STATUS, true);
        startService(intent);
    }

    public void onEventMainThread(TrackParcelable trackParcelable) {
        if (MediaPlayerHandler.getPlayerState() == MediaPlayerHandler.MediaPlayerState.Idle) {
            //Hide the NowPlaying
            hideNowPlayingLayout();
        } else {
            mActivityContainerBinding.setNowPlayingVisible(true);
            mActivityContainerBinding.setTrackTitle(trackParcelable.songName);
            mActivityContainerBinding.setAlbumTitle(trackParcelable.albumName);

            int size = trackParcelable.albumImageUrls.size();
            if (size > 0)
                Picasso.with(ContainerActivity.this)
                        .load(trackParcelable.albumImageUrls.get(size - 1))
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                                mActivityContainerBinding.setAlbumImage(new BitmapDrawable(getResources(), bitmap));
                            }

                            @Override
                            public void onBitmapFailed(final Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(final Drawable placeHolderDrawable) {

                            }
                        });
            else
                mActivityContainerBinding.setAlbumImage(ContextCompat.getDrawable(ContainerActivity.this, R.drawable.ic_not_available));
        }
    }

    public void hideNowPlayingLayout() {
        mActivityContainerBinding.setNowPlayingVisible(false);
    }

    public void nowPlayingOnClick(View view) {
        Toast.makeText(this,
                mActivityContainerBinding.getTrackTitle(),
                Toast.LENGTH_SHORT)
                .show();
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
}
