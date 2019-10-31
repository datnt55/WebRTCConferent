package jp.co.miosys.aitec.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.Voice;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anand.brose.graphviewlibrary.GraphView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.models.VoiceMemo;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.LocationProvide;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.utils.VoiceAudioRecord;
import jp.co.miosys.aitec.utils.audio.AudioRecorder;
import jp.co.miosys.aitec.utils.audio.WavEncoder;
import jp.co.miosys.aitec.views.services.CountingFileRequestBody;
import jp.co.miosys.aitec.views.widgets.TimerCounter;
import jp.co.miosys.aitec.views.widgets.VoiceRecorder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.ROOT_DIRECTORY;
import static jp.co.miosys.aitec.utils.Globals.arrayMemo;

public class VoiceMemoActivity extends BaseActivity implements TimerCounter.TimerCounterListener, LocationProvide.OnUpdateLocation {

    public static final String SCALE = "scale";
    private static final int MY_PERMISSIONS_REQUEST_CODE = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    int scale = 8;
    private GraphView graphView;
    private AudioRecorder audioRecorder;
    private List samples;
    private TextView txtCount, txtProgress;
    private TimerCounter timerCounter;
    private RelativeLayout layoutRecord, layoutPlay, layoutUpload;
    private ImageView imgMic, imgPlay;
    private MediaPlayer mPlayer;
    private String recordFile = "";
    private RelativeLayout layoutProgress;
    private ProgressBar progressBar;
    private LocationProvide locationProvide;
    private DateTime startTime, endTime;
    private Location currentLocation;
    private ImageView btnBack;
    private TextView txtTitle;
    private boolean isUploading, isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_memo);
        locationProvide = new LocationProvide(this, this);
        locationProvide.startUpdatesButtonHandler();
        graphView = (GraphView) findViewById(R.id.graphView);
        graphView.setGraphColor(Color.rgb(255, 255, 255));
        graphView.setCanvasColor(Color.rgb(20, 20, 20));
        graphView.setTimeColor(Color.rgb(255, 255, 255));
        txtCount = (TextView) findViewById(R.id.txt_count);
        layoutProgress = (RelativeLayout) findViewById(R.id.layout_progress);
        layoutProgress.setAlpha(0f);
        txtProgress = (TextView) findViewById(R.id.txt_progress);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        layoutRecord = (RelativeLayout) findViewById(R.id.layout_record);
        layoutPlay = (RelativeLayout) findViewById(R.id.layout_play_back);
        layoutUpload = (RelativeLayout) findViewById(R.id.layout_upload);
        imgMic = (ImageView) findViewById(R.id.img_mic);
        imgPlay = (ImageView) findViewById(R.id.img_play);

        btnBack = (ImageView) findViewById(R.id.btn_back);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTitle.setText("Voice Memo");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initRecorder();
        setBusy();
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

    private void initRecorder() {
        audioRecorder = new AudioRecorder(new AudioRecorder.CallBack() {
            @Override
            public void recordProgress(final int progress) {
            }

            @Override
            public void volumn(int volumn) {

            }
        }, new WavEncoder());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SCALE, scale);
        super.onSaveInstanceState(outState);
    }

    public void controlClick(View v) throws IOException {
        if (isUploading){
            //Toast.makeText(this, "Ai-tec still uploading...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPlayer != null){
            //Toast.makeText(this, "Ai-tec still uploading...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer = null;
            imgPlay.setImageResource(R.drawable.ic_play_arrow);
        }
        if (audioRecorder.isRecording()) {
            imgMic.setImageResource(R.drawable.ic_mic);
            graphView.stopPlotting();
            audioRecorder.stop();
            audioRecorder.release();
            graphView.showFullGraph(samples);
            timerCounter.cancel();
            //txtCount.setText("00:00:00.000");
            endTime = new DateTime();
            isRecording = false;
            layoutPlay.setBackgroundResource(R.drawable.bg_record);
            layoutUpload.setBackgroundResource(R.drawable.bg_record);
            layoutRecord.setBackgroundResource(R.drawable.bg_record);

        } else if (checkRecordPermission() && checkStoragePermission()) {
            initRecorder();
            graphView.reset();
            audioRecorder.startRecord();
            audioRecorder.startPlotting(graphView);
            imgMic.setImageResource(R.drawable.ic_stop);
            timerCounter = new TimerCounter(this, this);
            startTime = new DateTime();
            isRecording = true;
            recordFile = audioRecorder.getVoiceFilePath();
            layoutPlay.setBackgroundResource(R.drawable.bg_record_disable);
            layoutUpload.setBackgroundResource(R.drawable.bg_record_disable);
            layoutRecord.setBackgroundResource(R.drawable.bg_record);
        } else {
            requestPermissions();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        graphView.stopPlotting();
        if (audioRecorder.isRecording()) {
            audioRecorder.stop();
        }
    }

    public void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CODE);

        } else {
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CODE);
            // MY_PERMISSIONS_REQUEST_CODE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    private boolean checkRecordPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onTimerCount(String tick) {
        txtCount.setText(tick);
    }

    public void playBackClick(View view) {
        if (isRecording){
            //Toast.makeText(this, "Ai-tec still recording...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUploading){
            //Toast.makeText(this, "Ai-tec still uploading...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (recordFile.equals("")){
            //Toast.makeText(this, "Record file has been uploaded",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(recordFile);
                mPlayer.prepare();
                mPlayer.start();
                imgPlay.setImageResource(R.drawable.ic_stop);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayer.stop();
                    mPlayer = null;
                    imgPlay.setImageResource(R.drawable.ic_play_arrow);
                    layoutPlay.setBackgroundResource(R.drawable.bg_record);
                    layoutUpload.setBackgroundResource(R.drawable.bg_record);
                    layoutRecord.setBackgroundResource(R.drawable.bg_record);
                }
            });

            layoutPlay.setBackgroundResource(R.drawable.bg_record);
            layoutUpload.setBackgroundResource(R.drawable.bg_record_disable);
            layoutRecord.setBackgroundResource(R.drawable.bg_record_disable);
        } else {
            mPlayer.stop();
            mPlayer.setOnCompletionListener(null);
            mPlayer = null;
            imgPlay.setImageResource(R.drawable.ic_play_arrow);
            layoutPlay.setBackgroundResource(R.drawable.bg_record);
            layoutUpload.setBackgroundResource(R.drawable.bg_record);
            layoutRecord.setBackgroundResource(R.drawable.bg_record);
        }
    }


    public void uploadFileClick(View view) {
        if (isRecording){
            //Toast.makeText(this, "Ai-tec still recording...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUploading){
            //Toast.makeText(this, "Ai-tec still uploading...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPlayer != null){
            //Toast.makeText(this, "Ai-tec still uploading...",Toast.LENGTH_SHORT).show();
            return;
        }

        if (recordFile.equals("")){
           // Toast.makeText(this, "Record file has been uploaded",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer = null;
            imgPlay.setImageResource(R.drawable.ic_play_arrow);
        }

        layoutPlay.setBackgroundResource(R.drawable.bg_record_disable);
        layoutUpload.setBackgroundResource(R.drawable.bg_record_disable);
        layoutRecord.setBackgroundResource(R.drawable.bg_record_disable);

        isUploading = true;
        layoutProgress.animate().alpha(1f);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        txtProgress.setText("0.0 %");
        //client
        OkHttpClient okHttpClient = new OkHttpClient();
        //request builder
        Request.Builder builder = new Request.Builder();
        builder.url(Globals.MEMO_UPLOAD);

        File uploadFile = new File(recordFile);

        DateTimeFormatter fmt = DateTimeFormat.forPattern(Globals.timeZoneFormatter);
        //your original request body
        String a = fmt.print(startTime);
        String b = fmt.print(endTime);
        String c = String.valueOf(currentLocation.getLatitude());
        String d = String.valueOf(currentLocation.getLongitude());
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.addFormDataPart("link_video", uploadFile.getName(), RequestBody.create(MediaType.parse("audio/*"), uploadFile));
        bodyBuilder.addFormDataPart("sender_id", new SharePreference(this).getLogin()[0]);
        bodyBuilder.addFormDataPart("password", new SharePreference(this).getLogin()[1]);
        bodyBuilder.addFormDataPart("start_time", fmt.print(startTime));
        bodyBuilder.addFormDataPart("end_time", fmt.print(endTime));
        bodyBuilder.addFormDataPart("collection_time", fmt.print(endTime));
        bodyBuilder.addFormDataPart("lat", String.valueOf(currentLocation.getLatitude()));
        bodyBuilder.addFormDataPart("lng", String.valueOf(currentLocation.getLongitude()));

        bodyBuilder.setType(MultipartBody.FORM);
        MultipartBody body = bodyBuilder.build();

        //wrap your original request body with progress
        RequestBody requestBody = ProgressHelper.withProgress(body, new ProgressUIListener() {

            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
            @Override
            public void onUIProgressStart(long totalBytes) {
                super.onUIProgressStart(totalBytes);
                Log.e("TAG", "onUIProgressStart:" + totalBytes);
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                Log.e("TAG", "=============start===============");
                Log.e("TAG", "numBytes:" + numBytes);
                Log.e("TAG", "totalBytes:" + totalBytes);
                Log.e("TAG", "percent:" + percent);
                Log.e("TAG", "speed:" + speed);
                Log.e("TAG", "============= end ===============");
                progressBar.setProgress((int) (100 * percent));
                DecimalFormat precision = new DecimalFormat("0.0");
                txtProgress.setText(String.valueOf(precision.format(100 * percent)) + " %");
            }

            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
            @Override
            public void onUIProgressFinish() {
                super.onUIProgressFinish();
                Log.e("TAG", "onUIProgressFinish:");
            }

        });

        //post the wrapped request body
        builder.post(requestBody);
        //call
        Call call = okHttpClient.newCall(builder.build());
        //enqueue
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "=============onFailure===============");
                isUploading = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutProgress.animate().alpha(0f);
                        layoutPlay.setBackgroundResource(R.drawable.bg_record_disable);
                        layoutUpload.setBackgroundResource(R.drawable.bg_record_disable);
                        layoutRecord.setBackgroundResource(R.drawable.bg_record);
                        graphView.reset();
                        graphView.clear();
                        txtCount.setText("00:00:00.000");
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseString = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isUploading = false;
                        layoutPlay.setBackgroundResource(R.drawable.bg_record_disable);
                        layoutUpload.setBackgroundResource(R.drawable.bg_record_disable);
                        layoutRecord.setBackgroundResource(R.drawable.bg_record);
                        graphView.reset();
                        graphView.clear();
                        txtCount.setText("00:00:00.000");
                        layoutProgress.animate().alpha(0f);
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.has("code")) {
                                if (jsonObject.getInt("code") == 200) {
                                    Toast.makeText(VoiceMemoActivity.this, "Upload voice memo success", Toast.LENGTH_SHORT).show();
                                    recordFile = "";
                                }else
                                    Toast.makeText(VoiceMemoActivity.this, "Upload voice memo failure",Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(VoiceMemoActivity.this, "Upload voice memo failure",Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStopUpdate() {

    }

    @Override
    public void onUpdate(Location mCurrentLocation) {
        currentLocation = mCurrentLocation;
        checkDistanceMemo(mCurrentLocation);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationProvide.stopLocationUpdates();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_CHANGE_STATUS);
            jsonObject.put("host", Globals.name_client);
            jsonObject.put("state", 1);
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void finishClick(View view) {
        finish();
    }
}
