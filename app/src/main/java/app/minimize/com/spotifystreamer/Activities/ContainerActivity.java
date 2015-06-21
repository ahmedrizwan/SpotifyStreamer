package app.minimize.com.spotifystreamer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import app.minimize.com.spotifystreamer.Fragments.ArtistsFragment;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class ContainerActivity extends AppCompatActivity {

    boolean mTwoPane;
    @InjectView(R.id.textViewArtistName)
    TextView textViewArtistName;
    @InjectView(R.id.mainToolbar)
    Toolbar mainToolbar;
    @InjectView(R.id.container)
    LinearLayout container;
    @InjectView(R.id.imageViewAlbum)
    ImageView imageViewAlbum;
    @InjectView(R.id.textViewTrackName)
    TextView textViewTrackName;
    @InjectView(R.id.layoutNowPlaying)
    RelativeLayout layoutNowPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        ButterKnife.inject(this);
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
        EventBus.getDefault()
                .register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault()
                .unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEventMainThread(TrackParcelable trackParcelable) {
        Toast.makeText(ContainerActivity.this, "status", Toast.LENGTH_SHORT)
                .show();
        if (MediaPlayerHandler.getPlayerState() == MediaPlayerHandler.MediaPlayerState.Idle) {
            //Hide the NowPlaying
            hideNowPlayingLayout();
        } else {
            layoutNowPlaying.setVisibility(View.VISIBLE);
            textViewTrackName.setText(trackParcelable.songName);
            textViewArtistName.setText(trackParcelable.artistName);
            int size = trackParcelable.albumImageUrls.size();
            if (size > 0)
                Picasso.with(ContainerActivity.this)
                        .load(trackParcelable.albumImageUrls.get(size - 1))
                        .into(imageViewAlbum);
            if (MediaPlayerHandler.getPlayerState() != MediaPlayerHandler.MediaPlayerState.Playing) {
                imageViewAlbum.setImageDrawable(ContextCompat.getDrawable(ContainerActivity.this, R.drawable.ic_not_available));
            }
        }
    }

    private void logHelper(final String message) {
        Log.e("Container Activity", message);
    }
}
