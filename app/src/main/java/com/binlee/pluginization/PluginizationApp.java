package com.binlee.pluginization;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.binlee.pluginization.util.DexMerger;
import com.binlee.pluginization.util.Hooks;
import com.binlee.pluginization.util.IOs;

import java.io.File;
import java.io.IOException;

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
        try {
            // hook
            Hooks.init(this);

            // copy plugin apk
            File file = IOs.file(getApplicationInfo().dataDir + "/files/plugin/plugin-debug.apk");
            IOs.copy(getAssets().open("plugin-debug.apk"), file);
            Log.d(TAG, "plugin: " + file + ", exists: " + file.exists());

            // Unable to get provider com.binlee.plugin.AnotherProvider:
            // java.lang.ClassNotFoundException:
            // Didn't find class "com.binlee.plugin.AnotherProvider" on path:
            // DexPathList[[zip file "/data/app/com.binlee.pluginization-KD7bSTMHikFhOr0D6ZiMvA==/base.apk"],
            // nativeLibraryDirectories=[/data/app/com.binlee.pluginization-KD7bSTMHikFhOr0D6ZiMvA==/lib/arm64, /system/lib64, /vendor/lib64]]

            // merge dex
            DexMerger.merge(this, new String[] {file.getAbsolutePath()});

            // install providers
            Hooks.parseProviders(this, file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
