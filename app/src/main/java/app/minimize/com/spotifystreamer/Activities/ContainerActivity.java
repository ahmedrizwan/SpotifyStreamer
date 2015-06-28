package app.minimize.com.spotifystreamer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import app.minimize.com.spotifystreamer.Fragments.ArtistsFragment;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Rx.RxBus;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContainerActivity extends AppCompatActivity {

    boolean mTwoPane;
    @Bind(R.id.textViewArtistName)
    TextView textViewArtistName;
    @Bind(R.id.mainToolbar)
    Toolbar mainToolbar;
    @Nullable
    @Bind(R.id.container)
    LinearLayout container;
    @Bind(R.id.imageViewAlbum)
    ImageView imageViewAlbum;
    @Bind(R.id.textViewTrackName)
    TextView textViewTrackName;
    @Bind(R.id.layoutNowPlaying)
    ViewGroup layoutNowPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        ButterKnife.bind(this);

        //ActionBar
        setSupportActionBar(mainToolbar);

        //Check for twoPanes
        if (findViewById(R.id.tracksContainer) != null) {
            mTwoPane = true;
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

        RxBus.getInstance().toObserverable().observeOn(Schedulers.newThread())
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
                        if(o instanceof TrackParcelable){
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideNowPlayingLayout() {
        layoutNowPlaying.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
//        EventBus.getDefault()
//                .register(this);
    }

    @Override
    public void onStop() {
//        EventBus.getDefault()
//                .unregister(this);
        super.onStop();
    }

    public void onEventMainThread(TrackParcelable trackParcelable) {
        if (MediaPlayerHandler.getPlayerState() == MediaPlayerHandler.MediaPlayerState.Idle) {
            //Hide the NowPlaying
            hideNowPlayingLayout();
        } else {
            layoutNowPlaying.setVisibility(View.VISIBLE);
            layoutNowPlaying.setOnClickListener(v -> {
                Toast.makeText(this,
                        ((TextView) layoutNowPlaying.findViewById(R.id.textViewTrackName)).getText()
                                .toString(),
                        Toast.LENGTH_SHORT)
                        .show();
                //TODO : launch the PlayerFragment here


            });
            textViewTrackName.setText(trackParcelable.songName);
            textViewArtistName.setText(trackParcelable.artistName);
            int size = trackParcelable.albumImageUrls.size();
            if (size > 0)
                Picasso.with(ContainerActivity.this)
                        .load(trackParcelable.albumImageUrls.get(size - 1))
                        .into(imageViewAlbum);
            else
                imageViewAlbum.setImageDrawable(ContextCompat.getDrawable(ContainerActivity.this, R.drawable.ic_not_available));
        }
    }

    private void logHelper(final String message) {
        Log.e("Container Activity", message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
