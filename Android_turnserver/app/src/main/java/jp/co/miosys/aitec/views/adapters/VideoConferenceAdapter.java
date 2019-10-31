//package jp.co.miosys.aitec.views.adapters;
//
//import android.content.Context;
//import android.graphics.drawable.GradientDrawable;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.webrtc.EglBase;
//import org.webrtc.MediaStream;
//import org.webrtc.SurfaceViewRenderer;
//import org.webrtc.VideoRenderer;
//
//import java.util.ArrayList;
//
//import jp.co.miosys.aitec.R;
//import jp.co.miosys.aitec.kurento.RemoteParticipant;
//import jp.co.miosys.aitec.kurento.managers.PeersManager;
//import jp.co.miosys.aitec.models.Contact;
//
///*[20170918] Ductx: #2598: Connect activity adapter to show list user*/
//public class VideoConferenceAdapter extends RecyclerView.Adapter<VideoConferenceAdapter.ViewHolder> {
//
//    private Context mContext;
//    private ArrayList<RemoteParticipant> arrContact;
//    private PeersManager peersManager;
//
//    public VideoConferenceAdapter(Context context, ArrayList<RemoteParticipant> contacts) {
//        mContext = context;
//        this.arrContact = contacts;
//    }
//
//    @Override
//    public int getItemCount() {
//        if (arrContact == null) return 0;
//        else return arrContact.size();
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.peer_video, viewGroup, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(final VideoConferenceAdapter.ViewHolder holder, final int position) {
//        holder.surfaceViewRenderer.setMirror(false);
//        EglBase rootEglBase = EglBase.create();
//        holder.surfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
//        holder.surfaceViewRenderer.setZOrderMediaOverlay(true);
//        holder.mainParticipant.setText(arrContact.get(position).getParticipantName());
//        if (arrContact.get(position).isAddStream()){
//            arrContact.get(position).getMediaStream().addTrack(peersManager.getLocalAudioTrack());
//            arrContact.get(position).getMediaStream().addTrack(peersManager.getLocalVideoTrack());
//            arrContact.get(position).getPeerConnection().removeStream( arrContact.get(position).getMediaStream());
//            arrContact.get(position).getPeerConnection().addStream( arrContact.get(position).getMediaStream());
//        }
//    }
//
//
//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }
//
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        private SurfaceViewRenderer surfaceViewRenderer;
//        private TextView mainParticipant;
//        private TextView txtWaiting;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//           surfaceViewRenderer = (SurfaceViewRenderer) itemView.findViewById(R.id.local_gl_surface_view);
//           mainParticipant = (TextView) itemView.findViewById(R.id.main_participant);
//           txtWaiting = (TextView) itemView.findViewById(R.id.txt_waiting);
//        }
//    }
//
//    public interface MemoSelectListener {
//        void onSelectMemo(Contact contactGuest);
//    }
//}