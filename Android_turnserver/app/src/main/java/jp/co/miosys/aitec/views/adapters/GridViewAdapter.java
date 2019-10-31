package jp.co.miosys.aitec.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Image;
import jp.co.miosys.aitec.utils.CommonUtils;
import jp.co.miosys.aitec.utils.Globals;
import jp.co.miosys.aitec.utils.ReadWriteFileUtils;
import jp.co.miosys.aitec.views.listeners.OnShowImageListener;

/**
 * Created by Duc on 9/5/2016.
 */
public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.RecyclerViewHolder> {// Recyclerview will extend to
    // recyclerview adapter
    private ArrayList<Image> arrayList;
    private Context mContext;
    private static OnShowImageListener mListener;

    public static void getListener(OnShowImageListener listener) {
        mListener = listener;
    }

    public GridViewAdapter(Context context, ArrayList<Image> arrayList) {
        this.mContext = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        try {
            String filePath = arrayList.get(position).getImagePath();
//            ExifInterface ef = new ExifInterface(filePath);
//            String timeStamp = ReadWriteFileUtils.changeFormatDateTime(ef.getAttribute(ExifInterface.TAG_DATETIME), Globals.patternExifDateTime, Globals.patternImageName);
            String timeStamp = ReadWriteFileUtils.getTimeStampInFileName(filePath);
            holder.txtImage.setText(timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fileName = arrayList.get(position).getImagePath().replace(':', '/');
        fileName = fileName.replace('/', '_');
        String loadURL="file://"+  arrayList.get(position).getImagePath();
        //Bitmap myBitmap = BitmapFactory.decodeFile(arrayList.get(position).getImagePath());
        //holder.imvThumbnail.setImageBitmap(myBitmap);
        CommonUtils.imageLoader(holder.imvThumbnail, loadURL);

    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.item_gridview, viewGroup, false);
        return new RecyclerViewHolder(itemView);
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        // View holder for gridview recycler view as we used in listview

        private FrameLayout lyItem;
        public TextView txtImage;
        public Button btnClose;
        public ImageView imvThumbnail;

        public RecyclerViewHolder(View view) {
            super(view);
            lyItem = (FrameLayout) view.findViewById(R.id.ly_item);
            txtImage = (TextView) view.findViewById(R.id.txt_image);
            btnClose = (Button) view.findViewById(R.id.btn_close);
            imvThumbnail = (ImageView) view.findViewById(R.id.imv_thumbnail);
            lyItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onShowImage(arrayList.get(getAdapterPosition()).getImagePath());
                    notifyDataSetChanged();
                }
            });
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrayList.size() == 1) {
                        mListener.onNoImage();
                    }
                    // Move file to closed folder, delete file in root folder
                    String fileName = ReadWriteFileUtils.getFileName(arrayList.get(getAdapterPosition()).getImagePath());
                    try {
                        File file = new File(Globals.IMAGE_CLOSED_DIRECTORY);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        ReadWriteFileUtils.copyFile(Globals.IMAGE_DIRECTORY + "/" + fileName, Globals.IMAGE_CLOSED_DIRECTORY + "/" + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ReadWriteFileUtils.deleteFile(arrayList.get(getAdapterPosition()).getImagePath());
                    arrayList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
        }
    }
}