package com.example.alfa;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Date;


public class TimeActivity extends AppCompatActivity {
    private TextView tV;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private int ALARM_RQST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
        tV = findViewById(R.id.tV);
    }


    public void todAlarm(View view) {
        openTimePickerDialog(true);
    }


    private void openTimePickerDialog(boolean is24r) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), is24r);
        timePickerDialog.setTitle("Choose time");
        timePickerDialog.show();
    }
    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();

            calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calSet.set(Calendar.MINUTE, minute);
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);

            if (calSet.compareTo(calNow) <= 0) {
                calSet.add(Calendar.DATE, 1);
            }
            setAlarm(calSet);
        }
    };


    private void setAlarm(Calendar calSet) {
        ALARM_RQST_CODE++;
        Intent intent = new Intent(this, AlarmReciver.class);
        intent.putExtra("msg",String.valueOf(ALARM_RQST_CODE)+" TOD");
        alarmIntent = PendingIntent.getBroadcast(this,
                ALARM_RQST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                calSet.getTimeInMillis(), alarmIntent);
        tV.setText(String.valueOf(ALARM_RQST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
    }
}