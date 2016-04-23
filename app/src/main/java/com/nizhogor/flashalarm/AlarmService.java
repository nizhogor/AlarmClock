package com.nizhogor.flashalarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {

    public static String TAG = AlarmService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent alarmIntent = new Intent(getBaseContext(), AlarmScreen.class);
        if (intent != null) {
            long alarmTimestamp = intent.getLongExtra(AlarmManagerHelper.ALARM_TIMESTAMP_MILLIS, -1);
            // if difference is more than 60 sec means that alarm is triggered by moving system time forward
            long difference = Math.abs(System.currentTimeMillis()-alarmTimestamp);
            if ( difference < 60 * 1000) {
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarmIntent.putExtras(intent);
                getApplication().startActivity(alarmIntent);
            }
            AlarmManagerHelper.setAlarms(this, false);

        } else
            System.out.println("----->Intent is null");

        return super.onStartCommand(intent, flags, startId);
    }
}