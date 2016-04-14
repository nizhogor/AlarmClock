package nizhogor.com.flashalarm;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;

import java.util.Calendar;


public class AlarmDetailsActivity extends FragmentActivity implements TimePickerDialogFragment.TimePickerDialogHandler,
        SettingsDialogFragment.OnFragmentInteractionListener {

    private TimePicker timePicker;
    private TextView alarmTime;
    private TextView alarmPeriod;
    private EditText edtName;
    private CustomToggleButton chkWeekly;
    private CustomToggleButton chkSunday;
    private CustomToggleButton chkMonday;
    private CustomToggleButton chkTuesday;
    private CustomToggleButton chkWednesday;
    private CustomToggleButton chkThursday;
    private CustomToggleButton chkFriday;
    private CustomToggleButton chkSaturday;
    private TextView txtToneSelection;

    private CustomToggleButton chkVibrate;
    private CustomToggleButton chkFlash;
    private CustomToggleButton chkRisingVolume;
    private SeekBar volumeSeekBar;
    private ImageView playImageView;

    private AlarmModel alarmDetails = new AlarmModel();
    private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
    private Context mContext = this;
    private HardwareManager hardwareManager;
    private int startVolume = 1;
    private boolean isTimeSet = false;
    private static final int SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_details);
        setupActionBar();
        hardwareManager = new HardwareManager(mContext);

        alarmTime = (TextView) findViewById(R.id.time_picker);
        alarmPeriod = (TextView) findViewById(R.id.details_time_period);
        edtName = (EditText) findViewById(R.id.alarm_details_name);
        chkWeekly = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_weekly);
        chkSunday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_sunday);
        chkMonday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_monday);
        chkTuesday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_tuesday);
        chkWednesday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_wednesday);
        chkThursday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_thursday);
        chkFriday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_friday);
        chkSaturday = (CustomToggleButton) findViewById(R.id.alarm_details_repeat_saturday);
        txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);

        chkVibrate = (CustomToggleButton) findViewById(R.id.alarm_details_vibrate);
        chkFlash = (CustomToggleButton) findViewById(R.id.alarm_details_flash);
        chkRisingVolume = (CustomToggleButton) findViewById(R.id.alarm_details_volume_rising);
        volumeSeekBar = (SeekBar) findViewById(R.id.volume_bar);
        playImageView = (ImageView) findViewById(R.id.play_button);

        long id = getIntent().getExtras().getLong("id");
        //long id = -1;
        if (id == -1) {
            alarmDetails = new AlarmModel();
            Calendar c = Calendar.getInstance();
            setAlarmTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            volumeSeekBar.setEnabled(false);
        } else {
            alarmDetails = dbHelper.getAlarm(id);
            edtName.setText(alarmDetails.name);
            chkWeekly.setChecked(alarmDetails.repeatWeekly);
            chkSunday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.SUNDAY));
            chkMonday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.MONDAY));
            chkTuesday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.TUESDAY));
            chkWednesday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.WEDNESDAY));
            chkThursday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.THURSDAY));
            chkFriday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.FRIDAY));
            chkSaturday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.SATURDAY));

            if (alarmDetails.alarmTone != null)
                txtToneSelection.setText(RingtoneManager.getRingtone(this, alarmDetails.alarmTone).getTitle(this));
            updateSeekBar();
            chkVibrate.setChecked(alarmDetails.vibrate);
            chkFlash.setChecked(alarmDetails.flash);
            chkRisingVolume.setChecked(alarmDetails.volume_rising);
            volumeSeekBar.setProgress(alarmDetails.getVolumeInt());
            playImageView.setImageResource(R.drawable.play);
            setAlarmTime(alarmDetails.timeHour, alarmDetails.timeMinute);
        }

        alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = getHourFromTextView();
                int minute = getMinuteFromTextView();

                FragmentManager fm = getSupportFragmentManager();

                if (!alarmDetails.digital_picker) {
                    DialogFragment newFragment = RadialTimePickerDialog.newInstance(new RadialTimePickerDialog.OnTimeSetListener() {
                                                                                        @Override
                                                                                        public void onTimeSet(RadialTimePickerDialog radialTimePickerDialog, int hr, int min) {
                                                                                            setAlarmTime(hr, min);
                                                                                        }
                                                                                    },
                            hour,
                            minute,
                            DateFormat.is24HourFormat(mContext));
                    newFragment.show(fm, "radial");
                } else {
                    // or R.style.BetterPickersDialogFragment.Light
                    TimePickerBuilder builder = new TimePickerBuilder().
                            setFragmentManager(fm).
                            setStyleResId(R.style.BetterPickersDialogFragment);
                    builder.show();
                }
            }
        });


        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                alarmDetails.setVolume(seekBar.getProgress());
                startPlaying();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
        });

        playImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (!hardwareManager.isPlaying()) {
                        startPlaying();
                    } else {
                        hardwareManager.stopPlaying();
                        playImageView.setImageResource(R.drawable.play);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
        ringToneContainer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AlarmDetailsActivity.this);
                //builder.setMessage("Please confirm")
                builder.setTitle("Choose alarm from")
                        .setCancelable(true)
                        .setItems(R.array.tone_sources, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent;
                                switch (which) {
                                    case 0:
                                        intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                                        startActivityForResult(intent, 1);
                                        break;
                                    case 1:
                                        intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                                        startActivityForResult(intent, 1);
                                        break;
                                    case 2:
                                        intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.EXTRA_RINGTONE_TYPE);
                                        startActivityForResult(intent, 1);
                                        break;
                                }
                            }
                        }).show();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private int getHourFromTextView() {
        int adjust = 0;
        //!add 24 hour case
        if (alarmPeriod.getText().toString().toLowerCase().equals("pm"))
            adjust = 12;
        String time = alarmTime.getText().toString();
        int militaryHour = Integer.parseInt(time.substring(0, 2)) + adjust;
        if (militaryHour == 24)
            return 12;
        return militaryHour;
    }

    private int getMinuteFromTextView() {
        String time = alarmTime.getText().toString();
        return Integer.parseInt(time.substring(3, 5));
    }

    private void setAlarmTime(int hour, int minute) {
        isTimeSet = true;
        if (!DateFormat.is24HourFormat(this)) {
            if (hour >= 12) {
                alarmPeriod.setText("pm");
                if (hour > 12) {
                    hour -= 12;
                }
            } else {
                alarmPeriod.setText("am");
            }
        } else {
            alarmPeriod.setText("");
        }
        alarmTime.setText(String.format("%02d:%02d", hour, minute));
    }

    private void startPlaying() {
        if (alarmDetails.alarmTone != null) {
            playImageView.setImageResource(R.drawable.stop);
            if (chkRisingVolume.isChecked()) {
                hardwareManager.PlayRisingAlarm(alarmDetails.alarmTone);
            } else
                hardwareManager.PlayAlarm(alarmDetails.alarmTone, volumeSeekBar.getProgress());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1: {
                    LinearLayout alarm_ringtone_container = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
                    alarm_ringtone_container.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                    alarmDetails.alarmTone = uri;
                    TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
                    if (uri == null) {
                        txtToneSelection.setText("Silent");
                    } else {
                        Ringtone ringtone = RingtoneManager.getRingtone(this, alarmDetails.alarmTone);
                        txtToneSelection.setText(ringtone.getTitle(this));
                    }
                    updateSeekBar();
                    break;
                }
                case SETTINGS: {

                }
                default: {
                    break;
                }
            }
        }
    }

    public void updateSeekBar() {
        if (!chkRisingVolume.isChecked() && alarmDetails.alarmTone != null) {
            volumeSeekBar.setEnabled(true);
        } else
            volumeSeekBar.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_alarm:
                onBackPressed();
                break;
            case R.id.remove_alarm:
                // if new alarm, simply drop model
                if (alarmDetails.id > 0) {
                    (new AlarmDBHelper(this)).deleteAlarm(alarmDetails.id);
                }
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.additional_options: {
                startDialogFragment();
                break;
            }
            case R.id.report: {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"nizhogor@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Problem with FlashAlarm");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    View layout = getLayoutInflater().inflate(R.layout.toast_layout,
                            (ViewGroup) findViewById(R.id.toast_layout_root));

                    TextView text = (TextView) layout.findViewById(R.id.text);
                    Resources resources = getResources();
                    String toastText = resources.getString(R.string.bug_toast_body) + " " + resources.getString(R.string.bug_adress);
                    text.setText(toastText);

                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
            case R.id.rate:
                String packageName = this.getPackageName();
                Uri uri = Uri.parse("market://details?id=" + packageName);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                }
                break;
            case R.id.help:
                Intent launchHelp = new Intent(this, HelpActivity.class);
                startActivity(launchHelp);
                break;
            default:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startDialogFragment() {
        SettingsDialogFragment dialogFragment = SettingsDialogFragment.newInstance(alarmDetails);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        dialogFragment.setShowsDialog(true);
        dialogFragment.show(ft, "dialog");
    }

    private void updateModelFromLayout() {
        alarmDetails.name = edtName.getText().toString();
        alarmDetails.repeatWeekly = chkWeekly.isChecked();
        alarmDetails.setRepeatingDay(AlarmModel.SUNDAY, chkSunday.isChecked());
        alarmDetails.setRepeatingDay(AlarmModel.MONDAY, chkMonday.isChecked());
        alarmDetails.setRepeatingDay(AlarmModel.TUESDAY, chkTuesday.isChecked());
        alarmDetails.setRepeatingDay(AlarmModel.WEDNESDAY, chkWednesday.isChecked());
        alarmDetails.setRepeatingDay(AlarmModel.THURSDAY, chkThursday.isChecked());
        alarmDetails.setRepeatingDay(AlarmModel.FRIDAY, chkFriday.isChecked());
        alarmDetails.setRepeatingDay(AlarmModel.SATURDAY, chkSaturday.isChecked());
        alarmDetails.timeHour = getHourFromTextView();
        alarmDetails.timeMinute = getMinuteFromTextView();

        alarmDetails.isEnabled = true;

        alarmDetails.setVolume(volumeSeekBar.getProgress());
        alarmDetails.flash = chkFlash.isChecked();
        alarmDetails.volume_rising = chkRisingVolume.isChecked();
        alarmDetails.vibrate = chkVibrate.isChecked();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_alarm_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveAlarm = menu.findItem(R.id.save_alarm);
        saveAlarm.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        Drawable dr = getResources().getDrawable(R.drawable.save_alarm);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, 85, 85, true));
        saveAlarm.setIcon(d);

        MenuItem deleteAlarm = menu.findItem(R.id.remove_alarm);
        deleteAlarm.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        dr = getResources().getDrawable(R.drawable.remove_alarm);
        bitmap = ((BitmapDrawable) dr).getBitmap();
        d = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, 85, 85, true));
        deleteAlarm.setIcon(d);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hardwareManager.stopPlaying();
    }

    @Override
    public void onBackPressed() {
        updateModelFromLayout();
        String timeUntilAlarm = AlarmManagerHelper.getTimeUntilAlarm(alarmDetails);

        String message = "";
        if (timeUntilAlarm.isEmpty()) {
            message = "No active days are set";
        } else {
            if (alarmDetails.alarmTone == null) {
                message = "Alarm melody is not set";
                alarmDetails.volume_rising = false;
            }
        }

        if (message.isEmpty()) {
            message = timeUntilAlarm;
        }

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();

        saveAlarm();
        super.onBackPressed();
    }

    private void saveAlarm() {
        AlarmManagerHelper.cancelAlarms(this);
        if (alarmDetails.id < 0) {
            dbHelper.createAlarm(alarmDetails);
        } else {
            dbHelper.updateAlarm(alarmDetails);
        }
        AlarmManagerHelper.setAlarms(this, true);
        setResult(RESULT_OK);
    }

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        setAlarmTime(hourOfDay, minute);
    }

    @Override
    public void onFragmentInteraction(AlarmModel alarmModel) {

    }
}
