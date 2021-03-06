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

import java.util.Timer;
import java.util.TimerTask;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.SeekbarChangeListener;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.databinding.FragmentPlayerBinding;
import de.greenrobot.event.EventBus;

/**
 * Created by ahmedrizwan on 6/15/15.
 */
public class PlayerDialogFragment extends DialogFragment {

    private static final String TAG = "PlayerDialogFragment";
    private int mAmountToUpdate = 0, currentProgress = 0;
    private Timer timer;
    private String mImageViewAlbumTransitionName;

    private FragmentPlayerBinding mFragmentPlayerBinding;
    private boolean mTwoPane = false;
    private MediaPlayerHandler mMediaPlayerHandler;
    private TrackParcelable mTrackParcelable;

    public static PlayerDialogFragment getInstance() {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        return playerDialogFragment;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mFragmentPlayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);

        EventBus.getDefault()
                .register(this);

        mTwoPane = ((ContainerActivity) getActivity()).isTwoPane();
        if (mTwoPane) {
            //make it full screen
            getDialog().getWindow()
                    .requestFeature(Window.FEATURE_NO_TITLE);
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_player);
        }

        mMediaPlayerHandler = MediaPlayerHandler.getInstance();

        try {
            mTrackParcelable = getArguments().getParcelable(Keys.KEY_TRACK_PARCELABLE);
        } catch (NullPointerException e) {
            mTrackParcelable = null;
        }

        ((ContainerActivity) getActivity()).hideCard();


        if (Utility.isVersionLollipopAndAbove())
            mFragmentPlayerBinding.imageViewAlbum.setTransitionName(mImageViewAlbumTransitionName);


        //next track listener
        mFragmentPlayerBinding.imageViewNext.setOnClickListener(v -> {
            mMediaPlayerHandler.nextTrack();
        });

        //previous track
        mFragmentPlayerBinding.imageViewPrevious.setOnClickListener(v -> {
            mMediaPlayerHandler.previousTrack();
        });

        //play/pause track
        mFragmentPlayerBinding.imageViewPlay.setOnClickListener(v -> {
            mMediaPlayerHandler.togglePlayPause();
        });

        //share click listener
        mFragmentPlayerBinding.imageButtonShare.setOnClickListener(v -> {
            shareTextUrl(mMediaPlayerHandler.getTrackParcelable().previewUrl, mMediaPlayerHandler.getTrackParcelable().songName);
        });

        if (savedInstanceState == null)
            playThisTrack();
        else {
            onEventMainThread(mMediaPlayerHandler.getTrackParcelable());
            mMediaPlayerHandler.resendPlayerEvents();
        }

        return mFragmentPlayerBinding.getRoot();
    }

    public void playThisTrack() {
        //pause the seekbar
        pauseTheSeekbar();
        if (mTrackParcelable != null)
            mMediaPlayerHandler.handlePlayback(mTrackParcelable);
        else
            mMediaPlayerHandler.resendPlayerEvents();
    }

    private void shareTextUrl(String url, String songName) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, songName);
        share.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(share, "Share Preview"));
    }

    public void onEventMainThread(TrackParcelable mTrackParcelable) {
        pauseTheSeekbar();
        mFragmentPlayerBinding.textViewTrackName.setText(mTrackParcelable.songName);
        mFragmentPlayerBinding.textViewTrackAlbum.setText(mTrackParcelable.albumName);
        int size = mTrackParcelable.albumImageUrls.size();
        if (size > 0) {
            Utility.loadImage(getActivity(),
                    mTrackParcelable.albumImageUrls.get(size - 1),
                    mTrackParcelable.albumImageUrls.get(0),
                    mFragmentPlayerBinding.imageViewAlbum,
                    val -> {
                        if (mTwoPane)
                            updateColors(Color.BLACK);
                        else
                            updateColors(val);
                    });
        }

    }

    public void updateColors(int mVibrantColor) {
        Log.e(TAG, "updateColors " + mVibrantColor);

        if (mTwoPane) {
            getDialog().getWindow()
                    .requestFeature(Window.FEATURE_NO_TITLE);
        } else {
            Utility.setActionBarAndStatusBarColor(((AppCompatActivity) getActivity()), mVibrantColor);
        }

        mFragmentPlayerBinding.imageViewPlay.setButtonBackgroundColor(getActivity(), mVibrantColor);
        mFragmentPlayerBinding.seekBarPlayer.setProgressColor(mVibrantColor);
        mFragmentPlayerBinding.seekBarPlayer.setThumbColor(mVibrantColor);

    }

    private void pauseTheSeekbar() {
        if (timer != null) {
            timer.cancel();
        }
        mFragmentPlayerBinding.seekBarPlayer.setProgress(0);
        mFragmentPlayerBinding.seekBarPlayer.setEnabled(false);
        mFragmentPlayerBinding.chronometerStart.setBase(SystemClock.elapsedRealtime());
        mFragmentPlayerBinding.chronometerStart.stop();
        mFragmentPlayerBinding.chronometerEnd.setBase(SystemClock.elapsedRealtime());
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
        mFragmentPlayerBinding.imageViewPlay.setMode(true);
        if (mFragmentPlayerBinding.chronometerStart != null) {
            mFragmentPlayerBinding.chronometerStart.setBase(SystemClock.elapsedRealtime());
            mFragmentPlayerBinding.chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                    stoppedEvent.duration);
            mFragmentPlayerBinding.chronometerStart.stop();
        }
        mFragmentPlayerBinding.seekBarPlayer.setProgress(0);
    }

    public void onEventMainThread(MediaPlayerHandler.PausedEvent pausedEvent) {
        mFragmentPlayerBinding.imageViewPlay.setMode(true);
        if (mFragmentPlayerBinding.chronometerStart != null) {
            mFragmentPlayerBinding.chronometerStart.setBase(SystemClock.elapsedRealtime() - pausedEvent.progress);
            mFragmentPlayerBinding.chronometerStart.stop();
        }
    }

    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent) {
        try {
            mFragmentPlayerBinding.imageViewPlay.setMode(false);
            mFragmentPlayerBinding.seekBarPlayer.setEnabled(true);

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
                                if (MediaPlayerHandler.getMediaPlayerState() == MediaPlayerHandler.MediaPlayerState.Playing) {
                                    if ((mAmountToUpdate * mFragmentPlayerBinding.seekBarPlayer.getProgress() <
                                            playingEvent.duration + playingEvent.progress)) {
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
    }

    public void setImageViewAlbumTransitionName(final String imageViewAlbumTransitionName) {
        mImageViewAlbumTransitionName = imageViewAlbumTransitionName;
    }
}
