package app.minimize.com.spotifystreamer.HelperClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by ahmedrizwan on 3/8/15.
 */
public class PlayerNotificationReceiver extends BroadcastReceiver {

    private void logHelper(String message) {
        Log.e("PendingIntent", message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//
//        String action = intent.getAction();
//        String fileName = intent.getStringExtra(Keys.KEY_TRACK_NAME);
//        logHelper("Player Notification Receiver with FileName: " + fileName);
//        if (action != null && MediaPlayerHandler.getInstance() != null) {
//            logHelper(action);
//            switch (action) {
//                case Keys.PAUSE:
//                    logHelper("Pause inside pending intent...");
//                    Notifications.showPlayerNotifications(context, MediaPlayerInterface.MediaPlayerState.Paused, fileName);
//                    // pause the recorder
//                    MediaPlayerHandler.getInstance()
//                            .handleMediaPlayer(context,null);
//                    break;
//                case Keys.STOP: {
//                    logHelper("Stop inside pending intent...");
//                    Notifications.cancelNotification(context);
//                    // stop the recorder
//                    MediaPlayerHandler.getInstance()
//                            .stopMediaPlayer();
//                    break;
//                }
//                case Keys.RESUME: {
//                    logHelper("Resume inside pending intent...");
//                    Notifications.showPlayerNotifications(context, MediaPlayerInterface.MediaPlayerState.Playing, fileName);
//                    // resume the recorder
//                    MediaPlayerHandler.getInstance()
//                            .handleMediaPlayer(context, null);
//                    break;
//                }
//            }
//        }
    }
}
