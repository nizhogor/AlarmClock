package nizhogor.com.flashalarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

public class AlarmManagerHelper extends BroadcastReceiver {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TIME_HOUR = "timeHour";
    public static final String TIME_MINUTE = "timeMinute";
    public static final String TONE = "alarmTone";
    public static final String VOLUME = "volume";
    public static final String VOLUME_RISING = "volume_rising";
    public static final String VIBRATE = "vibrate";
    public static final String FLASH = "flash";
    public static final String VIBRATE_PATTERN = "vibrate_pattern";
    public static final String FLASH_PATTERN = "flash_pattern";

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context, false);
    }

    public static void setAlarms(Context context, boolean ignoreWeekly) {
        cancelAlarms(context);

        AlarmDBHelper dbHelper = new AlarmDBHelper(context);

        List<AlarmModel> alarms =  dbHelper.getAlarms();
        if ( alarms != null ) {
            for (AlarmModel alarm : alarms) {
                if (alarm.isEnabled) {

                    PendingIntent pIntent = createPendingIntent(context, alarm);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHour);
                    calendar.set(Calendar.MINUTE, alarm.timeMinute);
                    calendar.set(Calendar.SECOND, 00);

                    //Find next time to set
                    final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
                    boolean alarmSet = false;

                    //First check if it's later in the week
                    for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                        if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                                !(dayOfWeek == nowDay && alarm.timeHour < nowHour) &&
                                !(dayOfWeek == nowDay && alarm.timeHour == nowHour && alarm.timeMinute <= nowMinute)) {
                            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                            setAlarm(context, calendar, pIntent);
                            alarmSet = true;
                            break;
                        }
                    }

                    //Else check if it's earlier in the week
                    if (!alarmSet) {
                        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay && (ignoreWeekly || alarm.repeatWeekly)) {
                                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                                calendar.add(Calendar.WEEK_OF_YEAR, 1);

                                setAlarm(context, calendar, pIntent);
                                alarmSet = true;
                                break;
                            }
                        }
                    }

                    if (!alarmSet) {
                        ((AlarmListActivity) context).setAlarmEnabled(alarm.id, false);
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    public static void cancelAlarms(Context context) {
        AlarmDBHelper dbHelper = new AlarmDBHelper(context);

        List<AlarmModel> alarms =  dbHelper.getAlarms();

        if (alarms != null) {
            for (AlarmModel alarm : alarms) {
                if (alarm.isEnabled) {
                    PendingIntent pIntent = createPendingIntent(context, alarm);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pIntent);
                }
            }
        }
    }

    private static PendingIntent createPendingIntent(Context context, AlarmModel model) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(ID, model.id);
        intent.putExtra(NAME, model.name);
        intent.putExtra(TIME_HOUR, model.timeHour);
        intent.putExtra(TIME_MINUTE, model.timeMinute);
        intent.putExtra(TONE, model.alarmTone.toString());
        intent.putExtra(VIBRATE, model.vibrate);
        intent.putExtra(FLASH, model.flash);
        intent.putExtra(VOLUME, model.getVolumeFloat());
        intent.putExtra(VOLUME_RISING, model.volume_rising);
        intent.putExtra(FLASH_PATTERN, model.flash_pattern);
        intent.putExtra(VIBRATE_PATTERN, model.vibrate_pattern);

        return PendingIntent.getService(context, (int) model.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static String getTimeUntilAlarm(AlarmModel alarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHour);
        calendar.set(Calendar.MINUTE, alarm.timeMinute);
        calendar.set(Calendar.SECOND, 00);

        //Find next time to set
        final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
        boolean alarmSet = false;

        //First check if it's later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                    !(dayOfWeek == nowDay && alarm.timeHour < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.timeHour == nowHour && alarm.timeMinute <= nowMinute)) {
                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                alarmSet = true;
                break;
            }
        }

        //Else check if it's earlier in the week
        if (!alarmSet) {
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
                if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay) {
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    alarmSet = true;
                    break;
                }
            }
        }

        if (!alarmSet) {
            return "";
        }

        long difference = calendar.getTimeInMillis() - System.currentTimeMillis();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long inDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long inHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long inMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        String intro ="Alarm is set for ";
        String alert = intro;
        if (inDays > 0) {
            alert += inDays + " day";
            if (inDays > 1)
                alert +="s";
            if (inMinutes > 0 && inHours > 0) {
                alert += ", " + inHours + " hour" + ((inHours > 1) ? "s": "") + ", and " +
                        inMinutes + " minute" + ((inMinutes > 1) ? "s": "");

            } else if (inMinutes == 0 && inHours == 0 ) {
                // do nothing
            } else {
                alert += " and " + ((inMinutes == 0) ? inMinutes: inHours);
                if (inMinutes !=0)
                    alert += "minute";
                else
                    alert += "hour";
                if (inMinutes + inHours > 1) {
                    alert += "s";
                }
            }
        } else {
            if (inMinutes > 0 && inHours > 0) {
                alert += inHours + " hour" + ((inHours > 1) ? "s": "") + " and " +
                        inMinutes + " minute" + ((inMinutes > 1) ? "s": "");
            } else if (inMinutes == 0 && inHours == 0 ) {
                // do nothing
            } else {
                alert += ((inMinutes == 0) ? inMinutes: inHours);
                if (inMinutes !=0)
                    alert += "minute";
                else
                    alert += "hour";
                if (inMinutes + inHours > 1) {
                    alert += "s";
                }
            }
        }
        if (alert.equals(intro))
            return "";
        alert += " from now";
        return alert;
    }
}