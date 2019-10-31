package jp.co.miosys.aitec.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import jp.co.miosys.aitec.R;

public class PopupAdapter extends BaseAdapter {

    private List<String> mListData;
    private LayoutInflater layoutInflater;
    private Context context;

    public PopupAdapter(Context aContext, List<String> listData) {
        this.context = aContext;
        this.mListData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_spinner, null);
            TextView txtPopup = (TextView) convertView.findViewById(R.id.txt_popup);

            String text = mListData.get(position);
            txtPopup.setText(text);
        }
        return convertView;
    }
}