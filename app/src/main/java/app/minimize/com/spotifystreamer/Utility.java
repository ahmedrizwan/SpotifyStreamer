package app.minimize.com.spotifystreamer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
        new Thread(() -> {
            try {
                callable.call();
            } catch (Exception e) {
                Log.e("Exception(WorkerThread)", e.toString());
            }
        }).start();
    }

    public static boolean isVersionLollipopAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void launchFragment(final AppCompatActivity activity, int containerId, final Fragment fragment) {
        FragmentTransaction trans = activity.getSupportFragmentManager()
                .beginTransaction();
        trans.replace(containerId, fragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    public static void launchFragmentWithSharedElements(final Fragment fromFragment, final Fragment toFragment) {

    }

    public static int getPrimaryColorDarkFromSelectedTheme(Context context) {
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        int primaryColorDark = ta.getColor(1, Color.WHITE);
        ta.recycle();
        return primaryColorDark;
    }

    public static int getPrimaryColorFromSelectedTheme(Context context) {
        // Parse MyCustomStyle, using Context.obtainStyledAttributes()
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        int primaryColor = ta.getColor(0, Color.YELLOW);
        ta.recycle();
        return primaryColor;
    }
}
