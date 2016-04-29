package com.nizhogor.flashalarm;

public class AlarmModel {

    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;

    public long id = -1;
    public int timeHour;
    public int timeMinute;
    private boolean repeatingDays[];
    public boolean repeatWeekly;
    public String alarmTone;
    public String name;
    public boolean isEnabled;

    private int volume;
    public boolean volume_rising;
    public boolean flash;
    public int brightness = 0;
    public boolean vibrate;

    public String flash_pattern;
    public String vibrate_pattern;
    public Boolean dark_theme;
    public boolean digital_picker = true;
    public boolean snooze;

    //display colour

    public AlarmModel() {
        repeatingDays = new boolean[7];
        name = "";
        alarmTone = "";
        flash_pattern = "smle";
        vibrate_pattern = "c";
    }

    public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public float getVolumeFloat() {
        float log1 = (float) (Math.log(100 - volume) / Math.log(100));
        return 1 - log1;
    }

    public static float getVolumeScaled(int currentVolume) {
        float log1 = (float) (Math.log(100 - currentVolume) / Math.log(100));
        return 1 - log1;
    }

    public int getVolume() {
        return this.volume;
    }

}