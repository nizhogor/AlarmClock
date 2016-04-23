package com.nizhogor.flashalarm;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
        com.nizhogor.flashalarm.SettingsDialogFragment.OnFragmentInteractionListener {

    private static final int singleShotSave = 1;
    private static final int singleShotMenu = 2;

    private TimePicker timePicker;
    private TextView alarmTime;
    private TextView alarmPeriod;
    private EditText edtName;
    private com.nizhogor.flashalarm.CustomToggleButton chkWeekly;
    private com.nizhogor.flashalarm.CustomToggleButton chkSunday;
    private com.nizhogor.flashalarm.CustomToggleButton chkMonday;
    private com.nizhogor.flashalarm.CustomToggleButton chkTuesday;
    private com.nizhogor.flashalarm.CustomToggleButton chkWednesday;
    private com.nizhogor.flashalarm.CustomToggleButton chkThursday;
    private com.nizhogor.flashalarm.CustomToggleButton chkFriday;
    private com.nizhogor.flashalarm.CustomToggleButton chkSaturday;
    private TextView txtToneSelection;

    private com.nizhogor.flashalarm.CustomToggleButton chkVibrate;
    private com.nizhogor.flashalarm.CustomToggleButton chkFlash;
    private com.nizhogor.flashalarm.CustomToggleButton chkRisingVolume;
    private View volumeContainer;
    private SeekBar volumeSeekBar;
    private ImageView playImageView;

    private com.nizhogor.flashalarm.AlarmModel alarmDetails = new com.nizhogor.flashalarm.AlarmModel();
    private com.nizhogor.flashalarm.AlarmDBHelper dbHelper = new com.nizhogor.flashalarm.AlarmDBHelper(this);
    private Context mContext = this;
    private com.nizhogor.flashalarm.HardwareManager hardwareManager;
    private int startVolume = 1;
    private boolean isTimeSet = false;
    private static final int SETTINGS = 2;

    private static final String KEY_ID = "AlarmDetailsActivity:id";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(KEY_ID, alarmDetails.id);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_details);
        setupActionBar();
        hardwareManager = new com.nizhogor.flashalarm.HardwareManager(mContext);

        alarmTime = (TextView) findViewById(R.id.time_picker);
        alarmPeriod = (TextView) findViewById(R.id.details_time_period);
        edtName = (EditText) findViewById(R.id.alarm_details_name);
        chkWeekly = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_weekly);
        chkSunday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_sunday);
        chkMonday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_monday);
        chkTuesday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_tuesday);
        chkWednesday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_wednesday);
        chkThursday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_thursday);
        chkFriday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_friday);
        chkSaturday = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_repeat_saturday);
        txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);

        chkVibrate = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_vibrate);
        chkFlash = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_flash);
        chkRisingVolume = (com.nizhogor.flashalarm.CustomToggleButton) findViewById(R.id.alarm_details_volume_rising);
        volumeSeekBar = (SeekBar) findViewById(R.id.volume_bar);
        volumeContainer= findViewById(R.id.volume_container);
        playImageView = (ImageView) findViewById(R.id.play_button);

        setSoftKeyLogic(findViewById(R.id.root_layout));
        long id = -1;
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_ID)) {
            id = savedInstanceState.getLong(KEY_ID);
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                id = intent.getExtras().getLong("id");
            }
        }
        //long id = -1;
        if (id == -1) {
            alarmDetails = new com.nizhogor.flashalarm.AlarmModel();
            Calendar c = Calendar.getInstance();
            setAlarmTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            //volumeSeekBar.setEnabled(false);
        } else {
            alarmDetails = dbHelper.getAlarm(id);
            edtName.setText(alarmDetails.name);
            chkWeekly.setChecked(alarmDetails.repeatWeekly);
            chkSunday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.SUNDAY));
            chkMonday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.MONDAY));
            chkTuesday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.TUESDAY));
            chkWednesday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.WEDNESDAY));
            chkThursday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.THURSDAY));
            chkFriday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.FRIDAY));
            chkSaturday.setChecked(alarmDetails.getRepeatingDay(com.nizhogor.flashalarm.AlarmModel.SATURDAY));

            if (alarmDetails.alarmTone.equals("")) {
                txtToneSelection.setText("Silent");
            } else {
                String title = RingtoneManager.getRingtone(this, Uri.parse(alarmDetails.alarmTone)).getTitle(this);
                txtToneSelection.setText(title);
            }

            chkVibrate.setChecked(alarmDetails.vibrate);
            chkFlash.setChecked(alarmDetails.flash);
            chkRisingVolume.setChecked(alarmDetails.volume_rising);
            volumeSeekBar.setProgress(alarmDetails.getVolumeInt());
            if (chkRisingVolume.isChecked()) {
                volumeContainer.setVisibility(View.GONE);
            } else {
                volumeContainer.setVisibility(View.VISIBLE);
            }
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

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    public void setSoftKeyLogic(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setSoftKeyLogic(innerView);
            }
        }
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
        if (alarmDetails.alarmTone != null && !alarmDetails.alarmTone.equals("")) {
            playImageView.setImageResource(R.drawable.stop);
            if (chkRisingVolume.isChecked()) {
                hardwareManager.PlayRisingAlarm(alarmDetails.alarmTone);
            } else
                hardwareManager.PlayAlarm(alarmDetails.alarmTone, volumeSeekBar.getProgress());
        } else {
            showToast(getResources().getString(R.string.play_no_tone));
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

                    alarmDetails.alarmTone = (uri == null) ? "" : uri.toString();
                    TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
                    if (uri == null) {
                        txtToneSelection.setText("Silent");
                    } else {
                        Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                        txtToneSelection.setText(ringtone.getTitle(this));
                    }
                    //updateSeekBar();
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

        if (!chkRisingVolume.isChecked()) {
            volumeContainer.setAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom));
            volumeContainer.setVisibility(View.VISIBLE);

            //findViewById(R.id.hardware_buttons_container).setAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom));

           /* volumeContainer.animate()
                    .alpha(1f)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(null);*/
        } else {
            showToast("slide out");
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    volumeContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            volumeContainer.startAnimation(animation);
            //

        }

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
                    (new com.nizhogor.flashalarm.AlarmDBHelper(this)).deleteAlarm(alarmDetails.id);
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
                    Resources resources = getResources();
                    String message = resources.getString(R.string.bug_toast_body) + " " + resources.getString(R.string.bug_adress);
                    showToast(message);
                }
            }
            case R.id.rate:
                String packageName = this.getPackageName();
                Uri uri = Uri.parse("market://details?id=" + packageName);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

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
                Intent launchHelp = new Intent(this, com.nizhogor.flashalarm.HelpActivity.class);
                startActivity(launchHelp);
                break;
            default:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startDialogFragment() {
        com.nizhogor.flashalarm.SettingsDialogFragment dialogFragment = com.nizhogor.flashalarm.SettingsDialogFragment.newInstance(alarmDetails);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        dialogFragment.setShowsDialog(true);
        dialogFragment.show(ft, "dialog");
    }

    private void updateModelFromLayout() {
        alarmDetails.name = edtName.getText().toString();
        alarmDetails.repeatWeekly = chkWeekly.isChecked();
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.SUNDAY, chkSunday.isChecked());
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.MONDAY, chkMonday.isChecked());
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.TUESDAY, chkTuesday.isChecked());
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.WEDNESDAY, chkWednesday.isChecked());
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.THURSDAY, chkThursday.isChecked());
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.FRIDAY, chkFriday.isChecked());
        alarmDetails.setRepeatingDay(com.nizhogor.flashalarm.AlarmModel.SATURDAY, chkSaturday.isChecked());
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
        String timeUntilAlarm = com.nizhogor.flashalarm.AlarmManagerHelper.getTimeUntilAlarm(alarmDetails);

        String message = "";
        if (timeUntilAlarm.isEmpty()) {
            message = "No active days are set";
        } else {
            if (alarmDetails.alarmTone.equals("")) {
                message = "Alarm melody is not set";
                alarmDetails.volume_rising = false;
            }
        }

        if (message.isEmpty()) {
            message = timeUntilAlarm;
        }

        showToast(message);
        saveAlarm();
        super.onBackPressed();
    }

    public void showToast(String message) {
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
    }

    private void saveAlarm() {
        if (alarmDetails.id < 0) {
            dbHelper.createAlarm(alarmDetails);
        } else {
            dbHelper.updateAlarm(alarmDetails);
        }
        com.nizhogor.flashalarm.AlarmManagerHelper.setAlarms(this, true);
        setResult(RESULT_OK);
    }

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        setAlarmTime(hourOfDay, minute);
    }

    @Override
    public void onFragmentInteraction(com.nizhogor.flashalarm.AlarmModel alarmModel) {

    }
}
