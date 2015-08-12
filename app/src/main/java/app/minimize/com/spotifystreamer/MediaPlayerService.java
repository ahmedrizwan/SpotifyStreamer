package app.minimize.com.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.Notifications;
import de.greenrobot.event.EventBus;

/**
 * Created by ahmedrizwan on 6/18/15.
 */
public class MediaPlayerService extends Service {

    private static final String TAG = "MediaPlayerService";

    //MediaPlayer
    private MediaPlayerHandler mMediaPlayerHandler;

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Just instantiate the handler for mediaPlayer
        mMediaPlayerHandler = MediaPlayerHandler.getInstance(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_STICKY;
    }

    public void onEventMainThread(MediaPlayerHandler.StoppedEvent stoppedEvent) {
        Notifications.cancelNotification(this);
    }

    public void onEventMainThread(MediaPlayerHandler.PausedEvent pausedEvent) {
        handleNotification();
    }


    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent) {
        handleNotification();

    }
    private void handleNotification() {
        if(MyPreferenceFragment.isNotificationModeOn(this)) {
            try {
                int size = mMediaPlayerHandler.getTrackParcelable().albumImageUrls.size();
                Notifications.showPlayerNotifications(this, mMediaPlayerHandler.getTrackParcelable().albumImageUrls.get(size - 2), MediaPlayerHandler.getMediaPlayerState(), mMediaPlayerHandler.getTrackParcelable().songName);
            } catch (Exception e) {
                Notifications.showPlayerNotifications(this, null, MediaPlayerHandler.getMediaPlayerState(), mMediaPlayerHandler.getTrackParcelable().songName);
            }
        } else
            Notifications.cancelNotification(this);
    }

}
