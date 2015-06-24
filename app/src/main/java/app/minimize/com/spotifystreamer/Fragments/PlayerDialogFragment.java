package app.minimize.com.spotifystreamer.Fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;
import app.minimize.com.spotifystreamer.HelperClasses.SeekbarChangeListener;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.Views.MaterialSeekBar;
import app.minimize.com.spotifystreamer.Views.NextButton;
import app.minimize.com.spotifystreamer.Views.PlayButton;
import app.minimize.com.spotifystreamer.Views.PreviousButton;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by ahmedrizwan on 6/15/15.
 */
public class PlayerDialogFragment extends DialogFragment {


    @InjectView(R.id.textViewTrackName)
    TextView textViewTrackName;
    @InjectView(R.id.seekBarPlayer)
    MaterialSeekBar seekBarPlayer;
    @InjectView(R.id.chronometerStart)
    Chronometer chronometerStart;
    @InjectView(R.id.chronometerEnd)
    Chronometer chronometerEnd;
    @InjectView(R.id.imageViewPrevious)
    PreviousButton imageViewPrevious;
    @InjectView(R.id.imageViewPlay)
    PlayButton imageViewPlay;
    @InjectView(R.id.imageViewNext)
    NextButton imageViewNext;
    @InjectView(R.id.textViewTrackAlbum)
    TextView textViewTrackAlbum;
    @InjectView(R.id.imageViewAlbum)
    ImageView imageViewAlbum;

    private TracksFragment tracksFragment;
    private TrackParcelable mTrackParcelable;
    private int mAmountToUpdate = 0, currentProgress = 0;
    private Timer timer;
    private String imageViewAlbumTransitionName;
    private int vibrantColor = Color.BLACK;

    public static PlayerDialogFragment getInstance(TracksFragment tracksFragment) {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        playerDialogFragment.tracksFragment = tracksFragment;
        return playerDialogFragment;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, rootView);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_player);
        }

        ((ContainerActivity) getActivity()).hideNowPlayingLayout();

        mTrackParcelable = getArguments().getParcelable(getString(R.string.key_tracks_parcelable));
        List<TrackParcelable> trackParcelableList = getArguments().getParcelableArrayList(Keys.KEY_TRACK_PARCELABLE_LIST);
        vibrantColor = getArguments().getInt(Keys.COLOR_ACTION_BAR);
        Utility.setActionBarAndStatusBarColor(((AppCompatActivity) getActivity()), vibrantColor);
        Toast.makeText(getActivity(), trackParcelableList.indexOf(mTrackParcelable) + "", Toast.LENGTH_SHORT)
                .show();

        imageViewPlay.setOnClickListener(v -> {
            toggleTrack();
        });

        imageViewPlay.setButtonBackgroundColor(getActivity(), vibrantColor);
        seekBarPlayer.setProgressAndThumbColor(vibrantColor);

        if (Utility.isVersionLollipopAndAbove())
            imageViewAlbum.setTransitionName(imageViewAlbumTransitionName);

        if (mTrackParcelable != null) {
            textViewTrackName.setText(mTrackParcelable.songName);
            textViewTrackAlbum.setText(mTrackParcelable.albumName);
            int size = mTrackParcelable.albumImageUrls.size();
            if (size > 0) {
                Utility.loadImage(getActivity(),
                        mTrackParcelable.albumImageUrls.get(size - 1),
                        mTrackParcelable.albumImageUrls.get(0),
                        imageViewAlbum,
                        null);
            }
            playTrack();
        }
        return rootView;
    }

    private void toggleTrack() {
        Intent intent = new Intent(getActivity(),
                MediaPlayerService.class);
        //send trackParcelable
        intent.putExtra(Keys.KEY_TRACK_PARCELABLE, mTrackParcelable);
        getActivity().startService(intent);
    }

    private void playTrack() {
        Intent intent = new Intent(getActivity(),
                MediaPlayerService.class);
        //send trackParcelable
        intent.putExtra(Keys.KEY_TRACK_PARCELABLE, mTrackParcelable);
        new Thread(() -> {
            getActivity().startService(intent);
        }).start();

        imageViewPlay.setPauseMode(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault()
                .register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault()
                .unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEventMainThread(MediaPlayerHandler.StoppedEvent stoppedEvent) {
        imageViewPlay.setPauseMode(false);
        if (chronometerStart != null) {
            chronometerStart.setBase(SystemClock.elapsedRealtime());
            chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                    stoppedEvent.duration);
            chronometerStart.stop();
        }

        seekBarPlayer.setProgress(0);
    }

    @Subscribe
    public void onEventMainThread(MediaPlayerHandler.PausedEvent pausedEvent) {
        imageViewPlay.setPauseMode(false);
        if (chronometerStart != null) {
            chronometerStart.setBase(SystemClock.elapsedRealtime() - pausedEvent.progress);
            chronometerStart.stop();
        }
    }

    @Subscribe
    public void onEventMainThread(MediaPlayerHandler.PlayingEvent playingEvent) {
        try {
            imageViewPlay.setPauseMode(true);
            if (chronometerStart != null) {
                chronometerStart.setBase(SystemClock.elapsedRealtime() - playingEvent.progress);
                chronometerEnd.setBase(SystemClock.elapsedRealtime() -
                        playingEvent.duration);
                chronometerStart.start();
            }
            mAmountToUpdate = (playingEvent.duration) / Keys.SMOOTHNESS_FACTOR;
            seekBarPlayer.setOnSeekBarChangeListener(SeekbarChangeListener
                    .getInstance(playingEvent.duration, Keys.SMOOTHNESS_FACTOR, chronometerStart));
            seekBarPlayer.setMax(Keys.SMOOTHNESS_FACTOR);
            seekBarPlayer.setProgress(playingEvent.progress / mAmountToUpdate);

            if (timer != null) {
                timer.cancel();
                timer = null;
                logHelper("Timer is null now...");
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        (getActivity()).runOnUiThread(() -> {
                            try {
                                if (MediaPlayerHandler.getPlayerState() == MediaPlayerHandler.MediaPlayerState.Playing) {
                                    if ((mAmountToUpdate * seekBarPlayer.getProgress() < playingEvent.duration + playingEvent.progress)) {
                                        currentProgress = seekBarPlayer.getProgress();
                                        currentProgress += 1;
                                        seekBarPlayer.setProgress(currentProgress);
                                    }
                                } else {
                                    if (timer != null)
                                        timer.cancel();
                                }
                            } catch (NullPointerException e) {
                                if (timer != null)
                                    timer.cancel();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10, mAmountToUpdate);
        } catch (ArithmeticException e) {
            Log.e("MediaPlayerHandler", e.getMessage());
        } catch (NullPointerException e) {
            Log.e("MediaPlayerHandler Null", e.getMessage());
        }
    }

    private void logHelper(final String message) {
        Log.e("Exception", message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public void setImageViewAlbumTransitionName(String imageViewAlbumTransitionName) {
        this.imageViewAlbumTransitionName = imageViewAlbumTransitionName;
    }
}
