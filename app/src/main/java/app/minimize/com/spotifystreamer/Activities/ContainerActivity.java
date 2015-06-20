package app.minimize.com.spotifystreamer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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

import app.minimize.com.spotifystreamer.Fragments.ArtistsFragment;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerInterface;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContainerActivity extends AppCompatActivity {

    boolean mTwoPane;
    @InjectView(R.id.textViewAlbumName)
    TextView textViewAlbumName;

    public NowPlayingReceiver getNowPlayingReceiver() {
        return mNowPlayingReceiver;
    }

    NowPlayingReceiver mNowPlayingReceiver;

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
        mNowPlayingReceiver = new NowPlayingReceiver(null);
        intent.putExtra(Keys.KEY_GET_STATUS,
                mNowPlayingReceiver);
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


    public class NowPlayingReceiver extends ResultReceiver {

        public NowPlayingReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode,
                                       final Bundle resultData) {
            try {
                if (resultCode == Keys.KEY_STATUS_CODE) {
                    handleStatusReceiver(resultData);
                }
            } catch (NullPointerException e) {
                Log.e("Container Activity", e.toString());
            }
        }

        private void handleStatusReceiver(final Bundle resultData) {
            logHelper("handleStatus");
            if (MediaPlayerHandler.getPlayerState() == MediaPlayerInterface.MediaPlayerState.Idle) {
                //Hide the NowPlaying
                hideNowPlayingLayout();
            } else {
                layoutNowPlaying.setVisibility(View.VISIBLE);

                String trackName = resultData.getString(Keys.KEY_TRACK_NAME);
                String albumName = resultData.getString(Keys.KEY_ALBUM_NAME);
                textViewTrackName.setText(trackName);
                textViewAlbumName.setText(albumName);
                if (MediaPlayerHandler.getPlayerState() != MediaPlayerInterface.MediaPlayerState.Playing) {
                    imageViewAlbum.setImageDrawable(ContextCompat.getDrawable(ContainerActivity.this, R.drawable.ic_not_available));
                }
            }
        }
    }

    private void logHelper(final String message) {
        Log.e("Container Activity", message);
    }
}
