package com.sleticalboy.pluginization;

import android.app.Application;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.sleticalboy.pluginization.util.Hooks;

import java.lang.reflect.Method;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public class PluginizationApp extends Application {

    private static final String TAG = "PluginizationApp";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        final Hooks.InvokingListener listener = new Hooks.InvokingListener() {

            String mName;

            @Override
            public void before(final Object rawObj, final Method method, final Object... args) {
                mName = method.getName();
                if ("startActivity".equals(mName)) {
                    Log.d(TAG, "args: " + JSON.toJSONString(args));
                }
            }

            @Override
            public void after(final Object result) {
                if ("startActivity".equals(mName)) {
                    Log.d("PluginizationApp", "afterInvoke() result: " + result);
                }
            }

            @Override
            public boolean onMessage(final Message msg) {
                switch (msg.what) {
                    default:
                    case Hooks.LAUNCH_ACTIVITY:
                    case Hooks.PAUSE_ACTIVITY:
                    case Hooks.RESUME_ACTIVITY:
                        break;
                }
                Log.d("PluginizationApp", "onMessage() action: " + Hooks.codeToString(msg.what)
                        + ", msg: " + msg);
                return super.onMessage(msg);
            }
        };
        Hooks.hookHandlerCallback(listener);
        Hooks.hookActivityManager(this, listener);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
