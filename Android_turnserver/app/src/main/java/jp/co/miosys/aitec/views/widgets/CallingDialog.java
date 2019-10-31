package jp.co.miosys.aitec.views.widgets;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.utils.AnimationUtils;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.RingToneAndSound;

import static android.content.Context.POWER_SERVICE;
import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.currentLocation;
import static jp.co.miosys.aitec.utils.Globals.isReceiver;
import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.KMLHelper.START_CALL;

/**
 * Created by DatNT
 */

public class  CallingDialog extends DialogFragment {
    private RingToneAndSound toneAndSound;
    public int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private boolean isHost, isConference;
    private CallingDialogCallback listener;
    public static int TIME_OUT = 40*1000;
    public void setOnCallBack(CallingDialogCallback listener) {
        this.listener = listener;
    }
    private Handler handler;
    private Runnable callback;
    private boolean show = false;
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //hideNavigation(uiOptions);
        View view = inflater.inflate(R.layout.pop_up_calling, container);
        show = true;
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


    public void turnOnScreen() {
        PowerManager.WakeLock screenLock = ((PowerManager) getActivity().getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myapp:mywakelocktag");
        screenLock.acquire();
        //later
        screenLock.release();
    }

    private void initComponents(View customView) {
        turnOnScreen();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getActivity().getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
        toneAndSound = new RingToneAndSound(getActivity());
        handler = new Handler();
        Bundle bundle = getArguments();
        isHost = bundle.getBoolean(Globals.BUNDLE_HOST);
        isConference = bundle.getBoolean(Globals.BUNDLE_CONFERENCE);
        if (!isConference) {
            if (!isHost)
                toneAndSound.vibratorAndSound(true);
        } else
            toneAndSound.vibratorAndSound(true);
        ImageView imgCircle = (ImageView) customView.findViewById(R.id.img_circle);
        ImageView imgCircle1 = (ImageView) customView.findViewById(R.id.img_circle2);
        RelativeLayout btnAnswer = (RelativeLayout) customView.findViewById(R.id.btn_answer);
        RelativeLayout btnCancel = (RelativeLayout) customView.findViewById(R.id.btn_cancel);
        TextView txtName = (TextView) customView.findViewById(R.id.txt_name);
        if (isConference) {
            txtName.setTypeface(txtName.getTypeface(), Typeface.NORMAL);
            txtName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_content));
            txtName.setText(Globals.name_guest + getString(R.string.join_a_conference_message));
            ImageView imgCall = (ImageView) customView.findViewById(R.id.img_call);
            imgCall.setImageResource(R.drawable.img_group);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgCall.getLayoutParams();
            params.width = CommonUtils.convertDpToPx(70, getActivity());
            imgCall.setLayoutParams(params);

            TextView txtAnswer = (TextView) customView.findViewById(R.id.txt_answer);
            txtAnswer.setText(getString(R.string.join_conference));
        } else {
            txtName.setText(Globals.name_guest);
            if (isHost)
                txtName.setText(getString(R.string.send_a_p2p_message,Globals.name_guest));
            else
                txtName.setText(getString(R.string.join_a_p2p_message,Globals.name_guest));
            TextView txtAnswer = (TextView) customView.findViewById(R.id.txt_answer);
            txtAnswer.setText(getString(R.string.answer));
        }

        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.isReceiver = true;
                dismiss();
                if (listener != null)
                    listener.onCallAccept(isConference);
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.bottomMargin = CommonUtils.convertDpToPx(50, getActivity());
        if (isHost) {
            btnCancel.setLayoutParams(params);
            btnAnswer.setVisibility(View.GONE);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null)
                    listener.onCallDeny(isConference);
            }
        });

        AnimationUtils.startAnimation(imgCircle, 0);
        AnimationUtils.startAnimation(imgCircle1, 1000);

        callback = new Runnable() {
            @Override
            public void run() {
                dismiss();
                if (listener != null)
                    listener.onCallDeny(isConference);
            }
        };
        if (!isHost)
            handler.postDelayed(callback,TIME_OUT);
    }

    public void dismiss(){
        if (handler != null)
            handler.removeCallbacks(callback);
        toneAndSound.vibratorAndSound(false);
        dismissAllowingStateLoss();
        show = false;
    }

    public boolean isShowing(){
        return show;
    }
    public interface CallingDialogCallback {
        void onCallAccept(boolean isConference);

        void onCallDeny(boolean isConference);
    }
}