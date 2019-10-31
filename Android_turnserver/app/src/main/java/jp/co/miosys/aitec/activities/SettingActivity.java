package jp.co.miosys.aitec.activities;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.utils.SharePreference;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    private SwitchCompat switchVibrate;
    private SeekBar seekBarVolume;
    private SharePreference preference;
    private AudioManager audio;
    private ImageView btnBack;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        preference = new SharePreference(this);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switchVibrate = (SwitchCompat) findViewById(R.id.switch_vibrate);
        switchVibrate.setChecked(preference.getVibrate());

        seekBarVolume = (SeekBar) findViewById(R.id.seekBar_volume);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekBarVolume.setMax(maxVolume);
        if (preference.getVolume() == -1) {
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekBarVolume.setProgress(currentVolume/2);
        }else {
            seekBarVolume.setProgress(preference.getVolume());
        }

        switchVibrate.setOnCheckedChangeListener(this);
        seekBarVolume.setOnSeekBarChangeListener(this);

        btnBack = (ImageView) findViewById(R.id.btn_back);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTitle.setText("着信設定");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public void finishSetting(View view) {
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        preference.saveVibrate(isChecked);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        preference.saveVolume(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
