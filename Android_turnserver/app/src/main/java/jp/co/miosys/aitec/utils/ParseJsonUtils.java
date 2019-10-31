package jp.co.miosys.aitec.utils;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Category;
import jp.co.miosys.aitec.models.Memo;
import jp.co.miosys.aitec.views.listeners.OnGetImageUrlListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Duc on 13/3/2017.
 */
public class ParseJsonUtils {

    private static OnGetImageUrlListener listener;

    public void getImageUrl(OnGetImageUrlListener listener){
        this.listener = listener;
    }

    public static void parseJsonSendMesagge(final Context context, String response) {
        try {
            JSONObject result = new JSONObject(response);
            int status = result.getInt("code");
            if (status == 200) {
                String imageUrl = result.getString("image");
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveImageDialog((Activity) context);
                    }
                });

                listener.onGetImageUrl(imageUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveImageDialog(final Activity mActivity) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.notice));
        alertDialogBuilder.setMessage(mActivity.getString(R.string.send_image_success_message))
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public static ArrayList<Memo> parseListMemo(String string){
        ArrayList<Memo> memos = new ArrayList<>();
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
                memos.add(memo);
            }
            return memos;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return memos;
    }

    public static ArrayList<Category> parseCategories(String string){
        ArrayList<Category> categories = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONArray data = jsonObject.getJSONArray("data");
            for (int i = 0 ; i < data.length(); i++) {
                JSONObject memoObj = data.getJSONObject(i);
                String categoryId = memoObj.getString("id");
                String categoryName = memoObj.getString("name");
                String note = memoObj.getString("note");

                Category category = new Category(categoryId, categoryName, note);
                categories.add(category);
            }
            return categories;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return categories;
    }

}
