package jp.co.miosys.aitec.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.co.miosys.aitec.views.listeners.OnGetImageDownloadListener;

/**
 * [20170913] Ductx: #2595: Dowload image from server
 */

public class DowloadImageAsynTask extends AsyncTask<String, Void, String> {

    private String mImageUrl;
    private Context mContext;
    private Bitmap mBitmap;
    private FileOutputStream fos;
    private static OnGetImageDownloadListener mListener;

    public static void getImageDowloadComplete(OnGetImageDownloadListener listener) {
        mListener = listener;
    }

    public DowloadImageAsynTask(String mImageUrl, Context mContext) {
        this.mImageUrl = mImageUrl;
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            File fileLog = ReadWriteFileUtils.createLogFile();
            saveFile(fileLog);
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(fileLog)));

            return fileLog.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String path) {
        super.onPostExecute(path);
        if (!path.equals("")) {
            mListener.onGetImageDownload(path);
        }
    }

    // Code Error
    public String renameFileName(String path) {
        File from = new File(Globals.IMAGE_DIRECTORY, ReadWriteFileUtils.getFileName(path));
        String currentTime = CommonUtils.getCurrentLocalTimeFormat(Globals.patternFileName);
        File to = new File(Globals.IMAGE_DIRECTORY, Globals.name_guest + "_" + currentTime + ".jpg");
        from.renameTo(to);
        return to.getAbsolutePath();
    }

    public void saveFile(File file) {
        try {
            URL ulrn = new URL(mImageUrl);
            HttpURLConnection con = (HttpURLConnection) ulrn.openConnection();
            InputStream is = con.getInputStream();

            OutputStream fOut = null;

            fOut = new FileOutputStream(file);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                fOut.write(bytes, 0, read);
            }
            is.close();

            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
