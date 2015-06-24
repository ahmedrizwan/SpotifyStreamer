package app.minimize.com.spotifystreamer.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.SeekBar;

import app.minimize.com.spotifystreamer.R;

/**
 * Created by ahmedrizwan on 6/21/15.
 */
public class MaterialSeekBar extends SeekBar {

    @ColorInt
    int mThumbColor, mProgressColor;

    int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}, // enabled
            new int[]{android.R.attr.state_pressed}  // pressed
    };

    int[] colorsThumb = new int[]{
            Color.BLACK,
            Color.BLACK
    };

    int[] colorsProgress = new int[]{
            Color.BLACK,
            Color.BLACK
    };


    ColorStateList mColorStateListThumb, mColorStateListProgress;

    public void setThumbColor(final int thumbColor) {
        mThumbColor = thumbColor;
        invalidate();
        requestLayout();
    }

    public void setProgressColor(final int progressColor) {
        mProgressColor = progressColor;
        invalidate();
        requestLayout();
    }

    public MaterialSeekBar(final Context context) {
        super(context);
    }

    public MaterialSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme()
                .obtainStyledAttributes(
                        attrs,
                        R.styleable.MaterialSeekBar,
                        0, 0);
        try {
            mThumbColor = a.getColor(R.styleable.MaterialSeekBar_thumbColor, getPrimaryColorFromSelectedTheme(context));
            mProgressColor = a.getColor(R.styleable.MaterialSeekBar_progressColor, getPrimaryColorFromSelectedTheme(context));
            colorsThumb[0] = mThumbColor;
            colorsThumb[1] = mThumbColor;
            mColorStateListThumb = new ColorStateList(states, colorsThumb);
            colorsProgress[0] = mProgressColor;
            colorsProgress[1] = mProgressColor;
            mColorStateListProgress = new ColorStateList(states, colorsProgress);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setThumbTintList(mColorStateListThumb);
                setProgressTintList(mColorStateListProgress);
            }
//            else {
            //load up the drawable and apply color
//            setThumb(ContextCompat.getDrawable(context, R.drawable.ic_circle));
//            getThumb().setColorFilter(mThumbColor, PorterDuff.Mode.MULTIPLY);
//            LayerDrawable ld = (LayerDrawable) getProgressDrawable();
//            ld.setColorFilter(mProgressColor, PorterDuff.Mode.SRC_IN);
//            }
        } finally {
            a.recycle();
        }
    }

    public MaterialSeekBar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static int getPrimaryColorFromSelectedTheme(Context context) {
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme()
                .obtainStyledAttributes(attrs);
        int primaryColor = ta.getColor(0, Color.BLACK); //1 index for primaryColorDark
        //default value for primaryColor is set to black if primaryColor not found
        ta.recycle();
        return primaryColor;
    }

    public static int getPrimaryColorDarkFromSelectedTheme(Context context) {
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme()
                .obtainStyledAttributes(attrs);
        int primaryColorDark = ta.getColor(1, Color.BLACK); //1 index for primaryColorDark
        //default value for primaryColor is set to black if primaryColor not found
        ta.recycle();
        return primaryColorDark;
    }

}
