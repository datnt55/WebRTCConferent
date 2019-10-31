/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package jp.co.miosys.aitec.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.telecom.Conference;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.kurento.OneTecActivity;
import jp.co.miosys.aitec.kurento.VideoConferenceActivity;
import jp.co.miosys.aitec.managers.ConnectToRoom;
import jp.co.miosys.aitec.models.Contact;
import jp.co.miosys.aitec.models.LocationGPS;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.utils.ALog;
import jp.co.miosys.aitec.utils.AnimationUtils;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.CustomClick;
import jp.co.miosys.aitec.utils.DialogUtils;
import jp.co.miosys.aitec.utils.GPSTracker;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.KMLHelper;
import jp.co.miosys.aitec.utils.LocationProvide;
import jp.co.miosys.aitec.utils.NoticeDialog;
import jp.co.miosys.aitec.utils.RingToneAndSound;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.utils.SocketUtils;
import jp.co.miosys.aitec.views.adapters.ContactAdapter;
import jp.co.miosys.aitec.views.adapters.PopupAdapter;
import jp.co.miosys.aitec.views.listeners.OnGetApiDataListener;
import jp.co.miosys.aitec.views.services.ApiProcessService;
import jp.co.miosys.aitec.views.services.OkHttpService;
import jp.co.miosys.aitec.views.widgets.CallingDialog;
import okhttp3.Call;
import okhttp3.Response;

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.REQUEST_API_101;
import static jp.co.miosys.aitec.utils.Globals.SESSION_URL;
import static jp.co.miosys.aitec.utils.Globals.URL_REGISTER_ROOM;
import static jp.co.miosys.aitec.utils.Globals.arrayMemo;
import static jp.co.miosys.aitec.utils.Globals.currentLocation;
import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.Globals.locations;
import static jp.co.miosys.aitec.utils.Globals.room_id;
import static jp.co.miosys.aitec.utils.KMLHelper.CAPTURE_IMAGE;
import static jp.co.miosys.aitec.utils.KMLHelper.END_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.SEND_IMAGE;
import static jp.co.miosys.aitec.utils.KMLHelper.START_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.START_RECORD;
import static jp.co.miosys.aitec.utils.KMLHelper.VOICE_MEMO;
import static jp.co.miosys.aitec.utils.LocationProvide.REQUEST_CHECK_SETTINGS;
import static jp.co.miosys.aitec.utils.LocationProvide.REQUEST_PERMISSIONS_REQUEST_CODE;


/*[20170913] Ductx: #2598: Create connect activity*/

public class ConnectActivity extends BaseActivity implements SocketUtils.OnMessageReceive, ContactAdapter.GuestContactListener, LocationProvide.OnUpdateLocation, CallingDialog.CallingDialogCallback, OnGetApiDataListener, CustomClick.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private Context mContext;
    private RecyclerView rvContact;
    private ArrayList<Contact> arrayList = new ArrayList<>();
    private Contact mContactGuest;
    private SharePreference preference = new SharePreference(this);
    private RecyclerView.Adapter mAdapter;
    private ConnectToRoom ctr;
    private RelativeLayout mLyConnect;
    private TextView txtUser;
    private LinearLayout lyEmergency;
    private String roomId;
    private CallingDialog callingDialog;
    private ProgressDialog mProgressDialog;
    private boolean doubleBackToExitPressedOnce = false;
    private Button btnConference;
    private ImageView imgMenu;
    private CustomClick customClick;
    private Button btnP2P;
    private LocationProvide locationProvide;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_connect);
        mContext = this;
        ctr = new ConnectToRoom(this);
        customClick = new CustomClick(this);
        initView();
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
        locationProvide = new LocationProvide(this, this);
        locationProvide.startUpdatesButtonHandler();
        new ApiProcessService().getMemoList(this, this, Globals.REQUEST_API_101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationProvide.startUpdatesButtonHandler();
                } else {
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            // remove status isPatnerViewImage
            Globals.isPatnerViewImage = false;
            btnConference.setEnabled(true);
            btnConference.setClickable(true);
            btnP2P.setEnabled(true);
            btnP2P.setClickable(true);
            //init interface
            ContactAdapter.userSelectedListener(this);
            if (SocketUtils.isCloseSocket) {
                closeActivity();
            } else {
                socketUtils.setCallBack(this, this);
                getUserOnlineApi();
            }
        } catch (Exception e) {

        }
    }

    public void initView() {
        lyEmergency = (LinearLayout) findViewById(R.id.ly_emergency);
        txtUser = (TextView) findViewById(R.id.txt_user);
        mLyConnect = (RelativeLayout) findViewById(R.id.ly_connect);
        rvContact = (RecyclerView) findViewById(R.id.rv_contact);
        btnConference = (Button) findViewById(R.id.btn_conference);
        btnP2P = (Button) findViewById(R.id.btn_p2p);
        imgMenu = (ImageView) findViewById(R.id.img_menu);
        customClick.setView(imgMenu);
        txtUser.setText(preference.getLogin()[0]);
        /*
         Use this setting to improve performance if you know that changes in content do not change the layout size
         of the RecyclerView
         */
        rvContact.setHasFixedSize(true);
        // Use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvContact.setLayoutManager(mLayoutManager);

        // Divider Item Decoration in RecycleView
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(rvContact.getContext(), DividerItemDecoration.VERTICAL);
        rvContact.addItemDecoration(mDividerItemDecoration);

        // Specify an adapter
        mAdapter = new ContactAdapter(mContext, arrayList);
        rvContact.setAdapter(mAdapter);
        lyEmergency.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                GPSTracker tracker = new GPSTracker(mContext);
                if (tracker.canGetLocation()) {
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setMessage("Sending ...");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    boolean isSended = false;
                    while (tracker.getLatitude() != 0 && tracker.getLongitude() != 0 && !isSended) {
                        isSended = true;
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", Globals.FUNCTION_SEND_NOTIFICATION);
                            jsonObject.put("regId", Globals.id_client);
                            jsonObject.put("name", Globals.name_client);
                            jsonObject.put("dateTime", CommonUtils.getCurrentLocalTimeFormat(Globals.patternImageName));
                            jsonObject.put("latitude", tracker.getLatitude());
                            jsonObject.put("longitude", tracker.getLongitude());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String sendMessage = jsonObject.toString();
                        socketUtils.sendMessage(sendMessage);
                    }
                } else {
                    DialogUtils.settingRequestTurnOnLocation((Activity) mContext);
                }
                return false;
            }
        });
    }

    /*============================VMIO SYSTEM ====================================*/

    // Api get all user is online
    public void getUserOnlineApi() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_DISCOVERY);
            jsonObject.put("name", Globals.name_client);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
    }

    // Api call between two user
    public void connectOnclick(View view) {
        btnP2P.setEnabled(false);
        btnP2P.setClickable(false);
        if (mContactGuest != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                Globals.id_guest = mContactGuest.getId();
                jsonObject.put("type", Globals.FUNCTION_CALL);
                jsonObject.put("host", Globals.name_client);
                jsonObject.put("receive", mContactGuest.getName());
                jsonObject.put("name", preference.getLogin()[0]);
                //initiatePopupWindow(true);
                callingDialog = new CallingDialog();
                callingDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.NoTitleDialog);
                Bundle bundle = new Bundle();
                bundle.putBoolean(Globals.BUNDLE_HOST, true);
                bundle.putBoolean(Globals.BUNDLE_CONFERENCE, false);
                callingDialog.setArguments(bundle);
                callingDialog.setCancelable(false);
                ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction().add(callingDialog, "tag").commitAllowingStateLoss();
                callingDialog.setOnCallBack(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        } else {
            btnP2P.setEnabled(true);
            btnP2P.setClickable(true);
            Toast.makeText(this, "Please select a guest to call", Toast.LENGTH_SHORT).show();
        }
    }


    // [20180907    VMio] Show Dialog for User decide select VPN or not
    boolean continueWithoutVPN = true;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void conferenceOnclick(View view) {
        btnConference.setEnabled(false);
        btnConference.setClickable(false);
        // [20180907    VMio] Check VPN status before make Conference call. Since Some Telecom provider seems block conference traffic
        if (Globals.IsConferenceCheckVPN && !checkUseVPN()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog.Builder messageDialog = new AlertDialog.Builder(this);
            builder.setTitle("Notice")
                    .setMessage("VPN is not turned on. Do you want to continue?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            continueWithoutVPN = true;
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            continueWithoutVPN = false;
                            // [20180907    VMio] Show message guide user to quit app then restart after turn VPN on
                            messageDialog.setTitle("Message")
                                    .setMessage("Please restart app when VPN ready")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            continueWithoutVPN = true;
                                            dialog.dismiss();
                                        }
                                    });
                            dialog.dismiss();
                        }
                    })
                    .show();
            if (!continueWithoutVPN) return;
        }

        if (mContactGuest != null) {
            createSession();
        } else {
            Toast.makeText(this, getString(R.string.select_user_for_make_call), Toast.LENGTH_SHORT).show();
            btnConference.setEnabled(true);
            btnConference.setClickable(true);
        }

    }

    public void alertOnClick(View view) {
        Intent intent = new Intent(this, AlertListActivity.class);
        startActivity(intent);
    }

    private void createSession() {
        //String param = "{\"recordingMode\" : \"ALWAYS\",\"defaultRecordingLayout\":\"BEST_FIT\",\"defaultOutputMode\": \"INDIVIDUAL\" }";
        String param = "{\"recordingMode\" : \"ALWAYS\",\"defaultRecordingLayout\":\"BEST_FIT\"}";
        JSONObject json = null;
        try {
            json = new JSONObject(param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new OkHttpService(OkHttpService.Method.POST, true, this, SESSION_URL, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NoticeDialog dialog = new NoticeDialog();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Globals.BUNDLE, "Server or Internet is error");
                        dialog.setArguments(bundle);
                        dialog.setCancelable(false);
                        getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
                        btnConference.setEnabled(true);
                        btnConference.setClickable(true);
                    }
                });

                Log.e("Loi", e.getMessage());
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                String sessionName = "";
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    sessionName = (String) jsonObject.get("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //String roomId = Integer.toString((new Random()).nextInt(100000000));
                if (mContactGuest != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", Globals.FUNCTION_CONFERENCE_INVITATION);
                        jsonObject.put("host", Globals.name_client);
                        jsonObject.put("receive", mContactGuest.getName());
                        jsonObject.put("name", preference.getLogin()[0]);
                        jsonObject.put("room", sessionName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    registerRoomToServer(sessionName);
                    String sendMessage = jsonObject.toString();
                    socketUtils.sendMessage(sendMessage);
                    Intent intent = new Intent(mContext, VideoConferenceActivity.class);
                    intent.putExtra("Name", preference.getLogin()[0]);
                    intent.putExtra("guest", mContactGuest.getName());
                    intent.putExtra("room", sessionName);
                    startActivityForResult(intent, REQUEST_API_101);
                }
            }
        };
    }

    private void registerRoomToServer(String session) {
        JSONObject json = new JSONObject();
        try {
            json.put("talk_id", session);
            json.put("group_user_ids", Globals.name_client);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new OkHttpService(OkHttpService.Method.POST, false, this, URL_REGISTER_ROOM, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NoticeDialog dialog = new NoticeDialog();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Globals.BUNDLE, "Server or Internet is error");
                        dialog.setArguments(bundle);
                        dialog.setCancelable(false);
                        getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
                    }
                });
                Log.e("Loi", e.getMessage());
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                String sessionName = "";
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean checkUseVPN() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        //For 3G check
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();

        if (is3g) {
            Network[] networks = manager.getAllNetworks();
            for (int i = 0; i < networks.length; i++) {
                NetworkCapabilities caps = manager.getNetworkCapabilities(networks[i]);
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    return true;

            }
            return false;
        }

        boolean isTethering = manager.isActiveNetworkMetered();

        if (isTethering) {
            Network[] networks = manager.getAllNetworks();
            for (int i = 0; i < networks.length; i++) {
                NetworkCapabilities caps = manager.getNetworkCapabilities(networks[i]);
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    return true;

            }
            return false;
        }
        return true;
    }

    // Receive result from socket server
    @Override
    public void onMessage(String message) {
        try {
            final JSONObject result = new JSONObject(message);
            String function = result.getString("type");
            // Get all user online
            if (function.equals(Globals.FUNCTION_DISCOVERY)) {
                boolean isSelect = false;
                arrayList.clear();
                JSONArray array = result.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    boolean isBusy;
                    String regId = array.getJSONObject(i).getString("regId");
                    String name = array.getJSONObject(i).getString("name");
                    int status = array.getJSONObject(i).getInt("status");
                    if (status == 1) {
                        isBusy = false;
                    } else {
                        isBusy = true;
                    }
                    if (!name.equals(Globals.name_client)) {
                        if (mContactGuest != null && mContactGuest.getId().equals(regId) && !isBusy) {
                            arrayList.add(new Contact(regId, name, status, true));
                            isSelect = true;
                        } else {
                            arrayList.add(new Contact(regId, name, status, false));
                        }
                    }
                }
                if (!isSelect) {
                    mContactGuest = null;
                }
                mAdapter.notifyDataSetChanged();
            }
            // Client receiver receive call
            if (function.equals(Globals.FUNCTION_CALL)) {
                Globals.id_guest = result.getString("host");
                Globals.name_guest = result.getString("name");

                callingDialog = new CallingDialog();
                callingDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.NoTitleDialog);
                Bundle bundle = new Bundle();
                bundle.putBoolean(Globals.BUNDLE_HOST, false);
                bundle.putBoolean(Globals.BUNDLE_CONFERENCE, false);
                callingDialog.setArguments(bundle);
                callingDialog.setCancelable(false);
                ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction().add(callingDialog, "tag").commitAllowingStateLoss();
                callingDialog.setOnCallBack(this);
            }

            // Client host receive answer from receiver
            if (function.equals(Globals.FUNCTION_ANSWER)) {
                String status = result.getString("result");
                if (status.equals("success")) {
                    Globals.isReceiver = false;
                    CommonUtils.createLogFolder();
                    final String roomId = result.getString("room");
                    kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", Globals.name_guest, "PeerToPeer", null, currentLocation, roomId, null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callingDialog.dismiss();
                            ctr.connectToRoom(roomId, false, false, false, 0);
                        }
                    }, 1000);
                } else {
                    callingDialog.dismiss();
                }
            }

            // Send notification success
            if (function.equals(Globals.FUNCTION_SEND_NOTIFICATION_SUCCESS)) {
                mProgressDialog.dismiss();
                Toast.makeText(mContext, R.string.send_noti_success, Toast.LENGTH_LONG).show();
            }

            // Error connect to server
            if (function.equals(Globals.FUNCTION_ERROR_CONNECT)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Connection is Error", Toast.LENGTH_LONG).show();
                    }
                });

                if (socketUtils != null) {
                    socketUtils.release();
                    socketUtils = null;
                }
                closeActivity();
            }

            if (function.equals(Globals.FUNCTION_CONFERENCE_INVITATION)) {
                roomId = result.getString("room");
                Globals.name_guest = result.getString("host");
                //showDialogCallingGroup(roomId);
                if (callingDialog != null && callingDialog.isShowing())
                    return;
                callingDialog = new CallingDialog();
                callingDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.NoTitleDialog);
                Bundle bundle = new Bundle();
                bundle.putBoolean(Globals.BUNDLE_HOST, false);
                bundle.putBoolean(Globals.BUNDLE_CONFERENCE, true);
                callingDialog.setArguments(bundle);
                callingDialog.setCancelable(false);
                ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction().add(callingDialog, "tag").commitAllowingStateLoss();
                callingDialog.setOnCallBack(this);
            }
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    private void sendConferenceConfirm(String accept, String roomId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_CONFERENCE_CONFIRM);
            jsonObject.put("host", Globals.name_client);
            jsonObject.put("receive", Globals.name_guest);
            jsonObject.put("confirm", accept);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
        if (accept.equals("accept")) {
            Intent intent = new Intent(this, VideoConferenceActivity.class);
            intent.putExtra("Name", preference.getLogin()[0]);
            intent.putExtra("room", roomId);
            startActivityForResult(intent, REQUEST_API_101);
        }
    }

    @Override
    public void onGuestContact(Contact contactGuest) {
        mContactGuest = contactGuest;
        Globals.id_guest = mContactGuest.getId();
        Globals.name_guest = mContactGuest.getName();
    }

    // Api call between two user
    public void logOutOnclick(View view) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("確認");
        alertDialogBuilder.setMessage("ログアウトしても宜しいですか？")
                .setCancelable(false)
                .setPositiveButton("はい",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                logOut();
                                closeActivity();
                            }
                        })
                .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void closeActivity() {
        Intent intent = new Intent(ConnectActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", Globals.FUNCTION_DISCONECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
            finishAffinity ();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.back_to_exit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void logOut() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_DISCONECT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
        preference.saveLogIn("", "");
//        socketUtils.release();
    }

    @Override
    public void onStopUpdate() {

    }

    @Override
    public void onUpdate(Location currentLocation) {
        if (currentLocation == null)
            return;
        Globals.currentLocation = new Location(currentLocation);
        locations.add(new LocationGPS(currentLocation));
        if (kmlHelper != null)
            kmlHelper.addWayPoint(currentLocation);
        checkDistanceMemo(currentLocation);
    }

    private void checkDistanceMemo(Location currentLocation) {
        for (Memo memo : arrayMemo) {
            double distance = CommonUtils.calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), memo.getLat(), memo.getLon());
            if (distance < Globals.DISTANCE && (memo.getDistance() > Globals.DISTANCE || memo.getDistance() == -1)) {
                CommonUtils.showNotification(this, memo);
            }
            memo.setDistance(distance);
        }
    }

    @Override
    public void onCallAccept(boolean isConference) {
        if (isConference) {
            sendConferenceConfirm("accept", roomId);
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                String roomId = UUID.randomUUID().toString();
                jsonObject.put("type", Globals.FUNCTION_ANSWER);
                jsonObject.put("result", "success");
                jsonObject.put("host", Globals.name_guest);
                jsonObject.put("receive", Globals.id_client);
                jsonObject.put("room", roomId);
                CommonUtils.createLogFolder();
                ctr.connectToRoom(roomId, false, false, false, 0);
                if (currentLocation != null)
                    kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", Globals.name_guest, "PeerToPeer", null, currentLocation, roomId, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        }
    }

    @Override
    public void onCallDeny(boolean isConference) {
        if (isConference) {
            sendConferenceConfirm("deny", roomId);
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("host", Globals.name_client);
                jsonObject.put("receive", Globals.name_guest);
                jsonObject.put("type", Globals.FUNCTION_ANSWER);
                jsonObject.put("result", "reject");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
            btnP2P.setEnabled(true);
            btnP2P.setClickable(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            locationProvide = new LocationProvide(this, this);
            locationProvide.startUpdatesButtonHandler();
        }else if (requestCode == REQUEST_API_101){
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("result");

                new android.app.AlertDialog.Builder(this)
                        .setTitle(getText(R.string.notice))
                        .setMessage(getString(R.string.user_deny_call, result))
                        .setCancelable(false)
                        .setNeutralButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .create()
                        .show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationProvide.stopLocationUpdates();
    }

    @Override
    public void onGetApiData(Object obj, int requestCode) {
        if (requestCode == REQUEST_API_101) {
            arrayMemo = (ArrayList<Memo>) obj;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_menu:
                showPopupWindow();
                break;
        }
    }

    private void showPopupWindow() {
        PopupMenu popup = new PopupMenu(this, imgMenu);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_function);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.one_tec:
//                // do your code
//                startActivity(new Intent(this, OneTecActivity.class));
//                return true;
            case R.id.voice_memo:
                // do your code
                startActivity(new Intent(this,VoiceMemoActivity.class));
                return true;
            case R.id.alert_list:
                startActivity(new Intent(this,AlertListActivity.class));
                // do your code
                return true;
            case R.id.setting:
                // do your code
                startActivity(new Intent(this,SettingActivity.class));
                return true;
            default:
                return false;
        }
    }

    public void oneTecOnclick (View view){
        startActivity(new Intent(this, OneTecActivity.class));
    }


}
