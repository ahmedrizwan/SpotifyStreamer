package app.minimize.com.seek_bar_compat;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

/**
 * Created by ahmedrizwan on 7/8/15.
 */
public class SeekBarThumbDrawable extends BitmapDrawable {
    private static final String TAG = "SeekBarThumb";
    private int mHeight;
    private int mSeekBarHeight;
    private int mMargin;
    private SeekBarCompat mSeekBarCompat;
    private Paint mPaint = new Paint();
    private float xMultiple;
    private int shrinkScale = 5;
    private float mMax;
    private float mWidth;

    public SeekBarThumbDrawable(final int thumbColor, SeekBarCompat seekBarCompat) {
        mSeekBarCompat = seekBarCompat;
        mPaint.setColor(thumbColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void draw(final Canvas canvas) {
        try {
            canvas.drawCircle(mSeekBarCompat.getProgress() * xMultiple, mSeekBarHeight / 2, mHeight / shrinkScale, mPaint);
        } catch (Exception e) {
        }
    }

    @Override
    public void setAlpha(final int alpha) {

    }

    @Override
    public void setColorFilter(final ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void shrinkMode() {
        shrinkScale = 5;
        this.invalidateSelf();
    }

    public void expandMode() {
        shrinkScale = 3;
        this.invalidateSelf();
    }

    public void setMax(final int max) {
        mMax = max;
        Log.e(TAG, "setMax " + (mWidth - mHeight / 2) + " " + (mWidth - mMargin - mHeight / 2));
        xMultiple = (mWidth + mMargin - mHeight) / mMax;
    }

    public void setHeight(int thumbHeight, int seekBarHeight, int margin) {
        mHeight = thumbHeight;
        mSeekBarHeight = seekBarHeight;
        mMargin = margin;
        mWidth = mSeekBarCompat.getWidth();
        xMultiple = (mSeekBarCompat.getWidth() - thumbHeight) / mMax;
    }

    public void setColor(int thumbColor) {
        mPaint.setColor(thumbColor);
        invalidateSelf();
    }


}
