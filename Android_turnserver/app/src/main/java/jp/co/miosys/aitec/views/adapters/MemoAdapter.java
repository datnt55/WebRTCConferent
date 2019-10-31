package jp.co.miosys.aitec.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Contact;
import jp.co.miosys.aitec.models.Memo;

/*[20170918] Ductx: #2598: Connect activity adapter to show list user*/
public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Memo> arrMemo;
    public MemoSelectListener mListener;

    public void setMemoSelectedListener(MemoSelectListener listener) {
        mListener = listener;
    }

    public MemoAdapter(Context context, ArrayList<Memo> contacts) {
        mContext = context;
        this.arrMemo = contacts;
    }

    @Override
    public int getItemCount() {
        if (arrMemo == null) return 0;
        else return arrMemo.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_memo, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MemoAdapter.ViewHolder holder, final int position) {
        holder.txtCategory.setText(arrMemo.get(position).getCategoryName());
        holder.txtContent.setText(arrMemo.get(position).getContent());
        DateTimeFormatter dfm = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
        DateTime dateTime = dfm.parseDateTime(arrMemo.get(position).getMemoAt());
        dfm = DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
        holder.txtDateTime.setText(dfm.print(dateTime));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!= null)
                    mListener.onSelectMemo(arrMemo.get(position));
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtCategory, txtContent, txtDateTime;

        public ViewHolder(View itemView) {
            super(itemView);
            txtCategory = (TextView) itemView.findViewById(R.id.txt_category);
            txtContent = (TextView) itemView.findViewById(R.id.txt_content);
            txtDateTime = (TextView) itemView.findViewById(R.id.txt_datetime);

        }
    }

    public interface MemoSelectListener {
        void onSelectMemo(Memo contactGuest);
    }
}