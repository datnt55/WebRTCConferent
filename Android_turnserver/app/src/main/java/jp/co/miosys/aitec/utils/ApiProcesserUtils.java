package jp.co.miosys.aitec.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.activities.UploadApplication;
import jp.co.miosys.aitec.views.services.BaseService;


public class ApiProcesserUtils {

    private static ProgressDialog dialog;

    // send image
    public static void sendImage(final Context context, final File imageUri, String regIdGuest, final boolean isActivity) {
        if (!BaseService.isConnect(context)) {
            CommonUtils.toastNoConnect(context);
            return;
        }
        if (isActivity) {
            dialog = new ProgressDialog(context);
            dialog.setMessage("Sending ...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        RequestParams params;
        params = new RequestParams();
        params.put("reg_id", regIdGuest);
        try {
            params.put("data", imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BaseService.post(Globals.URL_SEND_IMAGE, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                // called when response HTTP status is "200 OK"
                Log.e("Json", new String(responseBody));
                final String result = new String(responseBody);
                if (isActivity) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ParseJsonUtils.parseJsonSendMesagge(context, result);
                        }
                    });

                }
                ReadWriteFileUtils.deleteFile(imageUri.toString());
                UploadApplication.isUpload = false;
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                //Log.e("Json", new String(responseBody));
                UploadApplication.isUpload = false;
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isActivity) {
                            dialog.dismiss();
                            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle(context.getString(R.string.notice));
                            alertDialogBuilder.setMessage(context.getString(R.string.send_image_error_message))
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                            android.app.AlertDialog alert = alertDialogBuilder.create();
                            alert.show();
                        }
                    }
                });

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
}
