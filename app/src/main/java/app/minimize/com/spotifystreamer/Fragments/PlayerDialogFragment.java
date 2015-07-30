package app.minimize.com.spotifystreamer.Fragments;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import app.minimize.com.spotifystreamer.Rx.RxBus;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.databinding.FragmentPlayerBinding;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ahmedrizwan on 6/15/15.
 */
public class PlayerDialogFragment extends DialogFragment {

    private TracksFragment tracksFragment;
    private TrackParcelable mTrackParcelable;
    private int mAmountToUpdate = 0, currentProgress = 0;
    private Timer timer;
    private String mImageViewAlbumTransitionName;
    private int vibrantColor = Color.BLACK;
    private boolean isTwoPane = false;

    private FragmentPlayerBinding mFragmentPlayerBinding;

    public static PlayerDialogFragment getInstance(TracksFragment tracksFragment) {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        playerDialogFragment.tracksFragment = tracksFragment;
        return playerDialogFragment;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mFragmentPlayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);

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

        mFragmentPlayerBinding.imageViewPlay.setOnClickListener(v -> {
            toggleTrack();
        });

        mFragmentPlayerBinding.imageViewPlay.setButtonBackgroundColor(getActivity(), vibrantColor);
        mFragmentPlayerBinding.seekBarPlayer.setProgressColor(vibrantColor);
        mFragmentPlayerBinding.seekBarPlayer.setThumbColor(vibrantColor);

        if (Utility.isVersionLollipopAndAbove())
            mFragmentPlayerBinding.imageViewAlbum.setTransitionName(mImageViewAlbumTransitionName);

        if (mTrackParcelable != null) {
            mFragmentPlayerBinding.textViewTrackName.setText(mTrackParcelable.songName);
            mFragmentPlayerBinding.textViewTrackAlbum.setText(mTrackParcelable.albumName);
            int size = mTrackParcelable.albumImageUrls.size();
            if (size > 0) {
                Utility.loadImage(getActivity(),
                        mTrackParcelable.albumImageUrls.get(size - 1),
                        mTrackParcelable.albumImageUrls.get(0),
                        mFragmentPlayerBinding.imageViewAlbum,
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
                    public void onCompleted() {}

                    @Override
                    public void onError(final Throwable e) {}

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
        
        return mFragmentPlayerBinding.getRoot();
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

        mFragmentPlayerBinding.imageViewPlay.setPauseMode(true);
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
        mFragmentPlayerBinding.imageViewPlay.setPauseMode(false);
        if (mFragmentPlayerBinding.chronometerStart != null) {
            mFragmentPlayerBinding.chronometerStart.setBase(SystemClock.elapsedRealtime());
            mFragmentPlayerBinding.chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                    stoppedEvent.duration);
            mFragmentPlayerBinding.chronometerStart.stop();
        }
        mFragmentPlayerBinding.seekBarPlayer.setProgress(0);
    }

    public void onEventMainThread(MediaPlayerHandler.PausedEvent pausedEvent) {
        mFragmentPlayerBinding.imageViewPlay.setPauseMode(false);
        if (mFragmentPlayerBinding.chronometerStart != null) {
            mFragmentPlayerBinding.chronometerStart.setBase(SystemClock.elapsedRealtime() - pausedEvent.progress);
            mFragmentPlayerBinding.chronometerStart.stop();
        }
    }

    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent) {
        try {
            mFragmentPlayerBinding.imageViewPlay.setPauseMode(true);
            if (mFragmentPlayerBinding.chronometerStart != null) {
                mFragmentPlayerBinding.chronometerStart.setBase(SystemClock.elapsedRealtime() - playingEvent.progress);
                mFragmentPlayerBinding.chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                        playingEvent.duration);
                mFragmentPlayerBinding.chronometerStart.start();
            }
            mAmountToUpdate = (playingEvent.duration) / Keys.SMOOTHNESS_FACTOR;
            mFragmentPlayerBinding.seekBarPlayer.setOnSeekBarChangeListener(SeekbarChangeListener
                    .getInstance(playingEvent.duration, Keys.SMOOTHNESS_FACTOR, mFragmentPlayerBinding.chronometerStart));
            mFragmentPlayerBinding.seekBarPlayer.setMax(Keys.SMOOTHNESS_FACTOR);
            mFragmentPlayerBinding.seekBarPlayer.setThumbAlpha(255);
            mFragmentPlayerBinding.seekBarPlayer.setProgress(playingEvent.progress / mAmountToUpdate);

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
                                    if ((mAmountToUpdate * mFragmentPlayerBinding.seekBarPlayer.getProgress() < playingEvent.duration + playingEvent.progress)) {
                                        currentProgress = mFragmentPlayerBinding.seekBarPlayer.getProgress();
                                        currentProgress += 1;
                                        mFragmentPlayerBinding.seekBarPlayer.setProgress(currentProgress);
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
    }



    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        tracksFragment.refreshActionBar();
    }

    public void setImageViewAlbumTransitionName(final String imageViewAlbumTransitionName) {
        mImageViewAlbumTransitionName = imageViewAlbumTransitionName;
    }
}
