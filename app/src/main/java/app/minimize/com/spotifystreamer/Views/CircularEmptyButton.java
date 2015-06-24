package app.minimize.com.spotifystreamer.Views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;

import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;

/**
 * Created by ahmedrizwan on 6/22/15.
 */
public class CircularEmptyButton extends EmptyButton {
    int buttonColor = Color.BLACK;
    public CircularEmptyButton(final Context context) {
        super(context);
    }

    public CircularEmptyButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        try {
            String background = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "background");
            background = background.replaceAll("\\D+", "");
            if (Integer.parseInt(background) == (R.attr.colorPrimary)) {
                buttonColor = (Utility.getPrimaryColorFromSelectedTheme(context));
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

}
