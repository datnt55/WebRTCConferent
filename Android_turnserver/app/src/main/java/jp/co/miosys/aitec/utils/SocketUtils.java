package jp.co.miosys.aitec.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.activities.LoginActivity;
import jp.co.miosys.aitec.activities.MapActivity;
import jp.co.miosys.aitec.activities.SplashActivity;
import jp.co.miosys.aitec.views.services.OkHttpService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static android.content.Context.NOTIFICATION_SERVICE;
import static jp.co.miosys.aitec.utils.Globals.HOST;
import static jp.co.miosys.aitec.utils.Globals.KMS_ROOT;
import static jp.co.miosys.aitec.utils.Globals.MIO_HOST;
import static jp.co.miosys.aitec.utils.Globals.SERVER_IP;
import static jp.co.miosys.aitec.utils.Globals.TurnServerURI;

/**
 * Created by Duc on 9/8/2017.
 */

public class SocketUtils extends WebSocketListener {
    public WebSocket mWebSocketClient;
    private OkHttpClient client;
    private Context mContext;
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private OnMessageReceive listener;
    public static boolean isCloseSocket;

    public SocketUtils(Context context, OnMessageReceive listener) {
        this.mContext = context;
        this.listener = listener;
        client = new OkHttpClient();
    }

    public void setCallBack(Context context, OnMessageReceive listener) {
        this.mContext = context;
        this.listener = listener;
    }

    public void release() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close(NORMAL_CLOSURE_STATUS, null);
            mWebSocketClient = null;
        }
        if (listener != null)
            listener = null;
    }

    public void connect() {
        if (Globals.KMS_ROOT == null){
            selectCompany(new SharePreference(mContext));
        }else {
            Request request = new Request.Builder().url(Globals.SERVER_IP).build();
            WebSocket ws = client.newWebSocket(request, this);
            client.dispatcher().executorService().shutdown();
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        this.mWebSocketClient = webSocket;
        isCloseSocket = false;
    }

    @Override
    public void onMessage(WebSocket webSocket, final String text) {
        super.onMessage(webSocket, text);
        if (listener != null) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onMessage(text);
                    showNotification(text);
                }
            });

        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        isCloseSocket = true;
        ALog.e("Websocket", "OnClosed " + reason);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Globals.FUNCTION_ERROR_CONNECT);
            String message = jsonObject.toString();
            if (listener != null)
                listener.onMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            //ALog.e("Websocket", "Error: " + response.body().string());
            isCloseSocket = true;
            // ALog.e("Websocket", "OnClosed " + s);
            //String c = CommonUtils.getCurrentActivity(mContext);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Globals.FUNCTION_ERROR_CONNECT);
            String message = jsonObject.toString();
            if (listener != null)
                listener.onMessage(message);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    //    public void connectWebSocket() {
//        URI uri;
//        try {
//            uri = new URI(Globals.SERVER_IP);
//            mWebSocketClient = new WebSocketClient(uri) {
//
//                @Override
//                public void onOpen(ServerHandshake serverHandshake) {
//                    ALog.i("Websocket", "Opened");
//                }
//
//                @Override
//                public void onMessage(String s) {
//                    final String message = s;
//                    if (listener != null) {
//                        ((Activity) mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                listener.onMessage(message);
//                                showNotification(message);
//                            }
//                        });
//
//                    }
//                }
//
//                @Override
//                public void onClose(int i, String s, boolean b) {
//                    if (!CommonUtils.getCurrentActivity(mContext).contains("LoginActivity")) {
//                        isCloseSocket = true;
//                        ALog.e("Websocket", "Socket is closed:" + isCloseSocket);
//                    }
//                    ((Activity) mContext).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mContext, "Error connect to server, please check your internet", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    ALog.e("Websocket", "OnClosed " + s);
//                    try {
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("type", Globals.FUNCTION_ERROR_CONNECT);
//                        String message = jsonObject.toString();
//                        listener.onMessage(message);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//
//
//                @Override
//                public void onError(Exception e) {
//                    ALog.e("Websocket", "Error " + e.getMessage());
//                    if (!CommonUtils.getCurrentActivity(mContext).contains("LoginActivity")) {
//                        isCloseSocket = true;
//                        ALog.e("Websocket", "Socket is closed:" + isCloseSocket);
//                    }
//                    ((Activity) mContext).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mContext, "Error connect to server, please check your internet", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                   // ALog.e("Websocket", "OnClosed " + s);
//                    //String c = CommonUtils.getCurrentActivity(mContext);
//                    try {
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("type", Globals.FUNCTION_ERROR_CONNECT);
//                        String message = jsonObject.toString();
//                        listener.onMessage(message);
//                    } catch (JSONException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            };
//            mWebSocketClient.connect();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }
//    }

    public void showNotification(String message) {
        // Client receiver receive notification
        try {
            JSONObject result = new JSONObject(message);
            String function = result.getString("type");
            if (function.equals(Globals.FUNCTION_SEND_NOTIFICATION)) {
                //Turn on screen
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = pm.isScreenOn();
                if (isScreenOn == false) {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "myapp:MyLock");
                    wl.acquire(10000);
                    PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:MyCpuLock");
                    wl_cpu.acquire(10000);
                }

                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra(Globals.PUSH_EXTRA_LONGTITUDE, result.getDouble("longitude"));
                intent.putExtra(Globals.PUSH_EXTRA_LATLITUDE, result.getDouble("latitude"));
                intent.putExtra(Globals.PUSH_GUEST_NAME, result.getString("userId"));
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                        .setAutoCancel(true)
                        .setContentTitle(mContext.getResources().getString(R.string.app_name))
                        .setContentText(CommonUtils.contentNoti(mContext, result.getString("userId"), result.getString("dateTime"), result.getString("latitude"), result.getString("longitude")))
                        .setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.emergency))
                        .setVibrate(new long[]{1, 1, 1})
                        .setPriority(Notification.PRIORITY_MAX)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(CommonUtils.contentNoti(mContext, result.getString("userId"), result.getString("dateTime"), result.getString("latitude"), result.getString("longitude"))));
                NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                manager.notify(1, builder.build());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            mWebSocketClient.send(message);
        } catch (Exception e) {
            ALog.i("Disconect", "Error " + e.getMessage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", Globals.FUNCTION_ERROR_CONNECT);
                String mess = jsonObject.toString();
                listener.onMessage(mess);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void selectCompany(final SharePreference preference) {
        JSONObject json = new JSONObject();
        try {
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
            json.put("company_code", preference.getCompany());
        }catch (JSONException ex){

        }
        new OkHttpService(OkHttpService.Method.POST, false, mContext, Globals.CHOOSE_COMPANY, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    MIO_HOST = data.getString("domain");
                    KMS_ROOT = data.getString("kms");
                    SERVER_IP = "ws://" + KMS_ROOT + ":9090";
                    TurnServerURI = "turn:" + KMS_ROOT + ":3478";
                    HOST = "https://" + KMS_ROOT + ":4443";
                    Globals.TOKEN_URL = HOST +"/api/tokens";
                    Globals.SAVE_RECORDING_URL = HOST +"/api/recordings/stop/";
                    Globals.START_RECORDING_URL = HOST +"/api/recordings/start";
                    Globals.GET_RECORDING_URL = HOST +"/api/recordings";
                    Globals.SESSION_URL = HOST+"/api/sessions";
                    Globals.URL_SEND_IMAGE = MIO_HOST +"/api/v1/chat/upload-image";
                    Globals.SAVE_CALL_DETAIL = MIO_HOST +"/api/v1/chat/send-message";
                    Globals.MEMO_UPLOAD = MIO_HOST + "/api/v1/chat/send-voice-memo" ;
                    Globals.SAVE_USER = MIO_HOST +"/api/v1/user/login-username-uuid";
                    Globals.URL_KML = MIO_HOST +"/api/v1/logger/add";
                    Globals.URL_REGISTER_ROOM = MIO_HOST +"/api/v1/chat/add-room";
                    Globals.URL_LIST_MEMO = MIO_HOST +"/api/v1/memo/list";

                    connect();
                } catch (JSONException e) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
    }

    public interface OnMessageReceive {
        void onMessage(String message);
    }
}
