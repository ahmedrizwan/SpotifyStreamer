package app.minimize.com.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import de.greenrobot.event.EventBus;

/**
 * Created by ahmedrizwan on 6/18/15.
 *
 */
public class MediaPlayerService extends Service {

    private static final String TAG = "MediaPlayerService";
    private TrackParcelable mTrackParcelable;

    //MediaPlayer
    private MediaPlayerHandler mMediaPlayerHandler;

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
        if (playerReceiver(intent)) return START_STICKY;
        return START_STICKY;
    }

    private boolean statusReceiver(final Intent intent) {
        try {
            if (intent.getBooleanExtra(Keys.KEY_GET_STATUS, false)) {
//                RxBus.getInstance().send(new MediaPlayerHandler.PlayingEvent(0,0));
                EventBus.getDefault().post(mTrackParcelable);
                Log.e(TAG, "statusReceiver ");
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    private boolean playerReceiver(Intent intent) {
        try {
            //retrieve the parcelables
            mTrackParcelable = intent.getParcelableExtra(Keys.KEY_TRACK_PARCELABLE);
            if (mTrackParcelable != null)
                mMediaPlayerHandler.handlePlayback(mTrackParcelable.previewUrl, mTrackParcelable.songName);
            return true;
        } catch (NullPointerException e) {
            logHelper("PlayTrack Exception");
            return false;
        }
    }

    private void logHelper(final String message) {
        Log.e("Service", message);
    }



}
