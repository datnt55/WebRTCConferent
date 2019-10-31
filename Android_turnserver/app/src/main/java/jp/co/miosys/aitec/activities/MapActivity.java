package jp.co.miosys.aitec.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.utils.Globals;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Double longtitude;
    private Double latitude;
    private String guestName = "";
    private ImageView btnBack;
    private TextView txtTitle;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng lng = new LatLng(latitude, longtitude);
        showPositionInMap(lng);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map);

        longtitude = getIntent().getDoubleExtra(Globals.PUSH_EXTRA_LONGTITUDE, 0);
        latitude = getIntent().getDoubleExtra(Globals.PUSH_EXTRA_LATLITUDE, 0);
        guestName = getIntent().getStringExtra(Globals.PUSH_GUEST_NAME);

        btnBack = (ImageView) findViewById(R.id.btn_back);
        txtTitle = (TextView) findViewById(R.id.txt_title);

        txtTitle.setText("Emergency Call From " + guestName);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void showPositionInMap(LatLng latLng) {
        // Add a marker in Sydney and move the camera
        mMap.clear();
        if (latLng != null) {
            String textShow = guestName + " (" + String.valueOf(longtitude) + "/" + String.valueOf(latitude) + ")";
            mMap.addMarker(new MarkerOptions().position(latLng).title(textShow).visible(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to current location
                    .zoom(16)                   // Sets the zoom
                    .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
