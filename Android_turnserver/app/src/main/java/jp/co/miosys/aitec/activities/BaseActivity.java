package jp.co.miosys.aitec.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.webrtc.MediaStream;

import jp.co.miosys.aitec.kurento.RemoteParticipant;
import jp.co.miosys.aitec.views.widgets.CaptureVideoRenderer;

/**
 * Created by Duc on 9/12/2017.
 */

/*[20170910] Ductx: #2584: Create splash activity*/

public class BaseActivity extends AppCompatActivity {

    public int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //hideNavigation(flags);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow();
//            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            // w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //hideNavigation(flags);
    }

    public void hideNavigation(final int uiOption) {
        //  This work only for android 4.4+
        final View decorView = getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(uiOption);
                    }
                }
            });
        }
    }

    public void hangup() {

    }

    public CaptureVideoRenderer addViewPeer(String remoteParticipant) {
        return null;
    }

    public boolean isFirstPartner() {
        return true;
    }

    public void showWaiting(String remoteParticipant) {

    }

    public void setNewLargeVideo(RemoteParticipant remoteParticipant) {

    }

    public void setRemoteParticipantName(String name, RemoteParticipant participant) {

    }

    public LinearLayout getViewsContainer() {
        return null;
    }

    public void gotRemoteStream(MediaStream mediaStream, String participantName) {

    }

    public void gotLocalStream(MediaStream mediaStream) {

    }
}
