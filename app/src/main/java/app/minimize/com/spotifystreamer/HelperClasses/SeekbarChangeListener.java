package app.minimize.com.spotifystreamer.HelperClasses;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.SeekBar;

/**
 * Created by ahmedrizwan on 6/19/15.
 */
public class SeekbarChangeListener implements SeekBar.OnSeekBarChangeListener {

    int duration = 0;
    int smoothnessFactor = 0;

    Chronometer mChronometerRunTime;

    private SeekbarChangeListener() {

    }

    public static SeekbarChangeListener getInstance(int duration, int smoothnessFactor, Chronometer chronometer) {
        SeekbarChangeListener seekbarChangeListener = new SeekbarChangeListener();
        seekbarChangeListener.setDurationAndSmoothnessFactor(duration, smoothnessFactor);
        seekbarChangeListener.setChronometer(chronometer);
        return seekbarChangeListener;
    }

    public void setDurationAndSmoothnessFactor(int duration, int smoothnessFactor) {
        this.duration = duration;
        this.smoothnessFactor = smoothnessFactor;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            try {
                MediaPlayerHandler.getPlayer()
                        .seekTo(progress * (duration / smoothnessFactor));
                seekBar.setProgress(progress);
                mChronometerRunTime.
                        setBase(SystemClock.elapsedRealtime() - MediaPlayerHandler.getPlayer()
                                .getCurrentPosition());
            } catch (Exception e) {
                Log.e("SeekBarChange", e.toString());
            }
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void setChronometer(final Chronometer chronometer) {
        mChronometerRunTime = chronometer;
    }
};