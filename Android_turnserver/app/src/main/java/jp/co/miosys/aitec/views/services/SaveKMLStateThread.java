package jp.co.miosys.aitec.views.services;

import android.app.Activity;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileFilter;

import jp.co.miosys.aitec.utils.Globals;

import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.Globals.mMioTempDirectory;

/**
 * Created by DatNT on 12/14/2017.
 */

public class SaveKMLStateThread implements Runnable {
    private Activity mThis;
    private boolean stopFlag;
    private DateTime sendServerTime;
    private static final int SLEEP_TIME_BETWEEN_SEND_REALTIME = 10000;  // every 3 seconds
    private Thread mThread;
    private String uploadFolder;
    private String session;

    public SaveKMLStateThread(Activity context,String session) {
        this.mThis = context;
        this.stopFlag = false;
        this.session = session;
        this.uploadFolder = mMioTempDirectory;
        mThread = new Thread(this);
    }

    public synchronized void start() {
        if (!mThread.isAlive())
            mThread.start();
    }

    @Override
    public void run() {
        try {
            while (!stopFlag) {
                saveKML();
                Thread.sleep(SLEEP_TIME_BETWEEN_SEND_REALTIME);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (Globals.startThread) {
            Globals.startThread = false;
        }
    }

    public void stop() {
        this.stopFlag = true;
    }

    private synchronized void saveKML() {
        synchronized (kmlHelper ) {
            kmlHelper.saveKMLFile(kmlHelper.getKMLFile());
        }
    }
}
