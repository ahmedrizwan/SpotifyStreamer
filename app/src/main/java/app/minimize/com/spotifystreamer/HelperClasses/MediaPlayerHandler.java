package app.minimize.com.spotifystreamer.HelperClasses;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;


/**
 * Created by ahmedrizwan on 2/9/15.
 */
public class MediaPlayerHandler implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    //State
    MediaPlayerInterface.MediaPlayerState mMediaPlayerState = MediaPlayerInterface.MediaPlayerState.Idle;
    private static MediaPlayerHandler sMediaPlayerHandler;
    private MediaPlayerInterface mMediaPlayerInterface;
    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private String mTrackUrl="", mTrackName="";

    private final String TAG = "MediaPlayerHandler";

    private MediaPlayerHandler() {
    }

    /***
     * Returns the state idle if mediaPlayerHandler is uninitialized
     *
     * @return Player state
     */
    public static MediaPlayerInterface.MediaPlayerState getPlayerState() {
        if (sMediaPlayerHandler == null)
            return MediaPlayerInterface.MediaPlayerState.Idle;
        else
            return sMediaPlayerHandler.mMediaPlayerState;
    }

    /***
     * Singleton instance getter
     *
     * @param mediaPlayerInterface
     * @return MediaPlayerHandler Instance
     */
    public static MediaPlayerHandler getInstance(MediaPlayerInterface mediaPlayerInterface) {
        if (sMediaPlayerHandler == null) {
            sMediaPlayerHandler = new MediaPlayerHandler();
            sMediaPlayerHandler.mMediaPlayerInterface = mediaPlayerInterface;
            sMediaPlayerHandler.mMediaPlayer = new MediaPlayer();
            sMediaPlayerHandler.setContext(mediaPlayerInterface.getContext());
        }
        return sMediaPlayerHandler;
    }

    /***
     * Sets the context
     *
     * @param context
     */
    public void setContext(final Context context) {
        mContext = context;
    }

    public void handlePlayback(String trackUrl, String trackName) {
        //check if its the same old file or a new one
        boolean newFile = !mTrackUrl.equals(trackUrl);
        mTrackUrl = trackUrl;
        mTrackName = trackName;

        if (newFile || mMediaPlayerState == MediaPlayerInterface.MediaPlayerState.Stopped) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
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
        if (mMediaPlayerState != MediaPlayerInterface.MediaPlayerState.Stopped && !newFile) {
            Log.e("Player", "Toggle!");
            togglePlayPause();
        } else {
            try {
                Log.e("Player", "Load!");
                // Start playback.
                if (audioManager.isMusicActive()) {
                    Intent i = new Intent("com.android.music.musicservicecommand");
                    i.putExtra("command", "pause");
                    mContext.sendBroadcast(i);
                }
                playUrl();
            } catch (IOException e) {
                Toast.makeText(mContext, "Unable to play " + mTrackName, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void playUrl() throws IOException {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mContext, Uri.parse(mTrackUrl));

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepare();
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {
        Log.e("onAudioFocusChange", "togglePlayPause");
        try {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                if (!mMediaPlayer.isPlaying())
                    togglePlayPause();
            }
        } catch (NullPointerException e) {
            logHelper(e.getMessage());
        }
    }

    private void togglePlayPause() {
        if (mMediaPlayerState == MediaPlayerInterface.MediaPlayerState.Playing) {
            mMediaPlayer.pause();
            setPlayerState(MediaPlayerInterface.MediaPlayerState.Paused);
            mMediaPlayerInterface.paused();
        } else {
            mMediaPlayer.start();
            setPlayerState(MediaPlayerInterface.MediaPlayerState.Playing);
            mMediaPlayerInterface.playing(mMediaPlayer.getDuration(), mMediaPlayer.getCurrentPosition());
        }
    }

    private void logHelper(final String message) {
        Log.e(TAG, message);
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        setPlayerState(MediaPlayerInterface.MediaPlayerState.Playing);
        mMediaPlayerInterface.playing(mMediaPlayer.getDuration(), 0);
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        mMediaPlayer.release();
        setPlayerState(MediaPlayerInterface.MediaPlayerState.Stopped);
        mMediaPlayerInterface.stopped();
    }

    public void setPlayerState(final MediaPlayerInterface.MediaPlayerState playerState) {
        mMediaPlayerState = playerState;
    }
}