package com.kryukov.daysuntil;
         
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {
    final static String CHANNEL_ID = "DaysUntilChan";
    final static int HOUR_TO_TRIGGER = 14;
    final static int MINUTE_TO_TRIGGER = 57;

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
                // Save selected date
                SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
                Editor e = sp.edit();
                e.putInt(WIDGET_YEAR + widgetID, year);
                e.putInt(WIDGET_MONTH + widgetID, monthOfYear);
                e.putInt(WIDGET_DAY + widgetID, dayOfMonth);
                e.commit();

                Calendar calendar = Calendar.getInstance();
                calendar.set(
                        year,
                        monthOfYear,
                        dayOfMonth,
                        ConfigActivity.HOUR_TO_TRIGGER,
                        ConfigActivity.MINUTE_TO_TRIGGER,
                        0
                );
                calendar.add(Calendar.MINUTE, -1);

                if (calendar.before(Calendar.getInstance())) {
                    Utils.showNotification(
                            getApplicationContext(),
                            "It's already happened",
                            widgetID
                    );
                } else {
                    Utils.scheduleAlarmForWidget(getApplicationContext(), widgetID, Calendar.getInstance());
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

        Calendar calendar = Utils.getCalendarFromPreferences(sp, widgetID);

        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        DatePickerDialog dpd = new DatePickerDialog(
                ConfigActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
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