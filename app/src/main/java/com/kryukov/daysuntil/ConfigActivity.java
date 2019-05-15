package com.kryukov.daysuntil;
         
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class ConfigActivity extends AppCompatActivity {
    final static String CHANNEL_ID = "DaysUntilChan";

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    public final static String WIDGET_PREF = "wp";
    public final static String WIDGET_YEAR = "wy_";
    public final static String WIDGET_MONTH = "wm_";
    public final static String WIDGET_DAY = "wd_";

    DatePickerDialog.OnDateSetListener dateSetListener;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }

        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
                Editor e = sp.edit();
                e.putInt(WIDGET_YEAR + widgetID, year);
                e.putInt(WIDGET_MONTH + widgetID, monthOfYear);
                e.putInt(WIDGET_DAY + widgetID, dayOfMonth);
                e.commit();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ConfigActivity.CHANNEL_ID);
                    builder.setWhen(System.currentTimeMillis());
                    builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
                    builder.setSmallIcon(R.drawable.ic_launcher_background);
                    builder.setContentTitle("It's already happened");
                    builder.setContentText("9:00");

                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    manager.notify(0, builder.build());

                } else {
                    Intent intent = new Intent(getApplicationContext(), Receiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), widgetID, intent, FLAG_CANCEL_CURRENT);
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }

                commitAndFinish(sp);
            }
        };

        showDialog();
    }

    public void commitAndFinish(SharedPreferences sp) {
        setResult(RESULT_OK, resultValue);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        DaysUntilWidget.updateWidget(this, appWidgetManager, sp, widgetID);

        finish();
    }

    public void showDialog() {
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);

        Calendar cal = Calendar.getInstance();

        if (sp.getInt(WIDGET_YEAR + widgetID, 0) != 0) {
            cal.set(
                    sp.getInt(WIDGET_YEAR + widgetID, 0),
                    sp.getInt(WIDGET_MONTH + widgetID, 0),
                    sp.getInt(WIDGET_DAY + widgetID, 0)
            );
        }

        DatePickerDialog dpd = new DatePickerDialog(
                ConfigActivity.this,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        dpd.show();
    }
}