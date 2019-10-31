package jp.co.miosys.aitec.views.services;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class OkHttpService {
    private static final String AUTH_TOKEN = "Basic T1BFTlZJRFVBUFA6dm02OXZtNjk=";
    private boolean isAuthor = false;

    public enum Method {
        POST,
        GET
    }

    public abstract void onFailureApi(Call call, Exception e);

    public abstract void onResponseApi(Call call, Response response) throws IOException;

    private ProgressDialog dialog;

    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public OkHttpService(Method method, final Context context, String url, Map<String, Object> params, boolean isShowProgress) {
        if (method == Method.POST)
            postOkHttpApi(context, url, params, isShowProgress);
        else if (method == Method.GET)
            getOkHttpApi(context, url, params, isShowProgress);
    }

    public OkHttpService(Method method, boolean isAuthor, final Context context, String url, JSONObject json, boolean isShowProgress) {
        this.isAuthor = isAuthor;
        if (method == Method.POST)
            postOkHttpApi(context, url, json, isShowProgress);
        else if (method == Method.GET)
            getOkHttpApi(context, url, null, isShowProgress);
    }

    public static final int API_REQUEST_TIMEOUT = 15000; // SECOND

    private void postOkHttpApi(final Context context, final String url, final Map<String, Object> params, final boolean isShowProgress) {
        try {
            if (isShowProgress)
                showLoadingDialog(context);
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .readTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .writeTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .hostnameVerifier(allHostsValid)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .retryOnConnectionFailure(false);

            final OkHttpClient client = builder.build();

            MultipartBody.Builder multipart = new MultipartBody.Builder();
            if (params != null)
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (param.getValue() instanceof File)
                        multipart.addFormDataPart(param.getKey(), ((File) param.getValue()).getName(), RequestBody.create(MediaType.parse("application/vnd.google-earth.kml+xml"), (File) param.getValue()));
                    else
                        multipart.addFormDataPart(param.getKey(), (String) param.getValue());
                }

            RequestBody requestBody = multipart
                    .setType(MultipartBody.FORM)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", AUTH_TOKEN)
                    .addHeader("content-type", "application/json")
                    //.addHeader("version", Constants.version)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (dialog != null) dialog.dismiss();
                    onFailureApi(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (dialog != null) dialog.dismiss();

//                    String title = url + "\n";
//                    for (Map.Entry<String, Object> param : params.entrySet()) {
//                        title += param.getKey();
//                        if (param.getValue() instanceof File)
//                            title += ":" + ((File) param.getValue()).getAbsolutePath();
//                        else
//                            title += ":" + param.getValue();
//                    }

                    if (response.isSuccessful() || response.code() == 400 || response.code() == 401 || response.code() == 403 || response.code() == 500)
                        onResponseApi(call, response);
                    else
                        onFailureApi(call, new Exception(response.code() + ""));
                        //showErrorTest(context, title + "\n" + response.body().string());
                }
            });
        } catch (Exception e) {
            if (dialog != null) dialog.dismiss();
            onFailureApi(null, e);
        }
    }

    private void postOkHttpApi(final Context context, final String url, final JSONObject json, final boolean isShowProcess) {
        try {
            if (isShowProcess)
                showLoadingDialog(context);


            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .readTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .writeTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .hostnameVerifier(allHostsValid)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .retryOnConnectionFailure(false);

            final OkHttpClient client = builder.build();
            RequestBody body = null;
            if (json != null)
                body = RequestBody.create(JSON, json.toString());
            Request request;
            if (isAuthor) {
                if (body != null) {
                    request = new Request.Builder()
                            .url(url)
                            .post(body)
                            //.addHeader("Accept", "application/json")
                            .addHeader("Authorization", AUTH_TOKEN)
                            .addHeader("content-type", "application/json")
                            // .addHeader("Device-Type", "android")
                            // .addHeader("version", Constants.version)
                            .build();
                } else {
                    RequestBody reqbody = RequestBody.create(null, new byte[0]);
                    request = new Request.Builder()
                            .url(url)
                            .method("POST", reqbody)
                            .addHeader("Authorization", AUTH_TOKEN)
                            .addHeader("content-type", "application/json")
                            // .addHeader("Device-Type", "android")
                            // .addHeader("version", Constants.version)
                            .build();
                }

            } else {
                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        //.addHeader("Accept", "application/json")
                        .addHeader("content-type", "application/json")
                        // .addHeader("Device-Type", "android")
                        // .addHeader("version", Constants.version)
                        .build();
            }
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (dialog != null) dialog.dismiss();
                    onFailureApi(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (dialog != null) dialog.dismiss();
                    if (response.isSuccessful() || response.code() == 400 || response.code() == 401 || response.code() == 403 || response.code() == 500)
                        onResponseApi(call, response);
                    else
                        onFailureApi(call, null);
                    //showErrorTest(context, title + "\n" + response.body().string());
                }
            });

        } catch (Exception e) {
            if (dialog != null) dialog.dismiss();
            onFailureApi(null, e);
        }
    }

    private void getOkHttpApi(final Context context, final String url, final Map<String, Object> params, final boolean isShowProcess) {
        try {
            if (isShowProcess)
                showLoadingDialog(context);
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .readTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .writeTimeout(API_REQUEST_TIMEOUT, MILLISECONDS)
                    .hostnameVerifier(allHostsValid)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .retryOnConnectionFailure(false);

            final OkHttpClient client = builder.build();
            HttpUrl.Builder httpBuider = HttpUrl.parse(url).newBuilder();
            if (params != null) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    httpBuider.addQueryParameter(param.getKey(), (String) param.getValue());
                }
            }
            Request request = new Request.Builder()
                    .url(httpBuider.build())
                    .get()
                    // .addHeader("Accept", "application/json")
                    .addHeader("Authorization", AUTH_TOKEN)
                    .addHeader("content-type", "application/json")
                    //.addHeader("Device-Type", "android")
                    //.addHeader("version", Constants.version)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (dialog != null) dialog.dismiss();
                    onFailureApi(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (dialog != null) dialog.dismiss();

                    if (dialog != null) dialog.dismiss();
//                    String title = url + "\n";
//                    for (Map.Entry<String, Object> param : params.entrySet()) {
//                        title += param.getKey();
//                        if (param.getValue() instanceof File)
//                            title += ":" + ((File) param.getValue()).getAbsolutePath();
//                        else
//                            title += ":" + (String) param.getValue();
//                    }

                    if (response.isSuccessful() || response.code() == 400 || response.code() == 401 || response.code() == 403 || response.code() == 500)
                        onResponseApi(call, response);
                    else
                        onFailure(call,new IOException());
                }
            });

        } catch (Exception e) {
            if (dialog != null) dialog.dismiss();
            onFailureApi(null, e);
        }
    }

    private void showLoadingDialog(final Context context) {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Đang tải dữ liệu...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showErrorTest(final Context context, final String message) {
        new android.os.Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Lỗi server");
                dialog.setMessage(message);
                dialog.setCancelable(false);
                dialog.setNeutralButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onFailureApi(null, null);
                    }
                });
                dialog.show();
            }
        }, 0);
    }
}
