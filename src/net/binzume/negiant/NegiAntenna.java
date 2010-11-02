package net.binzume.negiant;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.appwidget.AppWidgetManager;
//import android.util.Log;




public class NegiAntenna extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.d("NegiAntenna","onUpdate");
        Intent intent=new Intent(context,NegiAntennaService.class);
        intent.setAction("start");
        context.startService(intent);
    }
    
}
