package jp.co.miosys.aitec.views.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.co.miosys.aitec.models.Category;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.NoticeDialog;
import jp.co.miosys.aitec.utils.ParseJsonUtils;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.views.listeners.OnGetApiDataListener;
import okhttp3.Call;
import okhttp3.Response;

import static jp.co.miosys.aitec.utils.Globals.URL_LIST_MEMO;
import static jp.co.miosys.aitec.utils.Globals.URL_MEMO_CATEGORIES;

/**
 * Created by Duc on 4/11/2018.
 */

public class ApiProcessService {

    public void getMemoList(final AppCompatActivity context, final OnGetApiDataListener listener, final int requestCode) {
        SharePreference preference = new SharePreference(context);
        Map<String, Object> params = new HashMap<>();
        params.put("username", preference.getLogin()[0]);
        new OkHttpService(OkHttpService.Method.GET, context, URL_LIST_MEMO, params, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        NoticeDialog dialog = new NoticeDialog();
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable(Globals.BUNDLE, "Server or Internet is error");
//                        dialog.setArguments(bundle);
//                        dialog.setCancelable(false);
//                        context.getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
//                    }
//                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                ArrayList<Memo> result = ParseJsonUtils.parseListMemo(response.body().string());
                listener.onGetApiData(result, requestCode);
            }
        };
    }

    public void getMemoCategories(final AppCompatActivity context, final OnGetApiDataListener listener, final int requestCode) {
        SharePreference preference = new SharePreference(context);
        Map<String, Object> params = new HashMap<>();
       // params.put("username", preference.getLogin()[0]);
        new OkHttpService(OkHttpService.Method.GET, context, URL_MEMO_CATEGORIES, params, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        NoticeDialog dialog = new NoticeDialog();
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable(Globals.BUNDLE, "Server or Internet is error");
//                        dialog.setArguments(bundle);
//                        dialog.setCancelable(false);
//                        context.getSupportFragmentManager().beginTransaction().add(dialog, "tag").commitAllowingStateLoss();
//                    }
//                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                ArrayList<Category> result = ParseJsonUtils.parseCategories(response.body().string());
                listener.onGetApiData(result, requestCode);
            }
        };
    }
}