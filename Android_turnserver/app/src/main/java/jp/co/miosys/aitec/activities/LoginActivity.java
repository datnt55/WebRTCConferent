package jp.co.miosys.aitec.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.bmartel.protocol.http.utils.StringUtils;
import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Example;
import jp.co.miosys.aitec.models.IceServer;
import jp.co.miosys.aitec.utils.AnimationUtils;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.KMLHelper;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.utils.SocketUtils;
import jp.co.miosys.aitec.views.services.OkHttpService;
import okhttp3.Call;
import okhttp3.Response;

import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.Globals.mMioTempDirectory;
import static jp.co.miosys.aitec.utils.Globals.room_id;
import static jp.co.miosys.aitec.utils.KMLHelper.CAPTURE_IMAGE;
import static jp.co.miosys.aitec.utils.KMLHelper.END_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.SEND_IMAGE;
import static jp.co.miosys.aitec.utils.KMLHelper.START_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.START_RECORD;
import static jp.co.miosys.aitec.utils.KMLHelper.VOICE_MEMO;
import static jp.co.miosys.aitec.utils.SocketUtils.isCloseSocket;

/*[20170609] Ductx: #2599: Create login activity, call api login to socket server*/

public class LoginActivity extends BaseActivity implements SocketUtils.OnMessageReceive, TextWatcher {

    private EditText edtUser, edtPass;
    private String mUser, mPass;
    private Context mContext;
    private SharePreference preference = new SharePreference(this);
    private ProgressDialog mProgressDialog;
    private boolean isConnected = false;
    public static SocketUtils socketUtils;
    private LinearLayout lyTitle, lyLogin;
    public static Intent intent;
    public static String test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        kmlHelper = null;
        CommonUtils.getSizeScreen(mContext);
        // Get uuid to set id client in socket
        Globals.id_client = CommonUtils.getUUID(this);
        initView();
        checkPermission();
        int a = getLocationMode();
        //startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        List<IceServer> iceServers = new ArrayList<>();
//        LocationProvide locationProvide = new LocationProvide(this, new LocationProvide.OnUpdateLocation() {
//            @Override
//            public void onStopUpdate() {
//
//            }
//
//            @Override
//            public void onUpdate(LocationGPS mCurrentLocation) {
//                if (mCurrentLocation != null) {
//                    double a = mCurrentLocation.getLatitude();
//                    double b = mCurrentLocation.getLatitude();
//                }
//                int c = 1;
//            }
//        });
//        locationProvide.stopLocationUpdates();

       // new SpeedTestTask().execute();
//        List<String> urls1 = new ArrayList<>();
//        urls1.add("turn:numb.viagenie.ca");
//        iceServers.add(new IceServer(urls1, "muazkh", "webrtc@live.com"));

//        List<String> urls2 = new ArrayList<>();
//        urls2.add("turn:192.158.29.39:3478?transport=udp");
//        iceServers.add(new IceServer(urls2, "28224511:1379330808", "JZEOEt2V3Qb0y27GRntt2u2PAYA="));
//
//        List<String> urls3 = new ArrayList<>();
//        urls3.add("turn:192.158.29.39:3478?transport=tcp");
//        iceServers.add(new IceServer(urls3, "28224511:1379330808", "JZEOEt2V3Qb0y27GRntt2u2PAYA="));

        List<String> urls3 = new ArrayList<>();
        urls3.add(Globals.TurnServerURI);//"turn:157.7.209.73:1908");
        iceServers.add(new IceServer(urls3, Globals.TurnServerUser, Globals.TurnServerPass)); //"vmio", "vm69vm69"));


//        List<String> urls = new ArrayList<>();
//        urls.add("stun:stun01.sipphone.com");
//        urls.add("stun:stun.ekiga.net");
//        urls.add("stun:stun.fwdnet.net");
//        urls.add("stun:stun.ideasip.com");
//        urls.add("stun:stun.iptel.org");
//        urls.add("stun:stun.rixtelecom.se");
//        urls.add("stun:stun.schlund.de");
//        urls.add("stun:stunserver.org");
//        urls.add("stun:stun.softjoys.com");
//        urls.add("stun:stun.voiparound.com");
//        urls.add("stun:stun.voipbuster.com");
//        urls.add("stun:stun.voipstunt.com");
//        urls.add("stun:stun.voxgratia.org");
//        urls.add("stun:stun.xten.com");
//        IceServer iceServer2 = new IceServer(urls);
//        iceServers.add(iceServer2);

        String lifetimeDuration = "86400s";
        String blockStatus = "NOT_BLOCKED";
        String iceTransportPolicy = "all";

        Example example = new Example(lifetimeDuration, iceServers, blockStatus, iceTransportPolicy);

        try {
            Gson gson = new Gson();
            test = gson.toJson(example);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getLocationMode()
    {
        try {
            return Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

//    public class SpeedTestTask extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            SpeedTestSocket speedTestSocket = new SpeedTestSocket();
//
//            // add a listener to wait for speedtest completion and progress
//            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
//
//                @Override
//                public void onCompletion(SpeedTestReport report) {
//                    // called when download/upload is finished
//                    Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
//                    Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
//                }
//
//                @Override
//                public void onError(SpeedTestError speedTestError, String errorMessage) {
//                    // called when a download/upload error occur
//                }
//
//                @Override
//                public void onProgress(float percent, SpeedTestReport report) {
//                    // called to notify download/upload progress
//                    Log.v("speedtest", "[PROGRESS] progress : " + percent + "%");
//                    Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
//                    Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
//                }
//            });
//
//            speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso");
//
//            return null;
//        }
//    }
    private void initView() {
        lyTitle = (LinearLayout) findViewById(R.id.ly_title);
        lyLogin = (LinearLayout) findViewById(R.id.ly_login);
        TextView txtCompanyName = (TextView) findViewById(R.id.txt_company_name);
        txtCompanyName.setText(new SharePreference(this).getCompanyName());
        edtUser = (EditText) findViewById(R.id.edt_user);
        edtPass = (EditText) findViewById(R.id.edt_pass);
        mUser = preference.getLogin()[0];
        mPass = preference.getLogin()[1];
        edtUser.setText(mUser);
        edtPass.setText(mPass);
        // [20180907    VMio] Add revision for Debug
        TextView tvVerion = (TextView) findViewById(R.id.app_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            Globals.APP_VERSION = version;
            Globals.SVN_REVISION = String.valueOf(verCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(tvVerion != null) {
            if(Globals.IsDebug)
                tvVerion.setText(String.format("Ver %s.%s (2019)", Globals.APP_VERSION, Globals.SVN_REVISION));
            else
                tvVerion.setText(String.format("Ver %s (2019)", Globals.APP_VERSION));
        }
        initSocket();
        uploadKMLToServer();
        edtUser.addTextChangedListener(this);
    }

    public void checkPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Globals.REQUEST_WRITE_STORAGE);
        } else {
            threadStartActivity();
        }
    }

    public void initSocket() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!CommonUtils.haveNetWork(mContext)){
                        Toast.makeText(mContext,"Network is not available",Toast.LENGTH_SHORT).show();
                        if (mProgressDialog != null && mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        return;
                    }
                    if (socketUtils == null) {
                        socketUtils = new SocketUtils(mContext, LoginActivity.this);
                        socketUtils.connect();
                    }else {
                        socketUtils.setCallBack(LoginActivity.this, LoginActivity.this);
                        if (!mUser.equals("") && !mPass.equals(""))
                            logInApi(mUser, mPass);
                    }
                } catch (Exception e) {

                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Globals.REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    threadStartActivity();
                } else {
                    Toast.makeText(mContext, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void uploadKMLToServer(){
        if (!new File(mMioTempDirectory).exists())
            return;
        final File[] childFile = (new File(mMioTempDirectory)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file)
            {
                return (file.getPath().endsWith(".kml"));
            }
        });
        for (int i = 0; i < childFile.length; i++){
            final File kmlFile = childFile[i];
            String filename = childFile[i].getName().replace(".kml","");
            String userName = filename.split("_")[0];
            String roomId = filename.split("_")[1];
            String datetime = filename.split("_")[2];
            DateTimeFormatter date = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH-mm-ss");
            DateTime realDate = date.parseDateTime(datetime);
            date = DateTimeFormat.forPattern("yyyy-MM-dd");
            String realDateToString = date.print(realDate);

            TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();

            Map<String, Object> params = new HashMap<>();
            params.put("upload_file", kmlFile);
            params.put("uuid", uuid);
            params.put("talk_id", roomId);
            params.put("username", userName);
            params.put("logg_start", realDateToString);
            new OkHttpService(OkHttpService.Method.POST, this, Globals.URL_KML, params, false) {
                @Override
                public void onFailureApi(Call call, Exception e) {
                }

                @Override
                public void onResponseApi(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    kmlFile.delete();
                }
            };
        }
    }
    // Thread delay bg_splash screen
    private void threadStartActivity() {
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    Log.e(this.getClass().getName(), e.getMessage());
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int height = CommonUtils.convertDpToPx(getResources().getInteger(R.integer.login_ly_title_slide), mContext);
                            AnimationUtils.slideUp(lyTitle, height, 500);
                            lyLogin.setVisibility(View.VISIBLE);
                            AnimationUtils.scaleIn(lyLogin, 500, 0);
                            KeyboardVisibilityEvent.setEventListener((Activity) mContext, new KeyboardVisibilityEventListener() {
                                @Override
                                public void onVisibilityChanged(boolean isOpen) {
                                    if (getResources().getBoolean(R.bool.isTablet)) return;
                                    if (isOpen) {
                                        int height = CommonUtils.convertDpToPx(getResources().getInteger(R.integer.login_ly_login_slide), mContext);
                                        AnimationUtils.slideUp(lyLogin, height, 200);
                                    } else {
                                        AnimationUtils.slideUp(lyLogin, 0, 200);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        };
        timerThread.start();
    }

    public void logInOnclick(View view) {

        mUser = edtUser.getText().toString();
        mPass = edtPass.getText().toString();
        if (!mUser.equals("") && !mPass.equals("")) {
            logInApi(mUser, mPass);
        } else {
            Toast.makeText(this, getString(R.string.input_error), Toast.LENGTH_SHORT).show();
        }
    }

    // Api login
    public void logInApi(String user, String pass) {
        //threadCheckConnect(this);
        if (mProgressDialog == null ) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Login ...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
        hideKeyboard();
        if (isCloseSocket){
            if (socketUtils != null) {
                socketUtils.release();
                socketUtils = null;
            }
            initSocket();
        }else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", Globals.FUNCTION_LOGIN);
                jsonObject.put("regId", Globals.id_client);
                jsonObject.put("name", user);
                jsonObject.put("password", pass);
                String sendMessage = jsonObject.toString();
                socketUtils.sendMessage(sendMessage);
            } catch (JSONException e) {
                mProgressDialog.dismiss();
                e.printStackTrace();
            }
        }
    }

    // Receive result from socket server
    @Override
    public void onMessage(String message) {
        try {
            JSONObject result = new JSONObject(message);
            String function = result.getString("type");
            // 20171025 DucVmio Close service, Note: Don't init service when logout.
            if (function.equals("connect")) {
                if (!mUser.equals("") && !mPass.equals(""))
                    logInApi(mUser, mPass);
            }

            if (function.equals(Globals.FUNCTION_LOGIN)) {
                mProgressDialog.dismiss();
                isConnected = true;
                String status = result.getString("status");
                if (status.equals("error")) {
                    // Check user of this devices for accept login (temporary)
//                    if (preference.getLogin()[0].equals(mUser)) {
//                        Globals.name_client = mUser;
//                        Intent intent = new Intent(LoginActivity.this, ConnectActivity.class);
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(this, result.getString("message"), Toast.LENGTH_SHORT).show();
//                    }
                    Toast.makeText(this, getString(R.string.user_no_exist), Toast.LENGTH_SHORT).show();
                } else {
                    Globals.name_client = mUser;
                    preference.saveLogIn(mUser, mPass);
                    createUserInServerAiMap();
                    if (kmlHelper == null) {
                        kmlHelper = new KMLHelper();
                        kmlHelper.createGPXTrack();
                        kmlHelper.addMarker(START_CALL, "https://png.pngtree.com/svg/20170818/allow_call_435496.png");
                        kmlHelper.addMarker(END_CALL, "http://endat.org/wp-content/uploads/2017/09/phone-icon-red.png");
                        kmlHelper.addMarker(SEND_IMAGE, " https://www.svgimages.com/svg-image/s5/send-file-256x256.png");
                        kmlHelper.addMarker(START_RECORD, "http://icons.iconarchive.com/icons/martz90/circle/256/video-camera-icon.png");
                        kmlHelper.addMarker(CAPTURE_IMAGE, "https://www.keypointintelligence.com/img/advisoryIcons/consumer.png");
                        kmlHelper.addMarker(VOICE_MEMO, "https://cdn6.aptoide.com/imgs/0/a/5/0a547b9ae308e86c64566f1475384d80_icon.png");
                    }
                    Intent intent = new Intent(LoginActivity.this, ConnectActivity.class);
                    startActivity(intent);
                }
            }else if (function.equals(Globals.FUNCTION_ERROR_CONNECT)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogShow(mContext);
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createUserInServerAiMap(){
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("username", mUser);
            json.put("password", mPass);
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("API",json.toString());
        new OkHttpService(OkHttpService.Method.POST, false,this, Globals.SAVE_USER , json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String result = response.body().string();
            }
        };

    }

    // Dialog notice error connect to server
    private void dialogShow(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = new AlertDialog.Builder((Activity) context).create();
                dialog.setTitle(context.getResources().getString(R.string.error_connect_server));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (socketUtils != null) {
                            socketUtils.release();
                            socketUtils = null;
                        }
                        initSocket();
                        if (mProgressDialog != null && mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        //initSocket();
                    }
                });
                dialog.show();
            }
        });
    }

    private void hideKeyboard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public void onBackPressed() {
        preference.saveLogIn("","");
        Intent intent = new Intent(LoginActivity.this, SelectCompanyActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 10){
            Toast.makeText(this, getString(R.string.over_10_character),Toast.LENGTH_SHORT).show();
            s.delete(10, s.length());
        }else {
            if (s.toString().matches("^\\s*$")){
                Toast.makeText(this, getString(R.string.special_character),Toast.LENGTH_SHORT).show();
                //s.delete(s.length()-1, s.length());
            }
        }
    }
}
