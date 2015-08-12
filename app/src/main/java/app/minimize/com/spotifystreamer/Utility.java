package app.minimize.com.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.CallbackAction;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;

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
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void launchFragmentWithSharedElements(final boolean isTwoPane,
                                                        final Fragment fromFragment,
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
                transitionSet.setDuration(500);
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
                        .commit();
            }

        } else {
            if (isTwoPane)
                fromFragment.getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, toFragment)
                        .commit();
            else
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

    public static void triggerMethodOnceViewIsDisplayed(final View view, final Callable<Void> method) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    view.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                } else view.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
                try {
                    method.call();
                } catch (Exception e) {
                    Log.e("TriggerMethod", e.toString());
                }
            }
        });
    }

    public static void loadImage(Context context, String smallImageUrl,
                                 String largeImageUrl, ImageView imageView,
                                 CallbackAction<Integer> onSuccess) {

        Picasso.with(context)
                .load(smallImageUrl) // thumbnail url goes here
                .placeholder(R.drawable.ic_not_available)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        try {
                            int vibrantColor = Palette.from(((BitmapDrawable) imageView.getDrawable())
                                    .getBitmap())
                                    .generate()
                                    .getVibrantColor(Color.BLACK);
                            onSuccess.call(vibrantColor);
                        } catch (Exception e) {
                            Log.e("LoadImage", "onSuccess "+e.toString());
                        }
                        Picasso.with(context)
                                .load(largeImageUrl) // image url goes here
                                .placeholder(imageView.getDrawable())
                                .into(imageView);
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
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setBackgroundDrawable(new ColorDrawable(vibrantColor));
    }

    public static void hideKeyboard(Context context, View view) {
        //check if view has focus
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void saveTrackAndTrackListInService(Context context,
                                                      TrackParcelable trackParcelable,
                                                      ArrayList<TrackParcelable> topTracks) {
        Intent intent = new Intent(context,
                MediaPlayerService.class);
        intent.putExtra(Keys.KEY_SAVE_TRACKS, true);
        intent.putExtra(Keys.KEY_TRACK_PARCELABLE, trackParcelable);
        intent.putExtra(Keys.KEY_TOP_TRACK_PARCELABLES, topTracks);
        context.startService(intent);
    }

    public static void startService(final Context activity) {
        activity.startService(new Intent(activity, MediaPlayerService.class));
    }
}
