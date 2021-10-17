package com.ucode.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.ucode.segmentedcontrol.SimpleSegmentedControl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] title = new String[] {"Thử font 1", "Thử font 2", "Thử font 3"};
        SimpleSegmentedControl segmentedControl = findViewById(R.id.segment);
        segmentedControl.setSegmentTitles(title);
        segmentedControl.setCallback(new SimpleSegmentedControl.Callback() {
            @Override
            public void onSegmentSelected(int index) {
                Log.d(TAG, "onSegmentSelected: "+index);
            }
        });

    }
}