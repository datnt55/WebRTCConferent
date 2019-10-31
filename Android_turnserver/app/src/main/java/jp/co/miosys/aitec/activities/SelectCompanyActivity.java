package jp.co.miosys.aitec.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.HOST;
import static jp.co.miosys.aitec.utils.Globals.KMS_ROOT;
import static jp.co.miosys.aitec.utils.Globals.MIO_HOST;
import static jp.co.miosys.aitec.utils.Globals.SERVER_IP;
import static jp.co.miosys.aitec.utils.Globals.TurnServerURI;
import static jp.co.miosys.aitec.utils.Globals.URL_LIST_MEMO;
import static jp.co.miosys.aitec.utils.Globals.kmlHelper;
import static jp.co.miosys.aitec.utils.SocketUtils.isCloseSocket;

public class SelectCompanyActivity extends AppCompatActivity {
    private EditText edtCompanyCode;
    private ProgressBar progressBar;
    private SharePreference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_company);
        if (socketUtils != null) {
            isCloseSocket = false;
            socketUtils.release();
            socketUtils = null;
            kmlHelper = null;
        }
        preference = new SharePreference(this);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        edtCompanyCode = (EditText) findViewById(R.id.spinner);
        edtCompanyCode.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//        arrayCompany = new ArrayList<>();
//        arrayCompany.add("Kanto Construction Management");
//        arrayCompany.add("Tokyo Consultant");
//        arrayCompany.add("Asanuma-Gumi");
//        arrayCompany.add("Road Maintenance");
//
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayCompany);
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        edtCompanycode.setAdapter(dataAdapter);
//
//        edtCompanycode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                switch (position){
//                    case 0:
//                        company = "lNpKtI";
//                        break;
//                    case 1:
//                        company = "unpFuN";
//                        break;
//                    case 2:
//                        company = "dHwUBm";
//                        break;
//                    case 3:
//                        company = "F81xpF";
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        edtCompanyCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if  ((actionId == EditorInfo.IME_ACTION_DONE)) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtCompanyCode.getWindowToken(), 0);
                    findViewById(R.id.btn_select).performClick();
                    return true;

                }
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            progressBar.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {
            progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void onSelectCompany(final View view) {
        view.setClickable(false);
        view.setEnabled(false);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtCompanyCode.getWindowToken(), 0);
        JSONObject json = new JSONObject();
        try {
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
            json.put("company_code", edtCompanyCode.getText().toString());
        }catch (JSONException ex){

        }
        showProgress(true);
        new OkHttpService(OkHttpService.Method.POST, false, this, Globals.CHOOSE_COMPANY, json, false) {
            @Override
            public void onFailureApi(Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setClickable(true);
                        view.setEnabled(true);
                        Toast.makeText(SelectCompanyActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponseApi(Call call, Response response) throws IOException {
                String result = response.body().string();
                preference.saveCompany(edtCompanyCode.getText().toString());
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    preference.saveCompanyName(data.getString("name"));
                    MIO_HOST = data.getString("domain");
                    KMS_ROOT = data.getString("kms");
                    //KMS_ROOT = "192.168.1.99";
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
                            showProgress(false);
                            view.setClickable(true);
                            view.setEnabled(true);
                            Intent intent = new Intent(SelectCompanyActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            view.setClickable(true);
                            view.setEnabled(true);
                            Toast.makeText(SelectCompanyActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
    }
}
