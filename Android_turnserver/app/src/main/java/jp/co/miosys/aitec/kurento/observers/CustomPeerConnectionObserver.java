package jp.co.miosys.aitec.kurento.observers;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

import java.util.Arrays;

import jp.co.miosys.aitec.kurento.RemoteParticipant;
import jp.co.miosys.aitec.utils.ALog;
import jp.co.miosys.aitec.utils.NoticeDialog;

public class CustomPeerConnectionObserver implements PeerConnection.Observer {

    private String logTag = this.getClass().getCanonicalName();
    private RemoteParticipant remoteParticipant;

    public CustomPeerConnectionObserver(String logTag) {
        this.logTag = this.logTag+" "+logTag;
    }

    public CustomPeerConnectionObserver(String logTag, RemoteParticipant remoteParticipant) {
        this.logTag = this.logTag+" "+logTag;
        this.remoteParticipant = remoteParticipant;
    }

    public RemoteParticipant getRemoteParticipant() {
        return remoteParticipant;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        ALog.d(logTag, "onSignalingChange() called with: signalingState = [" + signalingState + "]");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        ALog.d(logTag, "onIceConnectionChange() called with: iceConnectionState = [" + iceConnectionState + "]");
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        ALog.d(logTag, "onIceConnectionReceivingChange() called with: b = [" + b + "]");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        ALog.d(logTag, "onIceGatheringChange() called with: iceGatheringState = [" + iceGatheringState + "]");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        ALog.d(logTag, "onIceCandidate() called with: iceCandidate = [" + iceCandidate + "]");
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        ALog.d(logTag, "onIceCandidatesRemoved() called with: iceCandidates = [" + Arrays.toString(iceCandidates) + "]");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        ALog.d(logTag, "onAddStream() called with: mediaStream = [" + mediaStream + "]");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        ALog.d(logTag, "onRemoveStream() called with: mediaStream = [" + mediaStream + "]");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        ALog.d(logTag, "onDataChannel() called with: dataChannel = [" + dataChannel + "]");
    }

    @Override
    public void onRenegotiationNeeded() {
        ALog.d(logTag, "onRenegotiationNeeded() called");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        ALog.d(logTag, "onAddTrack() called with: mediaStreams = [" + Arrays.toString(mediaStreams) + "]");
    }

}
