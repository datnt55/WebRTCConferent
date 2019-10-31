package jp.co.miosys.aitec.views.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.miosys.aitec.activities.UploadApplication;
import jp.co.miosys.aitec.models.Point2D;
import jp.co.miosys.aitec.utils.ApiProcesserUtils;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.ReadWriteFileUtils;

/**
 * Created by DatNT on 8/29/2017.
 */

public class DrawingView extends AppCompatImageView {

    private String colorCode = "#0000FF";
    private float width = 4f;
    private List<Holder> holderList = new ArrayList<Holder>();
    private Context mContext;
    private Paint geoPaint;
    public String[] exifData;
    boolean drawGrid = false;
    private Paint mGuidelinePaint;
    private float angle;

    private class Holder {
        Path path;
        Paint paint;

        Holder(String color, float width) {
            path = new Path();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(width);
            paint.setColor(Color.parseColor(color));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    public DrawingView(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        setDrawingCacheEnabled(true);
        width = 2;
        holderList.add(new Holder(colorCode, 2));

        geoPaint = new Paint();
        geoPaint.setAntiAlias(true);
        geoPaint.setColor(Color.YELLOW);
        geoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        geoPaint.setTextSize(40);

        mGuidelinePaint = newGuidelinePaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Holder holder : holderList) {
            canvas.drawPath(holder.path, holder.paint);
        }
        if (drawGrid){
            drawRuleOfThirdsGuidelines(canvas);
        }
    }
    public static Paint newGuidelinePaint() {

        final Paint paint = new Paint();
        int ar = Color.argb(100, 255, 255, 128);
        paint.setColor(ar);
        paint.setStrokeWidth(3);

        return paint;
    }
    private void drawRuleOfThirdsGuidelines(Canvas canvas) {
        final float left = 0;
        final float top = 0;
        final float right = this.getWidth()*3;
        final float bottom = this.getHeight()*3;
        float cos = (float) Math.cos(Math.toRadians(angle));
        // Draw vertical guidelines.
        final float oneThirdCropWidth = (this.getWidth()/cos) / 4;

        for (int i = -10 ; i < 10 ; i++) {
            final float x1 = oneThirdCropWidth*i;
            Point2D point = calculateNewPoint(x1,top,x1,bottom);
            canvas.drawLine(x1, top, point.x, point.y, mGuidelinePaint);
        }

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = (this.getHeight()/cos) / 4;
        for (int i = -10 ; i < 10 ; i++) {
            final float y1 = oneThirdCropHeight*i;
            Point2D point = calculateNewPoint(left,y1,right,y1);
            canvas.drawLine(left, y1, point.x, point.y, mGuidelinePaint);
        }
    }

    private Point2D calculateNewPoint(float x1, float y1, float x2, float y2){
        float cos = (float) Math.cos(Math.toRadians(angle));
        float sin = (float) Math.sin(Math.toRadians(angle));
        float x3 = x2*cos - y2*sin + x1*(1- cos) + y1*sin;
        float y3 = x2*sin + y2*cos - x1*sin + y1*(1-cos);
        return new Point2D(x3,y3);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                holderList.add(new Holder(colorCode, width));
                holderList.get(holderList.size() - 1).path.moveTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                holderList.get(holderList.size() - 1).path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void resetPaths() {
        for (Holder holder : holderList) {
            holder.path.reset();
        }
        holderList.clear();
        invalidate();
    }

    public void drawGrid(float degree, boolean gridLine){
        this.angle = degree;
        if (angle < -45 && angle > -90)
            angle = (90 + angle);
        else if (angle <= -90 && angle > -135)
            angle = (angle + 90);
        else if (angle <= -135 && angle >= -180)
            angle = (angle + 180);
        else if (angle <= 180 && angle >= 135)
            angle = -(180 - angle);
        else if (angle >= 90 && angle < 135)
            angle = -(angle - 90);
        else  if (angle < 90 && angle > 45)
            angle = -(90 - angle);
        //invalidate();
        drawGrid = gridLine;
        invalidate();
    }

    public void undo() {
        if (holderList.size() <= 0)
            return;
        holderList.get(holderList.size() - 1).path.reset();
        holderList.remove(holderList.get(holderList.size() - 1));
        invalidate();
    }

    public void sendImage(Context context, float angle, boolean gridLine) {
        drawGrid(angle,gridLine);
        File fileLog = ReadWriteFileUtils.createTempFileDontSend(context);
        saveDraw(fileLog);

        listener.onSendImage(fileLog);
    }

    public void saveDraw(File file) {
        try {
            getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            if (exifData == null) {
                ReadWriteFileUtils.saveExifGps(mContext, file);
            } else {
                ReadWriteFileUtils.reWriteExifGps(mContext, file, exifData);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setBrushColor(String colorCode) {
        this.colorCode = colorCode;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public interface OnSendImageListener {
        void onSendImage(File file);
    }

    private OnSendImageListener listener;

    public void setOnSendImageListener(OnSendImageListener listener) {
        this.listener = listener;
    }
}
