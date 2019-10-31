package jp.co.miosys.aitec.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import jp.co.miosys.aitec.R;

/**
 * Created by DatNT on 8/2/2017.
 */

public class NoticeDialog extends DialogFragment implements View.OnClickListener {

    private TextView txtEror, txtTitle;
    private Button btnOk;
    private Context mContext;
    public int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private SelectionCallBackListener listener;
    public interface SelectionCallBackListener {
        void onPositive();
    }

    //---empty constructor required
    public NoticeDialog() {

    }

    public void setOnCallBack(SelectionCallBackListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //hideNavigation(uiOptions);
        View view = inflater.inflate(R.layout.dialog_selection, container);
        initComponents(view);
        return view;
    }

    public void hideNavigation(final int uiOptions) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            final View decorView = getDialog().getWindow().getDecorView();
            decorView.setSystemUiVisibility(uiOptions);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                }
            });
        }
    }

    private void initComponents(View view) {
        txtEror = (TextView) view.findViewById(R.id.txt_error);
        btnOk = (Button) view.findViewById(R.id.btn_ok);
        txtTitle = (TextView) view.findViewById(R.id.txt_title);
        btnOk.setOnClickListener(this);
        txtEror.setText(getArguments().getString(Globals.BUNDLE));
        if (getArguments().containsKey(Globals.BUNDLE_TITLE))
            txtTitle.setText(getArguments().getString(Globals.BUNDLE_TITLE));
    }

    public void setMessage(String message){

    }
    @Override
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.95), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        return new Dialog(mContext, getTheme()) {
            @Override
            public void onBackPressed() {
                return;
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (listener!= null)
                    listener.onPositive();
                this.dismissAllowingStateLoss();
                break;
        }
    }

}