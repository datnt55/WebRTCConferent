package jp.co.miosys.aitec.views.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

import jp.co.miosys.aitec.models.Point2D;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;

/**
 * Created by DatNT on 11/12/2018.
 */

public class GridLineView extends View {
    private static final String SEMI_TRANSPARENT = "#FFFFFF80";
    private static final float DEFAULT_GUIDELINE_THICKNESS_PX = 2;
    private Paint mGuidelinePaint;
    private int width, height;
    private Context mContext;
    private float angle = 0 ;
    private Point2D oldVerticalStart, oldVerticalEnd;
    private Point2D oldHorizontalStart, oldHorizontalEnd;

    public GridLineView(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public GridLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    public GridLineView(Context context, int width, int height) {
        super(context);
        mContext = context;
        this.width = width;
        this.height = height;
        measure(width,height);
        init(context);
    }

    private void init(Context context) {
        mGuidelinePaint = newGuidelinePaint();
    }

    public void setGridSize(int width, int height){
        this.width = width;
        this.height = height;
        invalidate();
        //measure(width,height);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int hheight = MeasureSpec.getSize(heightMeasureSpec);
//        this.setMeasuredDimension(width, height);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }

    public static Paint newGuidelinePaint() {

        final Paint paint = new Paint();
        int ar = Color.argb(100, 255, 255, 128);
        paint.setColor(ar);
        paint.setStrokeWidth(DEFAULT_GUIDELINE_THICKNESS_PX);

        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawRuleOfThirdsGuidelines(canvas);
    }

    private void drawRuleOfThirdsGuidelines(Canvas canvas) {
        final float left = 0;
        final float top = 0;
        final float right = this.width*2;
        final float bottom = this.height*2;
        float cos = (float) Math.cos(Math.toRadians(angle));
        // Draw vertical guidelines.
        final float oneThirdCropWidth = (this.width/cos) / 4;

        for (int i = -10 ; i < 10 ; i++) {
            final float x1 = oneThirdCropWidth*i;
            Point2D point = calculateNewPoint(x1,top,x1,bottom);
            canvas.drawLine(x1, top, point.x, point.y, mGuidelinePaint);
            oldHorizontalStart = new Point2D(x1, top);
            oldHorizontalEnd = new Point2D(point.x, point.y);;
        }
//        final float x2 = 2 * oneThirdCropWidth;
//        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);
//        final float x3 = 3 * oneThirdCropWidth;
//        canvas.drawLine(x3, top, x3, bottom, mGuidelinePaint);


        // Draw horizontal guidelines.
        final float oneThirdCropHeight = (this.height/cos) / 4;
        for (int i = -10 ; i < 10 ; i++) {
            final float y1 = oneThirdCropHeight*i;
            Point2D point = calculateNewPoint(left,y1,right,y1);
            canvas.drawLine(left, y1, point.x, point.y, mGuidelinePaint);
            oldVerticalStart = new Point2D(left, y1);
            oldVerticalEnd = new Point2D(point.x, point.y);;
        }


//        final float y2 = 2 * oneThirdCropHeight;
//        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
//        final float y3 = 3 * oneThirdCropHeight;
//        canvas.drawLine(left, y3, right, y3, mGuidelinePaint);
    }
    private void drawRule4Guidelines(Canvas canvas) {
        final float left = 0;
        final float top = 0;
        final float right = this.width;
        final float bottom = this.height;

        // Draw vertical guidelines.
        final float oneThirdCropWidth = right / 4;

        final float x1 = 1 * oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, mGuidelinePaint);
        final float x2 = 2 * oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);
        final float x3 = 3 * oneThirdCropWidth;
        canvas.drawLine(x3, top, x3, bottom, mGuidelinePaint);


        // Draw horizontal guidelines.
        final float oneThirdCropHeight = bottom / 4;
        final float y1 = oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, mGuidelinePaint);
        final float y2 = 2 * oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
        final float y3 = 3 * oneThirdCropHeight;
        canvas.drawLine(left, y3, right, y3, mGuidelinePaint);
    }

    public void setAngle(float degree){
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

        invalidate();
    }

    private Point2D calculateNewPoint(float x1, float y1, float x2, float y2){
        float cos = (float) Math.cos(Math.toRadians(angle));
        float sin = (float) Math.sin(Math.toRadians(angle));
        float x3 = x2*cos - y2*sin + x1*(1- cos) + y1*sin;
        float y3 = x2*sin + y2*cos - x1*sin + y1*(1-cos);
        return new Point2D(x3,y3);
    }

    private Point2D calculateLineEquations(Point2D a, Point2D b){
        float a1 = (a.y-b.y)/(float)(a.x-b.x);
        float b1 = (b.y*a.x - a.y*b.x)/(float)(a.x-b.x);
        return new Point2D(a1, b1);
    }

    private float distanceBetween2Lines(Point2D line1, Point2D line2){
        float sqrt = (float) Math.sqrt(1+line1.x*line1.x);
        return (Math.abs(line1.y - line2.y))/sqrt;
    }

}
