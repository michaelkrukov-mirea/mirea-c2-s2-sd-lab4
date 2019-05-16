package com.kryukov.daysuntil;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

import androidx.core.app.NotificationCompat;

class Utils {
    /**
     *  Get next day's 00:00:00.500 as calendar
     */
    static Calendar getNextDay() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DATE, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 500);

        return calendar;
    }

    static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ConfigActivity.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    static void showNotification(Context context, String title, int widgetID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ConfigActivity.CHANNEL_ID);
        builder.setWhen(System.currentTimeMillis());
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle(title);
        builder.setContentText("9:00");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(widgetID, builder.build());
    }

    static boolean isAlarmForWidgetExists(Context context, int widgetID) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction("updateDaysCounter" + widgetID);
        intent.putExtra("widgetID", widgetID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_NO_CREATE);

        return pendingIntent != null;
    }

    static void scheduleAlarmForWidget(Context context, int widgetID, Calendar calendar) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction("updateDaysCounter" + widgetID);
        intent.putExtra("widgetID", widgetID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else{
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    static void clearAlarmForWidget(Context context, int widgetID) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction("updateDaysCounter" + widgetID);
        intent.putExtra("widgetID", widgetID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    static Calendar getCalendarFromPreferences(SharedPreferences sp, int widgetID) {
        int widgetYear = sp.getInt(ConfigActivity.WIDGET_YEAR + widgetID, 0);

        if (widgetYear == 0) {
            return null;
        }

        int widgetMonth = sp.getInt(ConfigActivity.WIDGET_MONTH + widgetID, 0);
        int widgetDay = sp.getInt(ConfigActivity.WIDGET_DAY + widgetID, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(widgetYear, widgetMonth, widgetDay, 0, 0, 0);

        return calendar;
    }

    private static long getDaysUntil(Calendar calendar) {
        Calendar b = Calendar.getInstance();

        long daysBetween = 0;

        while (b.before(calendar)) {
            b.add(Calendar.DATE, 1);
            daysBetween++;
        }

        return daysBetween;
    }

    static long getDaysUntilFromPreferences(SharedPreferences sp, int widgetID) {
        Calendar calendar = Utils.getCalendarFromPreferences(sp, widgetID);

        if (calendar == null) {
            return -1;
        }

        return getDaysUntil(calendar);
    }
}
