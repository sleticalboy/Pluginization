package com.sleticalboy.pluginization;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.sleticalboy.pluginization.util.Hooks;
import com.sleticalboy.pluginization.util.IOs;

import java.io.File;

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
        try {
            doHook();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        super.onCreate();
    }

    private void doHook() throws Exception {
        Hooks.init(this);
        File file = IOs.file(getApplicationInfo().dataDir + "/files/plugin/plugin-debug.apk");
        IOs.copy("/storage/emulated/0/plugin-debug.apk", file);
        Log.d(TAG, "plugin: " + file + ", exists: " + file.exists());
        Hooks.parseProviders(this, file.getAbsolutePath());
    }
}
