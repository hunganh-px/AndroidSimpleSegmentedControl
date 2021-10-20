package com.ucode.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ucode.segmentedcontrol.SimpleSegmentedControl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] title = new String[] {"Cao", "Thấp", "Ngẫu nhiên"};
        SimpleSegmentedControl segmentedControl = findViewById(R.id.segment);
        segmentedControl.setSegmentTitles(title);
        segmentedControl.setCallback(new SimpleSegmentedControl.Callback() {
            @Override
            public void onSegmentSelected(int index) {
                Log.d(TAG, "onSegmentSelected: "+index);
                ((TextView)findViewById(R.id.text)).setText(""+segmentedControl.getSelected());
            }
        });
        segmentedControl.setSelected(2);

    }
}