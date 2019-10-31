package jp.co.miosys.aitec.kurento.observers;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import jp.co.miosys.aitec.kurento.RemoteParticipant;
import jp.co.miosys.aitec.utils.ALog;


public class CustomSdpObserver implements SdpObserver {


    private String tag = this.getClass().getCanonicalName();
    private RemoteParticipant remoteParticipant;

    public CustomSdpObserver(String logTag) {
        this.tag = this.tag + " " + logTag;
    }

    public CustomSdpObserver(String logTag, RemoteParticipant remoteParticipant) {
        this.tag = this.tag + " " + logTag;
        this.remoteParticipant = remoteParticipant;
    }

    public RemoteParticipant getRemoteParticipant() {
        return remoteParticipant;
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        ALog.d(tag, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");
    }

    @Override
    public void onSetSuccess() {
        ALog.d(tag, "onSetSuccess() called");
    }

    @Override
    public void onCreateFailure(String s) {
        ALog.d(tag, "onCreateFailure() called with: s = [" + s + "]");
    }

    @Override
    public void onSetFailure(String s) {
        ALog.d(tag, "onSetFailure() called with: s = [" + s + "]");
    }

}