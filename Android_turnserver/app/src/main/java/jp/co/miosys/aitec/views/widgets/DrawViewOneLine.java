package jp.co.miosys.aitec.views.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.utils.CommonUtils;

public class DrawViewOneLine extends View {
    Paint paint = new Paint();
    public int default_width;
    public int default_margin;
    private Context mContext;

    public DrawViewOneLine(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DrawViewOneLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DrawViewOneLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        default_width = CommonUtils.convertDpToPx(2, mContext);
        paint.setColor(ContextCompat.getColor(mContext, R.color.gray_600));
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setStrokeWidth(default_width);
        canvas.drawLine(0, default_width, getWidth(), default_width, paint);
    }
}
