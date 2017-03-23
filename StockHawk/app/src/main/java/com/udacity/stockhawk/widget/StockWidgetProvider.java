package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailActivity;

/**
 * Created by siddharth.thakrey on 05-02-2017.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(QuoteSyncJob.ACTION_DATA_UPDATED))
        {
           AppWidgetManager am = AppWidgetManager.getInstance(context);
           int [] appWidgetIds = am.getAppWidgetIds(new ComponentName(context, StockWidgetProvider.class));
            am.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i=0; i< appWidgetIds.length; i++)
        {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);
            rv.setRemoteAdapter(R.id.widget_list, new Intent(context, StockWidgetRemoteViewsService.class));

            Intent detailIntent = new Intent(context, DetailActivity.class);
            PendingIntent pendingTemplateIntent = PendingIntent.getActivity(context, 0 , detailIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_list, pendingTemplateIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }
}
