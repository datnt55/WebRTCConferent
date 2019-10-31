package jp.co.miosys.aitec.kurento.listeners;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jp.co.miosys.aitec.activities.BaseActivity;
import jp.co.miosys.aitec.kurento.RemoteParticipant;
import jp.co.miosys.aitec.kurento.constants.JSONConstants;
import jp.co.miosys.aitec.kurento.managers.PeersManager;
import jp.co.miosys.aitec.kurento.observers.CustomSdpObserver;
import jp.co.miosys.aitec.utils.ALog;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.NoticeDialog;

public final class CustomWebSocketListener implements WebSocketListener {

    private static final String TAG = "CustomWebSocketAdapter";
    private static final String JSON_RPCVERSION = "2.0";
    private static final int PING_MESSAGE_INTERVAL = 40;

    private BaseActivity activity;
    private PeerConnection localPeer;
    private int id;
    private List<Map<String, String>> iceCandidatesParams;
    private Map<String, String> localOfferParams;
    private String userId;
    private String sessionName;
    private String participantName;
    private LinearLayout views_container;
    private Map<String, RemoteParticipant> participants;
    private String remoteParticipantId;
    private PeersManager peersManager;
    private String socketAddress;
    private WebSocket pingSocket;
    private String token;
    private WebSocketState currentState;

    public CustomWebSocketListener(BaseActivity videoConferenceActivity, PeersManager peersManager, String sessionName, String participantName, LinearLayout views_container, String socketAddress, String token) {
        this.activity = videoConferenceActivity;
        this.peersManager = peersManager;
        this.localPeer = peersManager.getLocalPeer();
        this.id = 0;
        this.sessionName = sessionName;
        this.participantName = participantName;
        this.views_container = views_container;
        this.socketAddress = socketAddress;
        this.iceCandidatesParams = new ArrayList<>();
        this.participants = new HashMap<>();
        this.token = token;
    }

    public Map<String, RemoteParticipant> getParticipants() {
        return participants;
    }
    public String getUserId() {
        return userId;
    }
    public int getId() {
        return id;
    }
    private void updateId() {
        id++;
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        ALog.i(TAG, "State changed: " + newState.name());
        currentState = newState;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ALog.i(TAG, "Connected");
        pingSocket = websocket;
        pingMessageHandler();
        String regex = "(openvidu)+";
        String baseAddress = socketAddress.split(regex)[0];
        Map<String, String> joinRoomParams = new HashMap<>();
        joinRoomParams.put("dataChannels", "false");
        joinRoomParams.put(JSONConstants.METADATA, "{\"clientData\": \"" + participantName + "\"}");
        joinRoomParams.put("secret", "vm69vm69");
        //joinRoomParams.put("platform", "mobile");
        joinRoomParams.put("session", sessionName);
        joinRoomParams.put("recording", "true");
        joinRoomParams.put("token", token);
        joinRoomParams.put("platform", CommonUtils.getDeviceName());
        sendJson(websocket, "joinRoom", joinRoomParams);

        if (localOfferParams != null) {
            sendJson(websocket, "publishVideo", localOfferParams);
        }
    }

    private void pingMessageHandler() {
        long initialDelay = 0L;
        ScheduledThreadPoolExecutor executor =
                new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Map<String, String> pingParams = new HashMap<>();
                if (id == 0) {
                    pingParams.put("interval", "240000");
                }
                sendJson(pingSocket, "ping", pingParams);
            }
        }, initialDelay, PING_MESSAGE_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
        ALog.i(TAG, "Connect error: " + cause);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        ALog.i(TAG, "Disconnected " + serverCloseFrame.getCloseReason() + " " + clientCloseFrame.getCloseReason() + " " + closedByServer);
        pingSocket.disconnect();
        pingSocket = null;
    }

    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Frame");
    }

    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Continuation Frame");
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Text Frame");
    }

    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Binary Frame");
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Close Frame");
    }

    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Ping Frame");
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Pong Frame");
    }

    @Override
    public void onTextMessage(final WebSocket websocket, String text) throws Exception {
        ALog.i(TAG, "Text Message " + text);
        JSONObject json = new JSONObject(text);
        if (json.has(JSONConstants.RESULT)) {
            handleResult(websocket, json);
        } else {
            handleMethod(websocket, json);
        }
    }

    private void handleResult(final WebSocket webSocket, JSONObject json) throws JSONException {
        final JSONObject result = new JSONObject(json.getString(JSONConstants.RESULT));
        if (result.has(JSONConstants.SDP_ANSWER)) {
            String id = json.getString(JSONConstants.ID);
            saveAnswer(id,result);
        } else if (result.has(JSONConstants.SESSION_ID)) {
            if (result.has(JSONConstants.VALUE)) {
                if (result.getJSONArray(JSONConstants.VALUE).length() > 0) {
                    addParticipantsAlreadyInRoom(result, webSocket);
                }
                final Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            userId = result.getString(JSONConstants.ID);
                            for (Map<String, String> iceCandidate : iceCandidatesParams) {
                                iceCandidate.put("endpointName", userId);
                                sendJson(webSocket, "onIceCandidate", iceCandidate);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mainHandler.post(myRunnable);
                //mainHandler.postDelayed(myRunnable,1500*result.getJSONArray(JSONConstants.VALUE).length());
            }
        } else if (result.has(JSONConstants.VALUE)) {
            ALog.i(TAG, "pong");
        } else {
            ALog.e(TAG, "Unrecognized " + result);
        }
    }

    private void addParticipantsAlreadyInRoom(final JSONObject result, final WebSocket webSocket) throws JSONException {
        final Handler mainHandler = new Handler(activity.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < result.getJSONArray(JSONConstants.VALUE).length(); i++) {
                        final int finalI = i;
                        remoteParticipantId = result.getJSONArray(JSONConstants.VALUE).getJSONObject(finalI).getString(JSONConstants.ID);
                        final RemoteParticipant remoteParticipant = new RemoteParticipant();
                        remoteParticipant.setId(remoteParticipantId);
                        participants.put(remoteParticipantId, remoteParticipant);
                        String participantName = new JSONObject(result.getJSONArray(JSONConstants.VALUE).getJSONObject(finalI).getString(JSONConstants.METADATA)).getString("clientData");
                        createVideoView(participantName);
                        remoteParticipant.setName(participantName);
                        peersManager.createRemotePeerConnection(remoteParticipant, participantName);
                        remoteParticipant.getPeerConnection().createOffer(new CustomSdpObserver("remoteCreateOffer") {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                super.onCreateSuccess(sessionDescription);
                                remoteParticipant.getPeerConnection().setLocalDescription(new CustomSdpObserver("remoteSetLocalDesc"), sessionDescription);
                                Map<String, String> remoteOfferParams = new HashMap<>();
                                remoteOfferParams.put("sdpOffer", sessionDescription.description);
                                remoteOfferParams.put("sender", remoteParticipantId + "_webcam");
                                sendJson(webSocket, "receiveVideoFrom", remoteOfferParams);
                            }
                        }, new MediaConstraints());
                        if (result.getJSONArray(JSONConstants.VALUE).length() > 1) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    private void reportError(String errorMessage) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NoticeDialog dialog = new NoticeDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Globals.BUNDLE, "Connection is error. Please try again");
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

    private void handleMethod(final WebSocket webSocket, JSONObject json) throws JSONException {
        if(!json.has(JSONConstants.PARAMS)) {
            ALog.e(TAG, "No params : "+ json);
            reportError("No params : ");
        } else {
            ALog.e(TAG, json.toString());
            final JSONObject params = new JSONObject(json.getString(JSONConstants.PARAMS));
            String method = json.getString(JSONConstants.METHOD);
            switch (method) {
                case JSONConstants.ICE_CANDIDATE:
                    iceCandidateMethod(params);
                    break;
                case JSONConstants.PARTICIPANT_JOINED:
                    participantJoinedMethod(params);
                    break;
                case JSONConstants.PARTICIPANT_PUBLISHED:
                    participantPublishedMethod(params, webSocket);
                    break;
                case JSONConstants.PARTICIPANT_LEFT:
                    participantLeftMethod(params);
                    break;
                default:
                    throw new JSONException("Can't understand method: " + method);
            }
        }
    }
    // New ver ."senderConnectionId"; old ver. "endpointName"
    private void iceCandidateMethod(final JSONObject params) throws JSONException {
        Handler mainHandler = new Handler(activity.getMainLooper());
        String key = "endpointName";
        if (params.has("senderConnectionId"))
            key = "senderConnectionId";
        final String finalKey = key;
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (params.getString(finalKey).equals(userId)) {
                        saveIceCandidate(params, null);
                    } else {
                        saveIceCandidate(params, params.getString(finalKey));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        mainHandler.post(myRunnable);
        //mainHandler.postDelayed(myRunnable, 3000);
    }

    private void participantJoinedMethod(JSONObject params) throws JSONException {
        final RemoteParticipant remoteParticipant = new RemoteParticipant();
        remoteParticipant.setId(params.getString(JSONConstants.ID));
        participants.put(params.getString(JSONConstants.ID), remoteParticipant);
        String participantName = new JSONObject(params.getString(JSONConstants.METADATA)).getString("clientData");
        remoteParticipant.setName(participantName);
        createVideoView(participantName);
        peersManager.createRemotePeerConnection(remoteParticipant,participantName );
    }

    private void createVideoView(final String remoteParticipant) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (!activity.isFirstPartner()) {
                    activity.showWaiting(remoteParticipant);
                    return;
                }
                activity.addViewPeer(remoteParticipant);
            }
        };
        mainHandler.post(myRunnable);
    }

    private void participantPublishedMethod(JSONObject params, final WebSocket webSocket) throws JSONException {
        remoteParticipantId = params.getString(JSONConstants.ID);
        RemoteParticipant remoteParticipantPublished = participants.get(remoteParticipantId);
        remoteParticipantPublished.getPeerConnection().createOffer(new CustomSdpObserver("remoteCreateOffer", remoteParticipantPublished) {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                getRemoteParticipant().getPeerConnection().setLocalDescription(new CustomSdpObserver("remoteSetLocalDesc"), sessionDescription);
                Map<String, String> remoteOfferParams = new HashMap<>();
                remoteOfferParams.put("sdpOffer", sessionDescription.description);
                remoteOfferParams.put("sender", getRemoteParticipant().getId() + "_webcam");
                sendJson(webSocket, "receiveVideoFrom", remoteOfferParams);

            }
        }, new MediaConstraints());
    }

    private void participantLeftMethod(JSONObject params) throws JSONException {
        if (currentState == WebSocketState.CLOSED || currentState == WebSocketState.CLOSING)
            return;
        final String participantId;
        if (params.has("name"))
            participantId = params.getString("name");
        else
            participantId = params.getString("connectionId");
        participants.get(participantId).getPeerConnection().close();
        Handler mainHandler = new Handler(activity.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                activity.setNewLargeVideo(participants.get(participantId));
            }
        };
        mainHandler.post(myRunnable);
        RemoteParticipant remoteParticipantToDelete = participants.get(participantId);
        participants.remove(remoteParticipantToDelete);
    }

    private void setRemoteParticipantName(final String name, final RemoteParticipant participant) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() { activity.setRemoteParticipantName(name, participant); }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        ALog.i(TAG, "Binary Message");
    }

    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Sending Frame");
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Frame sent");
    }

    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Frame unsent");
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        ALog.i(TAG, "Thread created");
    }

    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        ALog.i(TAG, "Thread started");
    }

    @Override
    public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        ALog.i(TAG, "Thread stopping");
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        ALog.i(TAG, "Error! " + cause);
    }

    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Frame error");
    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        ALog.i(TAG, "Message error! "+ cause);
    }

    @Override
    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
        ALog.i(TAG, "Message Decompression Error");
    }

    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
        ALog.i(TAG, "Text Message Error! " + cause);
    }

    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        ALog.i(TAG, "Send Error! " + cause);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
        ALog.i(TAG, "Unexpected error! " + cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        ALog.i(TAG, "Handle callback error! " + cause);
    }

    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
        ALog.i(TAG, "Sending Handshake! Hello!");
    }

    private void saveIceCandidate(JSONObject json, String endPointName) throws JSONException {
        IceCandidate iceCandidate = new IceCandidate(json.getString("sdpMid"), Integer.parseInt(json.getString("sdpMLineIndex")), json.getString("candidate"));
        if (endPointName == null) {
            localPeer.addIceCandidate(iceCandidate);
        } else {
            participants.get(endPointName).getPeerConnection().addIceCandidate(iceCandidate);
        }
    }

    private void saveAnswer(String id, JSONObject json) throws JSONException {
        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, json.getString("sdpAnswer"));
        if (localPeer.getRemoteDescription() == null) {
            localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemoteDesc"), sessionDescription);
        } else {
            for (Map.Entry<String, RemoteParticipant> entry : participants.entrySet()) {
                RemoteParticipant participant = entry.getValue();
                if (participant.getPeerConnection().getRemoteDescription() == null) {
                    participants.get(entry.getKey()).getPeerConnection().setRemoteDescription(new CustomSdpObserver("remoteSetRemoteDesc"), sessionDescription);
                    return;
                }
            }
//            participants.get(remoteParticipantId).getPeerConnection().setRemoteDescription(new CustomSdpObserver("remoteSetRemoteDesc"), sessionDescription);

        }
    }

    public void sendJson(WebSocket webSocket, String method, Map<String, String> params) {
        try {
            JSONObject paramsJson = new JSONObject();
            for (Map.Entry<String, String> param : params.entrySet()) {
                paramsJson.put(param.getKey(), param.getValue());
            }
            JSONObject jsonObject = new JSONObject();
            if (method.equals(JSONConstants.JOIN_ROOM)) {
                jsonObject.put(JSONConstants.ID, 1)
                        .put(JSONConstants.PARAMS, paramsJson);
            } else if (paramsJson.length() > 0) {
                jsonObject.put(JSONConstants.ID, getId())
                        .put(JSONConstants.PARAMS, paramsJson);
            } else {
                jsonObject.put(JSONConstants.ID, getId());
            }
            jsonObject.put("jsonrpc", JSON_RPCVERSION)
                    .put(JSONConstants.METHOD, method);
            String jsonString = jsonObject.toString();
            updateId();
            webSocket.sendText(jsonString);
            ALog.e(TAG, jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            ALog.i(TAG, e.getMessage());
        }
    }

    public void addIceCandidate(Map<String, String> iceCandidateParams) {
        iceCandidatesParams.add(iceCandidateParams);
    }

    public void setLocalOfferParams(Map<String, String> offerParams) {
        this.localOfferParams = offerParams;
    }
}