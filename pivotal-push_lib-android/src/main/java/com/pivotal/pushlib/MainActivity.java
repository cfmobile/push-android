package com.pivotal.pushlib;

import android.os.Bundle;
import android.widget.TextView;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity {

    @InjectView(R.id.outputText)
    TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputText.setText("Ready\n");
    }


}
