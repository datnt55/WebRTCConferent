//package jp.co.miosys.aitec.views.services;
//
//import android.app.ActivityManager;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.media.RingtoneManager;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import jp.co.miosys.aitec.R;
//import jp.co.miosys.aitec.activities.LoginActivity;
//import jp.co.miosys.aitec.activities.MapActivity;
//import jp.co.miosys.aitec.utils.CommonUtils;
//import jp.co.miosys.aitec.utils.Globals;
//import jp.co.miosys.aitec.utils.SharePreference;
//import jp.co.miosys.aitec.utils.SocketUtils;
//
//import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
//
///**
// * Created by Duc on 9/18/2017.
// */
//
//public class ReceiveCallService extends Service implements SocketUtils.OnCallingReceive {
//
//    int mStartMode;       // indicates how to behave if the service is killed
//    IBinder mBinder;      // interface for clients that bind
//    boolean mAllowRebind; // indicates whether onRebind should be used
//    public static boolean isReceiveCallServiceRunning = false; // indicates whether onRebind should be used
//    private Context mContext;
//
//    @Override
//    public void onCreate() {
//        // The service is being created
//        mContext = this;
//        if (socketUtils != null) {
//            socketUtils.setCallingCallBack(this, this);
//        } else {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        if (CommonUtils.haveNetWork(mContext)) {
//                            if (socketUtils == null) {
//                                // Reconnect when lost internet
//                                socketUtils = new SocketUtils(mContext, (SocketUtils.OnCallingReceive) mContext);
//                                socketUtils.connectWebSocket();
//                                Log.e("STEP1", "OK");
//                            }
//                        } else {
//                            socketUtils.release();
//                            socketUtils = null;
//                            stopService(LoginActivity.intent);
//                            Log.e("STEP2", "OK");
//                        }
//                    }
//                }
//            }).start();
//        }
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // The service is starting, due to a call to startService()
//        isReceiveCallServiceRunning = true;
//        return mStartMode;
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // A client is binding to the service with bindService()
//        return mBinder;
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        // All clients have unbound with unbindService()
//        return mAllowRebind;
//    }
//
//    @Override
//    public void onRebind(Intent intent) {
//        // A client is binding to the service with bindService(),
//        // after onUnbind() has already been called
//    }
//
//    @Override
//    public void onDestroy() {
//        // The service is no longer used and is being destroyed
//        isReceiveCallServiceRunning = false;
//    }
//
//    // Receive result from socket server
//
//    @Override
//    public void onCalling(String message) {
//        try {
//            JSONObject result = new JSONObject(message);
//            String function = result.getString("type");
//            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//            String currentActivity = am.getRunningTasks(1).get(0).topActivity.toString();
//
//            if (function.equals("connect")) {
//                SharePreference preference = new SharePreference(mContext);
//                try {
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("type", Globals.FUNCTION_LOGIN);
//                    jsonObject.put("regId", CommonUtils.getUUID(this));
//                    jsonObject.put("name", preference.getLogin()[0]);
//                    jsonObject.put("password", preference.getLogin()[1]);
//                    String sendMessage = jsonObject.toString();
//                    socketUtils.sendMessage(sendMessage);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            // Client receiver receive call (Doing)
////            if (!currentActivity.contains("ConnectActivity") && function.equals(Globals.FUNCTION_CALL)) {
////                //get Event Notification
////            Globals.id_guest = result.getString("host");
////            Globals.name_guest = result.getString("name");
////                Intent intent = new Intent(this, ConnectActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                intent.putExtra(Globals.PUSH_BOOLEAN_SERVICE, true);
////                startActivity(new Intent(intent));
////            }
//
//            // Client receiver receive notification
//            if (function.equals(Globals.FUNCTION_SEND_NOTIFICATION)) {
//                //Turn on screen
//                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
//                boolean isScreenOn = pm.isScreenOn();
//                if (isScreenOn == false) {
//                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
//                    wl.acquire(10000);
//                    PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
//                    wl_cpu.acquire(10000);
//                }
//
//                Intent intent = new Intent(this, MapActivity.class);
//                intent.putExtra(Globals.PUSH_EXTRA_LONGTITUDE, result.getDouble("longitude"));
//                intent.putExtra(Globals.PUSH_EXTRA_LATLITUDE, result.getDouble("latitude"));
//                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                        .setAutoCancel(true)
//                        .setContentTitle(this.getResources().getString(R.string.app_name))
//                        .setContentText(CommonUtils.contentNoti(mContext, result.getString("userId"), result.getString("dateTime"), result.getString("latitude"), result.getString("longitude")))
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                        .setVibrate(new long[]{1, 1, 1})
//                        .setPriority(Notification.PRIORITY_MAX)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentIntent(pendingIntent)
//                        .setStyle(new NotificationCompat.BigTextStyle().bigText(CommonUtils.contentNoti(mContext, result.getString("userId"), result.getString("dateTime"), result.getString("latitude"), result.getString("longitude"))));
//                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                manager.notify(1, builder.build());
//            }
//        } catch (Exception e) {
//            Log.e("TAG", e.toString());
//        }
//    }
//}