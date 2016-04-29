package com.nizhogor.flashalarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nizhogor.flashalarm.AlarmContract.Alarm;

import java.util.ArrayList;
import java.util.List;

public class AlarmDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "alarmclock.db";

    private static final String SQL_CREATE_ALARM = "CREATE TABLE " + Alarm.TABLE_NAME + " (" +
            Alarm._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Alarm.COLUMN_NAME_ALARM_NAME + " TEXT," +
            Alarm.COLUMN_NAME_ALARM_TIME_HOUR + " INTEGER," +
            Alarm.COLUMN_NAME_ALARM_TIME_MINUTE + " INTEGER," +
            Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS + " TEXT," +
            Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY + " BOOLEAN," +
            Alarm.COLUMN_NAME_ALARM_TONE + " TEXT," +
            Alarm.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN," +

            Alarm.COLUMN_NAME_TONE_VOLUME + " INTEGER," +
            Alarm.COLUMN_NAME_TONE_VOLUME_RISE + " BOOLEAN," +
            Alarm.COLUMN_NAME_ALARM_FLASH + " BOOLEAN," +
            Alarm.COLUMN_NAME_ALARM_BRIGHTNESS + " INTEGER," +
            Alarm.COLUMN_NAME_ALARM_VIBRATE + " BOOLEAN," +

            Alarm.COLUMN_NAME_ALARM_FLASH_PATTERN + " STRING," +
            Alarm.COLUMN_NAME_ALARM_VIBRATE_PATTERN + " STRING," +
            Alarm.COLUMN_NAME_ALARM_DARK_THEME + " BOOLEAN," +
            Alarm.COLUMN_NAME_DIGITAL_PICKER + " BOOLEAN," +
            Alarm.COLUMN_NAME_SNOOZE + " BOOLEAN" +
            ");";
    public static final String COLUMN_NAME_ALARM_ENABLED = "enabled";
    private static final String SQL_DELETE_ALARM =
            "DROP TABLE IF EXISTS " + Alarm.TABLE_NAME;

    public AlarmDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ALARM);
        onCreate(db);
    }

    private AlarmModel populateModel(Cursor c) {
        AlarmModel model = new AlarmModel();
        model.id = c.getLong(c.getColumnIndex(Alarm._ID));
        model.name = c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_NAME));
        model.timeHour = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TIME_HOUR));
        model.timeMinute = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TIME_MINUTE));
        model.repeatWeekly = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY)) != 0;
        model.alarmTone = c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TONE));
        model.isEnabled = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_ENABLED)) != 0;

        model.brightness = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_BRIGHTNESS));
        model.flash = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_FLASH)) != 0;
        model.vibrate = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_VIBRATE)) != 0;
        model.setVolume(c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_TONE_VOLUME)));
        model.volume_rising = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_TONE_VOLUME_RISE)) != 0;

        model.flash_pattern = c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_FLASH_PATTERN));
        model.vibrate_pattern = c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_VIBRATE_PATTERN));
        model.dark_theme = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_DARK_THEME)) != 0;
        model.digital_picker = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_DIGITAL_PICKER)) != 0;
        model.snooze = c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_SNOOZE)) != 0;

        String[] repeatingDays = c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS)).split(",");
        for (int i = 0; i < repeatingDays.length; ++i) {
            model.setRepeatingDay(i, !repeatingDays[i].equals("false"));
        }

        return model;
    }

    private ContentValues populateContent(AlarmModel model) {
        ContentValues values = new ContentValues();
        values.put(Alarm.COLUMN_NAME_ALARM_NAME, model.name);
        values.put(Alarm.COLUMN_NAME_ALARM_TIME_HOUR, model.timeHour);
        values.put(Alarm.COLUMN_NAME_ALARM_TIME_MINUTE, model.timeMinute);
        values.put(Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY, model.repeatWeekly);
        values.put(Alarm.COLUMN_NAME_ALARM_TONE, model.alarmTone);
        values.put(Alarm.COLUMN_NAME_ALARM_ENABLED, model.isEnabled);

        values.put(Alarm.COLUMN_NAME_TONE_VOLUME, model.getVolume());
        values.put(Alarm.COLUMN_NAME_TONE_VOLUME_RISE, model.volume_rising);
        values.put(Alarm.COLUMN_NAME_ALARM_FLASH, model.flash);
        values.put(Alarm.COLUMN_NAME_ALARM_BRIGHTNESS, model.brightness);
        values.put(Alarm.COLUMN_NAME_ALARM_VIBRATE, model.vibrate);

        values.put(Alarm.COLUMN_NAME_ALARM_FLASH_PATTERN, model.flash_pattern);
        values.put(Alarm.COLUMN_NAME_ALARM_VIBRATE_PATTERN, model.vibrate_pattern);
        values.put(Alarm.COLUMN_NAME_ALARM_DARK_THEME, model.dark_theme);
        values.put(Alarm.COLUMN_NAME_DIGITAL_PICKER, model.digital_picker);
        values.put(Alarm.COLUMN_NAME_SNOOZE, model.snooze);

        String repeatingDays = "";
        for (int i = 0; i < 7; ++i) {
            repeatingDays += model.getRepeatingDay(i) + ",";
        }
        values.put(Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

        return values;
    }

    public long createAlarm(AlarmModel model) {
        ContentValues values = populateContent(model);
        return getWritableDatabase().insert(Alarm.TABLE_NAME, null, values);
    }

    public long updateAlarm(AlarmModel model) {
        ContentValues values = populateContent(model);
        return getWritableDatabase().update(Alarm.TABLE_NAME, values, Alarm._ID + " = ?", new String[]{String.valueOf(model.id)});
    }

    public AlarmModel getAlarm(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "SELECT * FROM " + Alarm.TABLE_NAME + " WHERE " + Alarm._ID + " = " + id;

        Cursor c = db.rawQuery(select, null);


        if (c.moveToNext()) {
            AlarmModel model = populateModel(c);
            if (!c.isClosed()) {
                c.close();
            }
            return model;
        }

        return null;
    }

    public List<AlarmModel> getAlarms() {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "SELECT * FROM " + Alarm.TABLE_NAME;

        Cursor c = db.rawQuery(select, null);

        List<AlarmModel> alarmList = new ArrayList<AlarmModel>();

        while (c.moveToNext()) {
            alarmList.add(populateModel(c));
        }

        if (!alarmList.isEmpty()) {
            return alarmList;
        }

        return null;
    }

    public int deleteAlarm(long id) {
        return getWritableDatabase().delete(Alarm.TABLE_NAME, Alarm._ID + " = ?", new String[]{String.valueOf(id)});
    }
}