package app.minimize.com.spotifystreamer.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import app.minimize.com.spotifystreamer.R;


/**
 * Created by ahmedrizwan on 3/4/15.
 */
public class NextButton extends EmptyButton {

    private Rect mRect;
    Paint mPaint;
    Paint mPaintLine;
    Path mPath;
    int point1x, point1y, point2x, point2y, point3x, point3y;
    int widthHeight;
    boolean pauseMode = false;

    public NextButton(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public NextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3.0f);
        mPaint.setAntiAlias(true);

        mPaintLine = new Paint();
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(getPrimaryColorFromSelectedTheme(getContext()));
        mPaintLine.setStrokeWidth(3.0f);

        mPath = new Path();
        widthHeight = (int) getResources().getDimension(R.dimen.iv_previous_next);
        point1x = widthHeight / 3;
        point1y = widthHeight / 3;
        point2x = point1x;
        point2y = widthHeight - point1y;
        point3x = widthHeight - point1y - widthHeight / 10;
        point3y = widthHeight / 2;

        mPath.setFillType(Path.FillType.EVEN_ODD);
        mPath.moveTo(point1x, point1y);
        mPath.lineTo(point2x, point2y);
        mPath.lineTo(point3x, point3y);
        mPath.lineTo(point1x, point1y);
        mPath.close();

        mRect = new Rect(point3x, point1y, point3x + widthHeight / 15, point2y);
//        if (Keys.SDK_VERSION == Build.VERSION_CODES.LOLLIPOP) {
//            setBackground(getRippleCircularButton(context));
//        } else {}
//            setBackground(getCircularButton(context));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw the play button
        canvas.drawPath(mPath, mPaint);
        //draw a rect in front of it
        canvas.drawRect(mRect, mPaint);
    }

    public void setPauseMode(boolean pauseMode) {
        this.pauseMode = pauseMode;
        invalidate();
    }

    public boolean getPauseMode() {
        return pauseMode;
    }
}
