package app.minimize.com.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void launchFragmentWithSharedElements(final boolean isTwoPane, final Fragment fromFragment,
                                                        final Fragment toFragment,
                                                        final int container,
                                                        final View... views) {
        if (isVersionLollipopAndAbove()) {
            FragmentTransaction fragmentTransaction = fromFragment.getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            if (!isTwoPane) {
                final TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(new ChangeImageTransform());
                transitionSet.addTransition(new ChangeBounds());
                transitionSet.addTransition(new ChangeTransform());
                transitionSet.setDuration(300);
                fromFragment.setSharedElementReturnTransition(transitionSet);
                fromFragment.setSharedElementEnterTransition(transitionSet);
                toFragment.setSharedElementEnterTransition(transitionSet);
                toFragment.setSharedElementReturnTransition(transitionSet);

                for (View view : views) {
                    fragmentTransaction.addSharedElement(view, view.getTransitionName());
                }

                fragmentTransaction
                        .replace(container, toFragment)
                        .addToBackStack(null)
                        .commit();
            } else {

                fragmentTransaction
                        .replace(container, toFragment)
                        .addToBackStack(null)
                        .commit();
            }

        } else {
            Utility.launchFragment(((AppCompatActivity) fromFragment.getActivity()), container, toFragment);
        }

    }

    public static int getPrimaryColorDarkFromSelectedTheme(Context context) {
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme()
                .obtainStyledAttributes(attrs);
        int primaryColorDark = ta.getColor(1, Color.WHITE);
        ta.recycle();
        return primaryColorDark;
    }

    public static int getPrimaryColorFromSelectedTheme(Context context) {
        // Parse MyCustomStyle, using Context.obtainStyledAttributes()
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme()
                .obtainStyledAttributes(attrs);
        int primaryColor = ta.getColor(0, Color.YELLOW);
        ta.recycle();
        return primaryColor;
    }

    public static void loadImage(Context context, String smallImageUrl,
                                    String largeImageUrl, ImageView imageView, Callable<Void> onSuccess) {

        Picasso.with(context)
                .load(smallImageUrl) // thumbnail url goes here
                .placeholder(R.drawable.ic_not_available)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.with(context)
                                .load(largeImageUrl) // image url goes here
                                .placeholder(imageView.getDrawable())
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        try {
                                            onSuccess.call();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }

                    @Override
                    public void onError() {
                    }
                });
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setActionBarAndStatusBarColor(final AppCompatActivity activity, final int vibrantColor) {
        if (isVersionLollipopAndAbove()) {
            float[] hsv = new float[3];
            Color.colorToHSV(vibrantColor, hsv);
            hsv[2] *= 0.8f; // value component
            int darkColor = Color.HSVToColor(hsv);
            activity.getWindow()
                    .setStatusBarColor(darkColor);
        }
    }
}
