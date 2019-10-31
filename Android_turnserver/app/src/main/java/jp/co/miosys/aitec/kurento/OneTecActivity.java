package jp.co.miosys.aitec.kurento;

import android.Manifest;
import android.animation.ValueAnimator;
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
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import static jp.co.miosys.aitec.utils.Globals.SESSION_URL;
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

public class OneTecActivity extends BaseActivity implements
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

    Button start_finish_call;
    //SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer biggestVideoView;
    private CaptureVideoRenderer biggestRenderer;
    private String roomId, recordId = "";
    private Button btnEnableVideo, btnEnableAudio;
    private FloatingActionButton btnMemo;
    private boolean firstPartner = false, isInRoom = false , isMemo = false;
    private Spinner spnResolution;
    private Context mContext;
    private TextView txtMagnifiedName;
    private LocationProvide locationProvide;
    private boolean isBackCameraClient = true, enableGridLine = false; // [20190910 Stmfko] Disable grid view when start call
    private ImageView btnGridLine;
    private GridLineView layoutGrid;
    private Button btnSwitchCamera, btnCapture;
    private String name_client = "My Camera";
    private SensorManager sensorManager;
    private Location locRecording;
    private SaveKMLStateThread saveKMLStateThread;
    private CustomClick customClick;
    private EglBase rootEglBase;
    private ProgressDialog dialog;
    private DateTime  startTime, endTime, stopRecording;
    private Location currentLocation;
    private ArrayList<VoiceMemo> arrayVoiceMemo;
    private WaveView waveView;
    private ArrayList<Category> listCategory;
    private Category currentCategory;
    private TextView txtCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_one_tec);
        mContext = this;
        new ApiProcessService().getMemoCategories(this, this, Globals.REQUEST_API_102);
        currentLocation = null;
        locationProvide = new LocationProvide(this, this);
        locationProvide.startUpdatesButtonHandler();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        askForPermissions();
        spnResolution = (Spinner) findViewById(R.id.spn_resolution);
        start_finish_call = (Button) findViewById(R.id.start_finish_call);
        btnEnableVideo = (Button) findViewById(R.id.btn_video);
        btnEnableAudio = (Button) findViewById(R.id.btn_voice);
        txtCategory = (TextView) findViewById(R.id.txt_category);
        biggestVideoView = (SurfaceViewRenderer) findViewById(R.id.biggest_gl_surface_view);
        waveView = (WaveView) findViewById(R.id.wave_view);
        waveView.setAlpha(0f);
        //localVideoView = (SurfaceViewRenderer) findViewById(R.id.local_gl_surface_view);
        //main_participant = (TextView) findViewById(R.id.main_participant);
        //peer_container = (FrameLayout) findViewById(R.id.peer_container);
        btnGridLine = (ImageView) findViewById(R.id.btn_grid_line);
        btnGridLine.setImageResource(R.mipmap.btn_grid_off); // [20190910 Stmfko] Disable grid view when start call
        layoutGrid = (GridLineView) findViewById(R.id.layout_grid);
        layoutGrid.setAlpha(0f); // [20190910 Stmfko] Disable grid view when start call
        btnSwitchCamera = (Button) findViewById(R.id.btn_switch_camera);
        btnCapture = (Button) findViewById(R.id.btn_capture);
        txtMagnifiedName = (TextView) findViewById(R.id.txt_user_name);
        btnMemo = (FloatingActionButton) findViewById(R.id.btn_memo);
        dialog = (ProgressDialog) findViewById(R.id.progress_dialog);
        dialog.setAlpha(1f);
        //layoutParticipants = (LinearLayout) findViewById(R.id.linearLayout2);
        CommonUtils.getSizeScreen(this);
        arrayVoiceMemo = new ArrayList<>();
        kmlHelper = new KMLHelper();
        kmlHelper.createGPXTrack();
        kmlHelper.addMarker(START_CALL, "https://png.pngtree.com/svg/20170818/allow_call_435496.png");
        kmlHelper.addMarker(END_CALL, "http://endat.org/wp-content/uploads/2017/09/phone-icon-red.png");
        kmlHelper.addMarker(SEND_IMAGE, " https://www.svgimages.com/svg-image/s5/send-file-256x256.png");
        kmlHelper.addMarker(START_RECORD, "http://icons.iconarchive.com/icons/martz90/circle/256/video-camera-icon.png");
        kmlHelper.addMarker(CAPTURE_IMAGE, "https://www.keypointintelligence.com/img/advisoryIcons/consumer.png");
        kmlHelper.addMarker(VOICE_MEMO, "https://cdn6.aptoide.com/imgs/0/a/5/0a547b9ae308e86c64566f1475384d80_icon.png");

        biggestRenderer = new CaptureVideoRenderer(this, this);
        biggestVideoView.setMirror(false);
        rootEglBase = EglBase.create();
        biggestVideoView.init(rootEglBase.getEglBaseContext(), null);
        biggestVideoView.setEnableHardwareScaler(true);
        biggestVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        biggestRenderer.setTarget(biggestVideoView);
        this.peersManager = new PeersManager(this, null,biggestRenderer);

        CommonUtils.createLogFolder();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        customClick = new CustomClick(this);
        customClick.setView(btnCapture);

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
        setBusy();
        createSession();
    }

    private void setBusy() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_CHANGE_STATUS);
            jsonObject.put("host", Globals.name_client);
            jsonObject.put("state", 3);
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    }
                });

                Log.e("Loi", e.getMessage());
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    roomId = (String) jsonObject.get("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        start();
                    }
                });
            }
        };
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
            case R.id.btn_capture:
                capturePicture();
                break;
        }
    }

    private void capturePicture() {
        kmlHelper.addWayPointWithMarker(CAPTURE_IMAGE, "Capture Image", Globals.name_client, "One Tec", null, currentLocation, roomId, null);
        biggestRenderer.captureImage();
        dialog.setAlpha(1f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SocketUtils.isCloseSocket) {
            closeActivity();
        }
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
        kmlHelper.setKMLFileName(roomId);
        synchronized (Globals.startThread) {
            if (!Globals.startThread) {
                saveKMLStateThread = new SaveKMLStateThread(this, roomId);
                saveKMLStateThread.start();
                Globals.startThread = true;
            }
        }

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
        spnResolution.setSelection(0); // [20190910 Stmfko] Change max resolution: Default as max
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
        btnEnableAudio.setVisibility(View.VISIBLE);
        btnEnableAudio.setClickable(true);
        btnEnableAudio.setEnabled(true);
        btnEnableVideo.setVisibility(View.VISIBLE);
        btnSwitchCamera.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
        btnGridLine.setVisibility(View.VISIBLE);
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnGridLine.getLayoutParams();
//        params.addRule(RelativeLayout.LEFT_OF, R.id.btn_switch_camera);
//        params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        btnGridLine.setLayoutParams(params);
        layoutGrid.setAlpha(0f); // [20190910 Stmfko] Disable grid view when start call
        boolean enabled = peersManager.isRenderAudio();
        btnEnableAudio.setBackgroundResource(enabled ? R.mipmap.voice_on : R.mipmap.voice_off);
    }

    public void hangUp(View view) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("確認");
        alertDialogBuilder.setMessage("通話を終了しても宜しいですか？")
                .setCancelable(false)
                .setPositiveButton("はい",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                saveRecording(true);
                                dialog.dismiss();
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
        final String participantName = Globals.name_client;
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
               webSocketTask = (WebSocketTask) new WebSocketTask(OneTecActivity.this, peersManager, sessionName, participantName, socketAddress, token).execute(OneTecActivity.this);
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
                            kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", "", "One Tec", null, currentLocation, roomId, fmt.print(new DateTime()));
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
                                kmlHelper.addWayPointWithMarker(START_CALL, "Start Call", "", "One Tec", null, currentLocation, roomId, timeCreated);
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
    public void gotLocalStream(MediaStream mediaStream) {
        super.gotLocalStream(mediaStream);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setAlpha(0f);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnMemo.setVisibility(View.VISIBLE);
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void gotRemoteStream(final MediaStream stream, final String remoteParticipant) {
        if (!isInRoom) {
            isInRoom = false;
            getSessionTime();
        }
        final VideoTrack videoTrack = stream.videoTracks.getFirst();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Check if current device is mute state
                if (!peersManager.isRenderAudio())
                    stream.audioTracks.getFirst().setEnabled(false);

                if (!firstPartner) {
//                    biggestParticipant = new RemoteParticipant();
//                    biggestParticipant.setName(remoteParticipant);
//                    biggestParticipant.setMediaStream(stream.audioTracks.getFirst());
//                    biggestParticipant.setMute(false);
                    biggestVideoView.setVisibility(View.VISIBLE);
                    videoTrack.addRenderer(new VideoRenderer(biggestRenderer));
                    firstPartner = true;
                    txtMagnifiedName.setText(remoteParticipant);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnEnableAudio.setVisibility(View.VISIBLE);
                            btnMemo.setVisibility(View.VISIBLE);
                        }
                    }, 8000);
                    if (currentLocation != null)
                        locRecording = new Location(currentLocation);
                } else {
                }
            }
        });
    }

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

        webSocketTask.setCancelled(true);

        peersManager.hangup(true);

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
        finish();

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
            if (function.equals(Globals.FUNCTION_ERROR_CONNECT)) {
                //closeActivity();
                if (socketUtils != null) {
                    socketUtils.release();
                    socketUtils = null;
                    socketUtils = new SocketUtils(mContext, this);
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
            }
        } catch (Exception e) {
//            ALog.e("Error", e.toString());
        }
    }

    public void startMemo(View view) {
        if (isMemo){
            btnMemo.setImageResource(R.drawable.ic_speaker_notes);
            btnMemo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            waveView.animate().alpha(0f);
            endTime = new DateTime();
            kmlHelper.addWayPointWithMarker(VOICE_MEMO, "Voice Memo", "", "Conference", null, currentLocation,startTime,endTime, roomId, null);
            arrayVoiceMemo.add(new VoiceMemo(Globals.name_client,currentCategory.getCategoryId(), new DateTime(),startTime,endTime,"",currentLocation.getLatitude(), currentLocation.getLongitude()));
            txtCategory.setText("");
            isMemo = !isMemo;
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
                    isMemo = !isMemo;
                }
            });

        }
    }

    private void closeActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
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


    private void saveRecording(final boolean sendToServer) {
        dialog.animate().alpha(1f);
        new OkHttpService(OkHttpService.Method.POST, true, this, Globals.SAVE_RECORDING_URL + roomId, null, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //recordId = "https://157.7.141.150:4443/recordings/" + roomId + ".mp4";
                        recordId = Globals.HOST + "/recordings/" + roomId + "/" + roomId + ".mp4";
                        stopRecording = new DateTime();
                        if (sendToServer) {

                        }
                        dialog.animate().alpha(0f);
                        hangup();
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
                if (saveKMLStateThread != null)
                    saveKMLStateThread.stop();
                if (sendToServer) {
                    //sendKmlToServer(recordedUrl);
                    sendCallDetailToServer(recordedUrl, null);
                } else {
                    try {
                        final String kmlFile = kmlHelper.getKMLFile();
                        synchronized (kmlHelper) {
                            kmlHelper.saveKMLFile(kmlFile);
                        }
                        Globals.startThread = false;
                        new File(kmlFile).delete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ALog.e("Save KML", ex.toString());
                    }

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.animate().alpha(0f);
                        hangup();
                    }
                });
            }
        };

    }


    private void sendKmlToServer(final String recordedUrl) {
        kmlHelper.addWayPointWithMarker(START_RECORD, "Start Record", Globals.name_client, "One Tec", recordedUrl, locRecording, roomId, null);
        kmlHelper.addWayPointWithMarker(END_CALL, "End Call", Globals.name_client, "One Tec", null, currentLocation, roomId, null);
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
            params.put("group_user_ids", Globals.name_client);
            params.put("username", Globals.name_client);
            params.put("room_type", String.valueOf(1));
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

    private void sendCallDetailToServer(final String recordedUrl, final String imageUrl) {
        JSONObject json = new JSONObject();
        try {
            json.put("talk_id", roomId);
            json.put("sender_id", Globals.name_client);
            json.put("password", new SharePreference(this).getLogin()[1]);
            json.put("group_user_ids", Globals.name_client);
            json.put("receiver_ids", Globals.name_client);
            json.put("room_type", String.valueOf(1));

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"Oops, something wrong, please try again",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponseApi(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = response.body().string();
                            Toast.makeText(mContext, "Success", Toast.LENGTH_LONG).show();
                            if (imageUrl == null)
                                sendKmlToServer(recordedUrl);
                        }catch (Exception ex){
                            Toast.makeText(mContext, "Errors upload image", Toast.LENGTH_LONG).show();
                        }
                    }
                });

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
//        if (biggestParticipant.getName().equals(name_client))
//            biggestParticipant.setMute(!enabled);
    }

    public boolean isFirstPartner() {
        return firstPartner;
    }

    public boolean setFirstPartner() {
        return firstPartner = true;
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
                try {
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
                }catch (Exception e){
                    Toast.makeText(mContext,"Oops, something wrong, please try again",Toast.LENGTH_SHORT).show();
                }

            }
        });
        this.dialog.setAlpha(0f);
    }
    // Receive image
    @Override
    public void onGetImageDownload(String path) {
        DialogUtils.receiveImageDialog(mContext);
    }

    @Override
    public void onGetImageUrl(String imageUrl) {
        if (kmlHelper != null)
            kmlHelper.addWayPointWithMarker(SEND_IMAGE, "Send Image", Globals.name_client, "One Tec", imageUrl, currentLocation, roomId, null);
        sendCallDetailToServer(null, imageUrl);
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
        this.currentLocation = new Location(location);
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

    boolean isRecord = false;

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