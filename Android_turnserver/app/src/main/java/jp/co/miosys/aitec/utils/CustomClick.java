package jp.co.miosys.aitec.utils;

import android.view.View;

public class CustomClick implements View.OnClickListener {

    public CustomClick(OnClickListener mListener) {
        this.mListener = mListener;
    }

    public void setView(View view) {
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (isDoubleClick(view.getId()))
            return;

        mListener.onClick(view);
    }

    private OnClickListener mListener;

    public interface OnClickListener {
        void onClick(View v);
    }

    private static long lastClickTime = 0;
    private static int lastViewId = -333;
    private final static int SPACE_TIME = 600;

    public synchronized static boolean isDoubleClick(int viewID) {
        long currentTime = System.currentTimeMillis();
        boolean isClick2;
        if (viewID != lastViewId || currentTime - lastClickTime > SPACE_TIME) {
            isClick2 = false;
        } else {
            isClick2 = true;
        }
        lastClickTime = currentTime;
        lastViewId = viewID;
        return isClick2;
    }
}
