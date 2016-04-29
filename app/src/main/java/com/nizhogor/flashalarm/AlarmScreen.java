package com.nizhogor.flashalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmScreen extends Activity {

    private final String TAG = this.getClass().getSimpleName();

    private WakeLock mWakeLock;
    private Camera mCamera;
    private Vibrator mVibrator;
    private float startVolume = (float) 0.1f;
    private static final int WAKELOCK_TIMEOUT = 60 * 1000;
    private HardwareManager hardwareManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup layout
        this.setContentView(R.layout.activity_alarm_screen);
        Intent intent = getIntent();

        String name = intent.getStringExtra(AlarmManagerHelper.NAME);
        int timeHour = intent.getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
        int timeMinute = intent.getIntExtra(AlarmManagerHelper.TIME_MINUTE, 0);
        String tone = intent.getStringExtra(AlarmManagerHelper.TONE);

        int volume = intent.getIntExtra(AlarmManagerHelper.VOLUME, 0);
        boolean flash = intent.getBooleanExtra(AlarmManagerHelper.FLASH, false);
        boolean volume_rising = intent.getBooleanExtra(AlarmManagerHelper.VOLUME_RISING, false);
        boolean vibrate = intent.getBooleanExtra(AlarmManagerHelper.VIBRATE, false);

        String vibratePattern = intent.getStringExtra(AlarmManagerHelper.VIBRATE_PATTERN);
        String flashPattern = intent.getStringExtra(AlarmManagerHelper.FLASH_PATTERN);

        hardwareManager = new HardwareManager(this);

        TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
        tvName.setText(name);

        Calendar c = Calendar.getInstance();
        long lDate = c.getTimeInMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, y");
        String strDate = dateFormat.format(lDate);
        TextView alarmDate = (TextView) findViewById(R.id.alarm_screen_date);
        alarmDate.setText(strDate);

        String ampm = "";
        TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
        TextView period = (TextView) findViewById(R.id.alarm_screen_period);
        if (!DateFormat.is24HourFormat(this)) {
            ampm = "am";
            if (timeHour >= 12) {
                ampm = "pm";
                if (timeHour > 12)
                    timeHour = timeHour - 12;
            }
        }
        tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));
        period.setText(ampm);

        Button dismissButton = (Button) findViewById(R.id.alarm_screen_button);

        //Ensure wakelock release
        Runnable releaseWakelock = new Runnable() {

            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        };

        new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);

        if (vibrate) {
            hardwareManager.startVibrate(vibratePattern);
        }
        if (flash) {
            hardwareManager.startFlash(flashPattern);
        }
        if (tone != null && !tone.equals("")) {
            if (volume_rising)
                hardwareManager.PlayRisingAlarm(tone);
            else
                hardwareManager.PlayAlarm(tone, volume);
        }

        dismissButton.setOnClickListener(new View.OnClickListener()

                                         {
                                             @Override
                                             public void onClick(View view) {
                                                 hardwareManager.stopPlaying();
                                                 hardwareManager.stopVibrate();
                                                 hardwareManager.stopFlash();
                                                 finish();
                                             }
                                         }

        );
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.i(TAG, "Wakelock aquired!!");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();


        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }


    @Override
    public void onBackPressed() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        hardwareManager.stopFlash();
        hardwareManager.stopPlaying();
        hardwareManager.stopVibrate();

        super.onBackPressed();
    }


}