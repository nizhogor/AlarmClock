package com.nizhogor.flashalarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("NewApi")
public class CustomToggleButton extends FrameLayout {
    /*
    public static int layoutId = 1000;
    public static int buttonId = 1;
    public static int labelId=100;
    */
    TypedArray a;
    private TextView label;
    private CompoundButton button;
    private RelativeLayout mLayout;
    private Context mContext;

    public CustomToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        //Initalise and configure view layout
        RelativeLayout layout = new RelativeLayout(context);
        mLayout = layout;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        layout.setBackgroundResource(R.drawable.view_touch_selector);

        //Initalise and configure compound button

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            button = new Switch(context);
        } else {

            button = new CheckBox(context);
        }

        button.setId(View.generateViewId());
        label = new TextView(context);
        label.setId(View.generateViewId());
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.details_day));
        setColor();


        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                button.toggle();
                setColor();

            }
        });


        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setColor();
                if (label.getText().equals("Rising volume")) {
                    ((AlarmDetailsActivity) mContext).updateSeekBar();
                }
            }
        });


        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 10;
        buttonParams.bottomMargin = 10;

        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonParams.addRule(RelativeLayout.ALIGN_RIGHT, label.getId());
        button.setBackgroundColor(Color.TRANSPARENT);

        RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        labelParams.leftMargin = 8;
        labelParams.addRule(RelativeLayout.ALIGN_BASELINE, button.getId());

        //Empty view to force bottom margin
        View emptyView = new View(context);
        RelativeLayout.LayoutParams emptyViewParams = new RelativeLayout.LayoutParams(0, 0);
        emptyViewParams.addRule(RelativeLayout.BELOW, button.getId());

        //Add our components to the layout
        layout.addView(label, labelParams);
        layout.addView(button, buttonParams);
        layout.addView(emptyView, emptyViewParams);
        addView(layout);

        //Manage attributes
        int[] attributeSet = {
                android.R.attr.text,
                android.R.attr.checked
        };

        a = getContext().getTheme().obtainStyledAttributes(attrs, attributeSet, 0, 0);

        try {
            label.setText(a.getText(0));
            //button.setChecked(a.getBoolean(2, false));
            setChecked(a.getBoolean(2, false));
        } finally {
            a.recycle();
        }


    }

    public void setColor() {
        if (button.isChecked()) {
            label.setTextColor(getResources().getColor(R.color.bpBlue));
            mLayout.setBackgroundColor(getResources().getColor(R.color.black_overlay));
        } else {
            label.setTextColor(Color.LTGRAY);
            mLayout.setBackgroundColor(Color.BLACK);

        }
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setChecked(boolean isChecked) {
        button.setChecked(isChecked);
        setColor();
    }


    public boolean isChecked() {
        return button.isChecked();
    }
}
