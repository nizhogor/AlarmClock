package nizhogor.com.flashalarm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

public class AlarmListAdapter extends BaseAdapter {

    private Context mContext;
    private List<AlarmModel> mAlarms;

    public AlarmListAdapter(Context context, List<AlarmModel> alarms) {
        mContext = context;
        mAlarms = alarms;
    }

    public void setAlarms(List<AlarmModel> alarms) {
        mAlarms = alarms;
    }

    @Override
    public int getCount() {
        if (mAlarms != null) {
            return mAlarms.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mAlarms != null) {
            return mAlarms.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mAlarms != null) {
            return mAlarms.get(position).id;
        }
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.alarm_list_item, parent, false);
        }

        AlarmModel model = (AlarmModel) getItem(position);
        TextView txtTime = (TextView) view.findViewById(R.id.alarm_item_time);
        TextView dayPeriod = (TextView) view.findViewById(R.id.time_period);

        if(DateFormat.is24HourFormat(mContext)){
            txtTime.setText(String.format("%02d : %02d", model.timeHour, model.timeMinute));
            txtTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.large_clock_digits));
            dayPeriod.setText("");
            //txtTime.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.large_clock_digits));
        }
        else{
            int hour = model.timeHour;
            String ampm = "am";
            if (model.timeHour >= 12) {
                ampm = "pm";
                if (model.timeHour >12)
                    hour = model.timeHour - 12;
                if (hour == 0) {
                    hour = 12;
                }
            }
            txtTime.setText(String.format("%02d : %02d", hour, model.timeMinute));
            txtTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.medium_clock_digits));
            dayPeriod.setText(ampm);
            dayPeriod.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.time_period));
        }
        TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
        txtName.setText(model.name);

        updateText((TextView) view.findViewById(R.id.alarm_item_sunday), model.getRepeatingDay(AlarmModel.SUNDAY));
        updateText((TextView) view.findViewById(R.id.alarm_item_monday), model.getRepeatingDay(AlarmModel.MONDAY));
        updateText((TextView) view.findViewById(R.id.alarm_item_tuesday), model.getRepeatingDay(AlarmModel.TUESDAY));
        updateText((TextView) view.findViewById(R.id.alarm_item_wednesday), model.getRepeatingDay(AlarmModel.WEDNESDAY));
        updateText((TextView) view.findViewById(R.id.alarm_item_thursday), model.getRepeatingDay(AlarmModel.THURSDAY));
        updateText((TextView) view.findViewById(R.id.alarm_item_friday), model.getRepeatingDay(AlarmModel.FRIDAY));
        updateText((TextView) view.findViewById(R.id.alarm_item_saturday), model.getRepeatingDay(AlarmModel.SATURDAY));

        updateImage((ImageView) view.findViewById(R.id.flash), model.flash, R.drawable.ic_flash_on_white_24dp);
        updateImage((ImageView) view.findViewById(R.id.vibrate), model.vibrate, R.drawable.ic_vibration_white_24dp);
        updateImage((ImageView) view.findViewById(R.id.rising), model.volume_rising, R.drawable.ic_filter_list_white_24dp);
        ToggleButton btnToggle = (ToggleButton) view.findViewById(R.id.alarm_item_toggle);
        btnToggle.setChecked(model.isEnabled);
        btnToggle.setTag(Long.valueOf(model.id));
        btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((AlarmListActivity) mContext).setAlarmEnabled(((Long) buttonView.getTag()).longValue(), isChecked);
            }
        });

        view.setTag(Long.valueOf(model.id));
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                ((AlarmListActivity) mContext).startAlarmDetailsActivity(((Long) view.getTag()).longValue());
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                ((AlarmListActivity) mContext).deleteAlarm(((Long) view.getTag()).longValue());
                return true;
            }
        });

        return view;
    }

    private void updateText(TextView view, boolean isOn) {
        if (isOn) {
            view.setTextColor(Color.rgb(0, 255, 0));
        } else {
            view.setTextColor(Color.WHITE);
        }
    }

    private void updateImage(ImageView view, boolean isOn, int image){
        view.setImageResource(image);
        if(isOn) {
            view.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }
        else {
            view.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }
    }
}