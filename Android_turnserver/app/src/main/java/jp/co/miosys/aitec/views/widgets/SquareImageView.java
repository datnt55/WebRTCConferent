package jp.co.miosys.aitec.views.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Duc on 11/3/2017.
 */

public class SquareImageView extends ImageView {

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap imageBitmap = getBitmapResource();
        Bitmap temporaryBitmap;
        if (imageBitmap != null) {
            temporaryBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        } else {
            return;
        }

        int w = getWidth();

        Bitmap roundBitmap = getRoundedCroppedBitmap(cropBitmap(temporaryBitmap), w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);
    }

    private Bitmap getBitmapResource() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return null;
        }

        return ((BitmapDrawable) drawable).getBitmap();
    }

    private Bitmap cropBitmap(Bitmap sourceBmp) {
        Bitmap outputBmp;

        if (sourceBmp.getWidth() > sourceBmp.getHeight()) {
            outputBmp = Bitmap.createBitmap(sourceBmp, 0, 0, sourceBmp.getHeight(), sourceBmp.getHeight());
        } else if (sourceBmp.getWidth() < sourceBmp.getHeight()) {
            outputBmp = Bitmap.createBitmap(sourceBmp, 0, 0, sourceBmp.getWidth(), sourceBmp.getWidth());
        } else {
            outputBmp = sourceBmp;
        }

        return outputBmp;
    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap finalBitmap;
        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius) {
            // Set the filter to false, because we don't need very smooth one! It's cheaper!
            finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius, false);
        } else {
            finalBitmap = bitmap;
        }

        Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(), finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, finalBitmap.getWidth(), finalBitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawARGB(0, 0, 0, 0);

        // It doesn't matter which color!
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(0, 0, finalBitmap.getWidth(), finalBitmap.getHeight()), 30, 30, paint);

        // The second drawing should only be visible of if overlapping with the first
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(finalBitmap, rect, rect, paint);

        // Draw the border, if exist
//        canvas.drawRoundRect(new RectF(0, 0, finalBitmap.getWidth(), finalBitmap.getHeight()), 30, 30, getBorderPaint());

        return output;
    }

    private Paint getBorderPaint() {
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(5);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        return borderPaint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
