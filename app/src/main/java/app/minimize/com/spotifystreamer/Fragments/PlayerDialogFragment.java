package app.minimize.com.spotifystreamer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.SeekbarChangeListener;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.Views.NextButton;
import app.minimize.com.spotifystreamer.Views.PlayButton;
import app.minimize.com.spotifystreamer.Views.PreviousButton;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by ahmedrizwan on 6/15/15.
 */
public class PlayerDialogFragment extends DialogFragment {


    @InjectView(R.id.textViewTrackName)
    TextView textViewTrackName;
    @InjectView(R.id.seekBarPlayer)
    SeekBar seekBarPlayer;
    @InjectView(R.id.chronometerStart)
    Chronometer chronometerStart;
    @InjectView(R.id.chronometerEnd)
    Chronometer chronometerEnd;
    @InjectView(R.id.imageViewPrevious)
    PreviousButton imageViewPrevious;
    @InjectView(R.id.imageViewPlay)
    PlayButton imageViewPlay;
    @InjectView(R.id.imageViewNext)
    NextButton imageViewNext;

    private TracksFragment tracksFragment;
    private TrackParcelable mTrackParcelable;
    private int mAmountToUpdate = 0, currentProgress = 0;
    private Timer timer;

    public static PlayerDialogFragment getInstance(TracksFragment tracksFragment) {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        playerDialogFragment.tracksFragment = tracksFragment;
        return playerDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, rootView);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(R.string.title_player);
        }

        ((ContainerActivity) getActivity()).hideNowPlayingLayout();

        mTrackParcelable = getArguments().getParcelable(getString(R.string.key_tracks_parcelable));
        List<TrackParcelable> trackParcelableList = getArguments().getParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST);

        Toast.makeText(getActivity(),trackParcelableList.indexOf(mTrackParcelable)+"",Toast.LENGTH_SHORT).show();

        if (mTrackParcelable != null) {
            textViewTrackName.setText(mTrackParcelable.songName + "\n"
                    + mTrackParcelable.albumName);

            Utility.runOnWorkerThread(() -> {
                playTrack();
                return null;
            });

        }

        int colorPrimary = Utility.getPrimaryColorFromSelectedTheme(getActivity());
        DrawableCompat.setTint(seekBarPlayer.getThumb(), colorPrimary);
        DrawableCompat.setTint(seekBarPlayer.getProgressDrawable(), colorPrimary);
        return rootView;
    }

    private void playTrack() {
        Intent intent = new Intent(getActivity(),
                MediaPlayerService.class);
        //send trackParcelable
        intent.putExtra(Keys.KEY_TRACK_PARCELABLE, mTrackParcelable);
        getActivity().startService(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEventMainThread(MediaPlayerHandler.StoppedEvent stoppedEvent){
        if (chronometerStart != null) {
            chronometerStart.setBase(SystemClock.elapsedRealtime());
            chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                    stoppedEvent.duration);
            chronometerStart.stop();
        }

        seekBarPlayer.setProgress(0);
    }

    @Subscribe
    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent){
        try {
            if (chronometerStart != null) {
                chronometerStart.setBase(SystemClock.elapsedRealtime() - playingEvent.progress);
                chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                        playingEvent.duration);
                chronometerStart.start();
            }
            mAmountToUpdate = (playingEvent.duration) / Keys.SMOOTHNESS_FACTOR;
            seekBarPlayer.setOnSeekBarChangeListener(SeekbarChangeListener
                    .getInstance(playingEvent.duration, Keys.SMOOTHNESS_FACTOR, chronometerStart));
            seekBarPlayer.setMax(Keys.SMOOTHNESS_FACTOR);
            seekBarPlayer.setProgress(playingEvent.progress / mAmountToUpdate);

            if (timer != null) {
                timer.cancel();
                timer = null;
                logHelper("Timer is null now...");
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        (getActivity()).runOnUiThread(() -> {
                            try {
                                if (MediaPlayerHandler.getPlayerState() == MediaPlayerHandler.MediaPlayerState.Playing) {
                                    if ((mAmountToUpdate * seekBarPlayer.getProgress() < playingEvent.duration + playingEvent.progress)) {
                                        currentProgress = seekBarPlayer.getProgress();
                                        currentProgress += 1;
                                        seekBarPlayer.setProgress(currentProgress);
                                    }
                                } else {
                                    if (timer != null)
                                        timer.cancel();
                                }
                            } catch (NullPointerException e) {
                                if (timer != null)
                                    timer.cancel();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10, mAmountToUpdate);
        } catch (ArithmeticException e) {
            Log.e("MediaPlayerHandler", e.getMessage());
        } catch (NullPointerException e) {
            Log.e("MediaPlayerHandler Null", e.getMessage());
        }
    }

    private void logHelper(final String message) {
        Log.e("Exception", message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
