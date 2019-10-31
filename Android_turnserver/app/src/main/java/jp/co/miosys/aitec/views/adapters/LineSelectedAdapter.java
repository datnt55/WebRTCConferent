package jp.co.miosys.aitec.views.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Line;
import jp.co.miosys.aitec.utils.CommonUtils;

/**
 * Created by DatNT on 7/19/2017.
 */

public class LineSelectedAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Line> arrayLine;

    public LineSelectedAdapter(Context context, ArrayList<Line> lines) {
        this.mContext = context;
        arrayLine = lines;
    }

    @Override
    public int getCount() {
        return arrayLine.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = (LayoutInflater.from(mContext).inflate(R.layout.item_select_line, null));
        ImageView imvLine = (ImageView) view.findViewById(R.id.imv_line);
        LinearLayout layoutRoot = (LinearLayout) view.findViewById(R.id.root);
        LinearLayout.LayoutParams paramsRoot = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, CommonUtils.convertDpToPx(mContext.getResources().getInteger(R.integer.draw_spinner_height), mContext));
        layoutRoot.setLayoutParams(paramsRoot);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, CommonUtils.convertDpToPx(arrayLine.get(i).getId(), mContext));
        params.gravity = Gravity.CENTER;
        imvLine.setLayoutParams(params);
        return view;
    }
}