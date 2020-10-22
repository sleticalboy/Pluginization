package com.sleticalboy.pluginization;

import android.app.Application;
import android.content.Context;

import com.sleticalboy.pluginization.util.Hooks;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public class PluginizationApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Hooks.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
