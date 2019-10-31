package jp.co.miosys.aitec.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Color;

/**
 * Created by DatNT on 7/19/2017.
 */

public class ColorSelectedAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Color> arrayColor;

    public ColorSelectedAdapter(Context context, ArrayList<Color> colors) {
        this.mContext = context;
        this.arrayColor = colors;
    }

    @Override
    public int getCount() {
        return arrayColor.size();
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
        view = (LayoutInflater.from(mContext).inflate(R.layout.item_select_color, null));
        ImageView imvColor = (ImageView) view.findViewById(R.id.imv_color);
        imvColor.setBackgroundColor(android.graphics.Color.parseColor(arrayColor.get(i).getColorCode()));
        return view;
    }
}