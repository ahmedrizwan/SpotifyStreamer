package app.minimize.com.spotifystreamer.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import app.minimize.com.spotifystreamer.R;


/**
 * Created by ahmedrizwan on 3/16/15.
 */
public class EmptyButton extends ImageView {

    public EmptyButton(Context context) {
        super(context);
    }

    public EmptyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        int buttonColor;
        try {
            String background = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "background");
            background = background.replaceAll("\\D+", "");
            Log.e("Background", background + "  " + R.attr.colorPrimary);
            if (Integer.parseInt(background) == (R.attr.colorPrimary)) {
                buttonColor = (getPrimaryColorFromSelectedTheme(context));
            } else
                buttonColor = getResources().getColor(Integer.parseInt(background));
        } catch (Exception e) {
            buttonColor = Color.BLACK;
        }
        setButtonBackgroundColor(context, buttonColor);

    }

    public void setButtonBackgroundColor(Context context, int buttonColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackground(getRippleCircularButton(context, buttonColor));

        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setBackground(getCircularButton(buttonColor,
                    darker(buttonColor, 2)));
        } else {
            setBackgroundDrawable(getCircularButton(buttonColor,
                    darker(buttonColor, 2)));
        }
    }

    //
    public static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static int getPrimaryColorDarkFromSelectedTheme(Context context) {
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        int primaryColorDark = ta.getColor(1, Color.BLACK);
        ta.recycle();
        return primaryColorDark;
    }

    public static int getPrimaryColorFromSelectedTheme(Context context) {
        // Parse MyCustomStyle, using Context.obtainStyledAttributes()
        int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        int primaryColor = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return primaryColor;
    }

    //
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Drawable getRippleCircularButton(Context context, int color) {
        RippleDrawable progressDrawable = (RippleDrawable) getResources().getDrawable(R.drawable
                .record_background_ripple);
        Drawable primaryColor = progressDrawable.getDrawable(0);
        primaryColor.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        return progressDrawable;
    }

    //
    public static StateListDrawable getCircularButton(int color, int colorPressed) {
        StateListDrawable drawable = new StateListDrawable();
        ShapeDrawable drawablePrimaryDark = new ShapeDrawable(new OvalShape());
        drawablePrimaryDark.getPaint().setColor(colorPressed);
        drawable.addState(new int[]{android.R.attr.state_pressed}, drawablePrimaryDark);
        ShapeDrawable drawablePrimary = new ShapeDrawable(new OvalShape());
        drawablePrimary.getPaint().setColor(color);
        drawable.addState(new int[]{}, drawablePrimary);
        return drawable;
    }
}
