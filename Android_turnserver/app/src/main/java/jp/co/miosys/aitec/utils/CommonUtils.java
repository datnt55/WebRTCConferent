package jp.co.miosys.aitec.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.activities.MapActivity;
import jp.co.miosys.aitec.activities.MemoMapActivity;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.views.listeners.AnimateFirstDisplayListener;

import static android.content.Context.NOTIFICATION_SERVICE;
import static jp.co.miosys.aitec.utils.Globals.PUSH_EXTRA_MEMO;

/**
 * Created by Duc on 8/29/2017.
 */

public class CommonUtils {

    // Convert from dpi to pixel
    public static int convertDpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static void toastNoConnect(Context context) {
        Toast.makeText(context, R.string.error_connect_internet, Toast.LENGTH_SHORT).show();
        return;
    }

    public static LatLng exif2Loc(String flNm) {
        String sLat = "", sLatR = "", sLon = "", sLonR = "";
        try {
            ExifInterface ef = new ExifInterface(flNm);
            sLat = ef.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            sLon = ef.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            sLatR = ef.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            sLonR = ef.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        } catch (IOException e) {
            return null;
        }

        double lat = dms2Dbl(sLat);
        if (lat > 180.0) return null;
        double lon = dms2Dbl(sLon);
        if (lon > 180.0) return null;

        lat = sLatR.contains("S") ? -lat : lat;
        lon = sLonR.contains("W") ? -lon : lon;

        return new LatLng(lat, lon);
    }

    //-------------------------------------------------------------------------
    public static double dms2Dbl(String sDMS) {
        double dRV = 999.0;
        try {
            String[] DMSs = sDMS.split(",", 3);
            String s[] = DMSs[0].split("/", 2);
            dRV = (new Double(s[0]) / new Double(s[1]));
            s = DMSs[1].split("/", 2);
            dRV += ((new Double(s[0]) / new Double(s[1])) / 60);
            s = DMSs[2].split("/", 2);
            dRV += ((new Double(s[0]) / new Double(s[1])) / 3600);
        } catch (Exception e) {
        }
        return dRV;
    }

    public static String getUUID(Context context) {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tManager.getDeviceId() == null)
            return randomUUID();
        return tManager.getDeviceId();
    }

    public static String getCurrentActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.toString();
    }

    public static String getCurrentLocalTimeFormat(String format) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
        return fmt.print(new DateTime());
    }

    public static Bitmap drawTimeOnBitmap(Bitmap bm) {
        Paint geoPaint = new Paint();
        geoPaint.setAntiAlias(true);
        geoPaint.setColor(android.graphics.Color.YELLOW);
        geoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        Bitmap imageBitmap = Bitmap.createBitmap(bm);
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        String currentTime = getCurrentLocalTimeFormat(Globals.patternImageName);

        Canvas canvas = new Canvas(mutableBitmap);
        // 12102017 Ductx: Caculator textSize paint follow bitmap.
        int margin = bm.getWidth() / 20;
        int textWidth = bm.getWidth() * 4 / 5 - margin;
        for (int i = 2; i < textWidth; i += 2) {
            geoPaint.setTextSize(i);
            if (geoPaint.measureText(currentTime) >= textWidth) {
                canvas.drawText(currentTime, margin, bm.getHeight() - margin, geoPaint);
                break;
            }
        }
        return mutableBitmap;
    }

    public static String encodedImage(String filePath) {
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        return encodedImage;
    }

    public static boolean haveNetWork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String contentNoti(Context context, String userName, String dateTime, String latitude, String longtitude) {
        String result = userName + " " + context.getResources().getString(R.string.noti_text1) + " "
                + dateTime.split(" ")[0] + " " + context.getResources().getString(R.string.noti_text2) + " "
                + dateTime.split(" ")[1] + " " + context.getResources().getString(R.string.noti_text3) + " "
                + latitude + " " + context.getResources().getString(R.string.noti_text2) + " "
                + longtitude + " "
                + context.getResources().getString(R.string.noti_text4);
        return result;
    }

    public static void createLogFolder() {
        File rootFolder = new File(Globals.ROOT_DIRECTORY);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        File userFolder = new File(Globals.ROOT_DIRECTORY + "/" + Globals.name_client);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
        File patnerFolder = new File(Globals.ROOT_DIRECTORY + "/" + Globals.name_client + "/" + Globals.name_guest);
        if (!patnerFolder.exists()) {
            patnerFolder.mkdirs();
        }
        Globals.IMAGE_DIRECTORY = Globals.ROOT_DIRECTORY + "/" + Globals.name_client + "/" + Globals.name_guest;
        Globals.IMAGE_CLOSED_DIRECTORY = Globals.IMAGE_DIRECTORY + "/Close";
    }

    public static void getSizeScreen(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Globals.HEIGHT_SCREEN = displayMetrics.heightPixels;
        Globals.WIDTH_SCREEN = displayMetrics.widthPixels;
    }

    public static String randomUUID() {
        Random r = new Random();
        int range = 8999999;
        int result = r.nextInt(9999999 - range) + range;
        return String.valueOf(result);
    }

    public static void imageLoader(ImageView imageView, String imageUrl) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (imageUrl.contains("https"))
                imageUrl = imageUrl.replace("https","http");
        }

        ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.loading)
                .showImageOnFail(R.drawable.loading)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader.getInstance().displayImage(imageUrl, imageView, options, animateFirstListener);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    public final static double AVERAGE_RADIUS_OF_EARTH = 6371;
    public static double calculateDistance(double userLat, double userLng, double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                (Math.cos(Math.toRadians(userLat))) *
                        (Math.cos(Math.toRadians(venueLat))) *
                        (Math.sin(lngDistance / 2)) *
                        (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return AVERAGE_RADIUS_OF_EARTH * c;

    }

    public static void showNotification(Activity mContext, Memo memo){
        Intent intent = new Intent(mContext, MemoMapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(PUSH_EXTRA_MEMO, memo);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setAutoCancel(true)
                .setContentTitle(memo.getCategoryName())
                .setContentText(memo.getContent())
                .setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.emergency))
                .setVibrate(new long[]{1, 1, 1})
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(memo.getContent()));
        NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        int id;
        if (memo.getCategoryId() == null || memo.getCategoryId().equals("null")) {
            Random rand = new Random();
            id = rand.nextInt(50);
        }else
            id = Integer.valueOf(memo.getCategoryId());
        manager.notify(id, builder.build());
    }
}
