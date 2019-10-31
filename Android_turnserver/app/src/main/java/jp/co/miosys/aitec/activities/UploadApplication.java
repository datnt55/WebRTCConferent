package jp.co.miosys.aitec.activities;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.io.FilenameFilter;

import jp.co.miosys.aitec.utils.ALog;
import jp.co.miosys.aitec.utils.ApiProcesserUtils;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;

/**
 * Created by Duc on 10/25/2017.
 */

public class UploadApplication extends android.app.Application {

    private Context mContext;
    public static boolean isUpload;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initImageLoader(getApplicationContext());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    if (!isUpload) {
//                        if (CommonUtils.haveNetWork(mContext)) {
//                            File[] allFiles = (new File(Globals.IMAGE_DONT_SEND_DIRECTORY)).listFiles(new FilenameFilter() {
//                                public boolean accept(File dir, String name) {
//                                    return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
//                                }
//                            });
//                            if (allFiles != null && allFiles.length > 0) {
//                                ApiProcesserUtils.sendImage(mContext, allFiles[0], "", false);
//                            }
//                            isUpload = true;
//                        } else {
//                            isUpload = false;
//                        }
//                    }
//                }
//            }
//        }).start();
    }

    public void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them, or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
}
