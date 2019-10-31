package jp.co.miosys.aitec.kurento.managers;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioTrack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.activities.BaseActivity;
import jp.co.miosys.aitec.kurento.RemoteParticipant;
import jp.co.miosys.aitec.kurento.VideoConferenceActivity;
import jp.co.miosys.aitec.kurento.listeners.CustomWebSocketListener;
import jp.co.miosys.aitec.kurento.observers.CustomPeerConnectionObserver;
import jp.co.miosys.aitec.kurento.observers.CustomSdpObserver;
import jp.co.miosys.aitec.utils.ALog;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.NoticeDialog;
import jp.co.miosys.aitec.views.widgets.CaptureVideoRenderer;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;
import static jp.co.miosys.aitec.managers.PeerConnectionClient.AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT;
import static jp.co.miosys.aitec.managers.PeerConnectionClient.AUDIO_ECHO_CANCELLATION_CONSTRAINT;
import static jp.co.miosys.aitec.managers.PeerConnectionClient.AUDIO_HIGH_PASS_FILTER_CONSTRAINT;
import static jp.co.miosys.aitec.managers.PeerConnectionClient.AUDIO_NOISE_SUPPRESSION_CONSTRAINT;

import jp.co.miosys.aitec.kurento.VideoConferenceActivity.RemoteProxyRenderer;


public class PeersManager {

    private PeerConnection localPeer;
    private PeerConnectionFactory peerConnectionFactory;
    private CustomWebSocketListener webSocketAdapter;
    private WebSocket webSocket;
    private LinearLayout views_container;
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;
    private VideoRenderer localRenderer;
    private CaptureVideoRenderer localVideoView;
    private VideoCapturer videoGrabberAndroid;
    private BaseActivity activity;
    private boolean renderVideo = true, renderAudio = true;
    private final ExecutorService executor;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private final List<DataChannel> listPartnerPeer = new ArrayList<>();

    public PeersManager(BaseActivity activity, LinearLayout views_container, CaptureVideoRenderer localVideoView) {
        this.views_container = views_container;
        this.localVideoView = localVideoView;
        this.activity = activity;
        executor = Executors.newSingleThreadExecutor();
    }

    public PeerConnection getLocalPeer() {
        return localPeer;
    }

    public AudioTrack getLocalAudioTrack() {
        return localAudioTrack;
    }

    public VideoTrack getLocalVideoTrack() {
        return localVideoTrack;
    }

    public PeerConnectionFactory getPeerConnectionFactory() {
        return peerConnectionFactory;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public CustomWebSocketListener getWebSocketAdapter() {
        return webSocketAdapter;
    }

    public void setWebSocketAdapter(CustomWebSocketListener webSocketAdapter) {
        this.webSocketAdapter = webSocketAdapter;
    }

    public DataChannel getDataChanel() {
        return listPartnerPeer.get(0);
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void start() {
        WebRtcAudioTrack.setErrorCallback(new WebRtcAudioTrack.WebRtcAudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                webSocket.removeListener(webSocketAdapter);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(String errorMessage) {
                webSocket.removeListener(webSocketAdapter);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                webSocket.removeListener(webSocketAdapter);
                reportError(errorMessage);
            }
        });

        PeerConnectionFactory.initializeAndroidGlobals(activity, true);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        //options.networkIgnoreMask = 0;
        peerConnectionFactory = new PeerConnectionFactory(options);

        videoGrabberAndroid = createVideoGrabber();
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("levelControl", "true"));

        videoSource = peerConnectionFactory.createVideoSource(videoGrabberAndroid);
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        audioSource = peerConnectionFactory.createAudioSource(constraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        localAudioTrack.setEnabled(true);
        if (videoGrabberAndroid != null) {
            List<String> res = Arrays.asList(Globals.ConferenceVideoResolution.split("x"));
            videoGrabberAndroid.startCapture(Integer.parseInt(res.get(0)), Integer.parseInt(res.get(1)), Integer.parseInt(Globals.ConferenceVideoFPS));//(640, 360, 5);
        }

        localRenderer = new VideoRenderer(localVideoView);
        localVideoTrack.addRenderer(localRenderer);

        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("IceRestart", "true"));

        createLocalPeerConnection(sdpConstraints);

    }

    private void reportError(String errorMessage) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NoticeDialog dialog = new NoticeDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Globals.BUNDLE, activity.getString(R.string.connection_error_restart_app));
                dialog.setArguments(bundle);
                dialog.setCancelable(false);
                activity.getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
                dialog.setOnCallBack(new NoticeDialog.SelectionCallBackListener() {
                    @Override
                    public void onPositive() {
                        activity.hangup();
                    }
                });
            }
        });
    }

    private VideoCapturer createVideoGrabber() {
        VideoCapturer videoCapturer;
        videoCapturer = createCameraGrabber(new Camera1Enumerator(false));
        return videoCapturer;
    }

    private VideoCapturer createCameraGrabber(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    public void changeCaptureFormat(final int width, final int height, final int framerate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                changeCaptureFormatInternal(width, height, framerate);
            }
        });
    }

    private void changeCaptureFormatInternal(int width, int height, int framerate) {
        videoSource.adaptOutputFormat(width, height, framerate);
    }

    public void setVideoEnabled() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                renderVideo = !renderVideo;
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(renderVideo);
                }
            }
        });
    }

    public void setAudioEnabled() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                renderAudio = !renderAudio;
                if (localAudioTrack != null) {
                    localAudioTrack.setEnabled(renderAudio);
                }
            }
        });
    }

    public boolean isRenderVideo() {
        return renderVideo;
    }

    public void setRenderVideo(boolean renderVideo) {
        this.renderVideo = renderVideo;
    }

    public boolean isRenderAudio() {
        return renderAudio;
    }

    public void setRenderAudio(boolean renderAudio) {
        this.renderAudio = renderAudio;
    }

    public void switchCamera() {
        if (videoGrabberAndroid instanceof CameraVideoCapturer) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoGrabberAndroid;
            cameraVideoCapturer.switchCamera(null);
        } else {
            ALog.d(TAG, "Will not switch camera, video caputurer is not a camera");
        }
    }

    private void createLocalPeerConnection(final MediaConstraints sdpConstraints) {
        final List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer1 = new PeerConnection.IceServer(Globals.TurnServerURI, Globals.TurnServerUser, Globals.TurnServerPass);//"turn:157.7.209.73:1908","vmio", "vm69vm69");
        //PeerConnection.IceServer iceServer = new PeerConnection.IceServer("stun:stun.l.google.com:19302");
        // iceServers.add(iceServer);
        iceServers.add(iceServer1);
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302", "", ""));
        localPeer = peerConnectionFactory.createPeerConnection(iceServers, sdpConstraints, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Map<String, String> iceCandidateParams = new HashMap<>();
                iceCandidateParams.put("sdpMid", iceCandidate.sdpMid);
                iceCandidateParams.put("sdpMLineIndex", Integer.toString(iceCandidate.sdpMLineIndex));
                iceCandidateParams.put("candidate", iceCandidate.sdp);
                if (webSocketAdapter.getUserId() != null) {
                    iceCandidateParams.put("endpointName", webSocketAdapter.getUserId());
                    webSocketAdapter.sendJson(webSocket, "onIceCandidate", iceCandidateParams);
                } else {
                    webSocketAdapter.addIceCandidate(iceCandidateParams);
                }

            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
                if (newState == PeerConnection.IceConnectionState.FAILED) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            NoticeDialog dialog = new NoticeDialog();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Globals.BUNDLE_TITLE,  activity.getString(R.string.ice_error_title));
                            bundle.putSerializable(Globals.BUNDLE, activity.getString(R.string.connection_error_message));
                            dialog.setArguments(bundle);
                            dialog.setCancelable(false);
                            activity.getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
                            dialog.setOnCallBack(new NoticeDialog.SelectionCallBackListener() {
                                @Override
                                public void onPositive() {
                                    activity.hangup();
                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                activity.gotLocalStream(mediaStream);
            }
//            @Override
//            public void onDataChannel(final DataChannel dc) {
//                super.onDataChannel(dc);
//                dc.registerObserver(new DataChannel.Observer() {
//                    public void onBufferedAmountChange(long previousAmount) {
//                        ALog.d(TAG, "Data channel buffered amount changed: " + dc.label() + ": " + dc.state());
//                    }
//
//                    @Override
//                    public void onStateChange() {
//                        ALog.d(TAG, "Data channel state changed: " + dc.label() + ": " + dc.state());
//                    }
//
//                    @Override
//                    public void onMessage(final DataChannel.Buffer buffer) {
//                        ByteBuffer data = buffer.data;
//                        byte[] bytes = new byte[data.remaining()];
//                        data.get(bytes);
//                        final String command = new String(bytes);
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(activity,command,Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });
//            }
        });
//        DataChannel.Init init = new DataChannel.Init();
//        final DataChannel dataChannel = localPeer.createDataChannel("sendDataChannel", init);
//        dataChannel.registerObserver(new DataChannel.Observer() {
//            @Override
//            public void onBufferedAmountChange(long l) {
//                ALog.d(TAG, "Data channel buffered amount changed: " + dataChannel.label() + ": " + dataChannel.state());
//            }
//
//            @Override
//            public void onStateChange() {
//                String a = dataChannel.state().toString();
//                ALog.d(TAG, "Data channel state changed: " + dataChannel.label() + ": " + dataChannel.state());
//            }
//
//            @Override
//            public void onMessage(DataChannel.Buffer buffer) {
//                int i = 1;
//                // Incoming messages, ignore
//                // Only outcoming messages used in this example
//            }
//        });
//        listPartnerPeer.add(dataChannel);
    }

    public void createLocalOffer(MediaConstraints sdpConstraints) {

        localPeer.createOffer(new CustomSdpObserver("localCreateOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
                Map<String, String> localOfferParams = new HashMap<>();
                localOfferParams.put("hasAudio", "true");
                localOfferParams.put("hasVideo", "true");
                localOfferParams.put("audioActive", "true");
                localOfferParams.put("videoActive", "true");
                localOfferParams.put("doLoopback", "false");
                localOfferParams.put("frameRate", Globals.ConferenceVideoFPS); //5;
                //localOfferParams.put("videoDimensions", "{\"width\":640,\"height\":480}");//"1280x720");
                localOfferParams.put("resolution", Globals.ConferenceVideoResolution);//"1280x720");
                // localOfferParams.put("insertMode", "APPEND");
                localOfferParams.put("typeOfVideo", "CAMERA");
                localOfferParams.put("id", webSocketAdapter.getUserId());
                localOfferParams.put("sdpOffer", sessionDescription.description);
                if (webSocketAdapter.getId() > 1) {
                    webSocketAdapter.sendJson(webSocket, "publishVideo", localOfferParams);
                } else {
                    webSocketAdapter.setLocalOfferParams(localOfferParams);
                }
            }
        }, sdpConstraints);
    }

    public void createRemotePeerConnection(RemoteParticipant remoteParticipant, final String participantName) {
        final List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer1 = new PeerConnection.IceServer(Globals.TurnServerURI, Globals.TurnServerUser, Globals.TurnServerPass);//"turn:157.7.209.73:1908","vmio", "vm69vm69");
        //PeerConnection.IceServer iceServer = new PeerConnection.IceServer("stun:stun.l.google.com:19302");
        //iceServers.add(iceServer);
        iceServers.add(iceServer1);
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302", "", ""));
        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));

        PeerConnection remotePeer = peerConnectionFactory.createPeerConnection(iceServers, sdpConstraints, new CustomPeerConnectionObserver("remotePeerCreation", remoteParticipant) {

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Map<String, String> iceCandidateParams = new HashMap<>();
                iceCandidateParams.put("sdpMid", iceCandidate.sdpMid);
                iceCandidateParams.put("sdpMLineIndex", Integer.toString(iceCandidate.sdpMLineIndex));
                iceCandidateParams.put("candidate", iceCandidate.sdp);
                iceCandidateParams.put("endpointName", getRemoteParticipant().getId());
                webSocketAdapter.sendJson(webSocket, "onIceCandidate", iceCandidateParams);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                activity.gotRemoteStream(mediaStream, participantName);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                super.onIceConnectionChange(iceConnectionState);
                if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            NoticeDialog dialog = new NoticeDialog();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Globals.BUNDLE, "Cannot get ICE Candidate. Please try again");
                            dialog.setArguments(bundle);
                            dialog.setCancelable(false);
                            activity.getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
                            dialog.setOnCallBack(new NoticeDialog.SelectionCallBackListener() {
                                @Override
                                public void onPositive() {
                                    activity.hangup();
                                }
                            });
                        }
                    });

                }

            }

            //            @Override
//            public void onDataChannel(final DataChannel dc) {
//                super.onDataChannel(dc);
//                dc.registerObserver(new DataChannel.Observer() {
//                    public void onBufferedAmountChange(long previousAmount) {
//                        ALog.d(TAG, "Data channel buffered amount changed: " + dc.label() + ": " + dc.state());
//                    }
//
//                    @Override
//                    public void onStateChange() {
//                        ALog.d(TAG, "Data channel state changed: " + dc.label() + ": " + dc.state());
//                    }
//
//                    @Override
//                    public void onMessage(final DataChannel.Buffer buffer) {
//                        ByteBuffer data = buffer.data;
//                        byte[] bytes = new byte[data.remaining()];
//                        data.get(bytes);
//                        final String command = new String(bytes);
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(activity,command,Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });
//            }

        });
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("107");
        mediaStream.addTrack(localAudioTrack);
        mediaStream.addTrack(localVideoTrack);
        remotePeer.addStream(mediaStream);
        remoteParticipant.setPeerConnection(remotePeer);

//        DataChannel.Init init = new DataChannel.Init();
//        init.id = 1;
//        remotePeer.createDataChannel("ApprtcDemo data", init);
//        listPartnerPeer.add(dataChannel);


    }


    public void hangup(boolean lonely) {
        if (webSocketAdapter != null && localPeer != null) {
            webSocketAdapter.sendJson(webSocket, "leaveRoom", new HashMap<String, String>());
            webSocket.removeListener(webSocketAdapter);
            webSocket.disconnect();
            if (localPeer != null) {
                localPeer.dispose();
                localPeer = null;
            }

            Map<String, RemoteParticipant> participants = webSocketAdapter.getParticipants();
            for (RemoteParticipant remoteParticipant : participants.values()) {
                remoteParticipant.getPeerConnection().close();
                views_container.removeView(remoteParticipant.getView());

            }
            webSocketAdapter = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.removeRenderer(localRenderer);
            localVideoView.setTarget(null);
            localRenderer.dispose();
        }

        if (videoGrabberAndroid != null) {
            try {
                videoGrabberAndroid.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoGrabberAndroid.dispose();
            videoGrabberAndroid = null;
        }


        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        if (!lonely) {
            if (audioSource != null) {
                audioSource.dispose();
                audioSource = null;
            }

            if (localVideoTrack != null) {
                localVideoTrack.dispose();
                localVideoTrack = null;
            }

            if (localAudioTrack != null) {
                localAudioTrack.dispose();
                localAudioTrack = null;
            }

            if (peerConnectionFactory != null) {
                peerConnectionFactory.dispose();
                peerConnectionFactory = null;
            }
        }
    }
}
