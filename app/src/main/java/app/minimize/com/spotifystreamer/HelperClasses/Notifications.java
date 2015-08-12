package app.minimize.com.spotifystreamer.HelperClasses;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.R;

/**
 * Created by ahmedrizwan on 3/8/15.
 */
public class Notifications {

    public static void showPlayerNotifications(Context context, String albumImageUrl, MediaPlayerHandler.MediaPlayerState mediaPlayerState, String trackName) {
        //Create Notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent containerActivity = new Intent(context, ContainerActivity.class);
        containerActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Now the intent for pause
        Intent pauseIntent = new Intent(context, PlayerNotificationReceiver.class);
        pauseIntent.setAction(Keys.PAUSE);
        pauseIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);

        //Now the intent for stop
        Intent nextIntent = new Intent(context, PlayerNotificationReceiver.class);
        nextIntent.setAction(Keys.NEXT);
        nextIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);

        Intent previousIntent = new Intent(context, PlayerNotificationReceiver.class);
        previousIntent.setAction(Keys.PREVIOUS);
        previousIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);

        //Then intent for resume
        Intent playIntent = new Intent(context, PlayerNotificationReceiver.class);
        playIntent.setAction(Keys.PLAY);
        playIntent.putExtra(Keys.KEY_TRACK_NAME, trackName);

        if (mediaPlayerState == MediaPlayerHandler.MediaPlayerState.Playing) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, containerActivity, 0);
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            new Thread(() -> {
                try {
                    builder.setContentTitle(context.getString(R.string.app_name))
                            .setContentText("Playing " + trackName)
                            .setLargeIcon(Picasso.with(context).load(albumImageUrl).get())
                            .setSmallIcon(R.drawable.ic_stat_play)
                            .addAction(R.drawable.ic_stat_previous, Keys.PREVIOUS, previousPendingIntent)
                            .addAction(R.drawable.ic_stat_pause, Keys.PAUSE, pausePendingIntent)
                            .addAction(R.drawable.ic_stat_next, Keys.NEXT, nextPendingIntent);
                    builder.setContentIntent(pendingIntent);
                    Notification notification = builder.build();
                    notification.flags = Notification.FLAG_ONGOING_EVENT;
                    notificationManager.notify(Keys.KEY_NOTIFICATION_ID, notification);
                } catch (IOException e) {
                    Log.e("Notifications", "showPlayerNotifications " + e.toString());
                }
            }).start();

        } else if (mediaPlayerState == MediaPlayerHandler.MediaPlayerState.Paused) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, containerActivity, 0);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            new Thread(() -> {
                try {
                    builder.setContentTitle(context.getString(R.string.app_name))
                            .setContentText("Playing " + trackName)
                            .setLargeIcon(Picasso.with(context).load(albumImageUrl).get())
                            .setSmallIcon(R.drawable.ic_stat_play)
                            .addAction(R.drawable.ic_stat_previous, Keys.PREVIOUS, previousPendingIntent)
                            .addAction(R.drawable.ic_stat_play, Keys.PLAY, playPendingIntent)
                            .addAction(R.drawable.ic_stat_next, Keys.NEXT, nextPendingIntent);
                    builder.setContentIntent(pendingIntent);
                    Notification notification = builder.build();
                    notification.flags = Notification.FLAG_ONGOING_EVENT;
                    notificationManager.notify(Keys.KEY_NOTIFICATION_ID, notification);
                } catch (IOException e) {
                    Log.e("Notifications", "showPlayerNotifications " + e.toString());
                }
            }).start();
        } else if (mediaPlayerState == MediaPlayerHandler.MediaPlayerState.Stopped) {
            cancelNotification(context);
        }
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Keys.KEY_NOTIFICATION_ID);
    }
}
