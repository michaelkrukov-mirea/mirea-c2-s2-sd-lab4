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

        Calendar calendar;

        long daysBetween =  Utils.getDaysUntilFromPreferences(sp, widgetID);
        if (daysBetween == 0) {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, ConfigActivity.HOUR_TO_TRIGGER);
            calendar.set(Calendar.MINUTE, ConfigActivity.MINUTE_TO_TRIGGER);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                Utils.createChannel(ctx);
                Utils.showNotification(
                        ctx,
                        "It's now",
                        widgetID
                );
                return; // Skip creating new alarm
            }
        } else {
            calendar = Utils.getNextDay();
        }

        Utils.scheduleAlarmForWidget(ctx, widgetID, calendar);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
        DaysUntilWidget.updateWidget(ctx, appWidgetManager, sp, widgetID);
    }
}