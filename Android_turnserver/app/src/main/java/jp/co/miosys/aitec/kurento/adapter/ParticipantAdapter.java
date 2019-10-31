package jp.co.miosys.aitec.kurento.adapter;

/**
 * Created by DatNT on 04/11/18.
 */

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Contact;
import jp.co.miosys.aitec.utils.CustomClick;


public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Contact> mList;
    private ItemClickListener listener;

    public ParticipantAdapter(Context mContext, ArrayList<Contact> list) {
        this.mContext = mContext;
        this.mList = list;
    }

    @Override
    public ParticipantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }
    @Override
    public void onBindViewHolder(final ParticipantAdapter.ViewHolder holder, final int position) {
        if (mList.size() > 0) {
            holder.txtName.setText(mList.get(position).getName());
            if (mList.get(position).getState() == 3) {
                holder.imgPerson.setImageResource(R.drawable.ic_person_one_tec);
            } else if (mList.get(position).getState() == 2) {
                holder.imgPerson.setImageResource(R.drawable.ic_person_busy);
            } else {
                holder.imgPerson.setImageResource(R.drawable.ic_person_online);
        }
        }
    }

//    private String getDateTime(String dateTime) {
//        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
//        // Parsing the date
//        DateTime time = dtf.parseDateTime(dateTime);
//        DateTime current = new DateTime();
//        Period diff = new Period(time, current);
//        int hours = diff.getHours();
//        int minutes = diff.getMinutes();
//        int days = diff.getDays();
//        int month = diff.getMonths();
//        int years = diff.getYears();
//        if (years > 0) {
//            DateTimeFormatter df = DateTimeFormat.forPattern("dd MMMM yyyy");
//            return df.print(time);
//        } else if (month > 3) {
//            DateTimeFormatter df = DateTimeFormat.forPattern("dd MMMM yyyy");
//            return df.print(time);
//        } else if (month > 0) {
//            return days + " tháng trước";
//        } else if (days > 0) {
//            return days + " ngày trước";
//        } else if (hours > 0) {
//            return hours + " giờ trước";
//        } else if (minutes > 0) {
//            return minutes + " phút trước";
//        } else {
//            return " Vừa xong";
//        }
//    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements CustomClick.OnClickListener {

        private final TextView  txtName;
        private final ImageView imgPerson;
        private CustomClick customClick;

        ViewHolder(View itemView) {
            super(itemView);
            customClick = new CustomClick(this);
            imgPerson = (ImageView) itemView.findViewById(R.id.img_person);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            customClick.setView(imgPerson);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_person){
                if (listener != null)
                    listener.onClick(getAdapterPosition());
            }
        }
    }

    public interface ItemClickListener{
        void onClick(int position);
    }
}
