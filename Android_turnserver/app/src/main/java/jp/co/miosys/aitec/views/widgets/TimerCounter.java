package jp.co.miosys.aitec.views.widgets;

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DatNT on 12/14/2017.
 */

public class TimerCounter {
    private Timer T;
    private long count = 0;
    private TimerCounterListener listener;
    private Activity activity;
    public TimerCounter(Activity activity, TimerCounterListener listener) {
        this.activity = activity;
        T=new Timer();
        this.listener = listener;
        startCountUpTimer();
    }

    private void startCountUpTimer (){
        count = 0;
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final int hour = (int) (count/1000/60/60);
                int min = (int) (count/1000/60 - hour*60);
                int sec = (int) (count/1000 - min*60 - hour*60*60);
                int mili = (int) (count - sec*1000 - min*60*1000 - hour*60*60*1000);

                String txtMili = "", txtSec= "",txtMin = "", txtHour = "";
                if (mili < 100)
                    if (mili < 10)
                        txtMili = "00"+mili;
                    else
                        txtMili = "0"+mili;
                else
                    txtMili = mili+"";
                if (sec < 10)
                    txtSec = "0"+sec;
                else
                    txtSec = ""+sec;
                if (min < 10)
                    txtMin = "0"+ min;
                else
                    txtMin = ""+ min;
                if (hour< 10)
                    txtHour = "0"+ hour;
                else
                    txtHour = ""+ hour;

                if (listener != null) {
                    final String finalTxtHour = txtHour;
                    final String finalTxtMin = txtMin;
                    final String finalTxtSec = txtSec;
                    final String finalTxtMili = txtMili;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onTimerCount(finalTxtHour + ":" + finalTxtMin + ":" + finalTxtSec + "."+ finalTxtMili);

                        }
                    });

                }
                count++;
            }
        }, 1, 1);
    }

    public void cancel(){
        this.T.cancel();
    }
    public interface TimerCounterListener{
        void onTimerCount(String tick);
    }
}
