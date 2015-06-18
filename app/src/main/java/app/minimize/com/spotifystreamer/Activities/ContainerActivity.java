package app.minimize.com.spotifystreamer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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

    public PlayerReceiver getPlayerReceiver() {
        return mPlayerReceiver;
    }

    PlayerReceiver mPlayerReceiver;

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

        setSupportActionBar(mainToolbar);

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


    private void startServiceForStatusRetrieval() {
        Intent intent = new Intent(this,
                MediaPlayerService.class);
        mPlayerReceiver = new PlayerReceiver(null);
        intent.putExtra(Keys.KEY_GET_STATUS,
                mPlayerReceiver);
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


    public class PlayerReceiver extends ResultReceiver {

        public PlayerReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode,
                                       final Bundle resultData) {
            try {
                if (resultCode == Keys.KEY_STATUS_CODE) {
                    handleStatusReceiver(resultData);
                } else if (resultCode == Keys.KEY_PLAYER_CODE) {
                    handlePlayerReceiver(resultData);
                }
            } catch (NullPointerException e) {
                Log.e("Container Activity", e.toString());
            }
        }

        private void handlePlayerReceiver(final Bundle resultData) {
            logHelper("handlePlayer");
        }

        private void handleStatusReceiver(final Bundle resultData) {
            logHelper("handleStatus");
            if (MediaPlayerHandler.getPlayerState() == MediaPlayerInterface.MediaPlayerState.Idle) {
                //Hide the NowPlaying
                layoutNowPlaying.setVisibility(View.GONE);
            } else {
                layoutNowPlaying.setVisibility(View.VISIBLE);
                //TODO : Add track and album name to the NowPlaying along with play/pause info

            }
        }
    }

    private void logHelper(final String message) {
        Log.e("Container Activity", message);
    }
}
