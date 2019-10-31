package jp.co.miosys.aitec.views.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.utils.CommonUtils;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Drawable divider;
    private Context mContext;

    /**
     * Default divider will be used
     */
    public MarginItemDecoration(Context context) {
        this.mContext = context;
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        divider = context.getResources().getDrawable(R.drawable.sk_line_divider);
        styledAttributes.recycle();
    }

    /**
     * Custom divider will be used
     */
    public MarginItemDecoration(Context context, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = CommonUtils.convertDpToPx(30, mContext);
        int right = parent.getWidth() - parent.getPaddingRight() - CommonUtils.convertDpToPx(15, mContext);

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}