package com.binlee.pluginization;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.binlee.pluginization.util.Hooks;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public class PluginizationApp extends Application {

    private static final String TAG = "PluginizationApp";

    @Override
    protected void attachBaseContext(Context base) {
        Log.d(TAG, "attachBaseContext() called with: base = [" + base + "]");
        super.attachBaseContext(base);
        doInit();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        super.onCreate();
    }

    private void doInit() {
        // hook
        Hooks.init(this);
    }
}
