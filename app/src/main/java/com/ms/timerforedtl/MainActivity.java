package com.ms.timerforedtl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private static final String TAG = "MainActivity";
   
    int i = 0;
    int j = 0;
    int k = 0;
    long tempStart = 0;
    long start = System.currentTimeMillis();
    long tempEnd = 0;
    long finalTime = 0;
    long end = 0;

    List<Long> longs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView aaa = findViewById(R.id.aaa);

        aaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalTime=0;
                end = System.currentTimeMillis();
                for (int l = 0; l < longs.size(); l++) {
                    finalTime += longs.get(l);
                    Log.i(TAG, "onClick: "+stringTime(longs.get(l)));
                }

                Log.i("MainActivity", "Stop Time: " + stringTime(finalTime));

                long times = end - start - finalTime;

                Log.i("MainActivity", "Total Time: " + stringTime(times));
            }
        });
    }


    public String stringTime(long times) {
        return String.format(getString(R.string.lala),
                TimeUnit.MILLISECONDS.toHours(times),
                TimeUnit.MILLISECONDS.toMinutes(times) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(times)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(times) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(times)));
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: i " + i);
        if (i != 0)
            mTravelTime();
        i++;
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: j " + j);
        tempStart = System.currentTimeMillis();
        j++;
        super.onPause();

    }

    public void mTravelTime() {
        tempEnd = System.currentTimeMillis();
        longs.add(tempEnd - tempStart);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: k " + k);
        super.onDestroy();
        k++;
    }
}