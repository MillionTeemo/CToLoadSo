package com.zltech.ctoloadso;

import android.app.Application;
import android.content.Context;

/***********************************************************

 ******************************************************/
public class ApplicationLoader extends Application {
    public static Context applicationContext;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }
}
