package app.minimize.com.spotifystreamer;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.Callable;

/**
 * Created by ahmedrizwan on 6/9/15.
 */
public class Utility {
    public static void runOnUiThread(AppCompatActivity context, final Callable callable) {
        context.runOnUiThread(() -> {
            try {
                callable.call();
            } catch (Exception e) {
                Log.e("Exception(UiThread)", e.toString());
            }
        });
    }

    public static void runOnWorkerThread(Callable callable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    Log.e("Exception(WorkerThread)", e.toString());
                }
            }
        }).start();
    }

    public static boolean isVersionLollipopAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
