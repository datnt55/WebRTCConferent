package jp.co.miosys.aitec.views.services;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

/**
 * @date
 * @author THUCND-VMIO
 *
 */

public class BaseService {

	public static final int CONNECTION_TIME_OUT = 60000;
	public static final int RESPONSE_TIMEOUT = 60000;
	public static AsyncHttpClient syncHttpClient= new SyncHttpClient();
	public static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	/**
	 * Constructor
	 */
	public BaseService() {
	}
	public static void setCookieStore(PersistentCookieStore cookieStore) {
		getHttpClient().setCookieStore(cookieStore);
	  }

	  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		  getHttpClient().get(url, params, responseHandler);
	  }

	  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		  getHttpClient().post(url, params, responseHandler);
	  }
	/**
	 * Determine the network connection
	 * @param context
	 * @return TRUE if network connect is establishing
	 */
	public static boolean isConnect(Context context) {
		// Checking network configuration
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	/**
	 * Basic HttpClient
	 * @return
	 */
	public static AsyncHttpClient getHttpClient() {
		if (Looper.myLooper() == null)
	          return syncHttpClient;
		asyncHttpClient.setConnectTimeout(CONNECTION_TIME_OUT);
		asyncHttpClient.setResponseTimeout(RESPONSE_TIMEOUT);
		asyncHttpClient.setTimeout(CONNECTION_TIME_OUT);
//		asyncHttpClient.addHeader("Authorization", "Basic " + Base64.encodeToString((Globals.USERNAME + ":" + Globals.PASSWORD).getBytes(), Base64.NO_WRAP));
		return asyncHttpClient;
	}
}
