package com.nizhogor.flashalarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public final class HelpFragment extends Fragment {
    private static final String KEY_CONTENT = "HelpFragment:Content";
    public static final String active_alarms = "Active Alarms";
    public static final String alarm_settings = "Alarm Settings";
    public static final String additional_settings = "Additional Settings";

    public static HelpFragment newInstance(String content) {
        HelpFragment fragment = new HelpFragment();
        fragment.mContent = content;

        return fragment;
    }

    private String mContent = "???";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_fragment, container, false);
        TextView textView = (TextView) view.findViewById(R.id.help_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.help_image);
        int textId = 0;
        int imageId = 0;

        switch (mContent) {
            case additional_settings:
                textId = R.string.settings_help;
                imageId = R.drawable.settings_help;
                break;
            case active_alarms:
                textId = R.string.active_help;
                imageId = R.drawable.active_help;
                break;
            case alarm_settings:
                textId = R.string.details_help;
                imageId = R.drawable.details_help;
                break;
        }
        if ((textId & imageId) != 0) {
            textView.setText(getResources().getString(textId));
            imageView.setImageResource(imageId);
            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}