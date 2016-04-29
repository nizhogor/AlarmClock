package com.nizhogor.flashalarm;

import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class AboutDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.setTitle(getResources().getString(R.string.about_title));
        View v = inflater.inflate(R.layout.about_fragment, container, false);
       return v;
    }
}
