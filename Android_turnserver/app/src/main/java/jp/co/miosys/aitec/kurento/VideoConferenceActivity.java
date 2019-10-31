package jp.co.miosys.aitec.kurento;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.john.waveview.WaveView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.activities.BaseActivity;
import jp.co.miosys.aitec.activities.ImageViewActivity;
import jp.co.miosys.aitec.activities.LoginActivity;
import jp.co.miosys.aitec.activities.UploadApplication;
import jp.co.miosys.aitec.kurento.adapter.ParticipantAdapter;
import jp.co.miosys.aitec.kurento.fragments.PermissionsDialogFragment;
import jp.co.miosys.aitec.kurento.managers.PeersManager;
import jp.co.miosys.aitec.kurento.tasks.WebSocketTask;
import jp.co.miosys.aitec.managers.AppRTCAudioManager;
import jp.co.miosys.aitec.models.Category;
import jp.co.miosys.aitec.models.Contact;
import jp.co.miosys.aitec.models.LocationGPS;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.models.VoiceMemo;
import jp.co.miosys.aitec.utils.ALog;
import jp.co.miosys.aitec.utils.ApiProcesserUtils;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.CustomClick;
import jp.co.miosys.aitec.utils.DialogUtils;
import jp.co.miosys.aitec.utils.DowloadImageAsynTask;
import jp.co.miosys.aitec.utils.DrawPictureDialog;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.ImageZip;
import jp.co.miosys.aitec.utils.KMLHelper;
import jp.co.miosys.aitec.utils.LocationProvide;
import jp.co.miosys.aitec.utils.NoticeDialog;
import jp.co.miosys.aitec.utils.ParseJsonUtils;
import jp.co.miosys.aitec.utils.ReadWriteFileUtils;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.utils.SocketUtils;
import jp.co.miosys.aitec.views.listeners.OnGetApiDataListener;
import jp.co.miosys.aitec.views.listeners.OnGetImageDownloadListener;
import jp.co.miosys.aitec.views.listeners.OnGetImageUrlListener;
import jp.co.miosys.aitec.views.services.ApiProcessService;
import jp.co.miosys.aitec.views.services.OkHttpService;
import jp.co.miosys.aitec.views.services.SaveKMLStateThread;
import jp.co.miosys.aitec.views.widgets.CaptureVideoRenderer;
import jp.co.miosys.aitec.views.widgets.GridLineView;
import jp.co.miosys.aitec.views.widgets.ProgressDialog;
import okhttp3.Call;
import okhttp3.Response;

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.arrayMemo;
import static jp.co.miosys.aitec.utils.Globals.currentLocation;
import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.Globals.locations;
import static jp.co.miosys.aitec.utils.KMLHelper.CAPTURE_IMAGE;
import static jp.co.miosys.aitec.utils.KMLHelper.END_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.SEND_IMAGE;
import static jp.co.miosys.aitec.utils.KMLHelper.START_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.START_RECORD;
import static jp.co.miosys.aitec.utils.KMLHelper.VOICE_MEMO;


public class VideoConferenceActivity extends BaseActivity implements
        SocketUtils.OnMessageReceive,
        CaptureVideoRenderer.CapturePictureListener,
        OnGetImageDownloadListener,
        OnGetImageUrlListener, LocationProvide.OnUpdateLocation,
        SensorEventListener, CustomClick.OnClickListener, OnGetApiDataListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private static final int MY_PERMISSIONS_REQUEST = 102;

//    private VideoRenderer remoteRenderer;
//    private VideoRenderer biggestRenderer;


    private PeersManager peersManager;
    private WebSocketTask webSocketTask;
    private AppRTCAudioManager audioManager = null;

    LinearLayout views_container;
    Button start_finish_call;
    //SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer biggestVideoView;
    private CaptureVideoRenderer biggestRenderer;
    private RemoteParticipant biggestParticipant;
    // TextView main_participant;
    RecyclerView listParticipant;
    private String name, roomId, recordId = "";
    private ArrayList<Contact> participants = new ArrayList<>();
    private ParticipantAdapter adapter;
    private Button btnEnableVideo, btnEnableAudio;
    private FloatingActionButton btnMemo;
    private boolean firstPartner = false, isInRoom = false , isMemo = false;
    private Spinner spnResolution;
    private Context mContext;
    private TextView txtWaiting, txtMagnifiedName, txtNoParticipant, btnGallery;
    private LocationProvide locationProvide;
    private ArrayList<RemoteParticipant> listRemoteRender = new ArrayList<>();
    private String listUser;
    private boolean isBackCameraClient = true, enableGridLine = false; // [20190910 Stmfko] Disable grid view when start call
    private ImageView btnGridLine;
    private GridLineView layoutGrid;
    private Button btnCapture, btnSwitchCamera;
    private String name_client = "My Camera";
    private ProgressDialog dialog;
    private SensorManager sensorManager;
    private ArrayList<String> listRemoteParticipants = new ArrayList<>();
    private ArrayList<String> listInviteParticipants = new ArrayList<>();
    private Location locRecording;
    private SaveKMLStateThread saveKMLStateThread;
    private CustomClick customClick;
    private EglBase rootEglBase;
    private WaveView waveView;
    private DateTime startTime, endTime, stopRecording;
    private ArrayList<VoiceMemo> arrayVoiceMemo;
    private Location currentLocation;
    private ArrayList<Category> listCategory;
    private Category currentCategory;
    private TextView txtCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_conference_call);
        new ApiProcessService().getMemoCategories(this, this, Globals.REQUEST_API_102);
        kmlHelper = new KMLHelper();
        kmlHelper.createGPXTrack();
        kmlHelper.addMarker(START_CALL, "https://png.pngtree.com/svg/20170818/allow_call_435496.png");
        kmlHelper.addMarker(END_CALL, "http://endat.org/wp-content/uploads/2017/09/phone-icon-red.png");
        kmlHelper.addMarker(SEND_IMAGE, " https://www.svgimages.com/svg-image/s5/send-file-256x256.png");
        kmlHelper.addMarker(START_RECORD, "http://icons.iconarchive.com/icons/martz90/circle/256/video-camera-icon.png");
        kmlHelper.addMarker(CAPTURE_IMAGE, "https://www.keypointintelligence.com/img/advisoryIcons/consumer.png");
        kmlHelper.addMarker(VOICE_MEMO, "https://cdn6.aptoide.com/imgs/0/a/5/0a547b9ae308e86c64566f1475384d80_icon.png");
        mContext = this;
        currentLocation = null;
        locationProvide = new LocationProvide(this, this);
        locationProvide.startUpdatesButtonHandler();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        askForPermissions();
        spnResolution = (Spinner) findViewById(R.id.spn_resolution);
        views_container = (LinearLayout) findViewById(R.id.views_container);
        start_finish_call = (Button) findViewById(R.id.start_finish_call);
        btnEnableVideo = (Button) findViewById(R.id.btn_video);
        btnEnableAudio = (Button) findViewById(R.id.btn_voice);
        txtCategory = (TextView) findViewById(R.id.txt_category);
        biggestVideoView = (SurfaceViewRenderer) findViewById(R.id.biggest_gl_surface_view);
        //localVideoView = (SurfaceViewRenderer) findViewById(R.id.local_gl_surface_view);
        //main_participant = (TextView) findViewById(R.id.main_participant);
        //peer_container = (FrameLayout) findViewById(R.id.peer_container);
        txtWaiting = (TextView) findViewById(R.id.txt_waiting);
        btnGridLine = (ImageView) findViewById(R.id.btn_grid_line);
        btnGridLine.setImageResource(R.mipmap.btn_grid_off); // [20190910 Stmfko] Disable grid view when start call
        layoutGrid = (GridLineView) findViewById(R.id.layout_grid);
        layoutGrid.setAlpha(0f); // [20190910 Stmfko] Disable grid view when start call
        btnCapture = (Button) findViewById(R.id.btn_capture);
        btnSwitchCamera = (Button) findViewById(R.id.btn_switch_camera);
        txtMagnifiedName = (TextView) findViewById(R.id.txt_user_name);
        txtNoParticipant = (TextView) findViewById(R.id.txt_no_participant);
        btnMemo = (FloatingActionButton) findViewById(R.id.btn_memo);
        waveView = (WaveView) findViewById(R.id.wave_view);
        waveView.setAlpha(0f);
        dialog = (ProgressDialog) findViewById(R.id.progress_dialog);
        dialog.setAlpha(0f);

        if (getIntent().hasExtra("guest")) {
            listInviteParticipants.add(getIntent().getStringExtra("guest"));
        }else {
            // if after 20s, calling is not start. display notice
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (txtWaiting.getVisibility() == View.VISIBLE){
                        reportError(getString(R.string.connection_error_establish));
                    }
                }
            },20000);
        }
        //layoutParticipants = (LinearLayout) findViewById(R.id.linearLayout2);
        CommonUtils.getSizeScreen(this);
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutPeer.getLayoutParams();
//        params.width = Globals.WIDTH_SCREEN;
//        layoutPeer.setLayoutParams(params);
//
//        params = (RelativeLayout.LayoutParams) layoutFinishCall.getLayoutParams();
//        params.width = Globals.WIDTH_SCREEN;
//        layoutFinishCall.setLayoutParams(params);
//
//        params = (RelativeLayout.LayoutParams) layoutParticipants.getLayoutParams();
//        params.width = Globals.WIDTH_SCREEN;
//        layoutParticipants.setLayoutParams(params);

        ViewTreeObserver vto = biggestVideoView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    biggestVideoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    biggestVideoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int width = biggestVideoView.getMeasuredWidth();
                int height = biggestVideoView.getMeasuredHeight();
                layoutGrid.setGridSize(width, height);

            }
        });
        HorizontalScrollView.LayoutParams lpv = (HorizontalScrollView.LayoutParams) views_container.getLayoutParams();
        lpv.height = CommonUtils.convertDpToPx(158, this);
        views_container.setLayoutParams(lpv);

        ViewGroup.LayoutParams params = txtNoParticipant.getLayoutParams();
        params.height = CommonUtils.convertDpToPx(158, this);
        txtNoParticipant.setLayoutParams(params);

//        params = (RelativeLayout.LayoutParams) layoutMagnified.getLayoutParams();
//        params.width = Globals.WIDTH_SCREEN;
//        params.bottomMargin = CommonUtils.convertDpToPx(160, this);
//        layoutMagnified.setLayoutParams(params);
//
//
//        params = (RelativeLayout.LayoutParams) biggestVideoView.getLayoutParams();
//        params.width = Globals.WIDTH_SCREEN;
//        params.bottomMargin = CommonUtils.convertDpToPx(160, this);
//        biggestVideoView.setLayoutParams(params);

//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) peer_container.getLayoutParams();
//        lp.height = CommonUtils.convertDpToPx(154, this);
//        lp.width = CommonUtils.convertDpToPx(124, this);
//        lp.setMargins(10, 5, 10, 5);
//        peer_container.setLayoutParams(lp);

//        FrameLayout.LayoutParams lpVideo = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
//        lpVideo.height = CommonUtils.convertDpToPx(150, this);
//        lpVideo.width = CommonUtils.convertDpToPx(120, this);
//        localVideoView.setLayoutParams(lpVideo);
        biggestRenderer = new CaptureVideoRenderer(VideoConferenceActivity.this, VideoConferenceActivity.this);
        biggestVideoView.setMirror(false);
        rootEglBase = EglBase.create();
        biggestVideoView.init(rootEglBase.getEglBaseContext(), null);
        biggestVideoView.setEnableHardwareScaler(true);
        biggestVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        biggestRenderer.setTarget(biggestVideoView);

        arrayVoiceMemo = new ArrayList<>();
        this.peersManager = new PeersManager(this, views_container, addViewPeer(name_client));

        name = getIntent().getStringExtra("Name");
        roomId = getIntent().getStringExtra("room");

        CommonUtils.createLogFolder();
        getUserOnlineApi();
        listParticipant = (RecyclerView) findViewById(R.id.list_participate);
        listParticipant.bringToFront();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listParticipant.setLayoutManager(linearLayoutManager);
        adapter = new ParticipantAdapter(this, participants);
        listParticipant.setAdapter(adapter);
        adapter.setOnItemClickListener(new ParticipantAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                if (participants.get(position).getState() == 2 || participants.get(position).getState() == 3 ) {
                    Toast.makeText(VideoConferenceActivity.this,  participants.get(position).getName() + "は通話中です。", Toast.LENGTH_SHORT).show();
                    return;
                }
                participants.get(position).setState(2);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", Globals.FUNCTION_CONFERENCE_INVITATION);
                    jsonObject.put("host", Globals.name_client);
                    jsonObject.put("receive", participants.get(position).getName());
                    jsonObject.put("name", name);
                    jsonObject.put("room", roomId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String sendMessage = jsonObject.toString();
                socketUtils.sendMessage(sendMessage);
                listInviteParticipants.add(participants.get(position).getName());
            }
        });
        start();

        final ArrayList<String> categories = new ArrayList<String>();
        categories.add("360 x 240");
        categories.add("640 x 480");
        categories.add("1280 x 720");
        categories.add("1920 x 1080");
//        categories.add("2560 x 1440");
//        categories.add("3840 x 2160");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        spnResolution.setAdapter(dataAdapter);
        spnResolution.setSelection(0);  // [20190910 Stmfko] Change max resolution: Default as max
        spnResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String resolution = categories.get(position).replaceAll("\\s+", "");
                int width = Integer.valueOf(resolution.split("x")[0]);
                int height = Integer.valueOf(resolution.split("x")[1]);
                peersManager.changeCaptureFormat(width, height, 30);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        listUser = Globals.name_client;
        kmlHelper.setKMLFileName(roomId);
        synchronized (Globals.startThread) {
            if (!Globals.startThread) {
                saveKMLStateThread = new SaveKMLStateThread(this, roomId);
                saveKMLStateThread.start();
                Globals.startThread = true;
            }
        }
        btnGallery = (TextView) findViewById(R.id.btn_next);
        customClick = new CustomClick(this);
        customClick.setView(btnGallery);
        customClick.setView(btnCapture);
    }

    private void reportError(String errorMessage) {
        if (!isFinishing() && !isDestroyed()) {
            NoticeDialog dialog = new NoticeDialog();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Globals.BUNDLE, errorMessage);
            bundle.putString(Globals.BUNDLE_TITLE, getString(R.string.connection_error_title));
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
            dialog.setOnCallBack(new NoticeDialog.SelectionCallBackListener() {
                @Override
                public void onPositive() {
                    hangup();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                btnGallery.setEnabled(false);
                btnGallery.setClickable(false);
                goToImageGallery();
                break;
            case R.id.btn_capture:
                capturePicture();
                break;
        }
    }

    private void capturePicture() {
        String listUser = "";
        for (String remote : listRemoteParticipants)
            listUser = remote + "__";
        if (!listUser.equals(""))
            listUser = listUser.substring(0, listUser.length() - 2);
        kmlHelper.addWayPointWithMarker(CAPTURE_IMAGE, "Capture Image", listUser, "Conference", null, currentLocation, roomId, null);
        biggestRenderer.captureImage();
        dialog.setAlpha(1f);
    }

    @Override
    public CaptureVideoRenderer addViewPeer(String name) {
        View rowView = getLayoutInflater().inflate(R.layout.peer_video, null);
        int rowId = View.generateViewId();
        rowView.setId(rowId);
        views_container.addView(rowView);
        SurfaceViewRenderer videoView = (SurfaceViewRenderer) ((ViewGroup) rowView).getChildAt(0);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowView.getLayoutParams();
        lp.height = CommonUtils.convertDpToPx(154, mContext);
        lp.width = CommonUtils.convertDpToPx(124, mContext);
        lp.setMargins(10, 5, 10, 5);
        rowView.setLayoutParams(lp);

        FrameLayout.LayoutParams lpVideo = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        lpVideo.height = CommonUtils.convertDpToPx(150, mContext);
        lpVideo.width = CommonUtils.convertDpToPx(120, mContext);
        videoView.setLayoutParams(lpVideo);

        videoView.init(rootEglBase.getEglBaseContext(), null);
        videoView.setZOrderMediaOverlay(true);
        videoView.setEnableHardwareScaler(true);
        videoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        videoView.setMirror(false);

        TextView textView = (TextView) rowView.findViewById(R.id.main_participant);
        textView.setText(name);
        //participant.setParticipantNameText(textView);

        ImageView imgVolume = (ImageView) rowView.findViewById(R.id.img_volume);


        CaptureVideoRenderer renderer = new CaptureVideoRenderer(this, this);
        renderer.setTarget(videoView);

        //sendData("Test");
        if (!isBackCameraClient) {
            if (name.equals(name_client))
                videoView.setMirror(true);
        }


//        currentParticipate = new RemoteParticipant(remoteParticipant);
//        rowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateViewWithGridLine(oldParticipate);
//                showLargeVideoView(oldParticipate);
//            }
//        });
        //setMirror();

        RemoteParticipant participant = new RemoteParticipant();
        participant.setParticipantNameText(textView);
        participant.setName(name);
        participant.setVideoView(renderer);
        participant.setView(videoView);
        participant.setRoot(rowView);
        participant.setImgVolume(imgVolume);
        participant.setMute(false);
        setRemoteParticipantName(name, participant);
        if (!name.equals(name_client)) {
            listRemoteParticipants.add(name);
            TextView txtWaiting = (TextView) rowView.findViewById(R.id.txt_waiting);
            txtWaiting.setVisibility(View.VISIBLE);
            participant.setTxtWaiting(txtWaiting);
        }
        //participant.setView(peer_container);
        listRemoteRender.add(participant);

        return renderer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SocketUtils.isCloseSocket) {
            closeActivity();
        }
        btnGallery.setEnabled(true);
        btnGallery.setClickable(true);
        socketUtils.setCallBack(this, this);
        DowloadImageAsynTask.getImageDowloadComplete(this);
        ParseJsonUtils utils = new ParseJsonUtils();
        utils.getImageUrl(this);
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void onAudioManagerDevicesChanged(final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        //ALog.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", " + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Api get all users who is online
    public void getUserOnlineApi() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_DISCOVERY);
            jsonObject.put("regId", Globals.id_client);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
    }


    @Override
    public LinearLayout getViewsContainer() {
        return views_container;
    }

    public void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }


    public void start() {
        if (arePermissionGranted()) {
            peersManager.start();
//            peer_container.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    updateViewWithGridLine(localParticipant);
//                    showLargeVideoView(localParticipant);
//                }
//            });
            createLocalSocket();
        } else {
            DialogFragment permissionsFragment = new PermissionsDialogFragment();
            permissionsFragment.show(getSupportFragmentManager(), "Permissions Fragment");
        }
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        //ALog.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
        for (RemoteParticipant participant : listRemoteRender)
            if (participant.getName().equals(name_client))
                participant.setMediaStream(peersManager.getLocalAudioTrack());
    }

    public void hangUp(View view) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("確認");
        alertDialogBuilder.setMessage("通話を終了しても宜しいですか？")
                .setCancelable(false)
                .setPositiveButton("はい",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                if (listRemoteParticipants.size() == 1) {
                                    saveRecording(true);
                                    hangup();
                                } else {
                                    if (listUser.equals(Globals.name_client)) {
                                        saveRecording(false);
                                        hangup();
                                    } else {
                                        //getRecordingId();
                                        recordId = Globals.HOST + "/recordings/" + roomId + "/" + roomId + ".mp4";
                                        //sendKmlToServer(recordId);
                                        sendCallDetailToServer(recordId, null);
                                        hangup();
                                    }
                                }
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

    private boolean arePermissionGranted() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED);
    }


    public void createLocalSocket() {
        getToken();
    }

    private void getToken() {
        final String sessionName = roomId;
        final String participantName = name;
        final String socketAddress = Globals.HOST;
        String param = "{\"session\":\"" + sessionName + "\"}";
        JSONObject json = null;
        try {
            json = new JSONObject(param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new OkHttpService(OkHttpService.Method.POST, true, this, Globals.TOKEN_URL, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                Log.e("Loi", e.getMessage());
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                String token = "";
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    token = (String) jsonObject.get("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocketTask = (WebSocketTask) new WebSocketTask(VideoConferenceActivity.this, peersManager, sessionName, participantName, socketAddress, token).execute(VideoConferenceActivity.this);
            }
        };

    }

    private void getSessionTime() {
        new OkHttpService(OkHttpService.Method.GET, true, this, Globals.SESSION_URL + "/" + roomId, null, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                //Log.e("Loi", e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DateTimeFormatter fmt = DateTimeFormat.forPattern(Globals.timeZoneFormatter);
                        if (currentLocation != null)
                            kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", "", "Conference", null, currentLocation, roomId, fmt.print(new DateTime()));
                    }
                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                final String timeCreated;
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    long createdAt = jsonObject.getLong("createdAt");
                    DateTime someDate = new DateTime(createdAt);
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(Globals.timeZoneFormatter);
                    timeCreated = fmt.print(someDate);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (currentLocation != null)
                                kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", "", "Conference", null, currentLocation, roomId, timeCreated);
                        }
                    });
                    int a = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int a = 1;
            }
        };

    }

    @Override
    public void gotRemoteStream(final MediaStream stream, final String remoteParticipant) {
        if (!isInRoom) {
            isInRoom = true;
            getSessionTime();
        }
        final VideoTrack videoTrack = stream.videoTracks.getFirst();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtNoParticipant.setVisibility(View.GONE);
                listUser = listUser + "__" + remoteParticipant;

                // Check if current device is mute state
                if (!peersManager.isRenderAudio())
                    stream.audioTracks.getFirst().setEnabled(false);

                if (!firstPartner) {
                    biggestParticipant = new RemoteParticipant();
                    biggestParticipant.setName(remoteParticipant);
                    biggestParticipant.setMediaStream(stream.audioTracks.getFirst());
                    biggestParticipant.setMute(false);
                    biggestVideoView.setVisibility(View.VISIBLE);
                    videoTrack.addRenderer(new VideoRenderer(biggestRenderer));
                    firstPartner = true;
                    txtMagnifiedName.setText(remoteParticipant);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnEnableAudio.setVisibility(View.VISIBLE);
                            txtWaiting.setVisibility(View.GONE);
                            btnMemo.setVisibility(View.VISIBLE);
                        }
                    }, 8000);
                    txtWaiting.setVisibility(View.GONE);
                    if (currentLocation != null)
                        locRecording = new Location(currentLocation);
                    for (int i = 0; i < listRemoteRender.size(); i++) {
                        final RemoteParticipant participant = listRemoteRender.get(i);
                        if (participant.getName().equals(name_client)) {
                            participant.getView().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // mute
                                    boolean mute = participant.isMute();
                                    participant.setMute(biggestParticipant.isMute());
                                    biggestParticipant.setMute(mute);
                                    updateViewWithGridLine(participant.getName(), participant);

                                    String biggestName = biggestParticipant.getName();
                                    biggestParticipant.setName(participant.getName());
                                    participant.setName(biggestName);

                                    // change video view
                                    CaptureVideoRenderer renderer = biggestRenderer;
                                    biggestRenderer.setTarget(null);
                                    biggestRenderer = participant.getVideoView();
                                    participant.getVideoView().setTarget(null);
                                    participant.setVideoView(renderer);
                                    participant.getVideoView().setTarget(participant.getView());


                                    // change media stream
                                    AudioTrack mediaStream = biggestParticipant.getMediaStream();
                                    //biggestParticipant.getMediaStream().dispose();
                                    biggestParticipant.setMediaStream(participant.getMediaStream());
                                    // participant.getMediaStream().dispose();
                                    participant.setMediaStream(mediaStream);
                                    biggestRenderer.setTarget(biggestVideoView);


                                    String displayBiggestName = txtMagnifiedName.getText().toString();

                                    txtMagnifiedName.setText(participant.getParticipantNameText().getText().toString());
                                    participant.getParticipantNameText().setText(displayBiggestName);


                                    setMirror(biggestVideoView, biggestParticipant.getName());
                                    setMirror(participant.getView(), participant.getName());

                                }
                            });
                        }
                    }
                } else {
                    for (int i = 0; i < listRemoteRender.size(); i++) {
                        final RemoteParticipant participant = listRemoteRender.get(i);
                        if (participant.getName().equals(remoteParticipant)) {
                            videoTrack.addRenderer(new VideoRenderer(participant.getVideoView()));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (participant.getTxtWaiting() != null)
                                        participant.getTxtWaiting().setVisibility(View.GONE);
                                }
                            }, 8000);
                            participant.setName(remoteParticipant);
                            participant.setMediaStream(stream.audioTracks.getFirst());
                            participant.setMute(false);
                            participant.getView().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // mute
                                    boolean mute = participant.isMute();
                                    participant.setMute(biggestParticipant.isMute());
                                    biggestParticipant.setMute(mute);
                                    updateViewWithGridLine(participant.getName(), participant);

                                    String biggestName = biggestParticipant.getName();
                                    biggestParticipant.setName(participant.getName());
                                    participant.setName(biggestName);

                                    // change video view
                                    CaptureVideoRenderer renderer = biggestRenderer;
                                    biggestRenderer.setTarget(null);
                                    biggestRenderer = participant.getVideoView();
                                    participant.getVideoView().setTarget(null);
                                    participant.setVideoView(renderer);
                                    participant.getVideoView().setTarget(participant.getView());
                                    biggestRenderer.setTarget(biggestVideoView);

                                    // change media stream
                                    AudioTrack mediaStream = biggestParticipant.getMediaStream();
                                    //biggestParticipant.getMediaStream().dispose();
                                    biggestParticipant.setMediaStream(participant.getMediaStream());
                                    // participant.getMediaStream().dispose();
                                    participant.setMediaStream(mediaStream);


                                    String displayBiggestName = txtMagnifiedName.getText().toString();

                                    txtMagnifiedName.setText(participant.getParticipantNameText().getText().toString());
                                    participant.getParticipantNameText().setText(displayBiggestName);


                                    setMirror(biggestVideoView, biggestParticipant.getName());
                                    setMirror(participant.getView(), participant.getName());
                                }
                            });
                        }
                    }
                }
            }
        });
    }

//    private void showLargeVideoView(RemoteParticipant remoteParticipant) {
//        if (currentParticipate == null)
//            return;
//        if (biggestVideoTrack != null)
//            biggestVideoTrack.removeRenderer(biggestRenderer);
//        biggestVideoView.setMirror(false);
//        biggestVideoTrack = remoteParticipant.getVideoTrack();
//        biggestVideoView.setVisibility(View.VISIBLE);
//        biggestRenderer = new VideoRenderer(biggestVideoView);
//        biggestVideoTrack.addRenderer(biggestRenderer);
//        txtMagnifiedName.setText(remoteParticipant.getParticipantNameText().getText().toString());
//        if (!remoteParticipant.getParticipantNameText().getText().toString().equals(name_client)) {
//            for (Contact contact : participants)
//                if (contact.getName().equals(remoteParticipant.getParticipantNameText().getText().toString()))
//                    Globals.id_guest = contact.getId();
//        }
//
//        views_container.removeView(remoteParticipant.getView());
//
//        final RemoteParticipant oldParticipate = new RemoteParticipant(currentParticipate);
//
//        View rowView = getLayoutInflater().inflate(R.layout.peer_video, null);
//        int rowId = View.generateViewId();
//        rowView.setId(rowId);
//        views_container.addView(rowView);
//        SurfaceViewRenderer videoView = (SurfaceViewRenderer) ((ViewGroup) rowView).getChildAt(0);
//
//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowView.getLayoutParams();
//        lp.height = CommonUtils.convertDpToPx(154, mContext);
//        lp.width = CommonUtils.convertDpToPx(124, mContext);
//        lp.setMargins(10, 5, 10, 5);
//        rowView.setLayoutParams(lp);
//
//        FrameLayout.LayoutParams lpVideo = (FrameLayout.LayoutParams) videoView.getLayoutParams();
//        lpVideo.height = CommonUtils.convertDpToPx(150, mContext);
//        lpVideo.width = CommonUtils.convertDpToPx(120, mContext);
//        videoView.setLayoutParams(lpVideo);
//
//        videoView.setMirror(false);
//        EglBase rootEglBase = EglBase.create();
//        videoView.init(rootEglBase.getEglBaseContext(), null);
//        videoView.setZOrderMediaOverlay(true);
//        TextView textView = (TextView) rowView.findViewById(R.id.main_participant);
//        String name = oldParticipate.getParticipantNameText().getText().toString();
//        oldParticipate.setParticipantNameText(textView);
//        //textView.setText(oldParticipate.getParticipantNameText().toString());
//        setRemoteParticipantName(name, oldParticipate);
//
//        oldParticipate.setView(rowView);
//
//        VideoRenderer renderer = new VideoRenderer(videoView);
//        if (oldParticipate.getVideoTrack() != null)
//            oldParticipate.getVideoTrack().addRenderer(renderer);
//        //sendData("Test");
//        if (!isBackCameraClient) {
//            if (oldParticipate.getParticipantNameText().getText().toString().equals(name_client))
//                videoView.setMirror(true);
//        } else {
//            if (oldParticipate.getParticipantNameText().getText().toString().equals(name_client))
//                videoView.setMirror(true);
//        }
//        if (currentParticipate.getParticipantNameText().getText().toString().equals(name_client)) {
//            localParticipant = new RemoteParticipant(currentParticipate);
//            localParticipant.setVideoView(videoView);
//        }
//        currentParticipate = new RemoteParticipant(remoteParticipant);
//        rowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateViewWithGridLine(oldParticipate);
//                showLargeVideoView(oldParticipate);
//            }
//        });
//        setMirror();
//    }

    public void sendData(final String data) {

        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        peersManager.getDataChanel().send(new DataChannel.Buffer(buffer, false));


    }

    @Override
    public void setRemoteParticipantName(String name, RemoteParticipant remoteParticipant) {
        remoteParticipant.setName(name);
        remoteParticipant.getParticipantNameText().setText(name);
        remoteParticipant.getParticipantNameText().setPadding(20, 3, 20, 3);
    }

    @Override
    public void hangup() {
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        biggestRenderer.setTarget(null);
        if (biggestVideoView != null) {
            biggestVideoView.release();
            biggestVideoView = null;
        }

        for (RemoteParticipant participant : listRemoteRender) {
            participant.getVideoView().setTarget(null);
            participant.getView().release();
        }

        webSocketTask.setCancelled(true);

        peersManager.hangup(listUser.equals(Globals.name_client));

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_END_CALL);
            jsonObject.put("host", Globals.name_client);
            jsonObject.put("receive", "-1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);

        if (rootEglBase != null) {
            rootEglBase.release();
            rootEglBase = null;
        }
        sendEndCallToUserRinging();
        if (!this.isDestroyed()) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }


    private void sendEndCallToUserRinging() {
        for (String user : listInviteParticipants) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("host", Globals.name_client);
                jsonObject.put("receive", user);
                jsonObject.put("type", Globals.FUNCTION_ANSWER);
                jsonObject.put("result", "reject");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        }
    }

    @Override
    protected void onDestroy() {
        hangup();
        locationProvide.stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onStop() {
        //hangup();
        super.onStop();
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject result = new JSONObject(message);
            String function = result.getString("type");
            // Get all user online
            if (function.equals(Globals.FUNCTION_DISCOVERY)) {
                boolean isSelect = false;
                participants.clear();
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

                    if (!regId.equals(Globals.id_client)) {
                        boolean userInConference = false;
                        for (String remote : listRemoteParticipants)
                            if (remote.equals(name)) {
                                userInConference = true;
                                participants.add(new Contact(regId, name, status, false));
                                break;
                            }
                        if (!userInConference)
                            participants.add(new Contact(regId, name, status, false));
                    }
                }
                adapter.notifyDataSetChanged();
            } else if (function.equals(Globals.FUNCTION_SEND_IMAGE_URL)) {
                final String imageUrl = result.getString("url");
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        String url = imageUrl;
                        DowloadImageAsynTask asynTask = new DowloadImageAsynTask(url, mContext);
                        asynTask.execute();
                    }
                });
            } else if (function.equals(Globals.FUNCTION_ERROR_CONNECT)) {
                //closeActivity();
                if (socketUtils != null) {
                    socketUtils.release();
                    socketUtils = null;
                    socketUtils = new SocketUtils(mContext, VideoConferenceActivity.this);
                    socketUtils.connect();
                }
            } else if (function.equals("connect")) {
                // re-login
                String mUser = new SharePreference(mContext).getLogin()[0];
                String mPass = new SharePreference(mContext).getLogin()[1];
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", Globals.FUNCTION_LOGIN);
                    jsonObject.put("regId", Globals.id_client);
                    jsonObject.put("name", mUser);
                    jsonObject.put("password", mPass);
                    String sendMessage = jsonObject.toString();
                    socketUtils.sendMessage(sendMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (function.equals(Globals.FUNCTION_LOGIN)) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", Globals.FUNCTION_CHANGE_STATUS);
                    jsonObject.put("host", Globals.name_client);
                    jsonObject.put("state", 2);
                    String sendMessage = jsonObject.toString();
                    socketUtils.sendMessage(sendMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (function.equals(Globals.FUNCTION_IS_VIEW_IMAGE)) {
                boolean data = result.getBoolean("data");
                if (data) {
                    Globals.isPatnerViewImage = true;
                    //txtViewImage.setVisibility(View.VISIBLE);
                } else {
                    Globals.isPatnerViewImage = false;
                    //txtViewImage.setVisibility(View.GONE);
                }
            } else if (function.equals(Globals.FUNCTION_CHANGE_CAMERA)) {
                if (biggestParticipant.getName().equals(result.getString("data"))) {
                    if (result.getInt("camera") == 0)
                        txtMagnifiedName.setText("鏡像");
                    else
                        txtMagnifiedName.setText(result.getString("data"));
                } else {
                    for (RemoteParticipant participant : listRemoteRender) {
                        if (participant.getName().equals(result.getString("data")))
                            if (result.getInt("camera") == 0)
                                participant.getParticipantNameText().setText("鏡像");
                            else
                                participant.getParticipantNameText().setText(result.getString("data"));
                    }
                }

            } else if (function.equals(Globals.FUNCTION_CONFERENCE_CONFIRM)) {

                if (result.getString("result").equals("deny")) {
                    String name = result.getString("host");
                    Toast.makeText(mContext, name + "は会話への参加をキャンセルしました。", Toast.LENGTH_SHORT).show();
                    if (name.equals(listInviteParticipants.get(0)))
                        hangUpWithIntent(name);
                }
                listInviteParticipants.remove(result.getString("host"));
            } else if (function.equals(Globals.FUNCTION_MUTE)) {
                if (txtMagnifiedName.getText().toString().equals(result.getString("data"))) {
                    if (txtMagnifiedName.getText().toString().equals(name_client))
                        btnEnableAudio.setBackgroundResource(result.getBoolean("status") ? R.mipmap.voice_on : R.mipmap.voice_off);
                    else
                        btnEnableAudio.setBackgroundResource(result.getBoolean("status") ? R.drawable.ic_volume : R.drawable.ic_mute);
                    biggestParticipant.setMute(!result.getBoolean("status"));
                } else {
                    for (RemoteParticipant participant : listRemoteRender) {
                        if (participant.getName().equals(result.getString("data")))
                            participant.setMute(!result.getBoolean("status"));
                    }
                }

            }
        } catch (Exception e) {
//            ALog.e("Error", e.toString());
        }
    }

    private void hangUpWithIntent(String name) {
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        biggestRenderer.setTarget(null);
        if (biggestVideoView != null) {
            biggestVideoView.release();
            biggestVideoView = null;
        }

        for (RemoteParticipant participant : listRemoteRender) {
            participant.getVideoView().setTarget(null);
            participant.getView().release();
        }

        webSocketTask.setCancelled(true);

        peersManager.hangup(listUser.equals(Globals.name_client));

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_END_CALL);
            jsonObject.put("host", Globals.name_client);
            jsonObject.put("receive", "-1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);

        if (rootEglBase != null) {
            rootEglBase.release();
            rootEglBase = null;
        }
        sendEndCallToUserRinging();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",name);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    private void closeActivity() {
        Intent intent = new Intent(VideoConferenceActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void switchCamera(View view) {
        peersManager.switchCamera();
        isBackCameraClient = !isBackCameraClient;
        setMirror(biggestVideoView, name_client);

        if (!isBackCameraClient)
            txtMagnifiedName.setText("鏡像");
        else
            txtMagnifiedName.setText(name_client);

//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("type", Globals.FUNCTION_CHANGE_CAMERA);
//            jsonObject.put("users", new JSONArray(listRemoteParticipants));
//            jsonObject.put("host", Globals.name_client);
//            jsonObject.put("camera", isBackCameraClient ? 1 : 0);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String sendMessage = jsonObject.toString();
//        socketUtils.sendMessage(sendMessage);
    }

    private void sendCallDetailToServer(final String recordedUrl, final String imageUrl) {
        JSONObject json = new JSONObject();
        try {
            json.put("talk_id", roomId);
            json.put("sender_id", Globals.name_client);
            json.put("password", new SharePreference(this).getLogin()[1]);
            json.put("group_user_ids", listUser);
            json.put("receiver_ids", listUser);
            DateTimeFormatter fmt = DateTimeFormat.forPattern(Globals.timeZoneFormatter);
            if (stopRecording == null)
                json.put("collection_time", fmt.print(new DateTime()));
            else
                json.put("collection_time", fmt.print(stopRecording));
            if (recordedUrl != null) {
                json.put("link_video", recordedUrl);
                json.put("link_file", recordedUrl.replace("mp4", "jpg"));
            }
            if (imageUrl != null)
                json.put("link_image", imageUrl);
            if (locations.size() > 0) {
                json.put("lat", String.valueOf(locations.get(locations.size() - 1).getLatitude()));
                json.put("lng", String.valueOf(locations.get(locations.size() - 1).getLongitude()));
            }

            JSONArray jsonArray = new JSONArray();
            for (VoiceMemo voiceMemo : arrayVoiceMemo){
                JSONObject objectMemo = new JSONObject();
                objectMemo.put("username",voiceMemo.getUserName());
                objectMemo.put("collection_time",voiceMemo.getCollectionTime());
                objectMemo.put("start_time",voiceMemo.getStartTime());
                objectMemo.put("end_time",voiceMemo.getEndTime());
                objectMemo.put("category_id",voiceMemo.getCategory());
                objectMemo.put("lat",voiceMemo.getLat());
                objectMemo.put("lng",voiceMemo.getLon());
                jsonArray.put(objectMemo);
            }
            json.put("voice_memo",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("API", json.toString());
        new OkHttpService(OkHttpService.Method.POST, false, this, Globals.SAVE_CALL_DETAIL, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String result = response.body().string();
                //Toast.makeText(mContext, result,Toast.LENGTH_LONG).show();

                // send kml if end call
                if (imageUrl == null)
                    sendKmlToServer(recordedUrl);
            }
        };

    }

    private void saveRecording(final boolean sendToServer) {
        new OkHttpService(OkHttpService.Method.POST, true, this, Globals.SAVE_RECORDING_URL + roomId, null, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopRecording = new DateTime();
                        //recordId = "https://157.7.141.150:4443/recordings/" + roomId + ".mp4";
                        recordId = Globals.HOST + "/recordings/" + roomId + "/" + roomId + ".mp4";
                        if (sendToServer) {
                            //sendKmlToServer(recordId);
                            sendCallDetailToServer(recordId, null);
                        }
                    }
                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                String recordedUrl = "";
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    long createAt = jsonObject.getLong("createdAt");
                    double duration = jsonObject.getDouble("duration");
                    long durationMilis = (long) (duration*1000);
                    long endAt = createAt + durationMilis;
                    stopRecording = new DateTime(endAt);
                    recordedUrl = jsonObject.getString("url").equals("null")?Globals.HOST + "/recordings/" + roomId + "/" + roomId + ".mp4" : jsonObject.getString("url");
                } catch (JSONException e) {
                    e.printStackTrace();
                    recordedUrl = Globals.HOST + "/recordings/" + roomId + "/" + roomId + ".mp4";
                }
                if (sendToServer) {
                    //sendKmlToServer(recordedUrl);
                    sendCallDetailToServer(recordedUrl, null);
                } else {
                    try {
                        final String kmlFile = kmlHelper.getKMLFile();
                        synchronized (kmlHelper) {
                            kmlHelper.saveKMLFile(kmlFile);
                        }
                        if (saveKMLStateThread != null)
                            saveKMLStateThread.stop();
                        Globals.startThread = false;
                        new File(kmlFile).delete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ALog.e("Save KML", ex.toString());
                    }

                }
            }
        };

    }

    private void sendKmlToServer(final String recordedUrl) {
        String listUser = "";
        for (String remote : listRemoteParticipants)
            listUser = remote + "__";
        if (!listUser.equals(""))
            listUser = listUser.substring(0, listUser.length() - 2);
        kmlHelper.addWayPointWithMarker(START_RECORD, "Start Record", listUser, "Conference", recordedUrl, locRecording, roomId, null);
        kmlHelper.addWayPointWithMarker(END_CALL, "End Call", listUser, "Conference", null, currentLocation, roomId, null);
        final String kmlFile = kmlHelper.getKMLFile();
        boolean success = false;
        synchronized (kmlHelper) {
            success = kmlHelper.saveKMLFile(kmlFile);
        }
        if (success) {
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();

            Map<String, Object> params = new HashMap<>();
            params.put("upload_file", new File(kmlFile));
            params.put("uuid", uuid);
            params.put("talk_id", roomId);
            params.put("group_user_ids", this.listUser);
            params.put("username", Globals.name_client);
            new OkHttpService(OkHttpService.Method.POST, this, Globals.URL_KML, params, false) {
                @Override
                public void onFailureApi(Call call, Exception e) {
                    synchronized (kmlHelper) {
                        saveKMLStateThread.stop();
                        Globals.startThread = false;
                        kmlHelper.saveKMLFile(kmlFile);
                    }
                }

                @Override
                public void onResponseApi(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    synchronized (kmlHelper) {
                        if (saveKMLStateThread != null)
                            saveKMLStateThread.stop();
                        Globals.startThread = false;
                        new File(kmlFile).delete();
                    }
                    //sendCallDetailToServer(recordedUrl, null);
                }
            };
        }
    }

    private void startRecording() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session", roomId);
            //jsonObject.put("outputMode","INDIVIDUAL");
            //jsonObject.put("resolution","1280x720");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new OkHttpService(OkHttpService.Method.POST, true, this, Globals.START_RECORDING_URL, jsonObject, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                Log.e("Loi", e.getMessage());
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                String recordedUrl = "";
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    recordedUrl = (String) jsonObject.get("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

    }

    private void getRecordingId() {

        new OkHttpService(OkHttpService.Method.GET, true, this, Globals.GET_RECORDING_URL, null, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                Log.e("Loi", e.getMessage());
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray items = jsonObject.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        if (item.getString("sessionId").equals(roomId)) {
                            recordId = item.getString("url");
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (recordId.equals("") || recordId.equals("null"))
                    recordId = Globals.HOST + "/recordings/" + roomId + "/" + roomId + ".mp4";
                sendKmlToServer(recordId);
            }
        };

    }

    public void setMirror(SurfaceViewRenderer renderer, String name) {
        if (renderer == null)
            return;
        if (!isBackCameraClient) {
            if (name.equals(name_client))
                renderer.setMirror(true);
            else
                renderer.setMirror(false);
        } else {
            if (name.equals(name_client))
                renderer.setMirror(false);
        }
    }

    public void enableVideo(View view) {
        boolean enabled = !peersManager.isRenderVideo();
        btnEnableVideo.setBackgroundResource(enabled ? R.mipmap.camera_on : R.mipmap.camera_off);
        peersManager.setVideoEnabled();
        if (enabled) {
            btnCapture.setText(getString(R.string.call_activity_btn_capture));
            btnCapture.setClickable(true);
            btnCapture.setEnabled(true);
        }else {
            btnCapture.setText(getString(R.string.call_activity_btn_capture_disable));
            btnCapture.setClickable(false);
            btnCapture.setEnabled(false);
        }
    }

    public void enableAudio(View view) {
        boolean enabled = !peersManager.isRenderAudio();
        btnEnableAudio.setBackgroundResource(enabled ? R.mipmap.voice_on : R.mipmap.voice_off);
        peersManager.setAudioEnabled();
        if (biggestParticipant.getName().equals(name_client))
            biggestParticipant.setMute(!enabled);
        for (RemoteParticipant participant : listRemoteRender) {
            if (!participant.getName().equals(name_client)) {
                participant.getMediaStream().setEnabled(enabled);
            }
        }


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_MUTE);
            jsonObject.put("status", enabled);
            jsonObject.put("host", Globals.name_client);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
    }

    @Override
    public boolean isFirstPartner() {
        return firstPartner;
    }

    public boolean setFirstPartner() {
        return firstPartner = true;
    }

    @Override
    public void showWaiting(String name) {
        txtWaiting.setVisibility(View.VISIBLE);
        listRemoteParticipants.add(name);
    }

    @Override
    public void setNewLargeVideo(RemoteParticipant remoteParticipant) {
        if (remoteParticipant == null || remoteParticipant.getName() == null) {
            hangup();
            return;
        }
        if (remoteParticipant.getName().equals(biggestParticipant.getName())) {
            Iterator<RemoteParticipant> itr = listRemoteRender.iterator();
            while (itr.hasNext()) {
                RemoteParticipant participant = itr.next();
                participant.getVideoView().setTarget(null);
                participant.getView().release();
                if (participant.getName().equals(remoteParticipant.getName())) {
                    itr.remove();
                    break;
                }
            }

            final RemoteParticipant newParticipant = listRemoteRender.get(listRemoteRender.size() - 1);

            biggestRenderer.setTarget(null);
            biggestRenderer = newParticipant.getVideoView();
            newParticipant.getVideoView().setTarget(null);

            biggestRenderer.setTarget(biggestVideoView);


            newParticipant.getView().release();
            newParticipant.getView().post(new Runnable() {

                @Override
                public void run() {
                    views_container.removeView(newParticipant.getRoot());
                }
            });

            biggestParticipant.setName(newParticipant.getName());
            biggestParticipant.setMute(newParticipant.isMute());

            txtMagnifiedName.setText(newParticipant.getParticipantNameText().getText().toString());
            setMirror(biggestVideoView, biggestParticipant.getName());
            updateViewWithGridLine(biggestParticipant.getName(), newParticipant);
            for (Contact contact : participants)
                if (contact.getName().equals(newParticipant.getName()))
                    Globals.id_guest = contact.getId();
        } else {
            for (final RemoteParticipant participant : listRemoteRender) {
                if (participant.getName().equals(remoteParticipant.getName())) {
                    participant.getVideoView().setTarget(null);
                    participant.getView().release();
                    participant.getView().post(new Runnable() {

                        @Override
                        public void run() {
                            views_container.removeView(participant.getRoot());
                        }
                    });
                }
            }
            Iterator<RemoteParticipant> itr = listRemoteRender.iterator();
            while (itr.hasNext()) {
                RemoteParticipant participant = itr.next();
                if (participant.getName().equals(remoteParticipant.getName())) {
                    itr.remove();
                    break;
                }
            }
        }
        listRemoteParticipants.remove(remoteParticipant.getName());
        if (listRemoteParticipants.size() <= 0) {
            txtNoParticipant.setVisibility(View.VISIBLE);
            views_container.removeAllViews();
        }

    }

    private void showDialogCapture() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DrawPictureDialog dialog = new DrawPictureDialog();
        //Bitmap overlayBitmap = CommonUtils.drawTimeOnBitmap(captureBitmap);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        Globals.captureBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();

        Bundle bundle = new Bundle();
        //bundle.putByteArray(Globals.BUNDLE_SEND_IMAGE, byteArray);
        bundle.putBoolean(Globals.BUNDLE_SEND_GRID, enableGridLine);
        bundle.putFloat(Globals.BUNDLE_SEND_ANGLE, angle);
        bundle.putStringArray(Globals.BUNDLE_SEND_EXIF, null);
        dialog.setArguments(bundle);
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.show(fragmentManager, "Input Dialog");
        dialog.setOnSendDataListener(new DrawPictureDialog.SendDataListener() {
            @Override
            public void onSend(File imageFile) {
                String kmlFile = kmlHelper.getKMLFile();
                synchronized (kmlHelper) {
                    kmlHelper.saveKMLFile(kmlFile);
                }
                String result = ReadWriteFileUtils.createTempFileDontSend(mContext).getAbsolutePath();
                boolean check = ImageZip.mergeFiles(imageFile.getAbsolutePath(), kmlFile, result);
                if (check) {
                    //peerConnectionClient.sendData(locations);
                    UploadApplication.isUpload = true;
                    ApiProcesserUtils.sendImage(mContext, new File(result), Globals.id_guest, true);
                }else {
                    Toast.makeText(mContext,"Oops, something wrong, please try again",Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.dialog.setAlpha(0f);
    }

    @Override
    public void onCapturePicture(Bitmap picture) {
        if (picture != null) {
            Globals.captureBitmap = picture;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDialogCapture();
                }
            });
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,"Oops, something wrong, please try again",Toast.LENGTH_SHORT).show();
                    dialog.setAlpha(0f);
                }
            });

        }

    }

    // Receive image
    @Override
    public void onGetImageDownload(String path) {
        DialogUtils.receiveImageDialog(mContext);
    }

    @Override
    public void onGetImageUrl(String imageUrl) {
        String listUser = "";
        for (String remote : listRemoteParticipants)
            listUser = remote + "__";
        if (!listUser.equals(""))
            listUser = listUser.substring(0, listUser.length() - 2);
        synchronized (kmlHelper) {
            kmlHelper.addWayPointWithMarker(SEND_IMAGE, "Send Image", listUser, "Conference", imageUrl, currentLocation, roomId, null);
        }
        sendImageUrl(imageUrl);
    }

    private void sendImageUrl(String url) {
        for (String remoteParticipant : listRemoteParticipants) {
            String guestId = remoteParticipant;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", Globals.FUNCTION_SEND_IMAGE_URL);
                jsonObject.put("receive", guestId);
                jsonObject.put("url", url);
                String sendMessage = jsonObject.toString();
                socketUtils.sendMessage(sendMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sendCallDetailToServer(null, url);
    }

    @Override
    public void onStopUpdate() {

    }

    @Override
    public void onUpdate(Location location) {
        if (location == null)
            return;
        if (currentLocation == null)
            kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", "", "Conference", null, location, roomId, null);
        if (locRecording == null)
            locRecording = new Location(location);
        currentLocation = new Location(location);
        locations.add(new LocationGPS(location));
        kmlHelper.addWayPoint(location);
        checkDistanceMemo(location);
    }


    private void checkDistanceMemo(Location currentLocation) {
        for (Memo memo : arrayMemo) {
            double distance = CommonUtils.calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), memo.getLat(), memo.getLon());
            if (distance < Globals.DISTANCE  && (memo.getDistance() > Globals.DISTANCE  || memo.getDistance() == -1)) {
                CommonUtils.showNotification(this, memo);
            }
            memo.setDistance(distance);
        }
    }


    public void enableGridLine(View view) {
        enableGridLine = !enableGridLine;
        if (enableGridLine) {
            btnGridLine.setImageResource(R.mipmap.btn_grid_on);
            layoutGrid.animate().alpha(1);
        } else {
            btnGridLine.setImageResource(R.mipmap.btn_grid_off);
            layoutGrid.animate().alpha(0);
        }
        //updateViewWithGridLine(currentParticipate);
    }

    private void updateViewWithGridLine(String biggestName, RemoteParticipant participant) {
        if (participant.getName().equals(name_client)) {
            btnEnableAudio.setVisibility(View.VISIBLE);
            btnEnableAudio.setClickable(true);
            btnEnableAudio.setEnabled(true);
            btnEnableVideo.setVisibility(View.VISIBLE);
            btnSwitchCamera.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.VISIBLE);
            btnGridLine.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnGridLine.getLayoutParams();
            params.addRule(RelativeLayout.LEFT_OF, R.id.btn_switch_camera);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnGridLine.setLayoutParams(params);
            if (enableGridLine)
                layoutGrid.setAlpha(1f);
            else
                layoutGrid.setAlpha(0f);
            boolean enabled = peersManager.isRenderAudio();
            btnEnableAudio.setBackgroundResource(enabled ? R.mipmap.voice_on : R.mipmap.voice_off);
        } else {
            btnEnableAudio.setVisibility(View.VISIBLE);
            btnEnableVideo.setVisibility(View.INVISIBLE);
            btnEnableAudio.setClickable(false);
            btnEnableAudio.setEnabled(false);
            btnSwitchCamera.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);
            btnGridLine.setVisibility(View.GONE);
            layoutGrid.setAlpha(0f);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnGridLine.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnGridLine.setLayoutParams(params);
            boolean enabled = biggestParticipant.isMute();
            btnEnableAudio.setBackgroundResource(enabled ? R.drawable.ic_mute : R.drawable.ic_volume);
        }
        for (RemoteParticipant p : listRemoteRender)
            if (p.getName().equals(biggestName))
                p.setMute(p.isMute());
    }

    float[] result;
    float angle = 0;
    private float RTmp[] = new float[9];
    private float Rot[] = new float[9];
    private float I[] = new float[9];
    private float grav[] = new float[3];
    private float mag[] = new float[3];
    private float results[] = new float[3];
    protected float[] gravSensorVals = new float[3];
    protected float[] magSensorVals = new float[3];
    private boolean isFlat = false;
    private Display mDisplay;

    @Override
    public void onSensorChanged(SensorEvent evt) {
//        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
////            gravSensorVals[0] = evt.values[0];
////            gravSensorVals[1] = evt.values[1];
////            gravSensorVals[2] = evt.values[2];
//
//        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            magSensorVals = lowPass(evt.values.clone(), magSensorVals);
////            magSensorVals[0] = evt.values[0];
////            magSensorVals[1] = evt.values[1];
////            magSensorVals[2] = evt.values[2];
//
//        }
//
//        if (gravSensorVals != null && magSensorVals != null) {
//            SensorManager.getRotationMatrix(RTmp, I, gravSensorVals, magSensorVals);
//
//            Rot = new float[9];
//            switch (mDisplay.getRotation()) {
//                case Surface.ROTATION_0:
//                    Rot = RTmp.clone();
//                    break;
//                case Surface.ROTATION_90:
//                    SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, RTmp);
//                    break;
//                case Surface.ROTATION_180:
//                    SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, RTmp);
//                    break;
//                case Surface.ROTATION_270:
//                    SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, RTmp);
//                    break;
//            }
//            SensorManager.getOrientation(RTmp, results);
//
//            //ARView.azimuth = (float)(((results[0]*180)/Math.PI)+180);
//            angle = -(float) (((results[1] * 180 / Math.PI)));
//            angle = Math.round(angle);
//            //ARView.roll = (float)(((results[2]*180/Math.PI)));
//            Log.e("angle", angle + "");
//            layoutGrid.setAngle((float) angle);
//        }
        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
            float aX = gravSensorVals[0];
            float aY = gravSensorVals[1];
            float aZ = evt.values[2];
            if (Math.abs(aZ) > 8.5) {
                if (!isFlat) {
                    isFlat = true;
                    ValueAnimator mAnimator = ValueAnimator.ofFloat(angle, 0);
                    mAnimator.setInterpolator(new AccelerateInterpolator());
                    mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            final Float fraction = (Float) animation.getAnimatedValue();
                            angle = fraction.intValue();
                            layoutGrid.setRotation(angle);
                        }
                    });
                    mAnimator.setDuration(500);
                    mAnimator.start();
                }
            } else if (Math.abs(aZ) < 7.5) {
                angle = (float) Math.toDegrees((Math.atan2(aX, aY)));
                angle = Math.round(angle);
                if (isFlat) {
                    ValueAnimator mAnimator = ValueAnimator.ofFloat(0, angle);
                    mAnimator.setInterpolator(new AccelerateInterpolator());
                    mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            final Float fraction = (Float) animation.getAnimatedValue();
                            int angle = fraction.intValue();
                            layoutGrid.setRotation(angle);
                        }
                    });
                    mAnimator.setDuration(200);
                    mAnimator.start();
                    isFlat = false;
                } else {

                    layoutGrid.setRotation(angle);
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    static final float ALPHA = 0.01f;

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private void goToImageGallery() {
        Intent intent = new Intent(VideoConferenceActivity.this, ImageViewActivity.class);
        startActivity(intent);
    }

    boolean isRecord = false;

    public void startRecord(View view) {
        if (!isRecord) {
            boolean enabled = !peersManager.isRenderAudio();
            btnEnableAudio.setBackgroundResource(enabled ? R.mipmap.voice_on : R.mipmap.voice_off);
            peersManager.setAudioEnabled();
            if (biggestParticipant.getName().equals(name_client))
                biggestParticipant.setMute(!enabled);
            for (RemoteParticipant participant : listRemoteRender) {
                if (!participant.getName().equals(name_client)) {
                    participant.getMediaStream().setEnabled(enabled);
                }
            }
        } else {
        }
        isRecord = !isRecord;
    }

    public void startMemo(View view) {
        isMemo = !isMemo;
        if (!isMemo){
            btnMemo.setImageResource(R.drawable.ic_speaker_notes);
            btnMemo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            waveView.animate().alpha(0f);
            endTime = new DateTime();
            kmlHelper.addWayPointWithMarker(VOICE_MEMO, "Voice Memo", "", "Conference", null, currentLocation,startTime,endTime, roomId, null);
            arrayVoiceMemo.add(new VoiceMemo(Globals.name_client,currentCategory.getCategoryId(), new DateTime(),startTime,endTime,"",currentLocation.getLatitude(), currentLocation.getLongitude()));
            txtCategory.setText("");
        }else {
            DialogUtils.categoryListDialog(this, listCategory, new DialogUtils.CategorySelectedCallback() {
                @Override
                public void onSelect(int position) {
                    btnMemo.setImageResource(R.drawable.ic_speaker_notes_off);
                    btnMemo.setBackgroundTintList(getResources().getColorStateList(R.color.red_200));
                    waveView.animate().alpha(1f);
                    startTime = new DateTime();
                    currentCategory = listCategory.get(position);
                    txtCategory.setText(getString(R.string.current_category,currentCategory.getCategoryName()));
                }
            });

        }
    }

    @Override
    public void onGetApiData(Object obj, int requestCode) {
        listCategory = (ArrayList<Category>) obj;
    }

    public class RemoteProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                //Logging.d(TAG, "Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }
            //Logging.d(TAG, "Render Frame");
            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }
}