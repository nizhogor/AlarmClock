package com.nizhogor.flashalarm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mix on 6/30/2015.
 */
public class HardwareManager {

    Context mContext;
    MediaPlayer mPlayer;
    private int startVolume = 1;
    Vibrator mVibrator;
    Camera mCamera;
    private static final String ON = Camera.Parameters.FLASH_MODE_TORCH;
    private static final String OFF = Camera.Parameters.FLASH_MODE_OFF;

    private static String mPattern;
    private Timer mTimer;
    private int flashPatternIndex = 0;

    private boolean mIsVibrating = false;
    private boolean mIsFlashing = false;

    public HardwareManager(Context context) {
        mContext = context;

    }

    public boolean isPlaying() {
        if (mPlayer != null && mPlayer.isPlaying())
            return true;
        else
            return false;
    }

    public void stopPlaying() {
        if (isPlaying())
            mPlayer.stop();

    }

    public void PlayAlarm(String alarmToneStr, float volume) {
        Uri alarmTone = Uri.parse(alarmToneStr);
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
        }
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(mContext, alarmTone);
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);
            float volumeLevel = com.nizhogor.flashalarm.AlarmModel.getVolumeFloat((int) volume);
            mPlayer.setVolume(volumeLevel, volumeLevel);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PlayRisingAlarm(String alarmToneStr) {
        startVolume = 1;
        PlayAlarm(alarmToneStr, com.nizhogor.flashalarm.AlarmModel.getVolumeFloat(startVolume));
        //AudioManager audioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
        //audioManager.setStreamVolume (AudioManager.STREAM_ALARM, 1,0);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //AudioManager audioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
                //int currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                //int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                if (startVolume < 100) {
                    startVolume += 1;
                    float volume = com.nizhogor.flashalarm.AlarmModel.getVolumeFloat(startVolume);
                    //audioManager.setStreamVolume (AudioManager.STREAM_ALARM, currentAlarmVolume+3,0);
                    mPlayer.setVolume(volume, volume);
                } else
                    this.cancel();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 350);
    }

    private long getLength(char c) {
        switch (c) {
            case 's':
                return 500L;
            case 'm':
                return 1000L;
            case 'l':
                return 1500L;
            case 'e':
                return 2000L;
            case 'c':
                return -1L;
            default:
                return 0L;
        }
    }

    private long[] getPattern(String strPattern) {
        //500 s 1000 m 1500 l 2000 e
        int patternLength = strPattern.length();
        long[] pattern = new long[patternLength * 2 + 1];
        //first value is how long to wait before starts, than alternates
        pattern[0] = 2500L;

        for (int i = 0; i < patternLength; i++) {
            long patternValue = getLength(strPattern.charAt(i));
            pattern[2 * (i + 1) - 1] = patternValue;
            pattern[2 * (i + 1)] = patternValue;
        }
        return pattern;
    }

    public void startVibrate(String strPattern) {
        mIsVibrating = true;
        long[] pattern = getPattern(strPattern);

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (pattern[1] == -1L) {
            mVibrator.vibrate(10 * 60 * 1000); // 10 mins by default
        } else {
            mVibrator.vibrate(pattern, 0);
        }
    }

    public void stopVibrate() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        mIsVibrating = false;
    }

    public void startFlash(String strPattern) {
        long[] pattern = getPattern(strPattern);
        mIsFlashing = true;
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mCamera = Camera.open();
        }
        if (pattern[1] == -1L) {
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(ON);
            mCamera.setParameters(p);
            mCamera.startPreview();
            return;
        }
        mTimer = new Timer();
        FlashTimerTask flashTimerTask = new FlashTimerTask(pattern, 0);

        mTimer.schedule(flashTimerTask, 0);
    }

    public void stopFlash() {
        mIsFlashing = false;
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mCamera != null) {
            flashPatternIndex = 0;
            mCamera.release();
        }
    }

    private class FlashTimerTask extends TimerTask {
        private long[] mFlashPattern = null;
        private Context mAppContext = null;

        private int mIndex = 0;

        protected FlashTimerTask(long[] pattern, int index) {
            super();
            mFlashPattern = pattern.clone();
            if (index == pattern.length - 1)
                index = 0;
            else
                mIndex = index;
        }

        @Override
        public void run() {
            Camera.Parameters p = mCamera.getParameters();
            if (mCamera.getParameters().getFlashMode().equals(ON)) {
                p.setFlashMode(OFF);
            } else {
                p.setFlashMode(ON);
            }
            mCamera.setParameters(p);
            mCamera.startPreview();
            //skip first one. First pattern value is used by vibrator only
            mTimer.schedule(new FlashTimerTask(mFlashPattern, ++mIndex), mFlashPattern[mIndex]);
        }
    }

    public boolean isFlashing() {
        return mIsFlashing;
    }

    public boolean isVibrating() {
        return mIsVibrating;
    }
}
