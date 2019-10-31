package jp.co.miosys.aitec.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Color;
import jp.co.miosys.aitec.models.Line;
import jp.co.miosys.aitec.views.adapters.ColorSelectedAdapter;
import jp.co.miosys.aitec.views.adapters.LineSelectedAdapter;
import jp.co.miosys.aitec.views.widgets.DrawingView;
import jp.co.miosys.aitec.views.widgets.GridLineView;

public class DrawPictureDialog extends DialogFragment implements DrawingView.OnSendImageListener {
    private ArrayList<Color> arrayColor;
    private ArrayList<Line> arrayLine;
    private DrawingView drawing;
    private Context mContext;
    private Bitmap mImage;
    private String[] exifData;
    private Spinner spColor, spLine;
    private ColorSelectedAdapter adapterColor;
    private LineSelectedAdapter adapterLine;
    private SendDataListener listener;
    private GridLineView gridLineView;
    private RelativeLayout layoutDraw;
    private RelativeLayout layoutCanvas;
    private boolean gridLine = false;
    private float angle;
    public int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    //---empty constructor required
    public DrawPictureDialog() {
    }

    public void setOnSendDataListener(SendDataListener listener){
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        //hideNavigation(uiOptions);
        View view = inflater.inflate(R.layout.activity_draw_picture, container);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        //byte[] byteArray = getArguments().getByteArray(Globals.BUNDLE_SEND_IMAGE);
        exifData = getArguments().getStringArray(Globals.BUNDLE_SEND_EXIF);
        gridLine = getArguments().getBoolean(Globals.BUNDLE_SEND_GRID,false);
        angle = getArguments().getFloat(Globals.BUNDLE_SEND_ANGLE,1f);
        mImage = Globals.captureBitmap;//BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        //mImage = CommonUtils.drawTimeOnBitmap(mImage);
        mContext = getActivity();
        initData();
        spLine = (Spinner) view.findViewById(R.id.sp_line);
        spColor = (Spinner) view.findViewById(R.id.sp_color);
        drawing = (DrawingView) view.findViewById(R.id.drawing);
        layoutCanvas = (RelativeLayout) view.findViewById(R.id.layout_canvas);
        drawing.exifData = exifData;
        drawing.setImageBitmap(mImage);
        drawing.setOnSendImageListener(this);
        layoutDraw = (RelativeLayout) view.findViewById(R.id.layout_draw);
        if (!gridLine)
            layoutDraw.setVisibility(View.GONE);
        ViewTreeObserver viewTreeObserver = layoutCanvas.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layoutCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layoutCanvas.getWidth();
                    int height = layoutCanvas.getHeight();
                    int imageWidth = mImage.getWidth();
                    int imageHeight = mImage.getHeight();
                    float ratio = width/(float)imageWidth;
                    if (imageHeight*ratio > height) {
                        int realWidth = (int) ((height/(float)imageHeight)*imageWidth);
                        if (gridLine) {
                            gridLineView = new GridLineView(mContext, realWidth, height);
                            layoutDraw.addView(gridLineView);
                            gridLineView.setAngle(angle);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gridLineView.getLayoutParams();
                            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            params.width = realWidth;
                            params.height = height;
                            gridLineView.setLayoutParams(params);
                        }
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) drawing.getLayoutParams();
                        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        params.width = realWidth;
                        params.height = height;
                        drawing.setLayoutParams(params);
                    }


                }
            });
        }
        ImageView imgColor = (ImageView) view.findViewById(R.id.img_color);
        imgColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spColor.performClick();
            }
        });
        adapterColor = new ColorSelectedAdapter(mContext.getApplicationContext(), arrayColor);
        spColor.setAdapter(adapterColor);
        spColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                drawing.setBrushColor(arrayColor.get(position).getColorCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageView imgLine = (ImageView) view.findViewById(R.id.img_line);
        imgLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spLine.performClick();
            }
        });
        adapterLine = new LineSelectedAdapter(mContext.getApplicationContext(), arrayLine);
        spLine.setAdapter(adapterLine);
        spLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                drawing.setWidth(CommonUtils.convertDpToPx(arrayLine.get(position).getId(), mContext));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnUndo = (Button) view.findViewById(R.id.btn_undo);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawing.undo();
            }
        });

        Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker tracker = new GPSTracker(mContext);
                if (tracker.canGetLocation()) {
                    drawing.sendImage(mContext, angle,gridLine);
                    dismiss();
                } else {
                    DialogUtils.settingRequestTurnOnLocation((Activity) mContext);
                }
            }
        });

        Button btnClose = (Button) view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void hideNavigation(final int uiOptions) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            final View decorView = getDialog().getWindow().getDecorView();
            decorView.setSystemUiVisibility(uiOptions);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                }
            });
        }
    }


    private void initData() {
        arrayColor = new ArrayList<>();
        arrayColor.add(new Color("#990d17", true));
        arrayColor.add(new Color("#ffe92b", false));
        arrayColor.add(new Color("#53f40b", false));
        arrayColor.add(new Color("#4CC8D5", false));
        arrayColor.add(new Color("#7f0184", false));
        arrayColor.add(new Color("#FF4081", false));

        arrayLine = new ArrayList<>();
        arrayLine.add(new Line(2, true));
        arrayLine.add(new Line(5, false));
        arrayLine.add(new Line(8, false));
        arrayLine.add(new Line(12, false));
        arrayLine.add(new Line(16, false));
        arrayLine.add(new Line(20, false));
    }

    @Override
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onSendImage(File file) {
        if (listener != null)
            listener.onSend(file);
    }

    public interface SendDataListener{
        void onSend(File file);
    }
}
