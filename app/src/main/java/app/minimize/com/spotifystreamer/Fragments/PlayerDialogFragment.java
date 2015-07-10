package app.minimize.com.spotifystreamer.Fragments;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.minimize.com.seek_bar_compat.SeekBarCompat;
import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.SeekbarChangeListener;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Rx.RxBus;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.Views.NextButton;
import app.minimize.com.spotifystreamer.Views.PlayButton;
import app.minimize.com.spotifystreamer.Views.PreviousButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ahmedrizwan on 6/15/15.
 */
public class PlayerDialogFragment extends DialogFragment {


    @Bind(R.id.textViewTrackName)
    TextView textViewTrackName;
    @Bind(R.id.seekBarPlayer)
    SeekBarCompat seekBarPlayer;
    @Bind(R.id.chronometerStart)
    Chronometer chronometerStart;
    @Bind(R.id.chronometerEnd)
    Chronometer chronometerEnd;
    @Bind(R.id.imageViewPrevious)
    PreviousButton imageViewPrevious;
    @Bind(R.id.imageViewPlay)
    PlayButton imageViewPlay;
    @Bind(R.id.imageViewNext)
    NextButton imageViewNext;
    @Bind(R.id.textViewTrackAlbum)
    TextView textViewTrackAlbum;
    @Bind(R.id.imageViewAlbum)
    ImageView imageViewAlbum;

    private TracksFragment tracksFragment;
    private TrackParcelable mTrackParcelable;
    private int mAmountToUpdate = 0, currentProgress = 0;
    private Timer timer;
    private String imageViewAlbumTransitionName;
    private int vibrantColor = Color.BLACK;
    private boolean isTwoPane = false;

    public static PlayerDialogFragment getInstance(TracksFragment tracksFragment) {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        playerDialogFragment.tracksFragment = tracksFragment;
        return playerDialogFragment;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, rootView);

        isTwoPane = ((ContainerActivity) getActivity()).isTwoPane();
        mTrackParcelable = getArguments().getParcelable(getString(R.string.key_tracks_parcelable));
        List<TrackParcelable> trackParcelableList = getArguments().getParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST);
        vibrantColor = getArguments().getInt(Keys.COLOR_ACTION_BAR);
        Log.e("Vibrant Color", vibrantColor + "");
        if (isTwoPane) {
            getDialog().getWindow()
                    .requestFeature(Window.FEATURE_NO_TITLE);
        } else {
            Utility.setActionBarAndStatusBarColor(((AppCompatActivity) getActivity()), vibrantColor);
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_player);
        }

        ((ContainerActivity) getActivity()).hideNowPlayingLayout();

        imageViewPlay.setOnClickListener(v -> {
            toggleTrack();
        });

        imageViewPlay.setButtonBackgroundColor(getActivity(), vibrantColor);
        seekBarPlayer.setProgressColor(vibrantColor);
        seekBarPlayer.setThumbColor(vibrantColor);

        if (Utility.isVersionLollipopAndAbove())
            imageViewAlbum.setTransitionName(imageViewAlbumTransitionName);

        if (mTrackParcelable != null) {
            textViewTrackName.setText(mTrackParcelable.songName);
            textViewTrackAlbum.setText(mTrackParcelable.albumName);
            int size = mTrackParcelable.albumImageUrls.size();
            if (size > 0) {
                Utility.loadImage(getActivity(),
                        mTrackParcelable.albumImageUrls.get(size - 1),
                        mTrackParcelable.albumImageUrls.get(0),
                        imageViewAlbum,
                        null);
            }
            playTrack();
        }

        RxBus.getInstance()
                .toObserverable()
                .subscribeOn(Schedulers.newThread())
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
                        if (o instanceof MediaPlayerHandler.PlayingEvent) {
                            onEventMainThread(((MediaPlayerHandler.PlayingEvent) o));
                        } else if (o instanceof MediaPlayerHandler.PausedEvent) {
                            onEventMainThread(((MediaPlayerHandler.PausedEvent) o));
                        } else if (o instanceof MediaPlayerHandler.StoppedEvent) {
                            onEventMainThread(((MediaPlayerHandler.StoppedEvent) o));
                        }
                    }
                });
        return rootView;
    }

    private void toggleTrack() {
        Intent intent = new Intent(getActivity(),
                MediaPlayerService.class);
        //send trackParcelable
        intent.putExtra(Keys.KEY_TRACK_PARCELABLE, mTrackParcelable);
        getActivity().startService(intent);
    }

    private void playTrack() {
        Intent intent = new Intent(getActivity(),
                MediaPlayerService.class);
        //send trackParcelable
        intent.putExtra(Keys.KEY_TRACK_PARCELABLE, mTrackParcelable);
        new Thread(() -> {
            getActivity().startService(intent);
        }).start();

        imageViewPlay.setPauseMode(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onEventMainThread(MediaPlayerHandler.StoppedEvent stoppedEvent) {
        imageViewPlay.setPauseMode(false);
        if (chronometerStart != null) {
            chronometerStart.setBase(SystemClock.elapsedRealtime());
            chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                    stoppedEvent.duration);
            chronometerStart.stop();
        }

        seekBarPlayer.setProgress(0);
    }

    public void onEventMainThread(MediaPlayerHandler.PausedEvent pausedEvent) {
        imageViewPlay.setPauseMode(false);
        if (chronometerStart != null) {
            chronometerStart.setBase(SystemClock.elapsedRealtime() - pausedEvent.progress);
            chronometerStart.stop();
        }
    }

    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent) {
        try {
            imageViewPlay.setPauseMode(true);
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
        ButterKnife.unbind(this);
    }

    public void setImageViewAlbumTransitionName(String imageViewAlbumTransitionName) {
        this.imageViewAlbumTransitionName = imageViewAlbumTransitionName;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        tracksFragment.refreshActionBar();
    }
}
