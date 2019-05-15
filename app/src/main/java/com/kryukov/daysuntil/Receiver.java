package com.kryukov.daysuntil;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.channel_name);
            String description = ctx.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ConfigActivity.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, ConfigActivity.CHANNEL_ID);
        builder.setWhen(System.currentTimeMillis());
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("It's now");
        builder.setContentText("9:00");

        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }
}