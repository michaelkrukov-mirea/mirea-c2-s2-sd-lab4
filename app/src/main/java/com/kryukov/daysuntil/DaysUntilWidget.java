package com.kryukov.daysuntil;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.Calendar;

public class DaysUntilWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    @SuppressLint("DefaultLocale")
    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             SharedPreferences sp, int widgetID) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);

        long daysBetween =  Utils.getDaysUntilFromPreferences(sp, widgetID);
        if (daysBetween >= 0) {
            widgetView.setTextViewText(R.id.tv, String.format("%d", daysBetween));
        }

        Intent configIntent = new Intent(context, ConfigActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, widgetID, configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.linear_layout, configPendingIntent);

        appWidgetManager.updateAppWidget(widgetID, widgetView);

        if (!Utils.isAlarmForWidgetExists(context, widgetID)) {
            Utils.scheduleAlarmForWidget(context, widgetID, Calendar.getInstance());
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_YEAR + widgetID);
            editor.remove(ConfigActivity.WIDGET_MONTH + widgetID);
            editor.remove(ConfigActivity.WIDGET_DAY + widgetID);
            Utils.clearAlarmForWidget(context, widgetID);
        }
        editor.apply();
    }
}
