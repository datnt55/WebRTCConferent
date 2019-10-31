package jp.co.miosys.aitec.activities;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.kurento.VideoConferenceActivity;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.GPSTracker;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.NoticeDialog;
import jp.co.miosys.aitec.utils.SharePreference;
import jp.co.miosys.aitec.views.adapters.MemoAdapter;
import jp.co.miosys.aitec.views.services.OkHttpService;
import okhttp3.Call;
import okhttp3.Response;

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.PUSH_EXTRA_MEMO;
import static jp.co.miosys.aitec.utils.Globals.SESSION_URL;
import static jp.co.miosys.aitec.utils.Globals.URL_LIST_MEMO;
import static jp.co.miosys.aitec.utils.Globals.arrayMemo;

public class AlertListActivity extends AppCompatActivity implements MemoAdapter.MemoSelectListener {
    private RecyclerView listMemo;
    private MemoAdapter adapter;
    private ImageView btnBack;
    private TextView txtTitle;
    private TextView txtNoMemo;
    private GPSTracker gpsTracker;
    private ArrayList<Memo> memoArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        btnBack = (ImageView) findViewById(R.id.btn_back);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtNoMemo = (TextView) findViewById(R.id.txt_no_memo);
        txtTitle.setText("アラート受信リスト");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listMemo = (RecyclerView) findViewById(R.id.list_alert);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listMemo.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.sk_line_divider));
        listMemo.addItemDecoration(dividerItemDecoration);
        memoArrayList = new ArrayList<>();
        gpsTracker = new GPSTracker(this);
        Location location = gpsTracker.getLocation();

        for (Memo memo : arrayMemo) {
            if (calculateDistance(location, memo.getLat(), memo.getLon()) <= Globals.DISTANCE)
                memoArrayList.add(memo);
        }
        if (arrayMemo.size() <= 0){
            txtNoMemo.setVisibility(View.VISIBLE);
            listMemo.setVisibility(View.GONE);
        }else {
            txtNoMemo.setVisibility(View.GONE);
            listMemo.setVisibility(View.VISIBLE);
            adapter = new MemoAdapter(this, memoArrayList);
            adapter.setMemoSelectedListener(this);
            listMemo.setAdapter(adapter);
        }
    }

    private double calculateDistance(Location location1, double lat, double lon) {
        return CommonUtils.calculateDistance(location1.getLatitude(), location1.getLongitude(), lat, lon);
    }

    private void parseJson(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONArray data = jsonObject.getJSONArray("data");
            for (int i = 0 ; i < data.length(); i++) {
                JSONObject memoObj = data.getJSONObject(i);
                String categoryId = memoObj.getString("category_id");
                String categoryName = memoObj.getString("category_name");
                int userId = memoObj.getInt("user_id");
                int kmlId = memoObj.getInt("kml_id");
                String memoAt = memoObj.getString("collection_time");
                String content = memoObj.getString("content");
                double lat = memoObj.getDouble("lat");
                double lon = memoObj.getDouble("lng");

                Memo memo = new Memo(categoryId, categoryName, userId, kmlId, memoAt, content, lat, lon);
                arrayMemo.add(memo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSelectMemo(Memo memo) {
        Intent intent = new Intent(this, MemoMapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(PUSH_EXTRA_MEMO, memo);
        intent.putExtras(bundle);
        startActivity(intent);

    }
}
