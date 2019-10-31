package jp.co.miosys.aitec.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import jp.co.miosys.aitec.models.Image;

/**
 * Created by Duc on 9/26/2017.
 */

public class ReadWriteFileUtils {

    private static SharePreference preference;

    public static File createTempFileDontSend(Context context) {
        preference = new SharePreference(context);
        String userName = preference.getLogin()[0];
        File file = new File(Globals.IMAGE_DONT_SEND_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
        String currentTime = CommonUtils.getCurrentLocalTimeFormat(Globals.patternFileName);

        file = new File(Globals.IMAGE_DONT_SEND_DIRECTORY, userName + "_" + currentTime + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void copyFile(String src, String dst) throws IOException {
        InputStream in = new FileInputStream(new File(src));
        try {
            OutputStream out = new FileOutputStream(new File(dst));
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static File createLogFile() {
        File file = new File(Globals.IMAGE_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
        String currentTime = CommonUtils.getCurrentLocalTimeFormat(Globals.patternFileName);
        file = new File(Globals.IMAGE_DIRECTORY, Globals.name_guest + "_" + currentTime + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static ArrayList<Image> getImagesFromFolder(String folderPath) {
        ArrayList<Image> imagePathArr = new ArrayList<>();
        File file = new File(folderPath);
        if (!file.exists()) {
            return imagePathArr;
        }
        // listview Item la anh
        File[] allFiles = (new File(folderPath)).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
            }
        });

        for (int i = 0; i < allFiles.length; i++) {
            imagePathArr.add(new Image(allFiles[i].toString()));
        }
        // Sorting
        Collections.sort(imagePathArr, new Comparator<Image>() {
            @Override
            public int compare(Image ms2, Image ms1) {
                return ms1.getImagePath().compareTo(ms2.getImagePath());
            }
        });
        return imagePathArr;
    }

    public static String changeFormatDateTime(String dateTime, String formatInput, String formatOutput) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(formatInput);
        SimpleDateFormat outputFormat = new SimpleDateFormat(formatOutput);
        try {
            Date date = inputFormat.parse(dateTime);
            dateTime = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static String getTimeStampInFileName(String filePath) {
        // Divide userName and timestamp
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String timeStamp = fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf("."));
        String result = changeFormatDateTime(timeStamp, Globals.patternFileName, Globals.patternImageName);
        return result;
    }

    public static String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public static void deleteFolder(String folderPath) {
        File dir = new File(folderPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    public static void deleteFile(String filePath) {
        File dir = new File(filePath);
        dir.delete();
    }

    public static void saveExifGps(final Context context, final File file) {
            final GPSTracker tracker = new GPSTracker(context);
            if (tracker.canGetLocation()) {
                final ProgressDialog dialog = new ProgressDialog(context);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage("Đang lấy vị trí...");
                dialog.show();
                if (tracker.getLongitude() == 0 && tracker.getLatitude() == 0) {
                    tracker.getLocationCoodinate(new GPSTracker.LocateListener() {
                        @Override
                        public void onLocate(double mlongitude, double mlatitude) {
                            saveInternalExifGPS(tracker, file);
                            ((Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                } else {
                    saveInternalExifGPS(tracker, file);
                    dialog.dismiss();
                }
            }

    }

    private static void saveInternalExifGPS(GPSTracker tracker,File file){
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, dec2DMS(tracker.getLatitude()));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, dec2DMS(tracker.getLongitude()));
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, CommonUtils.getCurrentLocalTimeFormat(Globals.patternExifDateTime));

            if (tracker.getLatitude() > 0)
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            else
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            if (tracker.getLongitude() > 0)
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            else
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reWriteExifGps(Context context, File file, String[] exifData) {
        try {
            GPSTracker tracker = new GPSTracker(context);
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exifData[0]);
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exifData[1]);
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, exifData[2]);
            if (tracker.getLatitude() > 0)
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            else
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            if (tracker.getLongitude() > 0)
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            else
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] getExifGps(File file) {
        String lat = "", lon = "", timeStamp = "";
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            lat = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            lon = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            timeStamp = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] result = new String[]{lat, lon, timeStamp};
        return result;
    }

    private static String dec2DMS(double coord) {
        coord = coord > 0 ? coord : -coord;  // -105.9876543 -> 105.9876543
        String sOut = Integer.toString((int) coord) + "/1,";   // 105/1,
        coord = (coord % 1) * 60;         // .987654321 * 60 = 59.259258
        sOut = sOut + Integer.toString((int) coord) + "/1,";   // 105/1,59/1,
        coord = (coord % 1) * 60000;             // .259258 * 60000 = 15555
        sOut = sOut + Integer.toString((int) coord) + "/1000";   // 105/1,59/1,15555/1000
        return sOut;
    }
}
