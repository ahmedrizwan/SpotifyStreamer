package app.minimize.com.spotifystreamer.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

import app.minimize.com.spotifystreamer.R;


/**
 * Created by ahmedrizwan on 2/28/15.
 */
public class PlayButton extends EmptyButton {
    Paint mPaint;
    Paint mPaintLine;
    Path mPath;
    float point1x, point1y, point2x, point2y, point3x, point3y;
    int widthHeight;
    boolean pauseMode = false;

    public PlayButton(Context context) {
        super(context);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(getResources().getColor(R.color.color_primary));
        Log.e("Primary Color", getPrimaryColorFromSelectedTheme(context)+"");
        mPaint.setColor(getPrimaryColorFromSelectedTheme(context));
        mPaint.setStrokeWidth(3.0f);
        mPaint.setAntiAlias(true);

        mPaintLine = new Paint();
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(getResources().getColor(R.color.color_primary));
//        mPaintLine.setColor(Color.BLACK);
        mPaintLine.setStrokeWidth(3.0f);

        mPath = new Path();
        widthHeight = (int) getResources().getDimension(R.dimen.iv_play);
        point1x = widthHeight / 3;
        point1y = widthHeight / 4;
        point2x = point1x;
        point2y = widthHeight - point1y;
        point3x = widthHeight - point1y;
        point3y = widthHeight / 2;

        mPath.setFillType(Path.FillType.EVEN_ODD);
        mPath.moveTo(point1x, point1y);
        mPath.lineTo(point2x, point2y);
        mPath.lineTo(point3x, point3y);
        mPath.lineTo(point1x, point1y);
        mPath.close();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!pauseMode) {
            canvas.drawPath(mPath, mPaint);
        } else {
            canvas.drawRect(widthHeight / 3, widthHeight / 3,
                    widthHeight / 3 + widthHeight / 8, widthHeight - widthHeight / 3, mPaint);

            canvas.drawRect(2 * widthHeight / 3 - widthHeight / 8, widthHeight / 3,
                    2 * widthHeight / 3, widthHeight - widthHeight / 3,
                    mPaint);
        }

    }

    public void setPauseMode(boolean pauseMode) {
        this.pauseMode = pauseMode;
        invalidate();
    }

    public boolean getPauseMode() {
        return pauseMode;
    }
}
