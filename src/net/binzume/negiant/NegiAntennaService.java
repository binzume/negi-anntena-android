package net.binzume.negiant;
import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.os.IBinder;
import android.widget.RemoteViews;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.util.Log;
import android.app.PendingIntent;
import android.view.View;


public class NegiAntennaService extends Service{
    private static final String ACTION_CLICK = "net.binzume.NegiAntenna.ACTION_CLICK";
    private static final String ACTION_UPDATE = "net.binzume.NegiAntenna.UPDATE";
    private static final String ACTION_TIMER = "net.binzume.NegiAntenna.ACTION_TIMER";

    public NegiAntennaService(){
        
    }
    

    class SignalStrengthListener extends PhoneStateListener {
        
        
        
        @Override
        public void onSignalStrengthsChanged(SignalStrength signal) {
            super.onSignalStrengthsChanged(signal);
            Intent intent=new Intent(NegiAntennaService.this,NegiAntennaService.class);
            intent.setAction(ACTION_UPDATE);
            Log.d("NegiAntennaService", "signal: " + signal.getGsmSignalStrength());
            if (signal.isGsm()) {
                intent.putExtra("sig",signal.getGsmSignalStrength());
            } else {
                int strength = 0;
                if (signal.getEvdoDbm() < 0)
                  strength = signal.getEvdoDbm();
                else if (signal.getCdmaDbm() < 0)
                  strength = signal.getCdmaDbm();

                if (strength < 0){
                    intent.putExtra("sig",Math.round((strength + 113f) / 2f));
                } else {
                    intent.putExtra("sig",-1);
                }
            }
            NegiAntennaService.this.startService(intent);
            
            //Log.d("SignalStrengthListener", "isgsm: " + signal.isGsm());
            //Log.d("SignalStrengthListener", "signal: " + signal.getGsmSignalStrength());
            //Log.d("SignalStrengthListener", "signal: " + signal.getCdmaDbm());
            //Log.d("SignalStrengthListener", "signal: " + signal.getEvdoDbm());
        }
    }

    private SignalStrengthListener ps;
    private int signal = -1;
    private boolean show_text = false;
 
    @Override
    public void onCreate() {
        super.onCreate();
        ps = new SignalStrengthListener();
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(ps, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Log.d("NegiAntennaService", "onCreate()");
        //Log.d("TelephonyManager", "phone_type: " + tm.getPhoneType());
        //Log.d("TelephonyManager", "data_state: " + tm.getDataState());
        //Log.d("TelephonyManager", "data_act: " + tm.getDataActivity());
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("NegiAntenna",intent.getAction());

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.main);
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        if ("start".equals(intent.getAction())) {
            Intent clickIntent = new Intent();
            clickIntent.setAction(ACTION_CLICK);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, clickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.TextView01, pendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.ImageView01, pendingIntent);
            remoteViews.setTextViewText(R.id.TextView01, "");
        }
        
        if (ACTION_CLICK.equals(intent.getAction())) {
            show_text=true;
            Intent alarmIntent = new Intent(NegiAntennaService.this, NegiAntennaService.class);
            alarmIntent.setAction(ACTION_TIMER);
            PendingIntent timerintent = PendingIntent.getService(this, 0, alarmIntent, 0);
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            long after = System.currentTimeMillis() + 3000;
            am.set(AlarmManager.RTC, after, timerintent);

            String str = "Signal:" + signal;
            if (tm.getDataState()==2) {
                str += "\nConnected";
            }
            remoteViews.setTextViewText(R.id.TextView01, str);
        }

        if (ACTION_TIMER.equals(intent.getAction()) || "start".equals(intent.getAction())) {
            show_text=false;

            Intent alarmIntent = new Intent(NegiAntennaService.this, NegiAntennaService.class);
            alarmIntent.setAction(ACTION_TIMER);
            PendingIntent timerintent = PendingIntent.getService(this, 0, alarmIntent, 0);
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            long after = System.currentTimeMillis() + 60000;
            am.set(AlarmManager.RTC, after, timerintent);        
        }
        remoteViews.setViewVisibility(R.id.TextView01, show_text ? View.VISIBLE : View.GONE);

        if (ACTION_UPDATE.equals(intent.getAction())) {
            signal = intent.getIntExtra("sig",-99);
            String str = "Signal:" + signal;
            if (tm.getDataState()==2) {
                str += "\nConnected";
            }
            remoteViews.setTextViewText(R.id.TextView01, str);
        } else {
        }

        int imgid;
        /*
        if (signal>31 || signal<0) {
            imgid = R.drawable.antenna_n;
        } else if (signal>=24) {
            imgid = R.drawable.antenna4;
        } else if (signal >=18) {
            imgid = R.drawable.antenna3;
        } else if (signal >=12) {
            imgid = R.drawable.antenna2;
        } else if (signal >=6) {
            imgid = R.drawable.antenna1;
        } else {
            imgid = R.drawable.antenna0;
        }
*/        
        if (signal==99 || signal<=0) {
            imgid = R.drawable.negi00;
        } else if (signal>=16) {
            imgid = R.drawable.negi04;
        } else if (signal >=8) {
            imgid = R.drawable.negi03;
        } else if (signal >=4) {
            imgid = R.drawable.negi02;
        } else {
            imgid = R.drawable.negi01;
        }
        remoteViews.setImageViewResource(R.id.ImageView01, imgid);

        ComponentName thisWidget = new ComponentName(this, NegiAntenna.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, remoteViews);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
