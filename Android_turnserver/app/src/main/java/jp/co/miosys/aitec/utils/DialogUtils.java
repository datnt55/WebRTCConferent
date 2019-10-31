package jp.co.miosys.aitec.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.activities.CallActivity;
import jp.co.miosys.aitec.activities.ImageViewActivity;
import jp.co.miosys.aitec.models.Category;
import jp.co.miosys.aitec.models.LocationGPS;

/**
 * Created by Duc on 9/26/2017.
 */

public class DialogUtils {
    /*[20170910] Ductx: #2595: Show dialog Received image confirmation*/
    private static Dialog dialog;

    public static void receiveImageDialog(final Context context) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        // custom dialog
        dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.custom_dialog_show_image);
        dialog.setCancelable(false);
        // set the custom dialog components - text, image and button

        Button btnOpen = (Button) dialog.findViewById(R.id.btn_open);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        // if button is clicked, close the custom dialog
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(context, ImageViewActivity.class);
                //intent.putExtra(Globals.BUNDLE_SEND_GPS, locations);
                context.startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Show dialog request turn on gps
    public static void settingRequestTurnOnLocation(final Activity mActivity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle("Notice");  // GPS not found
        alertDialogBuilder.setMessage("You must enable gps to use the application")
                .setCancelable(false)
                .setPositiveButton("Continue",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mActivity.startActivityForResult(callGPSSettingIntent, Globals.REQUEST_CODE_LOCATION_PERMISSIONS);
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public static void categoryListDialog(Context context, ArrayList<Category> categories, final CategorySelectedCallback callback ){
        String[] listItems = new String[categories.size()];
        int count = 0;
        for (Category category : categories) {
            listItems[count] = category.getCategoryName();
            count ++;
        }
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setTitle(context.getString(R.string.choose_category_title));
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (callback != null)
                    callback.onSelect(i);
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
    public interface CategorySelectedCallback{
        void onSelect(int position);
    }
}
