package app.minimize.com.spotifystreamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerInterface;

/**
 * Created by ahmedrizwan on 6/18/15.
 */
public class MediaPlayerService extends Service implements MediaPlayerInterface {

    /***
     * A common bundle used for communicating with the activity
     */
    private Bundle mBundle = new Bundle();

    @Override
    public void playing(final int duration, final int progress) {
        if (mResultReceiver != null) {
            mBundle.putInt(Keys.KEY_DURATION, duration);
            mBundle.putInt(Keys.KEY_PROGRESS, progress);
            mResultReceiver.send(Keys.CODE_PLAYING, mBundle);
        }
    }

    @Override
    public void stopped() {
        if (mResultReceiver != null) {
            mResultReceiver.send(Keys.CODE_STOPPED, null);
        }
    }

    @Override
    public void paused() {
        if (mResultReceiver != null) {
            mResultReceiver.send(Keys.CODE_PAUSED, null);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    //MediaPlayer
    private MediaPlayerHandler mMediaPlayerHandler;
    //Bundle
    private ResultReceiver mResultReceiver;


    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mMediaPlayerHandler = MediaPlayerHandler.getInstance(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (statusReceiver(intent)) return START_STICKY;
        if(playerReceiver(intent)) return START_STICKY;
        return START_STICKY;
    }

    private boolean statusReceiver(final Intent intent) {
        try {
            ResultReceiver parcelableExtra = intent.getParcelableExtra(Keys.KEY_GET_STATUS);
            if (null != parcelableExtra) {
                mResultReceiver = parcelableExtra;
                handleStateIntent();
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    private void handleStateIntent() {
        logHelper("handleStateIntent");
        Bundle bundle = new Bundle();
        bundle.putInt(Keys.STATUS, MediaPlayerHandler.getPlayerState().ordinal());
        mResultReceiver.send(Keys.KEY_STATUS_CODE, bundle);
    }

    private boolean playerReceiver(Intent intent) {
        try {
            if (null != intent.getParcelableExtra(Keys.KEY_PLAYER_RECEIVER)) {
                mResultReceiver = intent.getParcelableExtra(Keys.KEY_PLAYER_RECEIVER);
                playTrack(intent);




                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    private void playTrack(final Intent intent) {
//        try {
            //set url
            String trackUrl = intent.getStringExtra(Keys.KEY_TRACK_URL);
            String trackName = intent.getStringExtra(Keys.KEY_TRACK_NAME);
            mMediaPlayerHandler.handlePlayback(trackUrl,trackName);

//        } catch (NullPointerException e) {
//            logHelper(e.getMessage());
//        }
    }

    private void logHelper(final String message) {
        Log.e("Service", message);
    }


}
