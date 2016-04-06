package nizhogor.com.flashalarm;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsDialogFragment extends DialogFragment {
    private static AlarmModel alarmDetails;
    private OnFragmentInteractionListener mListener;
    private TextView mVibratePattern;
    private TextView mFlashPattern;
    private TextView mVibrateLabel;
    private TextView mFlashLabel;
    private CustomToggleButton mDigitalPicker;
    private ImageView mFlashIcon;
    private ImageView mVibrateIcon;
    private HardwareManager mHardwareManager;

    private static Activity mActivity;

    private InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!isValidLetter(source.charAt(i))) {
                    showInputInstructionToast();
                    return "";
                }
            }
            return null;
        }
    };

    private boolean isValidLetter(char c) {
        switch (c) {
            case 's':
            case 'm':
            case 'l':
            case 'e':
                return true;
            default:
                return false;
        }
    }

    private void showInputInstructionToast() {
        LayoutInflater inflater = mActivity.getLayoutInflater();

        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) mActivity.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(R.string.pattern_hint);

        Toast toast = new Toast(mActivity);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setShowsDialog(true);
        mHardwareManager = new HardwareManager(this.getActivity());
    }

    public static SettingsDialogFragment newInstance(AlarmModel alarmModel) {
        alarmDetails = alarmModel;
        SettingsDialogFragment fragment = new SettingsDialogFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsDialogFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.settings_fragment, container, false);
        mFlashPattern = (TextView) v.findViewById(R.id.flash_pattern);
        mVibratePattern = (TextView) v.findViewById(R.id.vibrate_pattern);
        mFlashLabel = (TextView) v.findViewById(R.id.flash_label);
        mVibrateLabel = (TextView) v.findViewById(R.id.vibrate_label);
        mFlashIcon = (ImageView) v.findViewById(R.id.flash_icon);
        mVibrateIcon = (ImageView) v.findViewById(R.id.vibrate_icon);

        mFlashPattern.setFilters(new InputFilter[]{filter});
        mVibratePattern.setFilters(new InputFilter[]{filter});

        mFlashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHardwareManager.isFlashing()) {
                    mHardwareManager.stopFlash();
                    mFlashIcon.setImageResource(R.drawable.ic_flash_on_white_24dp);
                } else {
                    mHardwareManager.startFlash(mFlashPattern.getText().toString());
                    mFlashIcon.setImageResource(R.drawable.ic_action_ic_flash_on_green_24dp);
                }
            }
        });

        mVibrateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHardwareManager.isVibrating()) {
                    mHardwareManager.stopVibrate();
                    mVibrateIcon.setImageResource(R.drawable.ic_vibration_white_24dp);
                } else {
                    mHardwareManager.startVibrate(mVibratePattern.getText().toString());
                    mVibrateIcon.setImageResource(R.drawable.vibrate_green);
                }
            }
        });

        mFlashLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputInstructionToast();
            }
        });
        mVibrateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputInstructionToast();
            }
        });
        mDigitalPicker = (CustomToggleButton) v.findViewById(R.id.digital_picker);

        mFlashPattern.setText(alarmDetails.flash_pattern);
        mVibratePattern.setText(alarmDetails.vibrate_pattern);

        if (alarmDetails.digital_picker)
            mDigitalPicker.setChecked(true);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        alarmDetails.flash_pattern = mFlashPattern.getText().toString();
        alarmDetails.vibrate_pattern = mVibratePattern.getText().toString();
        alarmDetails.digital_picker = mDigitalPicker.isChecked();
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(AlarmModel alarmModel);
    }

}
