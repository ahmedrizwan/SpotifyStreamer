package app.minimize.com.spotifystreamer.HelperClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import app.minimize.com.spotifystreamer.Activities.Keys;


/**
 * Created by ahmedrizwan on 3/8/15.
 */
public class PlayerNotificationReceiver extends BroadcastReceiver {

    private void logHelper(String message) {
        Log.e("PendingIntent", message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String fileName = intent.getStringExtra(Keys.KEY_TRACK_NAME);
        logHelper("Player Notification Receiver with FileName: " + fileName);
        if (action != null && MediaPlayerHandler.getInstance() != null) {
            logHelper(action);
            switch (action) {
                case Keys.PAUSE:
                    // pause the recorder
                    MediaPlayerHandler.getInstance()
                            .togglePlayPause();
                    break;
                case Keys.NEXT:
                    MediaPlayerHandler.getInstance()
                            .nextTrack();
                    break;
                case Keys.PREVIOUS:
                    MediaPlayerHandler.getInstance()
                            .previousTrack();
                    break;
                case Keys.PLAY:
                    // pause the recorder
                    MediaPlayerHandler.getInstance()
                            .togglePlayPause();
                    break;
            }
        }
    }

}
