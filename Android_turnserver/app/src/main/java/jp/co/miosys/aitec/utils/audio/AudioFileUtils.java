package jp.co.miosys.aitec.utils.audio;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

import static jp.co.miosys.aitec.utils.Globals.ROOT_DIRECTORY;

public class AudioFileUtils {

    private final static String AUDIO_PCM_BASEPATH = "/" + ROOT_DIRECTORY + "/VoiceRecorder/";


    public static String getPcmFileAbsolutePath(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("fileName isEmpty");
        }
        String mAudioRawPath = "";
        if (!fileName.endsWith(".pcm")) {
            fileName = fileName + ".pcm";
        }
        File file = new File(AUDIO_PCM_BASEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioRawPath = AUDIO_PCM_BASEPATH + fileName;

        return mAudioRawPath;
    }


    public static String getWavFileAbsolutePath(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }

        String mAudioWavPath = "";
        if (!fileName.endsWith(".wav")) {
            fileName = fileName + ".wav";
        }
        File file = new File(AUDIO_PCM_BASEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioWavPath = AUDIO_PCM_BASEPATH + fileName;
        return mAudioWavPath;
    }

    public static String getAmrFileAbsolutePath(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }

        String mAudioWavPath = "";
        if (!fileName.endsWith(".amr")) {
            fileName = fileName + ".amr";
        }
        File file = new File(AUDIO_PCM_BASEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioWavPath = AUDIO_PCM_BASEPATH + fileName;
        return mAudioWavPath;
    }

}

