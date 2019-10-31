package jp.co.miosys.aitec.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Image;
import jp.co.miosys.aitec.models.LocationGPS;
import jp.co.miosys.aitec.utils.AnimationUtils;
import jp.co.miosys.aitec.utils.ApiProcesserUtils;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.CustomClick;
import jp.co.miosys.aitec.utils.DowloadImageAsynTask;
import jp.co.miosys.aitec.utils.DrawPictureDialog;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.ImageZip;
import jp.co.miosys.aitec.utils.KMLHelper;
import jp.co.miosys.aitec.utils.ReadWriteFileUtils;
import jp.co.miosys.aitec.utils.SocketUtils;
import jp.co.miosys.aitec.views.adapters.GridViewAdapter;
import jp.co.miosys.aitec.views.listeners.OnGetImageDownloadListener;
import jp.co.miosys.aitec.views.listeners.OnShowImageListener;

import static jp.co.miosys.aitec.activities.LoginActivity.socketUtils;
import static jp.co.miosys.aitec.utils.Globals.kmlHelper;


/*[20170912] Ductx: #2596: Create ImageView activity*/

public class ImageViewActivity extends BaseActivity implements OnMapReadyCallback, OnShowImageListener, OnGetImageDownloadListener, SocketUtils.OnMessageReceive, CustomClick.OnClickListener {

    private ArrayList<Image> imagePathArr = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView txtNoImage;
    private ImageView imvBack, imvImage;
    private TextView txtShowImage, txtShowMap, txtDatetime, txtTitle;
    private TextView btnEdit;
    private String currentPath = "";
    private GoogleMap mMap;
    private FrameLayout lyMap;
    private Context mContext;
    private RecyclerView.Adapter adapterImg;
    private RelativeLayout layoutImage;
    private boolean isShowImage;
    private ArrayList<LocationGPS> listGPS = new ArrayList<>();
    private CustomClick customClick;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_image_view);
        mContext = this;
        initComponent();
        sendNotiIsViewImage(true);
    }

    public void initComponent() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.receive_image_title));
        layoutImage = (RelativeLayout) findViewById(R.id.layout_image);
        lyMap = (FrameLayout) findViewById(R.id.ly_map);
        txtNoImage = (TextView) findViewById(R.id.txt_no_image);
        txtShowImage = (TextView) findViewById(R.id.txt_show_image);
        txtShowMap = (TextView) findViewById(R.id.txt_show_map);
        txtDatetime = (TextView) findViewById(R.id.txt_datetime);
        imvImage = (ImageView) findViewById(R.id.imv_image);
        imvBack = (ImageView) findViewById(R.id.btn_back);
        btnEdit = (TextView) findViewById(R.id.btn_edit);
        customClick = new CustomClick(this);
        customClick.setView(btnEdit);
        imvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backEvent();
            }
        });
        //listGPS = (ArrayList<LocationGPS>) getIntent().getSerializableExtra(Globals.BUNDLE_SEND_GPS);

        imagePathArr = ReadWriteFileUtils.getImagesFromFolder(Globals.IMAGE_DIRECTORY);
        if (imagePathArr.size() > 0) {
            Image image = imagePathArr.get(0);
            currentPath = image.getImagePath();
            imagePathArr.set(0, new Image(image.getImagePath()));
            Bitmap myBitmap = BitmapFactory.decodeFile(new File(image.getImagePath()).getAbsolutePath());
            imvImage.setImageBitmap(myBitmap);
            txtDatetime.setText(ReadWriteFileUtils.getTimeStampInFileName(image.getImagePath()));
        } else {
            txtNoImage.setVisibility(View.VISIBLE);
        }

        // Create listener
        GridViewAdapter.getListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapterImg = new GridViewAdapter(this, imagePathArr);

        recyclerView.setAdapter(adapterImg);
        recyclerView.setHasFixedSize(true);

        //Set RecyclerView type according to intent value
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    public void onShowImage(String path) {
        Bitmap myBitmap = BitmapFactory.decodeFile(new File(path).getAbsolutePath());
        imvImage.setImageBitmap(myBitmap);
        try {
//            ExifInterface ef = new ExifInterface(path);
//            String timeStamp = ReadWriteFileUtils.changeFormatDateTime(ef.getAttribute(ExifInterface.TAG_DATETIME), Globals.patternExifDateTime, Globals.patternImageName);
            String timeStamp = ReadWriteFileUtils.getTimeStampInFileName(path);

            txtDatetime.setText(timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentPath = path;
        lyMap.setVisibility(View.GONE);
        layoutImage.setVisibility(View.VISIBLE);
        AnimationUtils.slideUp(layoutImage, -Globals.HEIGHT_SCREEN, 0, 200);
        isShowImage = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txtShowImage.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            txtShowMap.setBackgroundTintList(getResources().getColorStateList(R.color.gray_200));
        }
        ImageZip.getKML(path);
        String kml = path.replace("jpg", "kml");
        listGPS = new KMLHelper().getTrajectory(kml);
    }

    public void onClickShowImage(View view) {
        Bitmap myBitmap = BitmapFactory.decodeFile(new File(currentPath).getAbsolutePath());
        imvImage.setImageBitmap(myBitmap);
        lyMap.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txtShowImage.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            txtShowMap.setBackgroundTintList(getResources().getColorStateList(R.color.gray_200));
        }
    }

    public void onClickShowMap(View view) throws IOException {
        Bitmap myBitmap = BitmapFactory.decodeFile(new File(currentPath).getAbsolutePath());
        imvImage.setImageBitmap(myBitmap);
        lyMap.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txtShowImage.setBackgroundTintList(getResources().getColorStateList(R.color.gray_200));
            txtShowMap.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
        }
        showPositionInMap();
    }

    public void showPositionInMap() {
        LatLng latLng = CommonUtils.exif2Loc(currentPath);
        mMap.clear();
        if (latLng != null) {
            String textShow = Globals.name_guest + " (" + String.valueOf(latLng.longitude) + "/" + String.valueOf(latLng.latitude) + ")";
            if (listGPS.size() != 0) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(listGPS.get(0).getLatitude(), listGPS.get(0).getLongitude()))
                        .title(textShow));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(listGPS.get(listGPS.size() - 1).getLatitude(), listGPS.get(listGPS.size() - 1).getLongitude()))
                        .title(textShow)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.finish)));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to current location
                    .zoom(16)                   // Sets the zoom
                    .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(12);
        PolylineOptions polylineOptions1 = new PolylineOptions().
                geodesic(true).
                color(ContextCompat.getColor(mContext, R.color.blue_light)).
                width(8);
        for (int i = 0; i < listGPS.size(); i++) {
            polylineOptions.add(new LatLng(listGPS.get(i).getLatitude(), listGPS.get(i).getLongitude()));
            polylineOptions1.add(new LatLng(listGPS.get(i).getLatitude(), listGPS.get(i).getLongitude()));
        }
        mMap.addPolyline(polylineOptions);
        mMap.addPolyline(polylineOptions1);
        if (listGPS.size() > 0)
            updateMapCamera();
    }

    private void updateMapCamera() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LocationGPS marker : listGPS) {
            builder.include(new LatLng(marker.getLatitude(), marker.getLongitude()));
        }
        LatLngBounds bounds = builder.build();
        int padding = (int) CommonUtils.convertDpToPx(40, mContext); // offset from edges of the map in pixels
        mMap.setPadding(20, padding, 20, padding);
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Globals.WIDTH_SCREEN, Globals.HEIGHT_SCREEN, 0);
        mMap.animateCamera(cu);
    }

    private void onClickEditImage() {
        if (!currentPath.equals("")) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            File file = new File(currentPath);
            Globals.captureBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            DrawPictureDialog dialog = new DrawPictureDialog();
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            overlayBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] byteArray = stream.toByteArray();

            Bundle bundle = new Bundle();
            //bundle.putByteArray(Globals.BUNDLE_SEND_IMAGE,byteArray);
            bundle.putStringArray(Globals.BUNDLE_SEND_EXIF, ReadWriteFileUtils.getExifGps(file));
            dialog.setArguments(bundle);
            dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
            dialog.setCancelable(false);
            dialog.show(fragmentManager, "Input Dialog");
            dialog.setOnSendDataListener(new DrawPictureDialog.SendDataListener() {
                @Override
                public void onSend(File imageFile) {
                    String kmlFile = kmlHelper.getKMLFile();
                    boolean success;
                    synchronized (kmlHelper) {
                        success = kmlHelper.saveKMLFile(kmlFile);
                    }
                    if (success) {
                        String result = ReadWriteFileUtils.createTempFileDontSend(mContext).getAbsolutePath();
                        ImageZip.mergeFiles(imageFile.getAbsolutePath(), kmlFile, result);
                        //peerConnectionClient.sendData(locations);
                        UploadApplication.isUpload = true;
                        ApiProcesserUtils.sendImage(mContext, new File(result), Globals.id_guest, true);
                    }
                }
            });
        }
    }

    @Override
    public void onNoImage() {
        txtNoImage.setVisibility(View.VISIBLE);
    }

    /*[20170910] Ductx: #2595: Create layout for received image confirmation screen*/
    @Override
    public void onMessage(final String message) {
        try {
            JSONObject result = new JSONObject(message);
            String function = result.getString("type");
            if (function.equals(Globals.FUNCTION_SEND_IMAGE_URL)) {
                final String imageUrl = result.getString("url");
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        String url = imageUrl;
                        DowloadImageAsynTask asynTask = new DowloadImageAsynTask(url, mContext);
                        asynTask.execute();
                    }
                });
            }
            if (function.equals(Globals.FUNCTION_IS_VIEW_IMAGE)) {
                boolean data = result.getBoolean("data");
                if (data) {
                    Globals.isPatnerViewImage = true;
                } else {
                    Globals.isPatnerViewImage = false;
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onGetImageDownload(String imagePath) {
        if (imagePathArr.size() == 0) {
            currentPath = imagePath;
            Bitmap myBitmap = BitmapFactory.decodeFile(new File(imagePath).getAbsolutePath());
            if (myBitmap == null) {
                return;
            }
            imvImage.setImageBitmap(myBitmap);
            txtDatetime.setText(ReadWriteFileUtils.getTimeStampInFileName(imagePath));
            txtNoImage.setVisibility(View.GONE);
            imagePathArr.add(0, new Image(imagePath));
            adapterImg.notifyDataSetChanged();
        } else {
            receiveImageDialog(mContext, imagePath);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Init interface
        socketUtils.setCallBack(this, this);
        DowloadImageAsynTask.getImageDowloadComplete(this);
    }

    private static Dialog mDialog;

    /*[20170910] Ductx: #2595: Show dialog Received image confirmation*/
    public void receiveImageDialog(final Context context, final String imagePath) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            imagePathArr.add(0, new Image(imagePath));
            adapterImg.notifyDataSetChanged();
        }
        // custom dialog
        mDialog = new Dialog(context, R.style.Theme_Dialog);
        mDialog.setContentView(R.layout.custom_dialog_show_image);
        mDialog.setCancelable(false);

        // set the custom dialog components - text, image and button

        Button btnOpen = (Button) mDialog.findViewById(R.id.btn_open);
        Button btnCancel = (Button) mDialog.findViewById(R.id.btn_cancel);
        // if button is clicked, close the custom dialog
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap myBitmap = BitmapFactory.decodeFile(new File(imagePath).getAbsolutePath());
                if (myBitmap == null) {
                    return;
                }
                imvImage.setImageBitmap(myBitmap);
                txtDatetime.setText(ReadWriteFileUtils.getTimeStampInFileName(imagePath));
                currentPath = imagePath;
                imagePathArr.add(0, new Image(imagePath));
                adapterImg.notifyDataSetChanged();
                mDialog.dismiss();
                if (!isShowImage) {
                    layoutImage.setVisibility(View.VISIBLE);
                    AnimationUtils.slideUp(layoutImage, -Globals.HEIGHT_SCREEN, 0, 200);
                    isShowImage = true;
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePathArr.add(0, new Image(imagePath));
                adapterImg.notifyDataSetChanged();
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    @Override
    public void onBackPressed() {
        backEvent();
    }

    private void backEvent() {
        if (isShowImage) {
            layoutImage.setVisibility(View.GONE);
            AnimationUtils.slideDown(layoutImage, Globals.HEIGHT_SCREEN, 200);
            isShowImage = false;
        } else {
            sendNotiIsViewImage(false);
            finish();
        }
    }

    private void sendNotiIsViewImage(boolean data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", Globals.FUNCTION_IS_VIEW_IMAGE);
            jsonObject.put("receive", Globals.name_guest);
            jsonObject.put("data", data);
            String sendMessage = jsonObject.toString();
            socketUtils.sendMessage(sendMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:
                onClickEditImage();
                break;
        }
    }
}
