package app.minimize.com.spotifystreamer.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
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
