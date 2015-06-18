package app.minimize.com.spotifystreamer.HelperClasses;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.R;

/**
 * Created by ahmedrizwan on 3/8/15.
 */
public class Notifications {

    public static void showPlayerNotifications(Context context, MediaPlayerInterface.MediaPlayerState mediaPlayerState, String trackName) {
        //Create Notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent containerActivity = new Intent(context, ContainerActivity.class);
        containerActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Now the intent for pause
        Intent pauseIntent = new Intent(context, PlayerNotificationReceiver.class);
        pauseIntent.setAction(Keys.PAUSE);
        pauseIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);
        //Now the intent for stop
        Intent stopIntent = new Intent(context, PlayerNotificationReceiver.class);
        stopIntent.setAction(Keys.STOP);
        stopIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);
        //Then intent for resume
        Intent resumeIntent = new Intent(context, PlayerNotificationReceiver.class);
        resumeIntent.setAction(Keys.RESUME);
        resumeIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);

        if (mediaPlayerState == MediaPlayerInterface.MediaPlayerState.Playing) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, containerActivity, 0);
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Playing " + trackName )
                    .setSmallIcon(R.drawable.ic_not_available)
                    .addAction(R.drawable.ic_not_available, Keys.PAUSE, pausePendingIntent)
                    .addAction(R.drawable.ic_not_available, Keys.STOP, stopPendingIntent);
            builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(Keys.KEY_NOTIFICATION_ID, notification);
        } else if (mediaPlayerState == MediaPlayerInterface.MediaPlayerState.Paused) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, containerActivity, 0);
            PendingIntent resumePendingIntent = PendingIntent.getBroadcast(context, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Playback paused...")
                    .setSmallIcon(R.drawable.ic_not_available)
                    .addAction(R.drawable.ic_not_available, Keys.RESUME, resumePendingIntent)
                    .addAction(R.drawable.ic_not_available, Keys.STOP, stopPendingIntent);
            builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(Keys.KEY_NOTIFICATION_ID, notification);
        } else if (mediaPlayerState == MediaPlayerInterface.MediaPlayerState.Stopped) {
            cancelNotification(context);
        }
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Keys.KEY_NOTIFICATION_ID);
    }
}
