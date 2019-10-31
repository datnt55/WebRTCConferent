package jp.co.miosys.aitec.kurento;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.views.widgets.CaptureVideoRenderer;

public class RemoteParticipant {

    private String id;
    private AudioTrack mediaStream;
    private PeerConnection peerConnection;
    private CaptureVideoRenderer videoView;
    private SurfaceViewRenderer view;
    private View root;
    private ImageView imgVolume;
    private boolean isMute;
    private TextView participantNameText;
    private TextView txtWaiting;
    private String name;

    public RemoteParticipant() {
    }

    public RemoteParticipant(RemoteParticipant participant) {
        if (participant.getId() != null)
            this.id = participant.getId();
        this.mediaStream = participant.getMediaStream();
        this.peerConnection = participant.getPeerConnection();
        this.videoView = participant.getVideoView();
        this.view = participant.getView();
        this.participantNameText = participant.getParticipantNameText();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AudioTrack getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(AudioTrack mediaStream) {
        this.mediaStream = mediaStream;
    }

    public PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public void setPeerConnection(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }


    public CaptureVideoRenderer getVideoView() {
        return videoView;
    }

    public void setVideoView(CaptureVideoRenderer videoView) {
        this.videoView = videoView;
    }

    public SurfaceViewRenderer getView() {
        return view;
    }

    public void setView(SurfaceViewRenderer view) {
        this.view = view;
    }

    public TextView getParticipantNameText() {
        return participantNameText;
    }

    public void setParticipantNameText(TextView participantNameText) {
        this.participantNameText = participantNameText;
    }

    public TextView getTxtWaiting() {
        return txtWaiting;
    }

    public void setTxtWaiting(TextView txtWaiting) {
        this.txtWaiting = txtWaiting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public View getRoot() {
        return root;
    }

    public boolean isMute() {
        return isMute;
    }

    public ImageView getImgVolume() {
        return imgVolume;
    }

    public void setImgVolume(ImageView imgVolume) {
        this.imgVolume = imgVolume;
    }

    public void setMute(boolean mute) {
        isMute = mute;
        if (imgVolume != null) {
            if (isMute)
                imgVolume.setImageResource(R.drawable.ic_mute);
            else
                imgVolume.setImageResource(R.drawable.ic_volume);
        }
    }

    public void setRoot(View root) {
        this.root = root;
    }
}
