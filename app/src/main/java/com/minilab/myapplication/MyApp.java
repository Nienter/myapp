package com.minilab.myapplication;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        try {
            Thread.sleep(10000);
            for (int i = 0; i < 1000; i++) {
                Log.d(TAG, "onCreate: "+i);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
