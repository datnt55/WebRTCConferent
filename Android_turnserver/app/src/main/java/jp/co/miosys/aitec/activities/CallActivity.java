/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package jp.co.miosys.aitec.activities;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoRenderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.managers.AppRTCAudioManager;
import jp.co.miosys.aitec.managers.AppRTCAudioManager.AudioDevice;
import jp.co.miosys.aitec.managers.AppRTCAudioManager.AudioManagerEvents;
import jp.co.miosys.aitec.managers.AppRTCClient;
import jp.co.miosys.aitec.managers.AppRTCClient.RoomConnectionParameters;
import jp.co.miosys.aitec.managers.AppRTCClient.SignalingParameters;
import jp.co.miosys.aitec.managers.DirectRTCClient;
import jp.co.miosys.aitec.managers.PeerConnectionClient;
import jp.co.miosys.aitec.managers.PeerConnectionClient.DataChannelParameters;
import jp.co.miosys.aitec.managers.PeerConnectionClient.PeerConnectionParameters;
import jp.co.miosys.aitec.managers.UnhandledExceptionHandler;
import jp.co.miosys.aitec.managers.WebSocketRTCClient;
import jp.co.miosys.aitec.models.LocationGPS;
import jp.co.miosys.aitec.utils.ApiProcesserUtils;
import jp.co.miosys.aitec.utils.CustomClick;
import jp.co.miosys.aitec.utils.DialogUtils;
import jp.co.miosys.aitec.utils.DowloadImageAsynTask;
import jp.co.miosys.aitec.utils.DrawPictureDialog;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.ImageZip;
import jp.co.miosys.aitec.utils.LocationProvide;
import jp.co.miosys.aitec.utils.NoticeDialog;
import jp.co.miosys.aitec.utils.ParseJsonUtils;
import jp.co.miosys.aitec.utils.ReadWriteFileUtils;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.utils.SocketUtils;
import jp.co.miosys.aitec.views.listeners.OnGetImageDownloadListener;
import jp.co.miosys.aitec.views.listeners.OnGetImageUrlListener;
import jp.co.miosys.aitec.views.widgets.CaptureVideoRenderer;
import jp.co.miosys.aitec.views.widgets.GridLineView;
import jp.co.miosys.aitec.views.widgets.ProgressDialog;

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.currentLocation;
import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.Globals.locations;
import static jp.co.miosys.aitec.utils.Globals.room_id;
import static jp.co.miosys.aitec.utils.KMLHelper.END_CALL;
import static jp.co.miosys.aitec.utils.KMLHelper.SEND_IMAGE;

/**
 * [20170913] Ductx: #2597: Create call activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends BaseActivity implements
        AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents,
        OnGetImageUrlListener, OnGetImageDownloadListener,
        SensorEventListener,
        SocketUtils.OnMessageReceive,
        LocationProvide.OnUpdateLocation, CustomClick.OnClickListener {
    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String EXTRA_ROOMID = "jp.co.miosys.aitec.ROOMID";
    public static final String EXTRA_URLPARAMETERS = "jp.co.miosys.aitec.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "jp.co.miosys.aitec.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "jp.co.miosys.aitec.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "jp.co.miosys.aitec.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "jp.co.miosys.aitec.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "jp.co.miosys.aitec.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "jp.co.miosys.aitec.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "jp.co.miosys.aitec.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "jp.co.miosys.aitec.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "jp.co.miosys.aitec.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "jp.co.miosys.aitec.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "jp.co.miosys.aitec.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "jp.co.miosys.aitec.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "jp.co.miosys.aitec.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "jp.co.miosys.aitec.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "jp.co.miosys.aitec.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "jp.co.miosys.aitec.AECDUMP";
    public static final String EXTRA_OPENSLES_ENABLED = "jp.co.miosys.aitec.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "jp.co.miosys.aitec.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "jp.co.miosys.aitec.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "jp.co.miosys.aitec.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL = "jp.co.miosys.aitec.ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "jp.co.miosys.aitec.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "jp.co.miosys.aitec.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "jp.co.miosys.aitec.TRACING";
    public static final String EXTRA_CMDLINE = "jp.co.miosys.aitec.CMDLINE";
    public static final String EXTRA_RUNTIME = "jp.co.miosys.aitec.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "jp.co.miosys.aitec.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "jp.co.miosys.aitec.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "jp.co.miosys.aitec.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "jp.co.miosys.aitec.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "jp.co.miosys.aitec.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "jp.co.miosys.aitec.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "jp.co.miosys.aitec.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "jp.co.miosys.aitec.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "jp.co.miosys.aitec.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "jp.co.miosys.aitec.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "jp.co.miosys.aitec.NEGOTIATED";
    public static final String EXTRA_ID = "jp.co.miosys.aitec.ID";
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private LocationProvide locationProvide;
    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    private final AtomicBoolean snapshotRequsted = new AtomicBoolean(false);
    float angle = 0;
    protected float[] gravSensorVals = new float[3];
    protected float[] magSensorVals = new float[3];
    private Display mDisplay;
    private boolean isFlat = false;
    private CustomClick customClick;

    @Override
    public void onSensorChanged(SensorEvent evt) {
        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
            float aX = gravSensorVals[0];
            float aY = gravSensorVals[1];
            float aZ = evt.values[2];
            //Log.e("z axis", aZ + "");
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
//        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
////            gravSensorVals[0] = evt.values[0];
////            gravSensorVals[1] = evt.values[1];
////            gravSensorVals[2] = evt.values[2];
//
//        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//              magSensorVals = lowPass(evt.values.clone(), magSensorVals);
////            magSensorVals[0] = evt.values[0];
////            magSensorVals[1] = evt.values[1];
////            magSensorVals[2] = evt.values[2];
//
//        }
//
//        if (gravSensorVals != null && magSensorVals != null) {
//            float[] RTmp = new float[9];
//            boolean rotationOK = SensorManager.getRotationMatrix(RTmp, null, gravSensorVals, magSensorVals);
//
//            float[] Rot = new float[9];
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
//            float results[] = new float[3];
//            if (rotationOK) {
//                SensorManager.getOrientation(Rot, results);
//
//                //ARView.azimuth = (float)(((results[0]*180)/Math.PI)+180);
//                angle = (float) (Math.toDegrees(results[2]));
//                angle = -Math.round(angle);
//                //ARView.roll = (float)(((results[2]*180/Math.PI)));
//                Log.e("angle", angle + "");
//                layoutGrid.setAngle((float) angle);
//            }
//        }
//
////        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
////            result = lowPass(event.values, result);
////            float aX = result[0];
////            float aY = result[1];
////            //aZ= event.values[2];
////            double angle = Math.atan2(aX, aY) / (Math.PI / 180);
////            if (Math.abs(angle - this.angle) > 0.5f) {
////                this.angle = (float) angle;
////                layoutGrid.setAngle((float) angle);
////            }
////        }
    }

    static final float ALPHA = 0.01f;

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class RemoteProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }
            Logging.d(TAG, "Render Frame");
            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }

    private void showDialogCapture() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DrawPictureDialog dialog = new DrawPictureDialog();
//        Bitmap overlayBitmap = CommonUtils.drawTimeOnBitmap(captureBitmap);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        overlayBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();

        Bundle bundle = new Bundle();
        //bundle.putByteArray(Globals.BUNDLE_SEND_IMAGE,byteArray);
        bundle.putStringArray(Globals.BUNDLE_SEND_EXIF, null);
        bundle.putBoolean(Globals.BUNDLE_SEND_GRID, enableGridLine);
        bundle.putFloat(Globals.BUNDLE_SEND_ANGLE, angle);
        dialog.setArguments(bundle);
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.show(fragmentManager, "Input Dialog");
        dialog.setOnSendDataListener(new DrawPictureDialog.SendDataListener() {
            @Override
            public void onSend(File imageFile) {
                String kmlFile = kmlHelper.getKMLFile();
                boolean success;
                synchronized (kmlHelper) {
                    success = kmlHelper.saveKMLFile(kmlFile);
                }
                if (success) {
                    String result = ReadWriteFileUtils.createTempFileDontSend(mContext).getAbsolutePath();
                    ImageZip.mergeFiles(imageFile.getAbsolutePath(), kmlFile, result);
                    //peerConnectionClient.sendData(locations);
                    UploadApplication.isUpload = true;
                    ApiProcesserUtils.sendImage(mContext, new File(result), Globals.id_guest, true);
                }
            }
        });
        this.dialog.setAlpha(0f);
    }

    private final RemoteProxyRenderer remoteProxyRenderer = new RemoteProxyRenderer();
    private CaptureVideoRenderer localProxyRenderer;
    private PeerConnectionClient peerConnectionClient = null;
    private AppRTCClient appRtcClient;
    private SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager = null;
    private EglBase rootEglBase;
    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private VideoFileRenderer videoFileRenderer;
    private final List<VideoRenderer.Callbacks> remoteRenderers =
            new ArrayList<VideoRenderer.Callbacks>();
    private Toast logToast;
    private boolean commandLineRun;
    private int runTimeMs;
    private boolean activityRunning;
    private RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private boolean isError;
    private boolean enableGridLine = true;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs = 0;
    private boolean micEnabled = true;
    private boolean videoEnabled = true;
    private boolean screencaptureEnabled = false;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds, isBackCameraPatner = true, isBackCameraClient = true;
    private ArrayList<LocationGPS> listPartnerPosition = new ArrayList<>();

    // Controls
//    private HudFragment hudFragment;
//    private CpuMonitor cpuMonitor;

    private View controlView;
    private ImageView btnBack, btnNext;
    private ImageButton cameraSwitchButton;
    //    private ImageButton cameraFlashButton;
    private ImageButton toggleMuteButton;
    private ImageButton toggleVideoButton;
    private ScalingType scalingType;
    private boolean videoCallEnabled = true;
    private TextView txtRes240, txtRes480, txtRes720, txtRes1080, txtRes1440, txtRes2160;
    private Context mContext;
    private int videoWidth, videoHeight;
    private TextView txtUser1;
    private TextView txtUser2;
    private TextView txtViewImage;
    private TextView txtPartnerLoading;
    private ImageView btnGridLine;
    private GridLineView layoutGrid;
    private Button btnCapture;
    private ProgressDialog dialog;
    private SensorManager sensorManager;
    private Location currentLocation;

    @Override
    protected void onResume() {
        super.onResume();
        btnNext.setEnabled(true);
        btnNext.setClickable(true);
        if (SocketUtils.isCloseSocket) {
            closeActivity();
        }
        // Init interface
        ParseJsonUtils utils = new ParseJsonUtils();
        utils.getImageUrl(this);
        socketUtils.setCallBack(this, this);
        DowloadImageAsynTask.getImageDowloadComplete(this);

        if (Globals.isPatnerViewImage) {
            txtViewImage.setVisibility(View.VISIBLE);
        } else {
            txtViewImage.setVisibility(View.GONE);
        }
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:My Tag");
        wl.acquire();
        wl.release();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        mContext = this;
        currentLocation = null;
        setContentView(R.layout.activity_call);
        localProxyRenderer = new CaptureVideoRenderer(this, new CaptureVideoRenderer.CapturePictureListener() {
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

                }
            }
        });
        videoWidth = getIntent().getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        videoHeight = getIntent().getIntExtra(EXTRA_VIDEO_HEIGHT, 0);
        initValue();
        initView();

        localProxyRenderer.setTarget(pipRenderer);
        pipRenderer.setMirror(false);

        locationProvide = new LocationProvide(this, this);
        locationProvide.startUpdatesButtonHandler();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        String roomId = getIntent().getStringExtra(EXTRA_ROOMID);
        kmlHelper.setKMLFileName(roomId);
    }

    public void initView() {

        txtUser1 = (TextView) findViewById(R.id.txt_user1);
        txtUser2 = (TextView) findViewById(R.id.txt_user2);
        txtUser1.setText(Globals.name_guest);
        txtUser2.setText(Globals.name_client);

        txtRes240 = (TextView) findViewById(R.id.txt_res240);
        txtRes480 = (TextView) findViewById(R.id.txt_res480);
        txtRes720 = (TextView) findViewById(R.id.txt_res720);
        txtRes1080 = (TextView) findViewById(R.id.txt_res1080);
        txtRes1440 = (TextView) findViewById(R.id.txt_res1440);
        txtRes2160 = (TextView) findViewById(R.id.txt_res2160);

        txtPartnerLoading = (TextView) findViewById(R.id.txt_partner_loading);

        // if after 20s, calling is not start. display notice
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (txtPartnerLoading.getVisibility() == View.VISIBLE){
                    showDialogError(getString(R.string.connection_error_establish));
                }
            }
        },12000);

        txtViewImage = (TextView) findViewById(R.id.txt_noti_patner_view_image);

        btnGridLine = (ImageView) findViewById(R.id.btn_grid_line);
        layoutGrid = (GridLineView) findViewById(R.id.layout_grid);
        layoutGrid.setAlpha(0f);
        btnCapture = (Button) findViewById(R.id.btn_capture);
        dialog = (ProgressDialog) findViewById(R.id.progress_dialog);
        dialog.setAlpha(0f);

        // 20171011 Ductx: Fix the resolution default is 480
        txtRes480.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        // Create UI controls.
        btnBack = (ImageView) findViewById(R.id.btn_back);
        btnNext = (ImageView) findViewById(R.id.btn_next);
        btnNext.setVisibility(View.VISIBLE);
        cameraSwitchButton = (ImageButton) findViewById(R.id.button_call_switch_camera);
//        cameraFlashButton = (ImageButton) findViewById(R.id.button_call_turn_flash);
        toggleMuteButton = (ImageButton) findViewById(R.id.button_call_toggle_mic);
        toggleVideoButton = (ImageButton) findViewById(R.id.button_call_toggle_video);

        // Add buttons click events.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
            }
        });

        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (peerConnectionClient != null) {
                    peerConnectionClient.switchCamera();
                    isBackCameraClient = !isBackCameraClient;
                    setSwappedFeeds(isSwappedFeeds);
                }
            }
        });

        toggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = onToggleMic();
                toggleMuteButton.setBackgroundResource(enabled ? R.mipmap.voice_on : R.mipmap.voice_off);
            }
        });

        toggleVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = onToggleVideo();
                toggleVideoButton.setBackgroundResource(enabled ? R.mipmap.camera_on : R.mipmap.camera_off);
            }
        });

        if (getIntent().getExtras() != null) {
            videoCallEnabled = getIntent().getBooleanExtra(CallActivity.EXTRA_VIDEO_CALL, true);
        }
        if (!videoCallEnabled) {
            cameraSwitchButton.setVisibility(View.INVISIBLE);
        }
        updateViewWithGridLine();
        customClick = new CustomClick(this);
        customClick.setView(btnCapture);
        customClick.setView(btnNext);
    }

    private void showDialogError(String errorMessage) {
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
                    disconnect();
                }
            });
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                btnNext.setEnabled(true);
                btnNext.setClickable(true);
                Intent intent = new Intent(CallActivity.this, ImageViewActivity.class);
                intent.putExtra(Globals.BUNDLE_SEND_GPS, listPartnerPosition);
                startActivity(intent);
                break;
            case R.id.btn_capture:
                capturePicture();
                break;
        }
    }

    @Override
    public void onStopUpdate() {

    }

    @Override
    public void onUpdate(Location currentLocation) {
        //peerConnectionClient.sendData(currentLocation.getLongitude()+";"+currentLocation.getLongitude());
        this.currentLocation = new Location(currentLocation);
        locations.add(new LocationGPS(currentLocation));
        kmlHelper.addWayPoint(currentLocation);
    }

    public void chooseResOnclick(View view) {

        txtRes240.setBackgroundColor(getResources().getColor(R.color.white));
        txtRes480.setBackgroundColor(getResources().getColor(R.color.white));
        txtRes720.setBackgroundColor(getResources().getColor(R.color.white));
        txtRes1080.setBackgroundColor(getResources().getColor(R.color.white));
        txtRes1440.setBackgroundColor(getResources().getColor(R.color.white));
        txtRes2160.setBackgroundColor(getResources().getColor(R.color.white));
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        if (view == txtRes240) {
            peerConnectionClient.changeCaptureFormat(360, 240, 30);
        } else if (view == txtRes720) {
            peerConnectionClient.changeCaptureFormat(1280, 720, 30);
        } else if (view == txtRes1080) {
            peerConnectionClient.changeCaptureFormat(1920, 1080, 30);
        } else if (view == txtRes1440) {
            peerConnectionClient.changeCaptureFormat(2560, 1440, 30);
        } else if (view == txtRes2160) {
            peerConnectionClient.changeCaptureFormat(3840, 2160, 30);
        } else {
            peerConnectionClient.changeCaptureFormat(640, 480, 30);
        }
    }

    public void initValue() {
        iceConnected = false;
        signalingParameters = null;
        final Intent intent = getIntent();
        // Create UI controls.
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);

        ViewTreeObserver vto = fullscreenRenderer.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    fullscreenRenderer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    fullscreenRenderer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int width = fullscreenRenderer.getMeasuredWidth();
                int height = fullscreenRenderer.getMeasuredHeight();
                layoutGrid.setGridSize(width, height);

            }
        });
//        hudFragment = new HudFragment();

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
                if (isSwappedFeeds) {
                    setTextUser(Globals.name_client, Globals.name_guest);
                } else {
                    setTextUser(Globals.name_guest, Globals.name_client);
                }
                updateViewWithGridLine();
            }
        });

        fullscreenRenderer.setOnClickListener(listener);
        remoteRenderers.add(remoteProxyRenderer);

        // Create video renderers.
        rootEglBase = EglBase.create();
        pipRenderer.init(rootEglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        String saveRemoteVideoToFile = intent.getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);

        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        if (saveRemoteVideoToFile != null) {
            int videoOutWidth = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
            int videoOutHeight = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
            try {
                videoFileRenderer = new VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, rootEglBase.getEglBaseContext());
                remoteRenderers.add(videoFileRenderer);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to open video file for output: " + saveRemoteVideoToFile, e);
            }
        }
        fullscreenRenderer.init(rootEglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
//        setSwappedFeeds(true /* isSwappedFeeds */);

        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }

        Uri roomUri = intent.getData();
        if (roomUri == null) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Didn't get any URL in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Get Intent parameters.
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        Log.d(TAG, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }
        DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                    intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }
        peerConnectionParameters =
                new PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_LEVEL_CONTROL, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false), dataChannelParameters);
        commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
        runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

        Log.d(TAG, "VIDEO_FILE: '" + intent.getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA) + "'");

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this, mContext);
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }
        // Create connection parameters.
        String urlParameters = intent.getStringExtra(EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

        // Create CPU monitor
//        cpuMonitor = new CpuMonitor(this);
//        hudFragment.setCpuMonitor(cpuMonitor);

        // Send intent arguments to fragments.
//        hudFragment.setArguments(intent.getExtras());
        // Activate call and HUD fragments and start the call.
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.add(R.id.hud_fragment_container, hudFragment);
//        ft.commit();

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }

        peerConnectionClient = PeerConnectionClient.getInstance();
        if (loopback) {
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
            peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }
        peerConnectionClient.createPeerConnectionFactory(
                getApplicationContext(), peerConnectionParameters, CallActivity.this);

        peerConnectionClient.changeCaptureFormat(640, 480, 30);

        if (screencaptureEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startScreenCapture();
        } else {
            startCall();
        }
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isBackFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @TargetApi(21)
    private VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                reportError("User revoked permission to capture the screen.");
            }
        });
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }
        sensorManager.unregisterListener(this);
//        cpuMonitor.pause();
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }
//        cpuMonitor.resume();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        //disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        rootEglBase.release();
        super.onDestroy();
    }

    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    public ArrayList<LocationGPS> getLocations() {
        return listPartnerPosition;
    }

    public boolean onToggleVideo() {
        if (peerConnectionClient != null) {
            videoEnabled = !videoEnabled;
            peerConnectionClient.setVideoEnabled(videoEnabled);
        }
        return videoEnabled;
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!iceConnected) {
            return;
        }
        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        if (callControlFragmentVisible) {
//            ft.show(hudFragment);
//        } else {
//            ft.hide(hudFragment);
//        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        //appRtcClient.connectToRoom(roomConnectionParameters);
        JSONObject jsonObject = new JSONObject();
        try {
            String roomId = getIntent().getStringExtra(EXTRA_ROOMID);
            jsonObject.put("type", Globals.FUNCTION_CREATE_ROOM);
            jsonObject.put("offer", Globals.name_client);
            jsonObject.put("answer", Globals.name_guest);
            jsonObject.put("id", roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AudioDevice audioDevice, Set<AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        txtPartnerLoading.setVisibility(View.GONE);
        setSwappedFeeds(false /* isSwappedFeeds */);
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AudioDevice device, final Set<AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        kmlHelper.addWayPointWithMarker(END_CALL, "End Call", Globals.name_guest, "PeerToPeer", null, currentLocation, room_id, null);
//        String kmlFile = kmlHelper.getKMLFile();
//        boolean success;
//        synchronized (kmlHelper) {
//            success = kmlHelper.saveKMLFile(kmlFile);
//        }
//        if (success){
//            TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//            String uuid = tManager.getDeviceId();
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("upload_file", new File(kmlFile));
//            params.put("uuid", uuid);
//            params.put("username", Globals.name_client);
//            new OkHttpService(OkHttpService.Method.POST, this, Globals.URL_KML, params, false) {
//                @Override
//                public void onFailureApi(Call call, Exception e) {
//
//                }
//
//                @Override
//                public void onResponseApi(Call call, Response response) throws IOException {
//                    String result = response.body().string();
//                    String a = "";
//                }
//            };
//        }
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        localProxyRenderer.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_END_CALL);
            jsonObject.put("host", Globals.name_client);
            jsonObject.put("receive", Globals.name_guest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sendMessage = jsonObject.toString();
        socketUtils.sendMessage(sendMessage);
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect();
                                }
                            })
                    .create()
                    .show();
        }
    }

    // [20171010] Ductx: CommentOut toast of RTC server.
    private void logAndToast(String msg) {
//        Log.d(TAG, msg);
//        if (logToast != null) {
//            logToast.cancel();
//        }
//        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
//        logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = null;
        String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    /*[20171102] Ductx: #2803: set Mirror camera is backward*/
    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyRenderer.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        if (!isBackCameraClient) {
            if (!isSwappedFeeds) {
                fullscreenRenderer.setMirror(false);
                pipRenderer.setMirror(true);
            } else {
                fullscreenRenderer.setMirror(true);
                pipRenderer.setMirror(false);
            }
        } else {
            fullscreenRenderer.setMirror(false);
            pipRenderer.setMirror(false);
        }
    }

    private void capturePicture() {
        dialog.setAlpha(1f);
        localProxyRenderer.captureImage();
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        logAndToast("Creating peer connection, delay=" + delta + "ms");
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), localProxyRenderer,
                remoteRenderers, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            logAndToast("Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onConnectedToRoom(final SignalingParameters params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    logAndToast("Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("Remote end hung up; dropping PeerConnection");
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
                    if (signalingParameters.initiator) {
                        //appRtcClient.sendOfferSdp(sdp);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", Globals.FUNCTION_OFFER_SDP);
                            jsonObject.put("id", Globals.room_id);
                            jsonObject.put("sdp", sdp.description);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String sendMessage = jsonObject.toString();
                        socketUtils.sendMessage(sendMessage);
                    } else {
                        //appRtcClient.sendAnswerSdp(sdp);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", Globals.FUNCTION_ANSWER_SDP);
                            jsonObject.put("receive", Globals.name_guest);
                            jsonObject.put("sdp", sdp.description);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String sendMessage = jsonObject.toString();
                        socketUtils.sendMessage(sendMessage);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    //appRtcClient.sendLocalIceCandidate(candidate);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", Globals.FUNCTION_CANDIDATE);
                        jsonObject.put("host", Globals.name_guest);
                        jsonObject.put("typeM", "candidate");
                        jsonObject.put("id", Globals.room_id);
                        jsonObject.put("label", candidate.sdpMLineIndex);
                        jsonObject.put("idM", candidate.sdpMid);
                        jsonObject.put("candidate", candidate.sdp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String sendMessage = jsonObject.toString();
                    socketUtils.sendMessage(sendMessage);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE connected, delay=" + delta + "ms");
                iceConnected = true;
                callConnected();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE disconnected");
                iceConnected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (!isError && iceConnected) {
//                    hudFragment.updateEncoderStatistics(reports);
//                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

    @Override
    public void onDataReceive(final ArrayList<LocationGPS> description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listPartnerPosition = description;
            }
        });
    }

    @Override
    public void onGotRemoteStream() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutGrid.animate().alpha(1f);
                btnGridLine.setImageResource(R.drawable.ic_grid_on);
            }
        });

    }

    @Override
    public void onMessage(final String message) {
        try {
            JSONObject result = new JSONObject(message);
            String function = result.getString("type");
            if (function.equals(Globals.FUNCTION_SEND_IMAGE_URL)) {
                final String imageUrl = result.getString("url");
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        String url = imageUrl;
                        DowloadImageAsynTask asynTask = new DowloadImageAsynTask(url, mContext);
                        asynTask.execute();
                    }
                });
            } else if (function.equals(Globals.FUNCTION_IS_VIEW_IMAGE)) {
                boolean data = result.getBoolean("data");
                if (data) {
                    Globals.isPatnerViewImage = true;
                    txtViewImage.setVisibility(View.VISIBLE);
                } else {
                    Globals.isPatnerViewImage = false;
                    txtViewImage.setVisibility(View.GONE);
                }
            } else if (function.equals(Globals.FUNCTION_ERROR_CONNECT)) {
                //closeActivity();
                if (socketUtils != null) {
                    socketUtils.release();
                    socketUtils = null;
                    socketUtils = new SocketUtils(mContext, CallActivity.this);
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
            } else if (function.equals(Globals.FUNCTION_CREATE_ROOM)) {
                boolean initiator = result.getBoolean("initiator");
                LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
                iceServers.add(new PeerConnection.IceServer(Globals.TurnServerURI, Globals.TurnServerUser, Globals.TurnServerPass));
//                iceServers.add(new PeerConnection.IceServer("turn:64.233.188.127:19305?transport=udp", "CL3OvtAFEgZe0L1IDhoYzc/s6OMTIICjBQ", "3Q+Pp/pCvG1QMNRmmy3V/ArT2B8="));
//                iceServers.add(new PeerConnection.IceServer("turn:[2404:6800:4008:c06::7f]:19305?transport=udp", "CL3OvtAFEgZe0L1IDhoYzc/s6OMTIICjBQ", "3Q+Pp/pCvG1QMNRmmy3V/ArT2B8="));
//                iceServers.add(new PeerConnection.IceServer("turn:64.233.188.127:443?transport=tcp", "CL3OvtAFEgZe0L1IDhoYzc/s6OMTIICjBQ", "3Q+Pp/pCvG1QMNRmmy3V/ArT2B8="));
//                iceServers.add(new PeerConnection.IceServer("turn:[2404:6800:4008:c06::7f]:443?transport=tcp", "CL3OvtAFEgZe0L1IDhoYzc/s6OMTIICjBQ", "3Q+Pp/pCvG1QMNRmmy3V/ArT2B8="));
//                iceServers.add(new PeerConnection.IceServer("turn:[2404:6800:4008:c06::7f]:443?transport=tcp", "CL3OvtAFEgZe0L1IDhoYzc/s6OMTIICjBQ", "3Q+Pp/pCvG1QMNRmmy3V/ArT2B8="));
                iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302", "", ""));
                LinkedList<IceCandidate> iceCandidates = null;
                SessionDescription offerSdp = null;
                if (!initiator) {
                    //iceCandidates = new LinkedList<>();
                    String messageType = "offer";
                    offerSdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(messageType), result.getString("sdp"));
//                    JSONArray iceCandidate = result.getJSONArray("ice");
//                    for (int i = 0 ; i <iceCandidate.length(); i++) {
//                        JSONObject jsonObject = iceCandidate.getJSONObject(i);
//                        IceCandidate candidate = new IceCandidate(jsonObject.getString("idM"), jsonObject.getInt("label"), jsonObject.getString("candidate"));
//                        iceCandidates.add(candidate);
//                    }
                }
                Globals.room_id = result.getString("id");
                SignalingParameters params = new SignalingParameters(iceServers, initiator, "", "", "", offerSdp, iceCandidates);
                onConnectedToRoomInternal(params);
            } else if (function.equals(Globals.FUNCTION_OFFER_SDP)) {
                final SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), result.getString("sdp"));
                final long delta = System.currentTimeMillis() - callStartedTimeMs;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (peerConnectionClient == null) {
                            Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                            return;
                        }
                        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                        peerConnectionClient.setRemoteDescription(sdp);
                        if (!signalingParameters.initiator) {
                            logAndToast("Creating ANSWER...");
                            // Create answer. Answer SDP will be sent to offering client in
                            // PeerConnectionEvents.onLocalDescription event.
                            peerConnectionClient.createAnswer();
                        }
                    }
                });
            } else if (function.equals(Globals.FUNCTION_CANDIDATE)) {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                IceCandidate candidate = new IceCandidate(result.getString("idM"), result.getInt("label"), result.getString("candidate"));
                peerConnectionClient.addRemoteIceCandidate(candidate);
            } else if (function.equals(Globals.FUNCTION_ANSWER_SDP)) {
                final SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), result.getString("sdp"));
                final long delta = System.currentTimeMillis() - callStartedTimeMs;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (peerConnectionClient == null) {
                            Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                            return;
                        }
                        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                        peerConnectionClient.setRemoteDescription(sdp);
                        if (!signalingParameters.initiator) {
                            logAndToast("Creating ANSWER...");
                            // Create answer. Answer SDP will be sent to offering client in
                            // PeerConnectionEvents.onLocalDescription event.
                            peerConnectionClient.createAnswer();
                        }
                    }
                });
            }
        } catch (
                Exception e) {
            e.getMessage();
        }

    }

    // Converts a JSON candidate to a Java object.
    private IceCandidate toJavaCandidate(JSONObject json) throws JSONException {
        return new IceCandidate(
                json.getString("id"), json.getInt("label"), json.getString("candidate"));
    }

    private void closeActivity() {
        Intent intent = new Intent(CallActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Receive image
    @Override
    public void onGetImageDownload(String path) {
        DialogUtils.receiveImageDialog(CallActivity.this);
    }

    /*[20170910] Ductx: #2595: Create layout for received image confirmation screen*/
    // Send image
    @Override
    public void onGetImageUrl(String imageUrl) {
        kmlHelper.addWayPointWithMarker(SEND_IMAGE, "Send Image", Globals.name_guest, "Conference", imageUrl, currentLocation, room_id, null);
        sendImageUrl(imageUrl);
    }

    private void sendImageUrl(String url) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_SEND_IMAGE_URL);
            jsonObject.put("receive", Globals.name_guest);
            jsonObject.put("url", url);
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onSendImage(File file) {
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("type", Globals.FUNCTION_SEND_IMAGE_URL);
//            jsonObject.put("data", CommonUtils.encodedImage(file.getAbsolutePath()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String message = jsonObject.toString();
//        socketUtils.sendMessage(message);
//    }

    private void setTextUser(String name1, String name2) {
        txtUser1.setText(name1);
        txtUser2.setText(name2);
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
    }

    private void updateViewWithGridLine() {
        if (txtUser1.getText().toString().equals(Globals.name_client)) {
            toggleMuteButton.setVisibility(View.VISIBLE);
            toggleVideoButton.setVisibility(View.VISIBLE);
            cameraSwitchButton.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnGridLine.getLayoutParams();
            params.addRule(RelativeLayout.LEFT_OF, R.id.button_call_switch_camera);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnGridLine.setLayoutParams(params);
            if (enableGridLine)
                layoutGrid.setVisibility(View.VISIBLE);
            else
                layoutGrid.setVisibility(View.GONE);
            btnGridLine.setVisibility(View.VISIBLE);
        } else {
            toggleMuteButton.setVisibility(View.GONE);
            toggleVideoButton.setVisibility(View.GONE);
            cameraSwitchButton.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);
            layoutGrid.setVisibility(View.GONE);
            btnGridLine.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnGridLine.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnGridLine.setLayoutParams(params);
        }
    }
}
