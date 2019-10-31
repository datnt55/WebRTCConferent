package jp.co.miosys.aitec.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.views.services.OkHttpService;
import okhttp3.Call;
import okhttp3.Response;

import static jp.co.miosys.aitec.utils.Globals.HOST;
import static jp.co.miosys.aitec.utils.Globals.KMS_ROOT;
import static jp.co.miosys.aitec.utils.Globals.MIO_HOST;
import static jp.co.miosys.aitec.utils.Globals.SERVER_IP;
import static jp.co.miosys.aitec.utils.Globals.TurnServerURI;

public class SplashActivity extends AppCompatActivity {
    private Activity mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;
        SharePreference preference = new SharePreference(this);
        if (!preference.getCompany().equals("")){
            onSelectCompany(preference);
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new SharePreference(mContext).saveLogIn("","");
                    Intent intent = new Intent(SplashActivity.this, SelectCompanyActivity.class);
                    startActivity(intent);
                }
            }, 1000);
        }
    }

    public void onSelectCompany(final SharePreference preference) {
        JSONObject json = new JSONObject();
        try {
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
            json.put("company_code", preference.getCompany());
        }catch (JSONException ex){

        }
        new OkHttpService(OkHttpService.Method.POST, false, this, Globals.CHOOSE_COMPANY, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    MIO_HOST = data.getString("domain");
                    KMS_ROOT = data.getString("kms");
                    SERVER_IP = "ws://" + KMS_ROOT + ":9090";
                    TurnServerURI = "turn:" + KMS_ROOT + ":3478";
                    HOST = "https://" + KMS_ROOT + ":4443";
                    Globals.TOKEN_URL = HOST +"/api/tokens";
                    Globals.SAVE_RECORDING_URL = HOST +"/api/recordings/stop/";
                    Globals.START_RECORDING_URL = HOST +"/api/recordings/start";
                    Globals.GET_RECORDING_URL = HOST +"/api/recordings";
                    Globals.SESSION_URL = HOST+"/api/sessions";
                    Globals.URL_SEND_IMAGE = MIO_HOST +"/api/v1/chat/upload-image";
                    Globals.SAVE_CALL_DETAIL = MIO_HOST +"/api/v1/chat/send-message";
                    Globals.SAVE_ONE_TEC = MIO_HOST +"/api/v1/chat/send-onetec";
                    Globals.MEMO_UPLOAD = MIO_HOST + "/api/v1/chat/send-voice-memo" ;
                    Globals.SAVE_USER = MIO_HOST +"/api/v1/user/login-username-uuid";
                    Globals.URL_KML = MIO_HOST +"/api/v1/logger/add";
                    Globals.URL_REGISTER_ROOM = MIO_HOST +"/api/v1/chat/add-room";
                    Globals.URL_LIST_MEMO = MIO_HOST +"/api/v1/memo/list";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
    }
}
