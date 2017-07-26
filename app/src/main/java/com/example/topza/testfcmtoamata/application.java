package com.example.topza.testfcmtoamata;

import android.app.Application;

import com.example.topza.testfcmtoamata.manager.Contextor;

/**
 * Created by topza on 7/27/2017.
 */

public class application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Contextor.getInstance().init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
