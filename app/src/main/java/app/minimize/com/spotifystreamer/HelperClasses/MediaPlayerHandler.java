package app.minimize.com.spotifystreamer.HelperClasses;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import de.greenrobot.event.EventBus;


/**
 * Created by ahmedrizwan on 2/9/15.
 */
public class MediaPlayerHandler implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public void stopMediaPlayer() {

    }

    public enum MediaPlayerState {Playing, Stopped, Paused, Idle}

    public void resendPlayerEvents() {
        EventBus.getDefault().post(mTrackParcelable);
        if (mMediaPlayerState == MediaPlayerState.Playing) {
            EventBus.getDefault()
                    .post(mPlayingEvent.setDuration(mMediaPlayer.getDuration())
                            .setProgress(mMediaPlayer.getCurrentPosition()));
        } else if (mMediaPlayerState == MediaPlayerState.Stopped) {
            EventBus.getDefault()
                    .post(mStoppedEvent);
        } else if (mMediaPlayerState == MediaPlayerState.Paused) {
            EventBus.getDefault()
                    .post(mPausedEvent.setProgress(mMediaPlayer.getCurrentPosition()));
        }

    }

    public TrackParcelable getTrackParcelable() {
        return mTrackParcelable;
    }

    public List<TrackParcelable> getTrackParcelables() {
        return mTrackParcelables;
    }

    public void setTrackParcelables(ArrayList<TrackParcelable> trackParcelables) {
        mTrackParcelables = trackParcelables;
    }

    public void setTrackParcelable(TrackParcelable trackParcelable) {
        mTrackParcelable = trackParcelable;
    }

    public void nextTrack() {
        int indexOfTrack = mTrackParcelables.indexOf(mTrackParcelable);
        if (indexOfTrack < mTrackParcelables.size()-1) {
            handlePlayback(mTrackParcelables.get(indexOfTrack + 1));
        }
    }

    public void previousTrack() {
        int indexOfTrack = mTrackParcelables.indexOf(mTrackParcelable);
        if (indexOfTrack > 0) {
            handlePlayback(mTrackParcelables.get(indexOfTrack - 1));
        }
    }

    //State
    MediaPlayerState mMediaPlayerState = MediaPlayerState.Idle;
    private static MediaPlayerHandler sMediaPlayerHandler;
    private MediaPlayer mMediaPlayer;
    private Context mContext;

    private TrackParcelable mTrackParcelable;
    private List<TrackParcelable> mTrackParcelables;


    //Events
    private PlayingEvent mPlayingEvent;
    private StoppedEvent mStoppedEvent;
    private PausedEvent mPausedEvent;

    private final String TAG = "MediaPlayerHandler";

    private MediaPlayerHandler() {
    }

    /***
     * Singleton instance getter
     *
     * @return MediaPlayerHandler Instance
     */
    public static MediaPlayerHandler getInstance(Context context) {
        if (sMediaPlayerHandler == null) {
            sMediaPlayerHandler = new MediaPlayerHandler();
            sMediaPlayerHandler.mMediaPlayer = new MediaPlayer();
            sMediaPlayerHandler.mPausedEvent = new PausedEvent(0, 0);
            sMediaPlayerHandler.mPlayingEvent = new PlayingEvent(0, 0);
            sMediaPlayerHandler.mStoppedEvent = new StoppedEvent(0);
            sMediaPlayerHandler.setContext(context);
        }
        return sMediaPlayerHandler;
    }

    public static MediaPlayerHandler getInstance() {
        return sMediaPlayerHandler;
    }


    public static MediaPlayer getPlayer() {
        if (sMediaPlayerHandler != null)
            return sMediaPlayerHandler.mMediaPlayer;
        else
            return null;
    }

    /***
     * Sets the context
     *
     * @param context
     */
    public void setContext(final Context context) {
        mContext = context;
    }

    public void handlePlayback(TrackParcelable trackParcelable) {
        //check if its the same old file or a new one
        boolean newFile;
        try {
            newFile = !mTrackParcelable.previewUrl.equals(trackParcelable.previewUrl);
        }catch (NullPointerException e){
            newFile = false;
        }
        mTrackParcelable = trackParcelable;

        if (newFile || mMediaPlayerState == MediaPlayerState.Stopped) {

            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }

            mMediaPlayer = new MediaPlayer();
        }

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            playPauseOrStopMediaPlayer(newFile, audioManager);
        }
    }

    private void playPauseOrStopMediaPlayer(final boolean newFile, final AudioManager audioManager) {
        Log.e("Player", "Granted!");
        if (mMediaPlayerState != MediaPlayerState.Stopped && !newFile && mMediaPlayerState!=MediaPlayerState.Idle) {
            Log.e("Player", "the Same old file!");
            EventBus.getDefault().post(mTrackParcelable);
            resendPlayerEvents();
        } else {
            try {
                Log.e("Player", "Load!");
                // Start playback.
                if (audioManager.isMusicActive()) {
                    Intent i = new Intent("com.android.music.musicservicecommand");
                    i.putExtra("command", "pause");
                    mContext.sendBroadcast(i);
                }
                EventBus.getDefault().post(mTrackParcelable);
                playUrl();
            } catch (IOException e) {
                Toast.makeText(mContext, "Unable to play " + mTrackParcelable.songName, Toast.LENGTH_SHORT)
                        .show();
            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void playUrl() throws IOException, IllegalStateException {
        mMediaPlayer = new MediaPlayer();
        new Thread(() -> {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(mContext, Uri.parse(mTrackParcelable.previewUrl));
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ).start();
    }


    @Override
    public void onAudioFocusChange(final int focusChange) {
        try {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                if (!mMediaPlayer.isPlaying())
                    togglePlayPause();
            }
        } catch (Exception e) {
            logHelper(e.toString());
        }
    }

    public static MediaPlayerState getMediaPlayerState() {
        if (sMediaPlayerHandler == null)
            return MediaPlayerState.Idle;
        else
            return sMediaPlayerHandler.mMediaPlayerState;
    }

    public void togglePlayPause() {
        if (mMediaPlayerState == MediaPlayerState.Playing) {
            mMediaPlayer.pause();
            setPlayerState(MediaPlayerState.Paused);
            EventBus.getDefault()
                    .post(mPausedEvent.setProgress(mMediaPlayer.getCurrentPosition()));
        } else if (mMediaPlayerState == MediaPlayerState.Paused) {
            mMediaPlayer.start();
            setPlayerState(MediaPlayerState.Playing);
            //Post the playingEvent
            EventBus.getDefault()
                    .post(mPlayingEvent.setDuration(mMediaPlayer.getDuration())
                            .setProgress(mMediaPlayer.getCurrentPosition()));
        } else {
            handlePlayback(getTrackParcelable());
        }
    }

    private void logHelper(final String message) {
        Log.e(TAG, message);
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        setPlayerState(MediaPlayerState.Playing);
        EventBus.getDefault()
                .post(mPlayingEvent.setDuration(mediaPlayer.getDuration())
                        .setProgress(0));
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        setPlayerState(MediaPlayerState.Stopped);
        EventBus.getDefault()
                .post(mStoppedEvent.setDuration(mediaPlayer.getDuration()));
        mMediaPlayer.release();
    }

    public void setPlayerState(final MediaPlayerState playerState) {
        mMediaPlayerState = playerState;
    }

    static public class PausedEvent {
        public int progress;
        public int duration;

        public PausedEvent(int duration, int progress) {
            this.progress = progress;
        }

        public PausedEvent setProgress(int progress) {
            this.progress = progress;
            return this;
        }

        public PausedEvent setDuration(int duration) {
            this.duration = duration;
            return this;
        }
    }

    static public class StoppedEvent {
        public int duration;

        public StoppedEvent setDuration(final int duration) {
            this.duration = duration;
            return this;
        }

        public StoppedEvent(final int duration) {
            this.duration = duration;
        }
    }

    static public class PlayingEvent {
        public int duration;
        public int progress;

        public PlayingEvent setDuration(final int duration) {
            this.duration = duration;
            return this;
        }

        public PlayingEvent setProgress(final int progress) {
            this.progress = progress;
            return this;
        }

        public PlayingEvent(final int duration, final int progress) {
            this.duration = duration;
            this.progress = progress;
        }
    }

}