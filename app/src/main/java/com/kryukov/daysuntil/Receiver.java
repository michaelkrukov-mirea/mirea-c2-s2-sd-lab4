package com.kryukov.daysuntil;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction() == null
                || intent.getExtras() == null
                || !intent.getAction().startsWith("updateDaysCounter")) {
            return;
        }

        SharedPreferences sp = ctx.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        int widgetID = intent.getExtras().getInt("widgetID");

        Calendar calendar = Utils.getCalendarFromPreferences(sp, widgetID);

        if (calendar != null && Utils.getDaysUntil(calendar) < 0) {
            Utils.createChannel(ctx);
            Utils.showNotification(ctx, "It's now", widgetID);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
        DaysUntilWidget.updateWidget(ctx, appWidgetManager, sp, widgetID);
    }
}