package app.minimize.com.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import de.greenrobot.event.EventBus;

/**
 * Created by ahmedrizwan on 6/18/15.
 */
public class MediaPlayerService extends Service {

    private static final String TAG = "MediaPlayerService";
    private TrackParcelable mTrackParcelable;
    private ArrayList<TrackParcelable> mTrackParcelables;
    private int mVibrantColor = Color.BLACK;
    private EventBus mEventBus;

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
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        try {
            int indexOfTrack = -2;
            String stringExtra = intent.getStringExtra(getString(R.string.key_event));
            switch (stringExtra) {
                case MediaPlayerHandler.PLAY:
                    if (mTrackParcelable != null) {
                        mMediaPlayerHandler.handlePlayback(mTrackParcelable.previewUrl, mTrackParcelable.songName);
                        mEventBus.post(mTrackParcelable);
                        mEventBus.post(mVibrantColor);
                    }
                    break;
                case MediaPlayerHandler.PAUSE:

                    break;
                case MediaPlayerHandler.STOP:

                    break;
                case MediaPlayerHandler.NEXT:
                    indexOfTrack = mTrackParcelables.indexOf(mTrackParcelable);
                    if (indexOfTrack < mTrackParcelables.size()) {
                        mTrackParcelable = mTrackParcelables.get(indexOfTrack + 1);
                        mMediaPlayerHandler.handlePlayback(mTrackParcelable.previewUrl, mTrackParcelable.songName);
                        mEventBus.post(mTrackParcelable);
                    }
                    break;
                case MediaPlayerHandler.PREVIOUS:
                    indexOfTrack = mTrackParcelables.indexOf(mTrackParcelable);
                    if (indexOfTrack > 0) {
                        mTrackParcelable = mTrackParcelables.get(indexOfTrack - 1);
                        mMediaPlayerHandler.handlePlayback(mTrackParcelable.previewUrl, mTrackParcelable.songName);
                        mEventBus.post(mTrackParcelable);
                    }
                    break;
            }
        } catch (Exception e) {

        }

        return START_STICKY;
    }

    public void onEventMainThread(TrackParcelable trackParcelable) {
        Log.e(TAG, "onEvent TrackParcelable");
        mTrackParcelable = trackParcelable;
    }

    public void onEventMainThread(ArrayList<TrackParcelable> trackParcelables) {
        mTrackParcelables = trackParcelables;
    }

    public void onEventMainThread(int vibrantColor) {
        mVibrantColor = vibrantColor;
    }


}
