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
import java.util.Date;

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

        int widgetYear = sp.getInt(ConfigActivity.WIDGET_YEAR + widgetID, 0);
        if (widgetYear != 0) {
            int widgetMonth = sp.getInt(ConfigActivity.WIDGET_MONTH + widgetID, 0);
            int widgetDay = sp.getInt(ConfigActivity.WIDGET_DAY + widgetID, 0);

            Calendar a = Calendar.getInstance();
            a.set(widgetYear, widgetMonth, widgetDay);

            Calendar b = Calendar.getInstance();
            b.setTime(new Date());

            long daysBetween = 0;
            while (b.before(a)) {
                b.add(Calendar.DAY_OF_MONTH, 1);
                daysBetween++;
            }

            widgetView.setTextViewText(R.id.tv, String.format("%d",daysBetween));
        }

        Intent configIntent = new Intent(context, ConfigActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.linear_layout, configPendingIntent);

        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_YEAR + widgetID);
            editor.remove(ConfigActivity.WIDGET_MONTH + widgetID);
            editor.remove(ConfigActivity.WIDGET_DAY + widgetID);
        }
        editor.apply();
    }
}
