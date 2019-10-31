package jp.co.miosys.aitec.views.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jp.co.miosys.aitec.R;
import jp.co.miosys.aitec.models.Contact;

/*[20170918] Ductx: #2598: Connect activity adapter to show list user*/
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Contact> arrContact;
    public static GuestContactListener mListener;

    public static void userSelectedListener(GuestContactListener listener) {
        mListener = listener;
    }

    public ContactAdapter(Context context, ArrayList<Contact> contacts) {
        mContext = context;
        this.arrContact = contacts;
    }

    @Override
    public int getItemCount() {
        if (arrContact == null) return 0;
        else return arrContact.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_select_contact, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactAdapter.ViewHolder holder, final int position) {
        holder.lyContact.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        GradientDrawable drawable = (GradientDrawable) mContext.getResources().getDrawable(R.drawable.online);
        if (arrContact.get(position).getState() == 3) {
            drawable.setColor(ContextCompat.getColor(mContext, R.color.orange));
        } else if (arrContact.get(position).getState() == 2) {
            drawable.setColor(ContextCompat.getColor(mContext, R.color.red_200));
        } else {
            drawable.setColor(ContextCompat.getColor(mContext, R.color.green_200));
        }
        holder.imvStatus.setBackground(drawable);

        if (arrContact.get(position).isSelected()) {
            holder.lyContact.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        } else {
            holder.lyContact.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        holder.txtName.setText(arrContact.get(position).getName());
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imvStatus;
        FrameLayout lyContact;


        public ViewHolder(View itemView) {
            super(itemView);
            lyContact = (FrameLayout) itemView.findViewById(R.id.ly_contact);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            imvStatus = (ImageView) itemView.findViewById(R.id.imv_status);

            lyContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrContact.get(getAdapterPosition()).getState() == 2 || arrContact.get(getAdapterPosition()).getState() == 3) {
                        Toast.makeText(mContext, mContext.getString(R.string.contact_busy, arrContact.get(getAdapterPosition()).getName()), Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < arrContact.size(); i++) {
                            arrContact.get(i).setSelected(false);
                        }
                        arrContact.get(getAdapterPosition()).setSelected(true);
                        mListener.onGuestContact(arrContact.get(getAdapterPosition()));
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public interface GuestContactListener {
        void onGuestContact(Contact contactGuest);
    }
}