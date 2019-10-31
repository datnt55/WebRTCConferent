package jp.co.miosys.aitec.utils;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import java.io.IOException;

/*[20170922] Ductx: #2688: Ringtone and sound when incoming*/

public class RingToneAndSound {

    private Vibrator v;
    private MediaPlayer mp = new MediaPlayer();
    private Context mContext;
    private AudioManager audio;
    private final long[] pattern = new long[]{0, 1000, 2000, 1000, 2000, 1000, 2000, 1000, 2000, 1000, 2000};
    private final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    private SharePreference preference;

    public RingToneAndSound(Context mContext) {
        this.mContext = mContext;
        preference = new SharePreference(mContext);
        initRingTone();
    }

    private void initRingTone() {
        audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        try {
            mp.setDataSource(mContext, uri);
            mp.prepare();
            mp.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void vibratorAndSound(final boolean isStart) {
                if (isStart) {
                    if (preference.getVibrate())
                        v.vibrate(pattern, 0);
                    if (preference.getVolume() != -1){
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, preference.getVolume(), 0);
                        mp.start();
                    }else {
                        int maxVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume/2, 0);
                        mp.start();
                    }

                } else {
                    mp.stop();
                    mp.release();
                    v.cancel();
                }
    }
}
