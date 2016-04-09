package nizhogor.com.flashalarm;

/**
 * Created by Mix on 6/16/2015.
 */

import android.provider.BaseColumns;

public final class AlarmContract {

    public AlarmContract() {}

    public static abstract class Alarm implements BaseColumns {
        public static final String TABLE_NAME = "alarm";
        public static final String COLUMN_NAME_ALARM_NAME = "name";
        public static final String COLUMN_NAME_ALARM_TIME_HOUR = "hour";
        public static final String COLUMN_NAME_ALARM_TIME_MINUTE = "minute";
        public static final String COLUMN_NAME_ALARM_REPEAT_DAYS = "days";
        public static final String COLUMN_NAME_ALARM_REPEAT_WEEKLY = "weekly";
        public static final String COLUMN_NAME_ALARM_TONE = "tone";
        public static final String COLUMN_NAME_TONE_VOLUME="volume";
        public static final String COLUMN_NAME_TONE_VOLUME_RISE="volume_rising";
        public static final String COLUMN_NAME_ALARM_FLASH = "flash";
        public static final String COLUMN_NAME_ALARM_BRIGHTNESS = "brightness";
        public static final String COLUMN_NAME_ALARM_VIBRATE = "vibrate";
        public static final String COLUMN_NAME_ALARM_ENABLED = "enabled";
        public static final String COLUMN_NAME_ALARM_FLASH_PATTERN = "flash_pattern";
        public static final String COLUMN_NAME_ALARM_VIBRATE_PATTERN = "vibrate_pattern";
        public static final String COLUMN_NAME_ALARM_DARK_THEME = "dark_theme";
        public static final String COLUMN_NAME_DIGITAL_PICKER = "digital_picker";
        public static final String COLUMN_NAME_SNOOZE = "snooze";
    }

}