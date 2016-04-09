package nizhogor.com.flashalarm;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private boolean updatingVibratePattern = false;
    private TextView mFlashLabel;
    private CustomToggleButton mDigitalPicker;
    private ImageView mFlashIcon;
    private ImageView mVibrateIcon;
    private HardwareManager mHardwareManager;

    private static Activity mActivity;

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            for (int i = start; i < end; i++) {

                if (!isValidLetter(source.charAt(i))) {
                    showInputInstructionToast(R.string.pattern_hint);
                    return "";
                }

            }

            return null;
        }
    };

    private final TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence charSequence, int a, int b, int c) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void afterTextChanged(Editable s) {
            if (s.length() != 0) {
                String str = s.toString();

                if (str.contains("c") && s.length() > 1) {
                    if (updatingVibratePattern) {
                        mVibratePattern.setText("");
                        mVibratePattern.append("c");
                    } else {
                        mFlashPattern.setText("");
                        mFlashPattern.append("c");
                    }
                    if (s.length() == 2) {
                        showInputInstructionToast(R.string.pattern_remove_constant);
                    }
                }
            }
        }
    };

    private boolean isValidLetter(char c) {
        switch (c) {
            case 's':
            case 'm':
            case 'l':
            case 'e':
            case 'c':
                return true;
            default:
                return false;
        }
    }

    private void showInputInstructionToast(int toastText) {
        LayoutInflater inflater = mActivity.getLayoutInflater();

        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) mActivity.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(toastText);

        Toast toast = new Toast(mActivity);
        toast.setDuration(Toast.LENGTH_SHORT);
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
        mFlashPattern.addTextChangedListener(textWatcher);
        mVibratePattern.setFilters(new InputFilter[]{filter});
        mVibratePattern.addTextChangedListener(textWatcher);

        mFlashPattern.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent m) {
                updatingVibratePattern = false;
                return false;
            }
        });
        mVibratePattern.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent m) {
                updatingVibratePattern = true;
                return false;
            }
        });

        mFlashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHardwareManager.isFlashing()) {
                    mHardwareManager.stopFlash();
                    mFlashIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                } else {
                    mHardwareManager.startFlash(mFlashPattern.getText().toString());
                    mFlashIcon.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        mVibrateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHardwareManager.isVibrating()) {
                    mHardwareManager.stopVibrate();
                    mVibrateIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                } else {
                    mHardwareManager.startVibrate(mVibratePattern.getText().toString());
                    mVibrateIcon.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        mFlashLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputInstructionToast(R.string.pattern_hint);
            }
        });
        mVibrateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputInstructionToast(R.string.pattern_hint);
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
